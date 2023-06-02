package com.instaJava.instaJava.enums;

public enum OperationEnum {
	EQUAL("equal"),
	LIKE("like"), 
	IN("in"),
	IN_DATES("inDates"),
	GREATER_THAN("greaterThan"), 
	LESS_THAN("lessThan"),
	BETWEEN("between"),
	IS_TRUE("isTrue"),
	IS_FALSE("isFalse");
	
	
	private String operation;

	private OperationEnum(String operation) {
		this.operation = operation;
	}

	public String getOperation() {
		return operation;
	}
}
