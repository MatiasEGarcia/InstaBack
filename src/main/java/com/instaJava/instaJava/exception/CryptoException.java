package com.instaJava.instaJava.exception;

import org.springframework.http.HttpStatus;

/**
 * 
 * @author matia
 *For exceptions in the context of cryptography.
 */
public class CryptoException extends SecurityException{

	private static final long serialVersionUID = 1L;

	
	public CryptoException(String msg, HttpStatus status) {
		super(msg, status);
	}
	
	public CryptoException(String msg, HttpStatus status, Exception e) {
		super(msg, status, e);
	}


}
