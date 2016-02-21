package com.quikr.mq;

import java.util.Map;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.google.gson.Gson;
import com.quikr.constants.CampaignConstants;
import com.quikr.utils.Utils;


public class MobileVerificationSender {

	private static ApplicationContext applicationContext;
	private static MobileVerificationSender instance=new MobileVerificationSender();
    
	private MobileVerificationSender()
	{
		initialize();
	}
	
	private void initialize() {
		applicationContext=new ClassPathXmlApplicationContext("rabbit-sender-context.xml");
	}

	public static MobileVerificationSender getInstance()
	{
		return instance;
	}


	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
        ApplicationContext context =new ClassPathXmlApplicationContext("rabbit-sender-context.xml");//loading beans
        AmqpTemplate aTemplate = (AmqpTemplate) context.getBean("tutorialTemplate");// getting a reference to the sender bean
 
        Map<String, String> x = new java.util.HashMap<String, String>();

        x.put("MOBILE", "7847042963");
        x.put("campaignID", "4");
        x.put("NAME", "Nitin");
        x.put("PIN_CODE", "560037");
        x.put("EMAILID", "nitin@gmail.com");
        x.put("STATE", "DELHI");
        x.put("CITY", "DELHI");
        x.put("campaignName", "EurekaForbes");
        Gson gson = new Gson();
        String str=convert(x); 
        //str="{'campaignName':'EurekaForbes','NAME':'abul','campaignID':'3','PIN_CODE':'560072','MOBILE':'9986452945','CITY':'Mumbai','EMAILID':'ABUL@HASAN.com','formIp':'192.168.22.54'}";
        
//        LinkedTreeMap result = gson.fromJson(str , LinkedTreeMap.class);
//        System.out.println(result);
//        Map.Entry entry=(Entry) result.entrySet();
        
        aTemplate.convertAndSend("mobileverification.ivr", str);

    }
    
    public Boolean sendFormDetailForManualVerificaion(String formDetail)
    {
    	//ApplicationContext context = new ClassPathXmlApplicationContext("rabbit-sender-context.xml");//loading beans
    	AmqpTemplate aTemplate = (AmqpTemplate) applicationContext.getBean("tutorialTemplate");
    	try{
    	aTemplate.convertAndSend(CampaignConstants.ROUTING_KEY_CONTENT, formDetail);
    	}
    	catch(Exception e)
    	{
    		Utils.printException("Unable to push to queue with routing key: LeadGen.RT and form detail: "+formDetail, e);
    		return false;
    	}
    	return true;
    }
   
    
    public Boolean sendMessageForMobileVerificaion(String json,String routingKey)
    {
    	//ApplicationContext context = new ClassPathXmlApplicationContext("rabbit-sender-context.xml");//loading beans
    	AmqpTemplate aTemplate = (AmqpTemplate) applicationContext.getBean("messageSender");
    	try{
    	aTemplate.convertAndSend(routingKey, json);
    	}
    	catch(Exception e)
    	{
    		Utils.printException("Unable to push to queue with routing key: LeadGen.RT and form detail: "+json, e);
    		return false;
    	}
    	return true;
    	
    }
    
    
    public static String convert(Map<String, String> map) {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return json;
    }
    
}
