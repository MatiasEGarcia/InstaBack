package com.instaJava.instaJava.exception;

import org.springframework.http.HttpStatus;

/**
 * 
 * This will be thrown when the client tries to do an action that the user auth cannot do, for distinct reasons
 */
public class InvalidActionException extends InvalidException{
	
	private static final long serialVersionUID = 1L;
	
	public InvalidActionException(String msg, HttpStatus status) {
		super(msg, status);
	}
	
	public InvalidActionException(String msg, HttpStatus status, Exception e) {
		super(msg, status, e);
	}
}
