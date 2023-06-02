package com.instaJava.instaJava.enums;

public enum OperationEnum {
	EQUAL("com.instaJava.instaJava.specification.operation.OpEqual"),
	LIKE("com.instaJava.instaJava.specification.operation.OpLike"), 
	IN("com.instaJava.instaJava.specification.operation.OpIn"),
	IN_DATES("com.instaJava.instaJava.specification.operation.OpInZonedDateTime"),
	GREATER_THAN("com.instaJava.instaJava.specification.operation.OpGreaterThan"), 
	LESS_THAN("com.instaJava.instaJava.specification.operation.OpLessThan"),
	BETWEEN("com.instaJava.instaJava.specification.operation.OpBetween"),
	IS_TRUE("com.instaJava.instaJava.specification.operation.IsTrue"),
	IS_FALSE("com.instaJava.instaJava.specification.operation.IsFalse");
	
	
	private String Direction;

	private OperationEnum(String direction) {
		Direction = direction;
	}

	public String getDirection() {
		return Direction;
	}

	public void setDirection(String direction) {
		Direction = direction;
	}
	
	
	
}
