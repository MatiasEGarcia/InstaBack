package com.instaJava.instaJava.exception;

/**
 * 
 *Is thrown when there was an error around the images.
 *
 */
public class ImageException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public ImageException(Exception e) {
		super(e);
	}

	public ImageException(String msg) {
		super(msg);
	}
}
