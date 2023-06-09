package com.instaJava.instaJava.exception;

/**
 * 
 * This will be thrown when the client tries to do an action that the user auth cannot do, for distinct reasons
 */
public class IllegalActionException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public IllegalActionException(Exception e) {
		super(e);
	}

	public IllegalActionException(String msg) {
		super(msg);
	}
}
