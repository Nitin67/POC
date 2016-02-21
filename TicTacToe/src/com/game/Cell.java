package com.game;

public enum Cell {

	EMPTY(" "),
	CROSS("X"),
	NOUGHT("O");
	String val;

	
	public String getVal() {
		return val;
	}


	public void setVal(String val) {
		this.val = val;
	}


	Cell(String val) {
		this.val=val;
	}
}