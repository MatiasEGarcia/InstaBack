package com.instaJava.instaJava.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * 
 * @author matia
 *
 *         When it wasn't found some record in database.
 *
 */
public class RecordNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	/**
	 * For example, if was user by id, and in the request were given id in 1,2,3,4
	 * those id values need to be here.
	 */
	private List<String> valuesSearched;
	
	public RecordNotFoundException(String msg,HttpStatus status) {
		super(msg,status);
	}
	
	public RecordNotFoundException(String msg, List<String> valuesSearched,HttpStatus status) {
		super(msg,status);
		this.valuesSearched = valuesSearched;
	}
	
	public RecordNotFoundException(String msg, List<String> valuesSearched,HttpStatus status,Exception e) {
		super(msg,status,e);
		this.valuesSearched = valuesSearched;
	}

	public List<String> getValuesNotFound() {
		return valuesSearched;
	}
}
