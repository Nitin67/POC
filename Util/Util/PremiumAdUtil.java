package premiumad.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import models.premiumAD.PremiumAD;

import org.apache.commons.lang3.StringUtils;

import premiumAd.request.OrderRequest;
import premiumAd.request.PremiumADRequest;
import premiumAd.request.PremiumAdGetPerformanceRequest;
import premiumAd.request.PurchaseRequest;
import premiumAd.request.TransactionRequest;
import services.viewModel.PerformanceUnit;

import com.google.gson.Gson;

public class PremiumAdUtil {
	public static final char MICROMARKET_ID_SEPERATOR = '_';
	public static final String PACK_PRODUCT_TYPE = "P";
	static Gson gson = new Gson();

	public static String createMicroMarketId(String cityId, String catergoryId,
			String subCategoryId) {
		/*
		 * if(StringUtils.isEmpty(cityId) || StringUtils.isEmpty(catergoryId) ||
		 * StringUtils.isEmpty(subCategoryId)){ throw new
		 * InvalidRequestException
		 * ("CityId, CategoryId and SubcatrgoryID is null"); }
		 */
		StringBuilder sb = new StringBuilder();
		sb.append(cityId);
		/*
		 * sb.append(MICROMARKET_ID_SEPERATOR); sb.append(catergoryId);
		 * sb.append(MICROMARKET_ID_SEPERATOR); sb.append(subCategoryId);
		 */
		return sb.toString();
	}

	public static String convertListIntoString(ArrayList<String> list) {
		if (list.isEmpty()) {
			return null;
		}
		String string = "";
		for (String s : list) {
			string += s + " ";
		}
		if (string == "") {
			return null;
		}
		return string;
	}

	// TODO: need to change in future for 7 , 14 days etc..
	public static Long getExpiryTime(String days) {
		if (StringUtils.isEmpty(days))
			days = "30";
		Long ts = null;
		if (days != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_WEEK, Integer.valueOf(days));
			ts = cal.getTime().getTime();
		}
		return ts;
	}

	public static int getExpiryInDays(Long expiry) {
		long diff = expiry * 1000 - System.currentTimeMillis();
		int days = (int) Math.ceil((double) diff / (24 * 60 * 60 * 1000));
		return days;
	}

	public static int getFinancialYear() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		return (month < Calendar.APRIL) ? year - 1 : year;
	}

	public static OrderRequest getOrderRequestFromPurchaseRequest(
			PurchaseRequest purchaseRequest) {
		OrderRequest orderRequest = JsonUtil
				.getObjectFromJson(JsonUtil.getJsonfromObject(purchaseRequest),
						OrderRequest.class);
		return orderRequest;
	}

	public static PremiumADRequest getPremiumADRequestFromPurchaseRequest(
			PurchaseRequest purchaseRequest) {
		PremiumADRequest premiumADRequest = JsonUtil.getObjectFromJson(
				JsonUtil.getJsonfromObject(purchaseRequest),
				PremiumADRequest.class);
		return premiumADRequest;
	}

	public static TransactionRequest getTransactionRequestFromPurchaseRequest(
			PurchaseRequest purchaseRequest) {
		TransactionRequest transactionRequest = JsonUtil.getObjectFromJson(
				JsonUtil.getJsonfromObject(purchaseRequest),
				TransactionRequest.class);
		return transactionRequest;
	}

	public static String generateOrderId(OrderRequest orderRequest) {
		String orderId = "";
		String productStatus = orderRequest.getProductStatus();
		String productType = orderRequest.getProductType();
		if (PremiumAdConstants.PAYMENT_TYPE_VOLUME_DISCOUNT.equals(productType)) {
			productStatus = PACK_PRODUCT_TYPE;
		}
		if (productStatus != null) {
			orderId += productStatus;
		}
		if (productType.equals("intMobileAlert")) {
			orderId += "ICAA";
		} else {
			orderId += orderRequest.getOrderStyle();
		}
		orderId += orderRequest.getProductId();
		orderId += orderRequest.getCityPrefix().toUpperCase();
		orderId += orderRequest.getPaymentType().toUpperCase();
		return orderId;
	}

	public static PremiumAdGetPerformanceRequest createPremiumAdGetPerformanceRequest(
			PremiumAD ad) {
		PremiumAdGetPerformanceRequest request = new PremiumAdGetPerformanceRequest();
		request.setAdId(ad.getAdId());
		request.setPerformanceType(PerformanceUnit.REPLY.toString());
		return request;

	}

	public static String getTodayDate() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}

	public static double RoundTo2Decimals(double val) {
		DecimalFormat df2 = new DecimalFormat("###.##");
		return Double.valueOf(df2.format(val));
	}

}
