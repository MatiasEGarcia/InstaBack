package com.instaJava.instaJava.exception;

import java.util.List;

/**
 * 
 * @author matia
 *When auth user try to add a user to a chat but is not allowed for some reason(like not found or status not acceptable)
 *throw this.
 *
 */
public class UserNotApplicableForChatException extends IllegalActionException{
	
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private List<String> usernameList; // having users username not applicable

	
	public UserNotApplicableForChatException(Exception e) {
		super(e);
	}

	
	public UserNotApplicableForChatException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor
	 * @param msg message 
	 * @param listOfUsersName list of users' username.
	 */
	public UserNotApplicableForChatException(String msg, List<String> listOfUsersName) {
		super(msg);
		if(!listOfUsersName.isEmpty() || listOfUsersName != null) {
			usernameList = listOfUsersName;
		}
	}
	
	public List<String> getUsernameList() {
		return usernameList;
	}
	
}
