package com.quikr.platform.datastore;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrResource extends AbstractDataStore {

	private static SolrResource									instance			= new SolrResource();
	private static Map<String, HttpSolrServer>	solrResources	= new HashMap<String, HttpSolrServer>();

	public static final int											SORT_ASC			= 1;
	public static final int											SORT_DESC			= 2;
	private HttpSolrServer											solrj					= null;
	private SolrQuery														query					= new SolrQuery();												;
	private QueryResponse												response;

	private SolrResource() {
	}

	public static SolrResource getInstance() {
		return instance;
	}

	public void setConnection(String baseUrl) {
	  if (StringUtils.isEmpty(baseUrl)){
	    throw new RuntimeException("Can not instantiate Solr without knowing the url to connect to ");
	  }
		if (solrResources.containsKey(baseUrl)) {
			solrj = solrResources.get(baseUrl);
		} else {
			solrj = new HttpSolrServer(baseUrl);
			solrResources.put(baseUrl, solrj);
		}

	}

	public void setQuery(String querystr) {
		query.setQuery(querystr);

	}

	public void setQuery(SolrQuery query) {
		this.query = query;
	}

	public void setRows(Integer rows) {
		query.setRows(rows);
	}

	public void setStart(Integer start) {
		query.setStart(start);
	}

	public void setDebugQuery(boolean bool) {
		query.set("debugQuery", bool);
	}

	public SolrQuery getQuery() {
		return query;
	}

	public SolrDocumentList getResults() {
		try {

			// play.Logger.error(query.getQuery());
			response = solrj.query(query);
		} catch (SolrServerException e) {
			// e.printStackTrace();
			// response.put("success", false);
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			// result.put("error_message", e.getMessage());
			play.Logger.debug(writer.toString());
			play.Logger.debug("error_message:" + e.getMessage());
		}
		return response.getResults();
	}

	public QueryResponse getResponse() {
		try {
			response = solrj.query(query);
		} catch (SolrServerException e) {
			// e.printStackTrace();
			// response.put("success", false);
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			// result.put("error_message", e.getMessage());
			play.Logger.debug(writer.toString());
			play.Logger.debug("error_message:" + e.getMessage());
		}
		return response;
	}

	public Object get() {
		String urlString = "http://localhost:8983/solr";
		CloudSolrServer solrCloud = new CloudSolrServer(urlString);
		return null;
	}

	public boolean update() {
		// TODO Auto-generated method stub
		return false;
	}

	public Integer create() {
		CloudSolrServer server = new CloudSolrServer("localhost:9983");
		server.setDefaultCollection("collection1");
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", "1234");
		doc.addField("name", "A lovely summer holiday");
		try {
			server.add(doc);
			server.commit();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setFilters(HashMap<String, String> filters) {

	}

	public void setFilter(String key, String value) {

	}

	public void setFieldNames() {

	}

	public void setFacets() {

	}

	public void setHighlights() {

	}

	public void setPhraseQuery() {

	}

	public void setSort(String field, int direction) {

	}

}
