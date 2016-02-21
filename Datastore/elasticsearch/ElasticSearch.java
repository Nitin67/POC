package com.quikr.platform.datastore.elasticsearch;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.node.Node;
//import org.elasticsearch.script.ScriptService.ScriptType;

import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.rescore.RescoreBuilder.Rescorer;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import play.Play;
import play.libs.Json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.quikr.constants.Constants;
import com.quikr.platform.datastore.AbstractDataStore;

public abstract class ElasticSearch extends AbstractDataStore {
	
	protected Client client;
	protected Node node;
	
	public boolean create(String index, String type, String id, String document, boolean refresh) {
		IndexResponse response = client.prepareIndex(index, type, id)
								.setSource(document)
								.setRefresh(refresh)
								.execute().actionGet();
		
		return response.isCreated();
	}
	
	public boolean create(String index, String type, String id, String document, String route, boolean refresh) {
		IndexRequestBuilder request = client.prepareIndex(index, type, id)
											.setSource(document);
		if (route != null){
			request.setRouting(route);
		}	
		IndexResponse response = request.execute().actionGet();
		
		return response.isCreated();
	}

	public Map<String, Object> get(String index, String type, String id){
		
			GetResponse response = client.prepareGet(index, type, id).execute().actionGet();
			Map<String, Object> doc = response.getSource();
			if(Play.isDev()){
				play.Logger.debug("[Elastic Search get called]: Index:"+index+" Type:"+type+" Id:"+id);
			}
			return doc;					
	}
	
	public long count(String index, QueryBuilder query, String... type){
		
		CountResponse response = client.prepareCount(index)
		        .setQuery(query)
		        .execute()
		        .actionGet();
		
		return response.getCount();
	}
	
	
	public SearchResponse search(SearchRequestBuilder request){
		return request.execute().actionGet();
	}
	
	public Map<String, Object> getWithRouting(String index, String type, String id){
		
		BoolFilterBuilder boolFinalFilter=FilterBuilders.boolFilter();
		
		boolFinalFilter.must(FilterBuilders.termFilter("id", id));
				
		FilterBuilder filterQuery = boolFinalFilter;
		QueryBuilder query=QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),filterQuery);
	
		SearchRequestBuilder request = client.prepareSearch(index)
									.setTypes(type)
									.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
									.setQuery(query)
									.setExplain(false);
		
		if (Play.isDev()){
			play.Logger.debug("Elastic Request : " + request.toString());
		}
		SearchResponse response = request.execute().actionGet();
		
		ObjectNode result = Json.newObject();
		ESAdsRead.processResponse(result, response, null);
		
		ObjectMapper jsonMapper = new ObjectMapper();
		TypeFactory typeFactory = jsonMapper.getTypeFactory();
		ArrayList<Map<String, Object>> storedata = null;
        try {
			storedata = jsonMapper.readValue(result.findPath("docs").toString(), ArrayList.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        if(storedata.isEmpty()){
        	return null;
        }
        
        return storedata.get(0);
}
	
	
	
	public boolean update(String index, String type, String id, String script){
		UpdateResponse response = client.prepareUpdate(index, type, id)
			  .setScript(script)
			  .execute().actionGet();
		return response.isCreated();
	}
	
	public boolean delete(String index, String type, String id){
		DeleteResponse response = client.prepareDelete(index, type, id)
		        .execute()
		        .actionGet();
		return response.isFound();
	}
	
	public boolean delete(String index, String type, String id, String route){
		DeleteRequestBuilder request = client.prepareDelete(index, type, id);
		if (route != null){
			request.setRouting(route);
		}
		DeleteResponse response = request.execute().actionGet();
		return response.isFound();
	}
	
	public BoolFilterBuilder prepareGeoFilters(JsonNode filters,String fieldName){
		
		BoolFilterBuilder finalFilter = FilterBuilders.boolFilter();
	
		if(!filters.findPath("radius").isMissingNode() && !filters.findPath("radius").isNull()){
			
			finalFilter.must(FilterBuilders.geoDistanceFilter(fieldName).point(filters.findPath("lat").asDouble(),filters.findPath("long").asDouble()).distance(filters.findPath("radius").asDouble(), DistanceUnit.KILOMETERS));
			
		}else{
			
			finalFilter.must(FilterBuilders.geoDistanceFilter(fieldName).point(filters.findPath("lat").asDouble(),filters.findPath("long").asDouble()).distance(100, DistanceUnit.KILOMETERS));	
		}
		
		
		
		if (finalFilter.hasClauses()){
			return finalFilter;
		}else {
			return null;
		}
		
	}
	
	public BoolFilterBuilder prepareAndFilters(JsonNode filters){
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
									range.gte(from).lte(to);
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
		if (finalFilter.hasClauses()){
			return finalFilter;
		}else {
			return null;
		}
	}
	
	public BoolFilterBuilder prepareOrFilters(JsonNode filters){
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
				if(name.contains("user_")){
					
					finalFilter.should(FilterBuilders.queryFilter(
	    					QueryBuilders.matchQuery(name, value.asText())));
					
				}else{
					finalFilter.should(FilterBuilders.termFilter(name, value.asText()));
				}
			}
		}
		if (finalFilter.hasClauses()){
			return finalFilter;
		}else {
			return null;
		}
	}
	
	public BoolFilterBuilder prepareNotFilters(JsonNode filters){
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
						bool.should(FilterBuilders.termFilter(name, val.asText()));
					}
					if (bool.hasClauses()){
						finalFilter.mustNot(bool);
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
						finalFilter.mustNot(range);
					}
				}else {
					finalFilter.mustNot(FilterBuilders.termFilter(name, value.asText()));
				}
			}
		}
		if (finalFilter.hasClauses()){
			return finalFilter;
		}else {
			return null;
		}
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
		private Rescorer rescorer;
		private int rescoreWindow;
		private String route;
		private SearchRequestBuilder request;
		private BoolFilterBuilder finalFilter;
		private BoolFilterBuilder notFilter;
		private BoolFilterBuilder orFilter;
		private GeoSortBuilder geoSortBuilder;
		
		
		public RequestBuilder(String index, ESSearchQuery query, String... type){
			this.index = index;
			//this.type = type;
			this.query = query;
			this.from = Constants.DEFAULT_FROM;
			this.count = Constants.DEFAULT_SIZE;
			this.fields = null;
			this.sort = null;
			this.rescorer = null;
			this.finalFilter = FilterBuilders.boolFilter();
			this.request = client.prepareSearch(index)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setFrom(from).setSize(count);
			if (type != null)
				request.setTypes(type);
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
		
		//TODO : add filters to OR first and assing to finalFilter in build()
		public RequestBuilder orFilters(JsonNode filters){
			BoolFilterBuilder filter = prepareOrFilters(filters);
			if (filter != null){
				finalFilter.must(filter);
			}
			return this;
		}
		
		
		public RequestBuilder andFilters(JsonNode filters){
			BoolFilterBuilder filter = prepareAndFilters(filters);
			if (filter != null){
				finalFilter.must(filter);
			}
			return this;
		}
		public RequestBuilder geoFilters(JsonNode filters,String fieldName){
			BoolFilterBuilder filter = prepareGeoFilters(filters,fieldName);
			if (filter != null){
				finalFilter.must(filter);
			}
			return this;
		}
		
		public RequestBuilder notFilters(JsonNode filters){
			BoolFilterBuilder filter = prepareNotFilters(filters);
			if (filter != null){
				finalFilter.must(filter);
			}
			return this;
		}

		public RequestBuilder addFacets(JsonNode facets, boolean isGlobal){
			if (facets != null && !facets.isMissingNode() && !facets.isNull()){
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
		
		public RequestBuilder addAggregation(AbstractAggregationBuilder aggregation){
			request.addAggregation(aggregation);
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
		
		public RequestBuilder setGeoSortBuilder(GeoSortBuilder geoSortBuilder){
			this.geoSortBuilder = geoSortBuilder;
			return this;
		} 
		
		public RequestBuilder setRoute(String route) {
			this.route = route;
			return this;
		}
		
		public SearchRequestBuilder build(){
			if (finalFilter.hasClauses()){
				request.setQuery(QueryBuilders.filteredQuery(query.prepareQuery(), finalFilter));
		  }else {
		  	request.setQuery(query.prepareQuery());
		  }	
			if (fields != null){
				for (String field : fields)
					request.addField(field);
			}
			
			if(geoSortBuilder!=null){
				
				request.addSort(geoSortBuilder.build());
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
			if (route != null){
				request.setRouting(route);
				if (Play.isDev()){
					play.Logger.debug("Elastic Request Routing added : " + route);
				}
			}
			
			if (Play.isDev()){
				play.Logger.debug("Elastic Request : " + request.toString());
			}
			
			return request;
		}

		
		/**
		 * @author Peeyush Chandel
		 * Elastic Search Geographical Sort Builder
		 */
		public class GeoSortBuilder{
			
			private String fieldName;
			private double lat;
			private double lon;
			private String unit;
			private String order;
			
			public GeoSortBuilder(String fieldName,double lat,double lon){
				this.fieldName = fieldName;
				this.lat=lat;
				this.lon=lon;
			}
			
			public GeoSortBuilder setFieldName(String fieldName){				
				
				this.fieldName = fieldName;
				return this;

			}
			
			public GeoSortBuilder setLatitude(double latitude){
				
				this.lat=latitude;
				return this;
			}
			
			public GeoSortBuilder setLongitude(double longitude){
				
				this.lon=longitude;
				return this;
			}
			
			public GeoSortBuilder setUnit(String unit){
				
				this.unit=unit;
				return this;
			}
			
			public GeoSortBuilder setOrder(String order){
				
				this.order=order;
				return this;
			}
			
			
			public SortBuilder build(){
				
				SortBuilder geoSort = SortBuilders.geoDistanceSort(this.fieldName).point(this.lat,this.lon).order(SortOrder.valueOf(this.order)).unit(DistanceUnit.valueOf(this.unit));
				
				return geoSort;
			}
			
			
			
		}
	}

	
}
