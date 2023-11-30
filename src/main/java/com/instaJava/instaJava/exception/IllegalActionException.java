package com.instaJava.instaJava.exception;

import org.springframework.http.HttpStatus;

/**
 * 
 * This will be thrown when the client tries to do an action that the user auth cannot do, for distinct reasons
 */
public class IllegalActionException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	private HttpStatus status;

	public IllegalActionException(Exception e) {
		super(e);
	}

	public IllegalActionException(String msg) {
		super(msg);
	}
	
	public IllegalActionException(String msg, HttpStatus status) {
		super(msg);
		this.status = status;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
}
