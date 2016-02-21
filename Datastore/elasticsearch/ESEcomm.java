package com.quikr.platform.datastore.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.Iterator;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.rescore.RescoreBuilder.Rescorer;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.databind.JsonNode;
import com.quikr.constants.Constants;

import play.Play;

public class ESEcomm extends ElasticSearch{
private static ESEcomm instance = null;
	
	private final boolean DEBUG = false;
	
	private ESEcomm(){
		if (Play.application().configuration().getBoolean("elastic.useclient")){
			String nodeName = Play.application().configuration().getString("elastic.ecomm.node.name");
			String clusterName = Play.application().configuration().getString("elastic.ecomm.clustername");
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
	
	public static ESEcomm getInstance(){
		if (instance == null){
			synchronized (ESAdsRead.class) {
				if (instance == null){
					instance = new ESEcomm();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Builder class for creating ES SearchRequest
	 * @author saurabh.a
	 */
	public class RequestBuilder {
		private String index;
		private String type; 
		private ESSearchQuery query;
		private int from;
		private int count;
		private String[] fields;
		private JsonNode sort;
		private Integer[] cityIds;
		private Rescorer rescorer;
		private int rescoreWindow;
		private SearchRequestBuilder request;
		private BoolFilterBuilder finalFilter;
		
		public RequestBuilder(String index, String type, ESSearchQuery search){
			this.index = index;
			this.type = type;
			this.query = search;
			this.from = Constants.DEFAULT_FROM;
			this.count = Constants.DEFAULT_SIZE;
			this.fields = null;
			this.sort = null;
			this.cityIds = null;
			this.rescorer = null;
			this.finalFilter = FilterBuilders.boolFilter();
			this.request = client.prepareSearch(index)
					.setTypes(type)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setFrom(from).setSize(count).setExplain(DEBUG);
		}
		
		public RequestBuilder setQuery(ESSearchQuery query){
			this.query = query;
			return this;
		}
		
		public RequestBuilder setIndex(String index) {
			this.index = index;
			return this;
		}

		public RequestBuilder setType(String type) {
			this.type = type;
			return this;
		}
		
		public RequestBuilder orFilters(JsonNode filters){
			BoolFilterBuilder filter = ESAdsRead.getInstance().prepareOrFilters(filters);
			if (filter != null){
				finalFilter.must(filter);
			}
			return this;
		}
		
		public RequestBuilder andFilter(String field, String value){
			finalFilter.must(FilterBuilders.termFilter(field, value));
			return this;
		}
		
		
		public RequestBuilder andFilters(JsonNode filters){
			BoolFilterBuilder filter = ESAdsRead.getInstance().prepareAndFilters(filters);
			if (filter != null){
				finalFilter.must(filter);
			}
			return this;
		}
		
		public RequestBuilder notFilters(JsonNode filters){
			BoolFilterBuilder filter = ESAdsRead.getInstance().prepareNotFilters(filters);
			if (filter != null){
				finalFilter.must(filter);
			}
			return this;
		}

		public RequestBuilder addFacets(JsonNode facets, boolean isGlobal){
			if (facets != null){
				Iterator<JsonNode> it = facets.iterator();
				while (it.hasNext()) {
					String fieldName = it.next().asText();
					FacetBuilder facet = FacetBuilders.termsFacet(fieldName)
							.size(Constants.FACET_COUNT).field(fieldName).global(isGlobal);
					request.addFacet(facet);
				}
			}
			return this;
		}


		public RequestBuilder setSort(JsonNode sort) {
			this.sort = sort;
			return this;
		}

		public RequestBuilder setFrom(int from) {
			this.from = from;
			request.setFrom(from);
			return this;
		}

		public RequestBuilder setCount(int count) {
			this.count = count;
			request.setSize(count);
			return this;
		}

		public RequestBuilder setCityIds(Integer[] cityIds) {
			this.cityIds = cityIds;
			return this;
		}

		public RequestBuilder addRescorer(Rescorer rescorer, Integer rescoreWindow) {
			if (rescoreWindow != null)
			request.addRescorer(rescorer, rescoreWindow);
			else 
			request.addRescorer(rescorer);	
			return this;
		}
		
		public RequestBuilder setRescoreWindow(int rescoreWindow) {
			this.rescoreWindow = rescoreWindow;
			return this;
		}

		public RequestBuilder setFields(String[] fields) {
			this.fields = fields;
			return this;
		}
		
		public SearchRequestBuilder build(){
			
			request.setQuery(QueryBuilders.filteredQuery(query.prepareQuery(), finalFilter));
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
			
			//TODO: Include routing and highlighter at a later stage.
			
			if (Play.isDev()){
				play.Logger.debug("Elastic Request : " + request.toString());
			}
			
			return request;
		}

	}
	
	@Override
	protected void finalize() throws Throwable {
		//node.close();
		client.close();
		super.finalize();
	}
}
