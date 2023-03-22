package com.instaJava.instaJava.exception;

public class AlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public AlreadyExistsException(Exception e) {
		super(e);
	}
	
	public AlreadyExistsException(String msg) {
		super(msg);
	}
}
