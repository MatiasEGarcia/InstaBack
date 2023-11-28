package com.instaJava.instaJava.exception;

/**
 * @author matia
 * Exception to throw when something is not found.
 * If this error it manage by a exception handler should return NOT_FOUND status.
 */
public class NotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public NotFoundException(Exception e) {
		super(e);
	}

	public NotFoundException(String msg) {
		super(msg);
	}
}
