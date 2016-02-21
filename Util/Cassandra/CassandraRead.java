package com.quikr.platform.cassandra;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.quikr.platform.datastore.CassandraConnecter;

public class CassandraRead {

	private CassandraConnecter cassandraInstanse=null;
	
	public CassandraRead(){
		cassandraInstanse=CassandraConnecter.getInstance();
	}
	
	public void getDataFromCassandra(){
		
		cassandraInstanse.connect("54.169.25.195",9042);
		
		final String fileUrl="/home/quikr/Documents/data_copy.csv";
		BufferedReader br=null;
		String line="";
		String cvsSplitBy = ",";
		
		
		final String writefileUrl="/home/quikr/Documents/data_generated.csv";
		
		
		CSVWriter csvwriter =null;
		try {
			br=new BufferedReader(new FileReader(fileUrl));
			
			csvwriter=new CSVWriter(new FileWriter(writefileUrl));
			
			String heading[] = "FromJid,Total Message Exchanges,Distinct Chats,ToJid,Chat Messages".split(cvsSplitBy);
			
			
			
			if(csvwriter!=null){
				csvwriter.writeNext(heading);
			}
			
			while((line = br.readLine()) != null){
				
				String[] data = line.split(cvsSplitBy);
				
				ResultSet result=null;
				//result =cassandraInstanse.getSession().execute("Select tojid,messagetype,messagebody from quikr_chat.chats WHERE fromjid = ?","orai-qa.com-e@stage-chat.quikr.com");
				result =cassandraInstanse.getSession().execute("Select tojid,messagetype,messagebody from quikr_chat.chats WHERE fromjid = ?",data[0]);
			
				if(result!=null){
					
					Iterator<Row> it=result.iterator();
					
					int totalMsgExchanged=0;
					
					
					Map<String,ArrayList<String>> chatMessage=new HashMap<String,ArrayList<String>>();
					
					//ArrayList<String> tmpToJidList=new ArrayList<String>();
					
					//ArrayList<String> msg=new ArrayList<String>();
					
					//String toJid=null;
					
					while(it.hasNext()){
						
						Row row = it.next();
						
						if(row!=null){
							
							if(!row.getString("messagetype").equalsIgnoreCase("reply")){
								totalMsgExchanged++;
								
								
								/*if(!tmpToJidList.contains(row.getString("tojid"))){
									
									tmpToJidList.add(row.getString("tojid"));
								}*/
								
							}
							//row.getString("messagebody")
							//ArrayList<String> msg=new ArrayList<String>();
							
							ArrayList<String> existingData=chatMessage.get(row.getString("tojid"));
							
							if(existingData==null){
								
								ArrayList<String> msg=new ArrayList<String>();
								
								msg.add(row.getString("messagebody"));
								//myMap.put(String.valueOf(new Date().getTime()),row.getString("messagebody"));
								
								chatMessage.put(row.getString("tojid"),msg);
							}else{
								
								
								existingData.add(row.getString("messagebody"));
								chatMessage.put(row.getString("tojid"),existingData);
							}
							
							//play.Logger.info(chatMessage.toString());
								
							
						}
					}
					
					
					
					//String dataFromCassandra = "orai-qa.com-e@stage-chat.quikr.com,"+totalMsgExchanged+","+distinctChats;
					
					String dataFromCassandra = data[0]+","+totalMsgExchanged+","+chatMessage.size()+",";
					
					String finalData[]=dataFromCassandra.split(cvsSplitBy);
					
					if(csvwriter!=null){
						csvwriter.writeNext(finalData);
						
						if(totalMsgExchanged>1){
						
							
							for (Map.Entry<String, ArrayList<String>> entry : chatMessage.entrySet()){
								
								for(int i=0;i<entry.getValue().size();i++){
									
									if(i==0){
										dataFromCassandra=",,,"+entry.getKey()+","+entry.getValue().get(i);
									}else{
										dataFromCassandra=",,,,"+entry.getValue().get(i);
									}
									
									finalData=dataFromCassandra.split(cvsSplitBy);
									
									csvwriter.writeNext(finalData);
								}
								
								
								
							}	
							
						}
						
					}
					
					
					play.Logger.info("Total Msg Exchanged: "+totalMsgExchanged);
					
					play.Logger.info("Distinct Chat: "+chatMessage.size());
					
					
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			play.Logger.debug("[CSV reading File Not Found Exception]: "
					+ writer.toString());

			
		}catch(Exception e){
			// TODO Auto-generated catch block
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			play.Logger.debug("[Cassandra Data Read General Exception]: "
					+ writer.toString());
		}finally{
			
			if(br!=null){
				try {
					
					br.close();
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));

					play.Logger.debug("[CSV File Closing IO Exception]: "
							+ writer.toString());

				}catch(Exception e){
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));

					play.Logger.debug("[CSV File Closing General Exception]: "
							+ writer.toString());
				}
			}
			
			if(csvwriter!=null){
				try {
					
					csvwriter.close();
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));

					play.Logger.debug("[Write CSV File Closing IO Exception]: "
							+ writer.toString());

				}catch (Exception e) {
					// TODO Auto-generated catch block
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));

					play.Logger.debug("[Write CSV File Closing General Exception]: "
							+ writer.toString());

				}
			}
			
		}
		
		
		
		
		
	}
	
}
