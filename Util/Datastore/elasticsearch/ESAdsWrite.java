package com.quikr.platform.datastore.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import play.Play;

public class ESAdsWrite extends ElasticSearch {
	
	private static ESAdsWrite instance = null;
	
	private ESAdsWrite(){
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
	
	public static ESAdsWrite getInstance(){
		if (instance == null){
			synchronized (ESAdsWrite.class) {
				if (instance == null){
					instance = new ESAdsWrite();
				}
			}
		}
		return instance;
	}
	
	@Override
	protected void finalize() throws Throwable {
		//node.close();
		client.close();
		super.finalize();
	}

}
