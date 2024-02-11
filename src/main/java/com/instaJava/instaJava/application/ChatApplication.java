package com.instaJava.instaJava.application;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqAddUserChat;
import com.instaJava.instaJava.dto.request.ReqCreateChat;
import com.instaJava.instaJava.dto.request.ReqDelUserFromChat;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.exception.UserNotApplicableForChatException;

public interface ChatApplication {
	
	/**
	 * Method to get all authenticated user's chats.
	 * @param pageNo.    - For pagination, number of the page.
	 * @param pageSize.  - For pagination, size of the elements in the same page.
	 * @return ResPaginationG<ChatDto> - chat list with pagination info.
	 */
	ResPaginationG<ChatDto> getAuhtUserChats(int pageNo, int pageSize);
	
	
	/**
	 * Method to create a chat entity and save it in dbb.
	 * @param ReqCreateChat - contain relevant info to create chat.
	 * @throws IllegalArgumentException - if reqChat is null, if reqChat.usersToAdd is null or empty.
	 * @throws RecordNotFoundException - if some user wasn't found.
	 * @throws UserNotApplicableForChatException - if there one user that cannot be added to the chat for some reason.
	 * @throws InvalidActionException if chat type is private and the number of users to add is more.
	 * @return Chat created.
	 */
	ChatDto create(ReqCreateChat reqChat);
	
	/**
	 * Method to set or update chat image in the case that is a group chat.
	 * 
	 * @param image - image to add to chat record.
	 * @param chatId - chat's id to update.
	 * @return ChatDto object with the updated Chat's info.(with messages not watched)
	 */
	ChatDto setImage(MultipartFile image,Long chatId);
	
	/**
	 * Set group chat's name(or update).
	 * @param chatId - chat's id, to know which update.
	 * @param name - new chat's name.
	 * @return ChatDto with Chat info updated.(with messages not watched)
	 */
	ChatDto setChatName(Long chatId,String name);
	
	/**
	 * Delete chat by id.
	 * @param chatId - chat's id.
	 * @return Deleted chat info.(without messages not watched)
	 */
	ChatDto deleteChatById(Long chatId);
	
	/**
	 * Method to get all Users information from a chat.
	 * @param chatId - chat's id
	 * @return List of users information.¿
	 */
	List<UserDto> getAllUsersByChatId(Long chatId);
	
	/**
	 * Function to add users in chat by username, simple or admin users.
	 * @param reqUserListChat - contain chat identification and users to add.
	 * @throws RecordNotFoundException - if some user wasn't found.
	 * @throws UserNotApplicableForChatException - if there one user that cannot be added to the chat for some reason.
	 * @return ChatDto with Chat info updated.
	 */
	ChatDto addUsers(ReqAddUserChat ReqAddUserChat);
	
	/**
	 * Function to quit users in chat by username, simple or admin users.
	 * @param reqUsersChat - contain chat identification and users to quit.
	 * @return ChatDto with Chat info updated.
	 */
	ChatDto quitUsersFromChat(ReqDelUserFromChat reqDelUserFromChat);
	
	/**
	 * Function to set a user as admin in a chat, it can be settled to admin if before was a simple user, or 
	 * can be settled to a simple user if before was an admin.
	 * @param chatId - chat's id.
	 * @param userId - user's id.
	 * @return ChatDto with Chat info updated.
	 */
	ChatDto changeAdminStatus(Long chatId, Long userId);
	
	
}
