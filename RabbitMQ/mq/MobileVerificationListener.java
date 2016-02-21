package com.quikr.mq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.quikr.leadgen.MobileVerificationManagerHelper;
import com.quikr.utils.Utils;

/**
 * This class implements org.springframework.amqp.core.MessageListener. It is
 * tied to TUTORIAL_EXCHANGE and listing to an anonomous queue which picks up
 * message in the TUTORIAL_EXCHANGE with a routing pattern of my.routingkey.1
 * specified in rabbt-listener-contet.xml file.
 */

public class MobileVerificationListener implements MessageListener {

	@Autowired
	private MobileVerificationManagerHelper mobileVerificationManagerHelper;

	public void onMessage(Message message) {
		System.out.println(message);
		if(message==null)  {
			Utils.printMessage("Null message");
			return;}
		Utils.printMessage("message received");
		
		try {
			mobileVerificationManagerHelper.handleMobileVerification(message);
		} catch (Exception e) {
			Utils.printMessage("MobileVerificationListener.class :  Exception: "
					+ e.getMessage());
		}
		Utils.printMessage("message processed");

	}
}
