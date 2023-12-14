package com.instaJava.instaJava.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqAddUserChat;
import com.instaJava.instaJava.dto.request.ReqCreateChat;
import com.instaJava.instaJava.dto.request.ReqDelUserFromChat;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.exception.InvalidImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.exception.UserNotApplicableForChatException;

public interface ChatService {

	/**
	 * Method to get Chat info by chat's id.
	 * @param chatId - chat's id.
	 * @return ChatDto object with Chat record info.
	 * @throws IllegalArgumentException  if chatId is null.
	 * @throws RecordNotFoundException if chat was not found.
	 */
	ChatDto getById(Long chatId);
	
	/**
	 * Method to get all authenticated user's chats.
	 * @param pageInfoDto - It has pagination info. (must not be null)
	 * @return ResPaginationG<ChatDto> - chat list with pagination info.
	 * @throws IllegalArgumentException - if some value in pageInfoDto is null and is needed.
	 * @throws RecordNotFoundException - if none chat was found.
	 */
	ResPaginationG<ChatDto> getAuthUserChats(PageInfoDto pageInfoDto);
	
	/**
	 * Method to create a chat entity and save it in dbb.
	 * @param reqChat - object with info to create chat entity (must not be null)
	 * @throws IllegalArgumentException - if some value in reqChat is null and is needed.
	 * @throws RecordNotFoundException - if some user wasn't found.
	 * @throws UserNotApplicableForChatException - if there one user that cannot be added to the chat for some reason.
	 * @throws InvalidActionException if chat type is private and the number of users to add is more.
	 * @return ChatDto object with the info of Chat created.
	 */
	ChatDto create(ReqCreateChat reqChat);
	
	/**
	 * Method to set or update chat image in the case that is a group chat.
	 * 
	 * @param image - image to add to chat record.
	 * @param chatId - chat's id to update.
	 * @return ChatDto object with the updated Chat's info.
	 * @throws InvalidImageException - if there was some error in image encode to base64.
	 * @throws InvalidActionException - if chat is private.
	 * @throws IllegalArgumentException - if image or chatId is null.
	 * @throws RecordNotFoundException - if chat to update is not found.
	 */
	ChatDto setImage(MultipartFile image,Long chatId);
	
	//faltan testear
	/**
	 * Set chat's name(or update).
	 * @param chatId - chat's id, to know which update.
	 * @param name - new chat's name.
	 * @return ChatDto with Chat info updated.
	 * @throws IllegalArgumentException  if any param is null or blank.
	 * @throws RecordNotFoundException if chat wasn't found.
	 * @throws InvalidException  if authenticated user is not admin in chat.
	 */
	ChatDto setChatName(Long chatId,String name);
	
	/**
	 * Delete chat by id.
	 * @param chatId - chat's id.
	 * @throws RecordNotFoundException if chat wasn't found.
	 */
	void deleteChatById(Long chatId);
	
	/**
	 * Method to get all Users from a chat.
	 * @param chatId - chat's id
	 * @return List of users.
	 * @throws RecordNotFoundException if chat was not found.
	 */
	List<UserDto> getAllUsersByChatId(Long chatId);
	
	
	/**
	 * Function to add users in chat by username, simple or admin users.
	 * @param reqUserListChat - contain chat identification and users to add.
	 * @return ChatDto with Chat info updated.
	 * @throws IllegalArgumentException if some param is null or empty.
	 * @throws InvalidActionException if authenticated user is not admin in chat
	 * @throws RecordNotFoundException if some user wanted to add was not found.
	 * @throws RecordNotFoundException if chat was not found.
	 */
	ChatDto addUsers(ReqAddUserChat ReqAddUserChat);
	
	/**
	 * Function to quit users in chat by username, simple or admin users.
	 * @param reqUsersChat - contain chat identification and users to quit.
	 * @return ChatDto with Chat info updated.
	 * @throws IllegalArgumentException if some param is null or empty.
	 * @throws InvalidActionException if authenticated user is not admin in chat
	 * @throws RecordNotFoundException if some user wanted to add was not found.
	 * @throws RecordNotFoundException if chat was not found.
	 */
	ChatDto quitUsersFromChat(ReqDelUserFromChat reqDelUserFromChat);
	
	/**
	 * Function to set a user as admin in a chat, it can be settled to admin if before was a simple user, or 
	 * can be settled to a simple user if before was an admin.
	 * @param chatId - chat's id.
	 * @param userId - user's id.
	 * @return ChatDto with Chat info updated.
	 * @throws IllegalArgumentException if some parameter is null.
	 * @throws RecordNotFoundException if chatUser was not found.
	 * @throws InvalidActionException if auth user is not admin or if is not a chat's user.
	 */
	ChatDto changeAdminStatus(Long chatId, Long userId);
}
