package premiumad.util;


import java.io.IOException;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class JsonUtil {
    
    private static final Logger log = play.Logger.underlying();

	static Gson gson = new Gson();
	
	static ObjectMapper objectMapper= new ObjectMapper();
	
	public static <T> T getObjectFromJson(String jsonString, Class<T> clazz){
		T object = gson.fromJson(jsonString, clazz);
		return object;
	}
	
	
	public static <T> String getJsonfromObject(T object){
		return gson.toJson(object);
	}
	
	public static <T> T getObjectFromJsonNode(JsonNode json, Class<T> clazz){
		T object=null;
		try {
			object = objectMapper.treeToValue(json, clazz);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}
	
	public static JsonNode getJsonNodeFromJson(String json)
	{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj=null;
		try {
			actualObj = mapper.readTree(json);
		} catch (JsonProcessingException e) {
		log.error("Unable to parse json", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		log.error("IO exception while parsing response of Platform Edit Ad API", e);
		}
		return actualObj;
	}
	 public static JsonNode convertObjectToJsonNode(Object object) {
		 JsonNode jsonAdObject = objectMapper.valueToTree(object);
		 return jsonAdObject;
	 }
	
	
	
}
