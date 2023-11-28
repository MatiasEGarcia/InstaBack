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
	 * If the search or request was by user's id then here should be userId(it's
	 * attribute)
	 */
	private String attributeSearched;
	/**
	 * For example, if was user by id, and in the request were given id in 1,2,3,4
	 * those id values need to be here.
	 */
	private List<String> valuesSearched;

	private HttpStatus status;

	public RecordNotFoundException(Exception e) {
		super(e);
	}

	public RecordNotFoundException(String msg) {
		super(msg);
	}

	public RecordNotFoundException(String msg, HttpStatus status) {
		super(msg);
		this.status = status;
	}
	
	public RecordNotFoundException(String msg, String attributeSearched, List<String> valuesSearched,
			HttpStatus status) {
		super(msg);
		this.valuesSearched = valuesSearched;
		this.attributeSearched = attributeSearched;
		this.status = status;
	}

	public List<String> getValuesNotFound() {
		return valuesSearched;
	}

	public String getAttributeSearched() {
		return attributeSearched;
	}

	public HttpStatus getStatus() {
		return this.status;
	}
}
