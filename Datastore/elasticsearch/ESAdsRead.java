package com.quikr.platform.datastore.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.rescore.RescoreBuilder.Rescorer;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import play.Play;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quikr.constants.Constants;
import com.quikr.platform.ads.AdsComparator;
import com.quikr.platform.ads.AdsHelper;

public class ESAdsRead extends ElasticSearch{
	
	
	private static ESAdsRead instance = null;
	
	private final boolean DEBUG = false;
	
	private ESAdsRead(){
		if (Play.application().configuration().getBoolean("elastic.useclient")){
			String nodeName = Play.application().configuration().getString("elastic.node.name");
			String clusterName = Play.application().configuration().getString("elastic.clustername");
			Settings settings = ImmutableSettings.settingsBuilder()
			        .put("node.name", nodeName).build();
			node = nodeBuilder().clusterName(clusterName).client(true).settings(settings).node();
			client = node.client();
		}else {	//Use Transport Client for local use.
			Settings settings = ImmutableSettings.settingsBuilder()
			        .put("client.transport.sniff", true).build();
			String elasticIp = Play.application().configuration().getString("elastic.transport.ip");
			if (Play.isDev()){
				play.Logger.debug("[ES Ads Read] Transport IP: " + elasticIp);
			}
			client = new TransportClient(settings)
			.addTransportAddress(new InetSocketTransportAddress(elasticIp, 9300));

		}
	}
	
	public static ESAdsRead getInstance(){
		if (instance == null){
			synchronized (ESAdsRead.class) {
				if (instance == null){
					instance = new ESAdsRead();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Search Data on ElasticSearch
	 * @param index Name of Index
	 * @param type  Type of Document
	 * @param query query to execute 
	 * @param facets facets to apply
	 * @param from for pagination
	 * @param count no. of documents needed
	 * @return
	 */
	public SearchResponse getResults(String index, String type, 
			QueryBuilder query,FacetBuilder[] facets,JsonNode sort,
			int from, int count, Integer[] cityIds, Rescorer rescorer, String[] fields){
		
		SearchRequestBuilder request = client.prepareSearch(index)
										.setTypes(type)
										.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
										.setQuery(query)
										.setFrom(from).setSize(count).setExplain(DEBUG);
		
		if (facets != null){
			for (FacetBuilder facet : facets){
				request.addFacet(facet);
			}
		}
		if (fields != null){
			for (String field : fields)
				request.addField(field);
		}
		if(sort!=null){
			Iterator<Entry<String, JsonNode>> it = sort.fields();
			while(it.hasNext()){
				
				Entry<String, JsonNode> node = it.next();
				if (node.getKey().equalsIgnoreCase("random")){
					SortBuilder randomSort = SortBuilders.scriptSort("Math.random()", "number");
					randomSort.order(SortOrder.DESC);
					request.addSort(randomSort);
				}else if (node.getKey().equalsIgnoreCase("random_search")){
					Integer premiumAdRandom = node.getValue().asInt();
					SortBuilder randomSort;
					if (premiumAdRandom != null){
						String script = "(doc['modifiedTime'].value + " + String.valueOf(premiumAdRandom) + ").hashCode()";
						randomSort = SortBuilders.scriptSort(script, "number");
					}else {
						randomSort = SortBuilders.scriptSort("Math.random()", "number");
					}
					randomSort.order(SortOrder.DESC);
					request.addSort(randomSort);
				}else {
					request.addSort(node.getKey(), SortOrder.valueOf(node.getValue().asText()));
				}	
			}
		}
		
		
		
		request.addHighlightedField("_all", 250);
		request.setHighlighterPreTags("%3Cspan+class%3D%22shl%22%3E");
		request.setHighlighterPostTags("%3C%2Fspan%3E");
		
		//Add routing
		String routeString = AdsHelper.getRoute(cityIds);
		if (routeString != null){
			request.setRouting(routeString);
			if (Play.isDev()){
				play.Logger.debug("Elastic Request Routing added : " + routeString);
			}
		}
		
		//Rescore results based on phrase query
		if (rescorer != null){
			request.addRescorer(rescorer, Constants.SEARCH_RESCORING_WINDOW);
		}
		
		if (Play.isDev()){
			play.Logger.debug("Elastic Request : " + request.toString());
		}
		SearchResponse response = request.execute()
								        .actionGet();
		
		return response;
	}	
	
	public static void processResponse(ObjectNode result,SearchResponse response, boolean[] sort){
		JsonNode res = Json.parse(response.toString());
		result.put("success", true);
		result.put("total", response.getHits().getTotalHits());
		result.put("timeTaken", response.getTook().getMillis());
		result.put("responseCode", response.status().getStatus());
		
		//Add facet count to result.
		Iterator<Entry<String, JsonNode>> facetIt = res.findPath("facets").fields();
		ObjectNode facetCount = Json.newObject();
		while (facetIt.hasNext()){
			Entry<String, JsonNode> facet = facetIt.next();
			ObjectNode facetNode = Json.newObject(); //Object to hold facet Data
			JsonNode terms = facet.getValue().findPath("terms");
			Iterator<JsonNode> termsIt = terms.iterator();
			while(termsIt.hasNext()){
				JsonNode termsVal = termsIt.next();
				facetNode.put(termsVal.findPath("term").asText(), termsVal.findPath("count").asInt());
			}
			facetCount.put(facet.getKey(), facetNode);
			
		}
		result.put("facet_count", facetCount);
		
		//Add found docs to result.
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ArrayNode arr = new ArrayNode(factory);
		
		if (sort != null){
			if (Play.isDev()){
				play.Logger.debug("[ES Ads READ] Sort Applied.");
			}
			ArrayList<JsonNode> adList = new ArrayList<JsonNode>();
			JsonNode hits = res.findPath("hits").findPath("hits");
			Iterator<JsonNode> it = hits.iterator();
			while(it.hasNext()){
				JsonNode doc = it.next();
				if (doc.findPath("_source").isMissingNode()){
					adList.add(AdsHelper.getAdWithFieldsFromJson(doc.findPath("fields")));
				}else {
					adList.add(AdsHelper.getAdWithFieldsFromJson(doc.findPath("_source")));
				}
				if (Play.isDev()){
					play.Logger.debug("before: " + doc.findPath("_source").findPath("id").asText());
				}
			}
			Collections.sort(adList, new AdsComparator(sort));
			for (JsonNode ad : adList){
				arr.add(ad);
				if (Play.isDev()){
					play.Logger.debug("after: " + ad.findPath("id").asText());
				}
			}
		}else {
			JsonNode hits = res.findPath("hits").findPath("hits");
			Iterator<JsonNode> it = hits.iterator();
			while(it.hasNext()){
				JsonNode doc = it.next();
				if (doc.findPath("_source").isMissingNode()){
					arr.add(AdsHelper.getAdWithFieldsFromJson(doc.findPath("fields")));
				}else {
					arr.add(AdsHelper.getAdWithFieldsFromJson(doc.findPath("_source")));
				}	
			}
		}
		
		
		
		result.put("docs", arr);
	}
	
	@Override
	protected void finalize() throws Throwable {
		//node.close();
		client.close();
		super.finalize();
	}
	
}
