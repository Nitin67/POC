package com.quikr.platform.datastore.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.percolate.PercolateRequestBuilder;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.indices.IndexMissingException;

import play.Play;

import com.fasterxml.jackson.databind.JsonNode;
import com.quikr.constants.Constants;

public class ESNotification extends ElasticSearch {
	
	private static ESNotification instance;
	private String nodeName;
	private String clusterName;
	public static String INDEX = "notification";
	public static String TYPE = "ads";
	private static String PERCOLATOR = ".percolator";

	private ESNotification() {
		if (Play.isProd()) {
			nodeName = Play.application().configuration().getString("notification.node.name");
			clusterName = Play.application().configuration().getString("notification.clustername");
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
			.addTransportAddress(new InetSocketTransportAddress("192.168.2.10", 9300));
		}
	}

	public static ESNotification getInstance() {
		if (instance == null) {
			synchronized (ESNotification.class) {
				if (instance == null) {
					instance = new ESNotification();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Create metric doc in ES.
	 * @param type
	 * @param document
	 * @param id
	 * @return
	 */
	public boolean createMetric(String index, String type, String id, String document) {
		//String index = INDEX + getMonthlyIndexNameSuffix();
		IndexResponse response = null;
		try {
			IndicesExistsRequest req = new IndicesExistsRequest(index);
			IndicesExistsResponse resp = client.admin().indices().exists(req)
					.actionGet();
			if (resp.isExists()) {
				response = client.prepareIndex(index, type, id)
							 .setSource(document)
							.execute().actionGet();
			} else {
				synchronized (ESNotification.class) {
					play.Logger.debug("[Notification Metrics] create index:" + index);
					Settings settings = ImmutableSettings.settingsBuilder()
							.put("number_of_shards", 1)
							.put("number_of_replicas", 1).build();

					CreateIndexResponse res = client.admin().indices()
							.prepareCreate(index).setSettings(settings)
							//.addMapping(type, getMapping())
							.execute()
							.actionGet();
				}
				response = client.prepareIndex(index, type, id).setSource(document)
						.execute().actionGet();
			}
		} catch (IndexMissingException e) {
			// Synchronization added to make sure correct mapping is added.
			synchronized (ESNotification.class) {
				Settings settings = ImmutableSettings.settingsBuilder()
						.put("number_of_shards", 1)
						.put("number_of_replicas", 1)
						.put("index.store.type", "mmapfs").build();

				CreateIndexResponse res = client.admin().indices()
						.prepareCreate(index).setSettings(settings)
						//.addMapping(type, getMapping())
						.execute().actionGet();
				return res.isAcknowledged();
			}
		}
		
		return response.isCreated();
	}

	
	public String[] percolate(JsonNode doc){
		PercolateRequestBuilder request = client.preparePercolate()
                .setIndices(INDEX)
                .setDocumentType(TYPE)
                .setSource(doc.toString());
        PercolateResponse response = request.execute().actionGet();
		ArrayList<String> result = new ArrayList<String>();
		//Iterate over the results
		for(PercolateResponse.Match match : response) {
		//Handle the result which is the name of
		//the query in the percolator
			result.add(match.getId().string());
		}
		if (result.size() > 0){
			return result.toArray(new String[result.size()]);
		}else {
			return null;
		}
	}
	
	/**
	 * Creates ElasticSearch Query. 
	 * @param data : JsonObject containg search keyword and required filters.
	 * @return QueryBuilder 
	 */
	public QueryBuilder createQuery(JsonNode data){
		QueryBuilder query;
		if (!data.findPath("keywords").isMissingNode() 
				&& !data.findPath("keywords").isNull()
					&& !data.findPath("keywords").asText().equalsIgnoreCase("")){
			QueryStringQueryBuilder queryString = QueryBuilders.queryString(data.get("keywords").asText());
			queryString.minimumShouldMatch(Constants.MM_PARAM_FOR_ORDER_BY_SEARCH);
			queryString.useDisMax(true);
			queryString.autoGeneratePhraseQueries(true);
			// Set Fields Param
			queryString.field("title").field("content");
			query = queryString;
		}else {
			query = QueryBuilders.matchAllQuery();
		}
		if (Play.isDev()){
			play.Logger.debug("[ES Notification] filter data:" +  data.findPath("filters").toString());
		}
		FilterBuilder filter = prepareQueryFilters(data.findPath("filters"),data.findPath("not_filters"),data.findPath("or_filters"));
		//add static filters
		addFixedFilters((BoolFilterBuilder) filter);
		if (Play.isDev()){
			play.Logger.debug("[ES Notification] filters:" +  filter.toString());
		}
		return QueryBuilders.filteredQuery(query, filter);
	}
	
	public BoolFilterBuilder prepareQueryFilters(JsonNode filters, JsonNode notFilters, JsonNode orFilters){
		BoolFilterBuilder finalFilter = FilterBuilders.boolFilter();
		if (filters != null && !filters.isMissingNode() && !filters.isNull()){
			Iterator<Entry<String, JsonNode>> filterIt = filters.fields();
			while (filterIt.hasNext()){
				Entry<String, JsonNode> filter = filterIt.next();
				String name = filter.getKey();
				Boolean isAnalyzedField = Constants.analyzedAdFields.get(name);
				if (isAnalyzedField != null && isAnalyzedField){
					name += "_txt";
				}
				JsonNode value = filter.getValue();
				
				 if (value.isArray()){
					Iterator<JsonNode> it = value.iterator();
					BoolFilterBuilder bool = FilterBuilders.boolFilter();
					while (it.hasNext()){
						JsonNode val = it.next();
						if (val.isObject()){
							RangeFilterBuilder range = FilterBuilders.rangeFilter(name);
							Iterator<Entry<String, JsonNode>> rangeIt = val.fields();
							if (rangeIt.hasNext()){
								Entry<String, JsonNode> rangeNode = rangeIt.next();
								int from = Integer.parseInt(rangeNode.getKey());
								int to = rangeNode.getValue().asInt();
								if (from == to){
									range.gte(from);
								}else{
									range.gt(from).lt(to);
								}
								bool.should(range);
							}
						}else {
							bool.should(FilterBuilders.termFilter(name, val.asText()));
						}	
					}
					if (bool.hasClauses()){
						finalFilter.must(bool);
					}
				}else if (value.isObject()){
					RangeFilterBuilder range = FilterBuilders.rangeFilter(name);
					Iterator<Entry<String, JsonNode>> rangeIt = value.fields();
					if (rangeIt.hasNext()){
						Entry<String, JsonNode> rangeNode = rangeIt.next();
						int from = Integer.parseInt(rangeNode.getKey());
						int to = rangeNode.getValue().asInt();
						if (from == to){
							range.gte(from);
						}else{
							range.gt(from).lt(to);
						}
						finalFilter.must(range);
					}
				}else {
					finalFilter.must(FilterBuilders.termFilter(name, value.asText()));
				}
			}
		}
		BoolFilterBuilder notFilter;
		if ((notFilter = prepareNotFilters(notFilters)) != null){
			finalFilter.must(notFilter);
		}
		BoolFilterBuilder orFilter;
		if ((orFilter = prepareOrFilters(orFilters)) != null){
			finalFilter.must(orFilter);
		}
		if (finalFilter.hasClauses()){
			return finalFilter;
		}else {
			return null;
		}
	}
	
	private void addFixedFilters(BoolFilterBuilder finalFilter){
		//NCA 
		/*FilterBuilder filter = FilterBuilders.termFilter("NCACreationValidation", 1);
		finalFilter.must(FilterBuilders.boolFilter().mustNot(filter));*/
		
	}
	
	public void addQuery(String id, QueryBuilder query) throws IOException{
		XContentBuilder docBuilder = XContentFactory.jsonBuilder().startObject()
									 .field("query", query)
									 .field("type", TYPE)
									 .endObject();
		boolean success = create(INDEX, PERCOLATOR, id, docBuilder.string(), true);
		if (Play.isDev()){
			play.Logger.debug("[ES Notification] addQuery success = " + success);
		}
	}
	
	public void removeQuery(String id){
		boolean ret = delete(INDEX, PERCOLATOR, id);
		if (!ret){
			play.Logger.debug("[ES Notification] deleteQuery failure id = " + id);
		}else if (Play.isDev()){
			play.Logger.debug("[ES Notification] deleteQuery success id = " + id);	
		}
		
	}

}
