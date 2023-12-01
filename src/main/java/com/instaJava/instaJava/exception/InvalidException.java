package com.instaJava.instaJava.exception;

import org.springframework.http.HttpStatus;

public class InvalidException  extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private final HttpStatus status;
	
	public InvalidException(String msg,HttpStatus status) {
		super(msg);
		this.status = status;
	}
	
	public InvalidException(String msg,HttpStatus status, Exception e) {
		super(msg, e);
		this.status = status;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
}
