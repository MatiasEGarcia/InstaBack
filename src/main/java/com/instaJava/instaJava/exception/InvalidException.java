package com.instaJava.instaJava.exception;

public class InvalidException  extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public InvalidException(Exception e) {
		super(e);
	}
	
	public InvalidException(String msg) {
		super(msg);
	}

}
