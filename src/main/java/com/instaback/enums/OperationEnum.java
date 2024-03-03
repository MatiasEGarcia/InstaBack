package com.instaback.enums;

/**
 * 
 * Different kind of operations to create queries for search(specifications).
 *
 */
public enum OperationEnum {
	EQUAL("equal"),
	NOT_EQUAL("notEqual"),
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
