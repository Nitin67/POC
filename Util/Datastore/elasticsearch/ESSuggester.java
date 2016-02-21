package com.quikr.platform.datastore.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionFuzzyBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import play.Play;

public class ESSuggester extends ElasticSearch {
	
private static ESSuggester instance = null;
	
	private ESSuggester(){
		if (Play.application().configuration().getBoolean("elastic.useclient")){
			String nodeName = Play.application().configuration().getString("elastic.writenode.name");
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
				play.Logger.debug("[ES Ads Write] Transport IP: " + elasticIp);
			}
			client = new TransportClient(settings)
			.addTransportAddress(new InetSocketTransportAddress(elasticIp, 9300));

		}
	}
	
	public static ESSuggester getInstance(){
		if (instance == null){
			synchronized (ESAdsWrite.class) {
				if (instance == null){
					instance = new ESSuggester();
				}
			}
		}
		return instance;
	}
	
	public ArrayList<Map<String, Object>> getSuggestion(String index, String type, String kewword, Integer size, String city){
		
		CompletionSuggestionFuzzyBuilder suggestionsBuilder = new CompletionSuggestionFuzzyBuilder("locality_suggester");
	    suggestionsBuilder.text(kewword);
	    suggestionsBuilder.field("locality_suggest");
	    suggestionsBuilder.size(size);
	    Iterable<? extends CharSequence>fieldvalues=Arrays.asList(city,"city");
	    suggestionsBuilder.addContextField("city", fieldvalues);
	    suggestionsBuilder.setFuzziness(Fuzziness.ONE);
	    
	    SuggestRequestBuilder suggestRequestBuilder =client.prepareSuggest(index).addSuggestion(suggestionsBuilder);
	    
	    SuggestResponse suggestResponse = suggestRequestBuilder.execute().actionGet();
	    
		ArrayList<Map<String, Object>> suggestion = new ArrayList<Map<String, Object>>();
	    
	    Iterator<? extends Suggest.Suggestion.Entry.Option> iterator =
	            suggestResponse.getSuggest().getSuggestion("locality_suggester").iterator().next().getOptions().iterator();

	    while (iterator.hasNext()) {
	        Suggest.Suggestion.Entry.Option next = iterator.next();
	        CompletionSuggestion.Entry.Option prefixOption = (CompletionSuggestion.Entry.Option) next;
	        
	        Map<String, Object> result = new HashMap<String, Object>();
	        result.put("suggestion", next.getText().string());
	        result.put("meta_data", prefixOption.getPayloadAsMap());
	        suggestion.add(result);
	    }	
		return suggestion;					
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		//node.close();
		client.close();
		super.finalize();
	}

}
