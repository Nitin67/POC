package com.quikr.platform.datastore.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;

import com.quikr.platform.application.CarInspectionReportApplication;

import play.Play;

public class ESCarReports extends ElasticSearch {
	
	private static ESCarReports instance = new ESCarReports();
	
	private final boolean DEBUG = false;
	
	private ESCarReports(){
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
				play.Logger.debug("[ES Car Reports] Transport IP: " + elasticIp);
			}
			client = new TransportClient(settings)
			.addTransportAddress(new InetSocketTransportAddress(elasticIp, 9300));

		}
	}
	
	public static ESCarReports getInstance(){
		return instance;
	}
	
	public Map<String, Object> processResponse(SearchResponse response){
		long total = response.getHits().getTotalHits();
		if (total >= 1){
			SearchHit[] docs = response.getHits().getHits();
			for (SearchHit doc : docs){
				String type = doc.getType();
				Map<String, Object> report =  doc.getSource();
				report.put("reportType", getRepresentableType(type));
				return report;
			}
		}
		return null;
		
	}
	
	private String getRepresentableType(String type){
		if (type.equalsIgnoreCase(CarInspectionReportApplication.TYPE_CARNATION)){
			return "Carnation";
		}else if (type.equalsIgnoreCase(CarInspectionReportApplication.TYPE_CARWALE)){
			return "Carwale";
		}
		return null;
	}
}
