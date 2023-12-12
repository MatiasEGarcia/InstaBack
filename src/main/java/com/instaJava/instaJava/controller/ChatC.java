package com.instaJava.instaJava.controller;

import java.util.List;

import org.springframework.data.domain.Sort.Direction;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqAddUserChat;
import com.instaJava.instaJava.dto.request.ReqCreateChat;
import com.instaJava.instaJava.dto.request.ReqDelUserFromChat;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.service.ChatService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.Image;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Validated
public class ChatC {

	private final ChatService chatService;
	private final MessagesUtils messUtils;
	
	/**
	 * Get method to get Auth user's chats.
	 * 
	 * @param pageNo. For pagination, number of the page. (optional)
	 * @param pageSize. For pagination, size of the elements in the same page. (optional)
	 * @param sortField. For pagination, sorted by.. (optional)
	 * @param sortDir. In what direction is sorted, asc or desc. (optional)
	 * @return paginated chats that were found,else a message that there wasn't any that meet the conditions.
	 */
	@GetMapping(produces = "application/json")
	public ResponseEntity<ResPaginationG<ChatDto>> getChatsByAuthUser(
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "chatId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir){
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		return ResponseEntity.ok(chatService.getAuthUserChats(pageInfoDto));
	}
	
	/**
	 * To create a chat without image.
	 * @param reqChat - contains chat info to create chat entity.
	 * @return new chat entity created.
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> create(@Valid @RequestBody ReqCreateChat reqChat){
		return ResponseEntity.ok(chatService.create(reqChat));
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
		return ResponseEntity.ok(chatService.setImage(file, chatId));
	}
	
	/**
	 * Update or set group chat name.
	 * @param name - new name
	 * @param chatId - chat's id to update.
	 * @return chat info updated.
	 */
	@PutMapping(value = "/name/{name}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> setName(@PathVariable("name") String name , @PathVariable("id") Long chatId){
		return ResponseEntity.ok(chatService.setChatName(chatId, name));
	}
	
	/**
	 * TO delete a chat 
	 * @param chatId - chat's id to delete.
	 * @return A message to know that was deleted.
	 */
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<ResMessage> delete(@PathVariable("id") Long chatId){
		chatService.deleteChatById(chatId);
		return ResponseEntity.ok(ResMessage.builder().message(messUtils.getMessage("generic.delete-ok")).build());
	}
	
	/**
	 * Get all the users from a chat.
	 * @param chatId - chat's id from which get the users.
	 * @return List of users who are in chat.
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<List<UserDto>> getUsersByChatId(@PathVariable("id") Long chatId){
		return ResponseEntity.ok(chatService.getAllUsersByChatId(chatId));
	}
	
	/**
	 * To add more users in chat.
	 * @param reqAddUserChat - object with info of chat and users to add.
	 * @return the chat with the users updated.
	 */
	@PostMapping(value="/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> addUsers(@Valid @RequestBody ReqAddUserChat reqAddUserChat){
		return ResponseEntity.ok(chatService.addUsers(reqAddUserChat));
	}
	
	/**
	 *To quit users from chat. 
	 * @param reqDelUserFromChat - object with info of chat and users to quit.
	 * @return chat with the users updated.
	 */
	@DeleteMapping(value="/quit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> quitUsers(@Valid @RequestBody ReqDelUserFromChat reqDelUserFromChat){
		return ResponseEntity.ok(chatService.quitUsersFromChat(reqDelUserFromChat));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
