package com.quikr.platform.datastore;



public class DataStoreFactory {
	
	
	public static AbstractDataStore getResource(DataStoreType type){
		
		switch(type){
			case SOLR:
				return SolrResource.getInstance();
			case MYSQL:
				return MySqlResource.getInstance();
			
			default:
				return null;
		}
	}

}
