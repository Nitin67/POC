package premiumad.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import services.viewModel.PerformanceUnit;

public class PremiumAdConstants {	
	 public static final Map<PerformanceUnit, Long> DEFAULT_MIN_UNITS;
	    static {
	    	 Map<PerformanceUnit, Long> aMap = new HashMap<PerformanceUnit, Long>();
	        aMap.put(PerformanceUnit.CLICK, 10L);
	        aMap.put(PerformanceUnit.IMPRESSION, 10L);
	        aMap.put(PerformanceUnit.REPLY, 10L);
	        DEFAULT_MIN_UNITS = Collections.unmodifiableMap(aMap);
	    }
	
	 public static final long MIN_VALID_MESSAGE_LENGTH = 10;
	    
	    
	 public static final String PURCHASE_ORDER_PENDING = "pending";
	 public static final String PURCHASE_ORDER_SUCCESS = "success";
	 public static final Integer PAYMENT_PENDING = 0;
	 public static final String PURCHASE_AD_BASIC = "basic";
	 public static final String PURCHASE_AD_PREMIUM = "premium";
	 public static final String PURCHASE_AD_EXPRIED = "expired";
	 public static final String AD_ID = "adId";
	 public static final String MICROMARKET_ID = "micromarketId";
	 public static final String USER_ID = "userId";
	 public static final String PRICING_UNIT = "pricingUnit";
	 public static final String AD_STATUS = "adStatus";
	 public static final String ORDER_STATUS = "orderStatus";
	 public static final String AD_START_TIME = "adStartTime";
	 public static final String AD_UPDATED_TIME = "adUpdatedTime";
	 public static final String AD_EXPIRY_TIME = "adExpiryTime";
	 public static final String ORDER_FAILURE_REASON = "orderFailureReason";
	 public static final String REFUND_ID = "refundId";
	 public static final String UNITS_PURCHASED = "unitsPurchased";
	 public static final String UNITS_USED = "unitsUsed";
	 public static final String TRANSACTION_ID = "id";
	 public static final String ORDER_ID = "orderId";
	 public static final String PAYMENT_STATUS = "paymentStatus";
	 public static final String ORDER_UPDATED_REASON = "orderUpdatedReason";
	 public static final String BANK = "bank";
	 public static final String MODE_OF_PAYMENT = "modeOfPayment";
	 public static final String PAYMENT_GATEWAY_TRANSACTION_ID = "tpslId";
	 public static final String BASIC_AD = "basic";
	 public static final String DEFAULT_MODE_OF_PAYMENT = "XX";
	 public static final String CITY_ID = "cityId";
	 public static final String CATEGORY_ID = "categoryId";
	 public static final String SUB_CATEGORY_ID = "subCategoryId";
	 public static final String PAYMENT_TYPE = "paymentType";
	 public static final String MODE_OF_PAYMENT_GATEGWAY = "modeOfPaymentGateway";
	 public static final String PRODUCT_TYPE = "productType";
	 public static final String ORDER_AMOUNT = "amount";
	 public static final String PRODUCT_STATUS = "productStatus";
	 public static final String PAYMENT_FOR = "paymentFor";
	 public static final String ORDER_REMARK = "remark";
	 public static final String PACK_ID = "packId";
	 public static final String PRODUCT_ID = "productId";
	 public static final String ATTEMPTS = "attempts";
	 public static final Integer PAYMENT_TO_INITATE_ORDER = -1;
	 public static final Integer PAYMENT_INITIATE_TRANSACTION = 0;
	 public static final Integer PAYMENT_SUCCESS = 1;
	 public static final Integer PAYMENT_FAIL = 2;
	 public static final Integer PAYMENT_NORESPONSE = 3;
	 public static final Integer PAYMENT_AFTER_THREE_ATTEMPT = 4;
	 public static final Integer CONVERT_TO_FREE_AD_BEFORE_PAYMENT = 5;
	 public static final Integer PAYMENT_REFUND = 6;
	 public static final Integer PAYMENT_ADMIN_DELETED = 7;
	 public static final Integer PAYMENT_PACK_DEACTIVATED = 8;
	 public static final Integer PAYMENT_PACK_RECREATION = 9;
	 public static final String INACTIVE_ORDER = "N";
	 public static final String ACTIVE_ORDER = "Y";
	 public static final String NEW_ORDER = "N";
	 public static final String RENEW_ORDER = "R";
	 public static final String EXPIRE_ORDER_TIMESTAMP = "expireTimeStamp";
	 public static final String ORDER_STYLE = "orderStyle";
	 public static final String AD_CREDIT_COUNT = "adCreditCount";
	 public static final String CITY_PREFIX = "cityPrefix";
	 public static final String REFERRER = "referrer";
	 public static final String BASE_PRICE = "basePrice";
	 public static final long DEFAULT_EXPIRY_TIME = 30;
	 public static final String LISTING_FEE = "LF";
	 public static final String AD_CONVERTEDTOFREE_EXPIRED = "e";
	 public static final String AD_CONVERTEDTOFREE_REFUND = "r";
	 
	 public static final String AD_STYLE_BASIC = "B";
	 public static final String AD_STYLE_TOP = "T";
	 public static final String AD_STYLE_URGENT = "H";
	 public static final String AD_STYLE_TOP_URGENT = "HT";
	 public static final String NOT_APPLICABLE = "NA";
	 
	 public static final String PAYMENT_TYPE_CREDITS = "v";
	 public static final String PAYMENT_TYPE_AD = "ad";
	 public static final String PAYMENT_TYPE_VOLUME_DISCOUNT = "vd";
	 
	 public static final String AD_ACTION_P2F = "P2F";
	 public static final String PRODUCT_AD = "ad";
	 public static final String PAY_AUTORENEW = "ar";
	 public static final boolean SPEC_CAT_ALLOW = true;
	 public static final String SPEC_CAT_IDs = "123,194,93";
	 public static final int SPEC_CAT_ADS_EXPIRE_TIME = 120;
	 public static final int ADS_EXPIRE_TIME = 90;
	 public static final int PAID_ADS_LIFE_TIME = 30;
	 
	 public static final int VOLUME_DISCOUNT_USER_INACTIVE = 0;
	 public static final int VOLUME_DISCOUNT_USER_ACTIVE = 1;
	 public static final int SECONDS_IN_A_DAY = 24 * 60 * 60;
	 
	 
	 
	 public static final String ANALYTICS_QUEUE_TYPE = "CLUSTER";
	 public static final String ANALYTICS_EXCHANGE_NAME = "rt_ppr_ppc_publish_x";
	 public static final String ANALYTICS_ROUTING_KEY = "*";
	 
	 public static final String PRODUCT_TYPE_VOLUME_DISCOUNT = "vd";
	 // Edit premium ad by admin 
	 public class EditPremiumAdByAdmin{
		 public static final String SET_EXPIRY_ACTION = "setExpiry";
		 public static final String PUBLISH_ACTION = "publish";
		 public static final String REVIEW_ACTION = "review";
		 public static final String EDIT_AD_ACTION = "editAd";
		 
	 }
	public static final String PAYMENT_FOR_M2T = "M2T";
	public static final Double SERVICE_TAX_MULTIPLIER = 1.145;
}
