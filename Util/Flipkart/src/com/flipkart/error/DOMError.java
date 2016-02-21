package com.flipkart.error;

public enum DOMError {

	MISSING_FIELD("Required field is missing",""),
	EXTRA_FIELD("Extra field in object",""),
	INVALID_TYPE("Invalid data type","");
	String errorMessage;
	String errorField;
	
	DOMError(String errString,String errorField)
	{
		this.errorMessage=errString;
		this.errorField=errorField;
	}

	public String getErrorMessage() {
		return errorField + ": "+ errorMessage;
	}

	public void setErrorMessage(String errorField) {
		this.errorField=errorField;
	}
}
