package com.instaJava.instaJava.service;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqChat;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.exception.UserNotApplicableForChatException;

public interface ChatService {

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
	 * @return ChatDto object with the info of Chat created.
	 */
	ChatDto create(ReqChat reqChat);
	
	/**
	 * Method to set or update chat image in the case that is a group chat.
	 * 
	 * @param image - image to add to chat record.
	 * @param chatId - chat's id to update.
	 * @return ChatDto object with the updated Chat's info.
	 * @throws ImageException - if there was some error in image encode to base64.
	 * @throws IllegalArgumentException - if image or chatId is null.
	 * @throws RecordNotFoundException - if chat to update is not found.
	 */
	ChatDto setImage(MultipartFile image,Long chatId);
}
