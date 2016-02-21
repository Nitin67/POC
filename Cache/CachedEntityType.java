package com.quikr.platform.cache;

public enum CachedEntityType {
	
			DEMAIL("demail"),
			CATEGORY("category"),
			USER("userLegacy"),
			ECOM_CATEGORY("ecom_category"),
			AD("ad"),
			AREA("area"),
			SESSION("session"),
			QUOTE("quote"),
			CUSTOMER_SESSION("customer_session"),
			PAGE_VIEWS("page_views"),
			LOCALITY_PROXIMITY("locality_proximity"),
			GEO_LOCATION("geo_location"),
			SEARCH_SUGGESTION("search_suggestion"),
			AD_CONSUMER("ad_consumer"),
			AD_AWS("ad_aws"),
			LEGACY("legacy"),
			USER_ONLINE("user_online_time"),
<<<<<<< HEAD
			USER_PRESENCE("user_chat_presence"),
			QUIKR_LIVE("quikr_live"),
			PRICE_METER("price_meter"),
			ECOM_BASSIC("ecom_basic");
=======
			CAMPAIGN("campaign");
>>>>>>> 79a386d... Lead Gen Changes

			
			private final String value;
			
			CachedEntityType(String val){
				this.value = val;
			}
			
			public String getValue(){
				return value;
			}
}
