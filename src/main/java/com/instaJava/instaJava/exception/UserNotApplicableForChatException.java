package com.instaJava.instaJava.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * 
 * @author matia
 *When auth user try to add a user to a chat but is not allowed for some reason(like not found or status not acceptable)
 *throw this.
 *
 */
public class UserNotApplicableForChatException extends IllegalActionException{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * If the search or request was by user's id then here should be userId(it's
	 * attribute)
	 */
	private String attributeSearched;
	private List<String> notApplicableList; // having users username not applicable
	private HttpStatus status;

	
	public UserNotApplicableForChatException(Exception e) {
		super(e);
	}

	
	public UserNotApplicableForChatException(String msg) {
		super(msg);
	}
	
	public UserNotApplicableForChatException(String msg, List<String> notApplicableList) {
		super(msg);
		this.notApplicableList = notApplicableList;
	}
	
	public UserNotApplicableForChatException(String msg, HttpStatus status, String attributeSearched, List<String> notApplicableList) {
		super(msg);
		this.status = status;
		this.attributeSearched = attributeSearched;
		this.notApplicableList = notApplicableList;
		this.notApplicableList = notApplicableList;
	}

	public String getAttributeSearched() {
		return attributeSearched;
	}

	public List<String> getNotApplicableList() {
		return notApplicableList;
	}

	public HttpStatus getStatus() {
		return status;
	}


	
	
}
