package com.instaback.exception;

import org.springframework.http.HttpStatus;

/**
 * @author matia
 * Exception to throw when something is not found.
 * If this error it manage by a exception handler should return NOT_FOUND status.
 */
public class NotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	private final HttpStatus status;

	public NotFoundException(String msg, HttpStatus status) {
		super(msg);
		this.status = status;
	}
	
	public NotFoundException(String msg, HttpStatus status,Exception e) {
		super(msg,e);
		this.status = status;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
}
