package com.instaJava.instaJava.exception;

/**
 * Is thrown when there was a try to save a record by it already exist.
 */
public class AlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public AlreadyExistsException(Exception e) {
		super(e);
	}
	
	public AlreadyExistsException(String msg) {
		super(msg);
	}
}
