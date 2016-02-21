package com.quikr.platform.datastore.elasticsearch;

import org.elasticsearch.index.query.QueryBuilder;

public interface ESSearchQuery {
	
	public QueryBuilder prepareQuery();

}
