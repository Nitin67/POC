package com.quikr.platform.datastore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quikr.platform.datastore.mysql.response.DBResponse;
import com.quikr.platform.datastore.mysql.response.DBRow;

import play.cache.Cache;
import play.db.DB;
import play.libs.Json;

public class MySqlResource extends AbstractDataStore{
	
	private static MySqlResource instance = new MySqlResource();
	
	private MySqlResource(){
	}

	public static MySqlResource getInstance(){
		return instance;
	}
	
	public Connection getConnection(String dbName){
		return DB.getConnection(dbName);
	}
	
	public DBResponse execute(String sql, String[] params,String[] colName, String db) throws SQLException{
		
		Connection connect=null;
		ResultSet rs=null;
		DBResponse response=null;
		PreparedStatement preparedStatement = null;
		
		try{ 
			connect= DB.getConnection(db);
			
			preparedStatement = connect.prepareStatement(sql);
			for(int i = 0 ; i < params.length ; i++){
				preparedStatement.setString(i+1,params[i]);
			}
			rs = preparedStatement.executeQuery();
			response = createResponse(rs,colName);
			
		}catch(SQLException e){
			connect.close();
			ObjectNode exception  = Json.newObject();
			exception.put("query", sql);
			exception.put("message", e.getMessage());
			throw new SQLException(exception.toString());
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(preparedStatement!=null){
				preparedStatement.close();
			}
			if(connect!=null){
				connect.close();
			}
		}
		
		return response;
	}
	
	private DBResponse createResponse(ResultSet result, String[] colName) throws SQLException{
		DBResponse response = new DBResponse();
		while(result.next()){
			
			DBRow row = new DBRow();
			for(int i = 0 ; i < colName.length ; i++){
				row.setData(colName[i],result.getString(colName[i]));
			}
			response.add(row);
		}
		return response;
	}
	
	public void close(Connection connect) throws SQLException{
		if (connect != null){
			connect.close();
		}
	}
	
	public int update(String sql, String[] params,Integer[] types, String db) throws SQLException{
		
		Connection connect=null;
		PreparedStatement preparedStatement=null;
		ResultSet rs=null;
		int ret=-1;
		try{
			connect = DB.getConnection(db);
			preparedStatement = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			for(int i = 0 ; i < params.length ; i++){
				if (params[i] == null || (params[i].equalsIgnoreCase("null"))){
					preparedStatement.setNull(i+1, types[i]);
				}else if (types[i] == Types.INTEGER
						|| types[i] == Types.BIGINT
						|| types[i] == Types.SMALLINT
						|| types[i] == Types.TINYINT){
					int val;
					try {
						val = Integer.parseInt(params[i]);
					}catch (Exception e){
						ObjectNode exception  = Json.newObject();
						exception.put("query", sql);
						exception.put("indexPosition", i);
						exception.put("type", types[i]);
						exception.put("message", e.getMessage());
						throw new IllegalArgumentException(exception.toString());
					}
					preparedStatement.setInt(i+1, val);
				}else {
					preparedStatement.setString(i+1, params[i]);
				}	
			}
			preparedStatement.executeUpdate();
			rs = preparedStatement.getGeneratedKeys();
			ret = -1;
			if(rs.next())
			{
			    ret = rs.getInt(1);
			}
		}catch(SQLException e){
			connect.close();
			ObjectNode exception  = Json.newObject();
			exception.put("query", sql);
			exception.put("message", e.getMessage());
			throw new SQLException(exception.toString());
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(preparedStatement!=null){
				preparedStatement.close();
			}
			if(connect!=null){
				connect.close();
			}
			
		}
		return ret;
	}
	
	public int getColumnType(String table, String column, String db){
		Integer dataType = 0;
		StringBuilder key = new StringBuilder("ColumnType_")
								.append(table)
								.append("_").append(column);
		Integer columnType = (Integer)Cache.get(key.toString());
		if (columnType != null){
			dataType = columnType;
		}else {
			Connection connect = DB.getConnection(db);
			DatabaseMetaData meta;
			try {
				meta = connect.getMetaData();
				ResultSet columns = meta.getColumns(null, null, table, column);
				if (columns.next()) {
					dataType = columns.getInt("DATA_TYPE");
		        }
				Cache.set(key.toString(), dataType);
				connect.close();
			} catch (SQLException e) {
				StringWriter writer = new StringWriter();
				e.printStackTrace(new PrintWriter(writer));
				play.Logger.debug(writer.toString());
				try {
					connect.close();
				} catch (SQLException e1) {
					StringWriter writer1 = new StringWriter();
					e.printStackTrace(new PrintWriter(writer1));
					play.Logger.debug(writer1.toString());
				}
			}
		}	
		return dataType;
	}
	
	//Incomplete Builder Classes
	public class Select {
		private List<String> fields;
		private String table;
		private Where where;
		
		public String build() throws Exception{
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			if (fields != null && fields.size() > 0){
				for (String col : fields){
					query.append(col).append(" ,");
				}
				query.deleteCharAt(query.length() - 1);
			}else {
				//TODO: create custom exception class
				throw new Exception("field names required.");
			}
			query.append(" FROM ").append(table);
			if (where != null){
				query.append(where.build());
			}
			return query.toString();
		}
	}
	
	public class Where {
		
		public void whereAND(String col, String val){
			
		}
		
		public void whereOR(String col, String val){
			
		}
		
		public String build(){
			StringBuilder where = new StringBuilder();
			
			return where.toString();
		}
	}
	

}
