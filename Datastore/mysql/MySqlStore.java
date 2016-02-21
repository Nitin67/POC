package com.quikr.platform.datastore.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.quikr.platform.datastore.DataStore;
import com.quikr.platform.datastore.mysql.response.DBResponse;
import com.quikr.platform.datastore.mysql.response.DBRow;

import play.db.DB;

public class MySqlStore implements DataStore {
	
	private String dbName;
	private Connection connection;
	
	public MySqlStore (String db){
		dbName = db;
		connection = DB.getConnection(dbName);
	}

	@Override
	public Object getConnection() {
		return connection;
	}
	
	public Object executeAndClose(DBCommand command) throws SQLException{
	    try{
	    	return command.execute(this);
	    } finally {
			this.connection.close();
	    }
	}
	
	public Object transaction(DBCommand command) throws SQLException{
	    try{
	        this.connection.setAutoCommit(false);
	        Object returnValue = command.execute(this);
	        this.connection.commit();
	        return returnValue;
	    } catch(Exception e){
			this.connection.rollback();
		    throw e; //or wrap it before rethrowing it
	    } finally {
	    	this.connection.setAutoCommit(true);
	    }
	}
	
	public Object transactionAndClose(final DBCommand command) throws SQLException{
	    return executeAndClose(new DBCommand(){
	      public Object execute(MySqlStore mysql) throws SQLException{
	    	  return mysql.transaction(command);
	      }
	    });
	}
	
	public DBResponse execute(String sql, String[] params,String[] colName) throws SQLException{
		
		
		ResultSet rs=null;
		DBResponse response=null;
		PreparedStatement preparedStatement = null;
		
		try{ 
			
			
			preparedStatement = connection.prepareStatement(sql);
			for(int i = 0 ; i < params.length ; i++){
				
				preparedStatement.setString(i+1,params[i]);
			}
			
			rs = preparedStatement.executeQuery();
			
			response = createResponse(rs,colName);
			
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(preparedStatement!=null){
				preparedStatement.close();
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
	
	public int update(String sql, String[] params,Integer[] types) throws SQLException{
		
		PreparedStatement preparedStatement=null;
		ResultSet rs=null;
		int ret=-1;
		try{
			preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			for(int i = 0 ; i < params.length ; i++){
				if (params[i] == null || params[i].equalsIgnoreCase("null")){
					preparedStatement.setNull(i+1, types[i]);
				}else if (types[i] == Types.INTEGER
						|| types[i] == Types.BIGINT
						|| types[i] == Types.SMALLINT
						|| types[i] == Types.TINYINT){
					preparedStatement.setInt(i+1, Integer.parseInt(params[i]));
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
		}finally{
			if(rs != null){
				rs.close();
			}
			if(preparedStatement != null){
				preparedStatement.close();
			}
		}
		return ret;
	}

}
