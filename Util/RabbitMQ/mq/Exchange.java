package com.quikr.mq;

public enum Exchange {
	
	LEADGEN_MOBILE_VERFICIATION_EXCHANGE("leadgen_mobile_verication_exchange","topic");
	
	private String exchangeName;
	private String exchangeType;
	Exchange(String exchangeName,String exchangeType) {
		this.exchangeName=exchangeName;
		this.exchangeType=exchangeType;
	}
	public String getExchangeName() {
		return exchangeName;
	}

	public String getExchangeType() {
		return exchangeType;
	}



}
