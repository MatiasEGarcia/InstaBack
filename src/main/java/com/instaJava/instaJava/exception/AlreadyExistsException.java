package com.instaJava.instaJava.exception;

/**
 * Is thrown when there was a try to save a record by it already exist.
 */
//I can add @ResponseStatus(value = HttpStatus.NOT_FOUND) a todas las exceptionces
public class AlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public AlreadyExistsException(Exception e) {
		super(e);
	}
	
	public AlreadyExistsException(String msg) {
		super(msg);
	}
}
