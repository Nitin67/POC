package com.quikr.platform.datastore.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Iterator;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;

import play.Play;
import play.libs.Json;
import akka.japi.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quikr.platform.metrics.MetricType;
import com.quikr.util.Utils;

public class ESMetrics extends ElasticSearch {

  private static final String INDEX = "search_keywords";
  private static int DEFAULT_SIZE = 30;
  private static int DEFAULT_DAYS = 30;
  private static long DEFAULT_INDEX_TIMEOUT = 100;

  private static ESMetrics instance;

  private ESMetrics() {
    if (Play.isProd()) {
      String nodeName = Play.application().configuration().getString("search.node.name");
      String clusterName = Play.application().configuration().getString("search.clustername");
      Settings settings = ImmutableSettings.settingsBuilder()
              .put("node.name", nodeName).build();
      node = nodeBuilder().clusterName(clusterName).client(true)
              .settings(settings).node();
      client = node.client();
    } else { // Use Transport Client for local use.
      Settings settings = ImmutableSettings.settingsBuilder()
              .put("client.transport.sniff", true).build();
      client = new TransportClient(settings)
              //.addTransportAddress(new InetSocketTransportAddress("172.16.1.44", 9300));
              .addTransportAddress(new InetSocketTransportAddress("192.168.2.7", 9300));

    }
  }

  public static ESMetrics getInstance() {
    if (instance == null) {
      synchronized (ESAdsWrite.class) {
        if (instance == null) {
          instance = new ESMetrics();
        }
      }
    }
    return instance;
  }

  public boolean create(String document, MetricType type) {
    String index = INDEX + "_" + Utils.getMonthlyIndexNameSuffix();
    IndexResponse response;
    try {
      IndicesExistsRequest req = new IndicesExistsRequest(index);
      IndicesExistsResponse resp = client.admin().indices().exists(req)
              .actionGet();
      if (resp.isExists()) {
        response = client.prepareIndex(index, type.toString()).setSource(document)
                .setTimeout(new TimeValue(DEFAULT_INDEX_TIMEOUT))
                .execute().actionGet();
      } else {
        synchronized (ESMetrics.class) {
          play.Logger.debug("[SearchKeyword] create index:" + index);
          Settings settings = ImmutableSettings.settingsBuilder()
                  .put("number_of_shards", 5)
                  .put("number_of_replicas", 1).build();

          CreateIndexResponse res = client.admin().indices()
                  .prepareCreate(index).setSettings(settings)
                  .addMapping(MetricType.SEARCH_KEYWORDS.toString(), getKeywordsMapping())
                  .addMapping(MetricType.CAT_API.toString(), getCatMetricMapping())
                  .execute()
                  .actionGet();
          return res.isAcknowledged();
        }
      }
    } catch (IndexMissingException e) {
      // Synchronization added to make sure correct mapping is added.
      synchronized (ESMetrics.class) {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("number_of_shards", 1)
                .put("number_of_replicas", 1)
                .put("index.store.type", "mmapfs").build();

        CreateIndexResponse res = client.admin().indices()
                .prepareCreate(index).setSettings(settings)
                .addMapping(MetricType.SEARCH_KEYWORDS.toString(), getKeywordsMapping())
                .addMapping(MetricType.CAT_API.toString(), getCatMetricMapping())
                .execute().actionGet();
        return res.isAcknowledged();
      }
    }

    return response.isCreated();
  }

  public JsonNode getKeywords(String city, Integer cutOff, Integer limit) {
    ObjectNode result = Json.newObject();
    if (limit == null) {
      limit = DEFAULT_SIZE;
    }
    Calendar cal = Calendar.getInstance();
    if (cutOff == null) {
      cal.add(Calendar.DATE, -1 * DEFAULT_DAYS);
    } else {
      cal.add(Calendar.DATE, -1 * cutOff);
    }
    try {
      BoolFilterBuilder finalFilter = FilterBuilders.boolFilter();
      finalFilter.must(FilterBuilders.termFilter("isUserSearch",true));
      if (city != null){
        finalFilter.must(FilterBuilders.termFilter("city", city.toLowerCase()));
      }
      FilterBuilder filter = FilterBuilders.rangeFilter("time").gte(
              cal.getTimeInMillis());
      finalFilter.must(filter);
      FacetBuilder facet = FacetBuilders.termsFacet("keywords")
              .size(limit).field("keywords_txt").facetFilter(finalFilter);
      SearchResponse response = getResults(facet);
      parseResponse(response, result);
    } catch (Exception e) {
      result.put("success", false);
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      result.put("message", e.getMessage());
      play.Logger.debug(writer.toString());
    }
    return result;
  }

  private void parseResponse(SearchResponse response, ObjectNode result) {
    JsonNode res = Json.parse(response.toString());
    result.put("success", true);

    // Add facet count to result.
    Iterator<JsonNode> facetIt = res.findPath("facets").findPath("keywords").findPath("terms")
            .iterator();
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ArrayNode keywords = new ArrayNode(factory);
    while (facetIt.hasNext()) {
      JsonNode facet = facetIt.next();
      keywords.add(facet.findPath("term").asText());
    }
    result.put("keywords", keywords);
  }

  public SearchResponse getResults(FacetBuilder facet) {
    String index = INDEX + "_" + Utils.getMonthlyIndexNameSuffix();
    SearchRequestBuilder request = client.prepareSearch(index)
            .setTypes(MetricType.SEARCH_KEYWORDS.toString()).setQuery(QueryBuilders.matchAllQuery())
            .setFrom(0).setSize(0).setExplain(false);

    request.addFacet(facet);

    if (Play.isDev()) {
      play.Logger.debug("Elastic Request : " + request.toString());
    }
    SearchResponse response = request.execute().actionGet();
    return response;
  }

  private XContentBuilder getKeywordsMapping() {
    XContentBuilder builder = null;
    try {
      builder = XContentFactory.jsonBuilder().startObject()
              .startObject(MetricType.SEARCH_KEYWORDS.toString())
              .startObject("properties")
              .startObject("city")
              .field("type", "string")
              .field("store", "false")
              .field("index", "not_analyzed")
              .endObject()
              .startObject("hits")
              .field("type", "long")
              .field("store", "false")
              .field("index", "not_analyzed")
              .endObject()
              .startObject("timeTaken")
              .field("type", "long")
              .field("store", "false")
              .field("index", "not_analyzed")
              .endObject()
              .startObject("time")
              .field("type", "long")
                      // .field("format", "yyyy/MM/dd HH:mm:ss")
              .field("store", "false").field("index", "not_analyzed")
              .endObject().startObject("keywords")
              .field("type", "string").field("store", "false")
              .field("index", "analyzed")
              .array("copy_to", "keywords_txt").endObject()
              .startObject("keywords_txt").field("type", "string")
              .field("store", "false").field("index", "not_analyzed")
              .endObject().startObject("extras").field("type", "string")
              .field("store", "false").field("index", "analyzed")
              .array("copy_to", "extras_txt").endObject()
              .startObject("extras_txt").field("type", "string")
              .field("store", "false").field("index", "not_analyzed")
              .endObject().
                      // more mapping
                              endObject().endObject().endObject();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (Play.isDev()) {
      play.Logger.debug("[Keywords] Index Mapping : "
              + builder.toString());
    }
    return builder;
  }

  private XContentBuilder getCatMetricMapping() {
    XContentBuilder builder = null;
    try {
      builder = XContentFactory.jsonBuilder().startObject()
              .startObject(MetricType.CAT_API.toString())
              .startObject("properties")
              .startObject("clientIP")
              .field("type", "string")
              .field("store", "false")
              .field("index", "not_analyzed")
              .endObject()
              .startObject("category")
              .field("type", "string")
              .field("store", "false")
              .field("index", "not_analyzed")
              .endObject()
              .startObject("timeToFetch")
              .field("type", "long")
              .field("store", "false")
              .field("index", "not_analyzed")
              .endObject()
              .startObject("categoryId")
              .field("type", "string")
              .field("store", "false")
              .field("index", "not_analyzed")
              .endObject()
              .endObject().endObject().endObject();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (Play.isDev()) {
      play.Logger.debug("[Keywords] Index Mapping : "
              + builder.toString());
    }
    return builder;
  }

}
