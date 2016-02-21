package premiumad.util;

import com.google.gson.Gson;

import premiumAd.request.PremiumAdSavePerformanceRequest;

public class TestMain {

	
	public static void main(String args[]){
		PremiumAdSavePerformanceRequest req = new PremiumAdSavePerformanceRequest();
		req.setAdId(123454567L);
		req.setChanneltype("ABC");
		req.setMessage("ABHISHEK BABHSJHAS KADHKJAHD HAD JA");
		req.setPerformanceType("REPLY");
		req.setReplierId("abhi8886@gmail.com");
		req.setSourceId("ReplyDB");
		req.setTimeStamp(null);
		Gson gs = new Gson();
		System.out.println(gs.toJson(req));
	}
}
