package com.instaback.exception;

import org.springframework.http.HttpStatus;

/**
 * Is thrown when there was a try to save a record by it already exist.
 */
//I can add @ResponseStatus(value = HttpStatus.NOT_FOUND) a todas las exceptionces
public class AlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private HttpStatus status;
	
	public AlreadyExistsException(Exception e) {
		super(e);
	}
	
	public AlreadyExistsException(String msg) {
		super(msg);
	}
	
	public AlreadyExistsException(String msg, HttpStatus status) {
		super(msg);
		this.status = status;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
}
