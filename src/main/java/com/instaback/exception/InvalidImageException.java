package com.instaback.exception;

import org.springframework.http.HttpStatus;

/**
 * 
 *Is thrown when there was an error around the images.
 *
 */
public class InvalidImageException extends InvalidException{
	
	private static final long serialVersionUID = 1L;
	
	public InvalidImageException(String msg,HttpStatus status) {
		super(msg, status);
	}
	
	public InvalidImageException(String msg,HttpStatus status, Exception e) {
		super(msg, status, e);
	}
}
