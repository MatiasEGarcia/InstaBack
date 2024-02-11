package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.ChatTypeEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.InvalidImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;

public interface ChatService {
	
	/**
	 * Get chat by id.
	 * @param chatId - chat's id.
	 * @return Chat found.
	 * @throws RecordNotFoundException if nonw chat was found.
	 */
	Chat getById(Long chatId);
	
	
	/**
	 * Method to get all authenticated user's chats.
	 * @param pageInfoDto - It has pagination info. (must not be null)
	 * @param userId - user's id. (must not be null)
	 * @param username - user's username. (must not be null)
	 * @return Page<ChatDto> - chat list with pagination info.
	 * @throws IllegalArgumentException - if some value in pageInfoDto is null and is needed.
	 * @throws RecordNotFoundException - if none chat was found.
	 */
	Page<Chat> getUserChats(PageInfoDto pageInfoDto, Long userId, String username);
	
	/**
	 * Method to create a chat entity and save it in dbb.
	 * @param name - chat's name (can be null)
	 * @param type - chat's type.
	 * @param usersToAdd - which users add.
	 * @param mapAreAdmin - to know which users add as admin. if key is true, then the user will be added as admin, otherwise as common user.
	 * @throws IllegalArgumentException - if some value in reqChat is null and is needed.
	 * @throws InvalidActionException if chat type is private and the number of users to add is more.
	 * @return Chat created.
	 */
	Chat create(String name, ChatTypeEnum type, List<User> listUsersToAdd, Map<String, Boolean> mapAreAdmin);
	
	/**
	 * Method to set or update chat image in the case that is a group chat.
	 * 
	 * @param image - image to add to chat record.
	 * @param chatId - chat's id to update.
	 * @return Updated Chat record.
	 * @throws InvalidImageException - if there was some error in image encode to base64.
	 * @throws InvalidActionException - if chat is private.
	 * @throws IllegalArgumentException - if image or chatId is null.
	 * @throws RecordNotFoundException - if chat to update is not found.
	 */
	Chat setImage(MultipartFile image,Long chatId);
	
	//faltan testear
	/**
	 * Set chat's name(or update).
	 * @param chatId - chat's id, to know which update.
	 * @param name - new chat's name.
	 * @return ChatDto with Chat info updated.
	 * @throws IllegalArgumentException  if any param is null or blank.
	 * @throws RecordNotFoundException if chat wasn't found.
	 * @throws InvalidActionException  if authenticated user is not admin in chat.
	 */
	Chat setChatName(Long chatId,String name);
	
	/**
	 * Delete chat by id.
	 * @param chatId - chat's id.
	 * @return Deleted chat.
	 * @throws RecordNotFoundException if chat wasn't found.
	 * @throws InvalidActionException  if authenticated user is not admin in chat.
	 */
	Chat deleteChatById(Long chatId);
	
	/**
	 * Method to get all Users from a chat.
	 * @param chatId - chat's id
	 * @return List of users.
	 * @throws RecordNotFoundException if chat was not found.
	 */
	List<User> getAllUsersByChatId(Long chatId);
	
	
	/**
	 * Function to add users in chat by username, simple or admin users.
	 * @param chatId - chat's id.
	 * @param listUsersToAdd - users to add on chat.
	 * @param mapAreAdmin - to know which users add as admin, key should be username, value = true if is admin, false if not.
	 * @return ChatDto with Chat info updated.
	 * @throws IllegalArgumentException if some param is null or empty.
	 * @throws InvalidActionException if authenticated user is not admin in chat
	 * @throws RecordNotFoundException if some user wanted to add was not found.
	 * @throws RecordNotFoundException if chat was not found.
	 */
	Chat addUsers(Long chatId, List<User> listUsersToAdd,Map<String, Boolean> mapAreAdmin);
	
	/**
	 * Function to quit users in chat by username, simple or admin users.
	 * @param chatId - chat's id.
	 * @param listUserUsername - users' usernames.
	 * @return  Chat with info updated.
	 * @throws IllegalArgumentException if some param is null or empty.
	 * @throws InvalidActionException if authenticated user is not admin in chat
	 * @throws RecordNotFoundException if chat was not found.
	 */
	Chat quitUsersFromChatByUsername(Long chatId, List<String> listUserUsername);
	
	/**
	 * Function to set a user as admin in a chat, it can be settled to admin if before was a simple user, or 
	 * can be settled to a simple user if before was an admin.
	 * @param chatId - chat's id.
	 * @param userId - user's id.
	 * @return Chat with info updated.
	 * @throws IllegalArgumentException if some parameter is null.
	 * @throws InvalidActionException if auth user is not admin or if is not a chat's user.
	 */
	Chat changeAdminStatus(Long chatId, Long userId);
}
