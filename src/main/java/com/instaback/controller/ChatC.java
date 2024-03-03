package com.instaback.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaback.application.ChatApplication;
import com.instaback.dto.ChatDto;
import com.instaback.dto.UserDto;
import com.instaback.dto.request.ReqAddUserChat;
import com.instaback.dto.request.ReqCreateChat;
import com.instaback.dto.request.ReqDelUserFromChat;
import com.instaback.dto.response.ResMessage;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.util.MessagesUtils;
import com.instaback.validator.Image;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Validated
public class ChatC {

	private final MessagesUtils messUtils;
	private final ChatApplication chApplication;
	
	/**
	 * Get method to get Auth user's chats.
	 * 
	 * @param pageNo. For pagination, number of the page. (optional)
	 * @param pageSize. For pagination, size of the elements in the same page. (optional)
	 * @return paginated chats that were found,else a message that there wasn't any that meet the conditions.
	 */
	@GetMapping( value = "/{pageNo}/{pageSize}" , produces = "application/json")
	public ResponseEntity<ResPaginationG<ChatDto>> getChatsByAuthUser(@PathVariable("pageNo") int pageNo,
			@PathVariable("pageSize") int pageSize){
		return ResponseEntity.ok(chApplication.getAuhtUserChats(pageNo, pageSize));
	}
	
	/**
	 * To create a chat without image.
	 * @param reqChat - contains chat info to create chat entity.
	 * @return new chat entity created.
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> create(@Valid @RequestBody ReqCreateChat reqChat){
		return ResponseEntity.ok(chApplication.create(reqChat));
	}
	
	/**
	 * To set already exist chat's image.
	 * @param file - new image file
	 * @param chatId - chat's id to update.
	 * @return chat updated.
	 */
	@PostMapping(value = "/image/{chatId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> setImage(@RequestPart("img") @NotNull @Image MultipartFile file,
			@PathVariable("chatId") Long chatId){
		return ResponseEntity.ok(chApplication.setImage(file, chatId));
	}
	
	/**
	 * Update or set group chat name.
	 * @param name - new name
	 * @param chatId - chat's id to update.
	 * @return chat info updated.
	 */
	@PutMapping(value = "/name/{name}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> setName(@PathVariable("name") String name , @PathVariable("id") Long chatId){
		return ResponseEntity.ok(chApplication.setChatName(chatId, name));
	}
	
	/**
	 * TO delete a chat 
	 * @param chatId - chat's id to delete.
	 * @return A message to know that was deleted.
	 */
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<ResMessage> delete(@PathVariable("id") Long chatId){
		chApplication.deleteChatById(chatId);
		return ResponseEntity.ok(ResMessage.builder().message(messUtils.getMessage("generic.delete-ok")).build());
	}
	
	/**
	 * Get all the users from a chat.
	 * @param chatId - chat's id from which get the users.
	 * @return List of users who are in chat.
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<List<UserDto>> getUsersByChatId(@PathVariable("id") Long chatId){
		return ResponseEntity.ok(chApplication.getAllUsersByChatId(chatId));
	}
	
	/**
	 * To add more users in chat.
	 * @param reqAddUserChat - object with info of chat and users to add.
	 * @return the chat with the users updated.
	 */
	@PostMapping(value="/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> aaddUsers(@Valid @RequestBody ReqAddUserChat reqAddUserChat){
		return ResponseEntity.ok(chApplication.addUsers(reqAddUserChat)); 
	}
	
	/**
	 *To quit users from chat. 
	 * @param reqDelUserFromChat - object with info of chat and users to quit.
	 * @return chat with the users updated.
	 */
	@DeleteMapping(value="/quit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> quitUsers(@Valid @RequestBody ReqDelUserFromChat reqDelUserFromChat){
		return ResponseEntity.ok(chApplication.quitUsersFromChat(reqDelUserFromChat));
	}
	
	/**
	 * To chage user's admin status on the chat.
	 * @param chatId - chat's id where the user is.
	 * @param userId - user's id.
	 * @return chat with the user's admin status updated.
	 */
	@PutMapping(value = "/adminStatus/{chatId}/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> changeAdminStatus(@PathVariable("chatId") Long chatId, @PathVariable("userId") Long userId){
		return ResponseEntity.ok(chApplication.changeAdminStatus(chatId, userId));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
