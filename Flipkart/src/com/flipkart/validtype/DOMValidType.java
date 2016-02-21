package com.flipkart.validtype;

public enum DOMValidType {

	INT("int"),
	STRING("string");
	private String typeName;
	
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	DOMValidType(String type)
	{
		typeName=type;
	}
}
