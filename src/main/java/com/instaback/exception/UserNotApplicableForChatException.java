package com.instaback.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * 
 * @author matia
 *When auth user try to add a user to a chat but is not allowed for some reason(like not found or status not acceptable)
 *throw this.
 *
 */
public class UserNotApplicableForChatException extends InvalidActionException{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * If the search or request was by user's id then here should be userId(it's
	 * attribute)
	 */
	private List<String> notApplicableList; // having users username not applicable
	
	public UserNotApplicableForChatException(String msg,HttpStatus status, List<String> notApplicableList) {
		super(msg, status);
		this.notApplicableList = notApplicableList;
	}
	
	public UserNotApplicableForChatException(String msg,HttpStatus status, List<String> notApplicableList,Exception e) {
		super(msg, status,e);
		this.notApplicableList = notApplicableList;
	}

	public List<String> getNotApplicableList() {
		return notApplicableList;
	}
}
