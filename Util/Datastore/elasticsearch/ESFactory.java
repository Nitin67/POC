package com.quikr.platform.datastore.elasticsearch;

public class ESFactory {
	
	public static ElasticSearch getES(ESInstanceType type){
		switch(type){
			case ESAdsREAD:
				return ESAdsRead.getInstance();
			case ESAdsWRITE:
				return ESAdsWrite.getInstance();
			case ESMetric:
				return ESMetrics.getInstance();
			case ESNotification:
				return ESNotification.getInstance();
			case ESEcomm:	
				return ESEcomm.getInstance();
			case ESAdMLT:	
				return ESAdMLT.getInstance();	
			case ESCarReports:
				return ESCarReports.getInstance();
		}
		return null;
	}

}
