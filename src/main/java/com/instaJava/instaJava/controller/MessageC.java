package com.instaJava.instaJava.controller;

import java.util.Set;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqNewMessage;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.service.MessageService;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Validated
public class MessageC {

	private final MessageService msgService;
	private final MessagesUtils messUtils;
	
	/**
	 * To create a message.
	 * @param reqNewMessage
	 * @return the messsage created.
	 */
	@PostMapping(consumes= MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MessageDto> create(@Valid @RequestBody ReqNewMessage reqNewMessage){
		MessageDto messageDto= msgService.create(reqNewMessage.getMessage(), Long.parseLong(reqNewMessage.getChatId()));
		return ResponseEntity.ok().body(messageDto);
	}
	
	/**
	 * To get messages in chat.
	 * @param chatId - chat's id.
	 * @param pageNo. For pagination, number of the page.
	 * @param pageSize. For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir. In what direction is sorted, asc or desc.
	 * @return messages with pagination info.
	 */
	@GetMapping(value="/{chatId}" , produces= MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<MessageDto>> getMessagesByChat(@PathVariable("chatId") Long chatId,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "messageId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir){
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		ResPaginationG<MessageDto> res = msgService.getMessagesByChat(chatId, pageInfoDto);
		return ResponseEntity.ok().body(res);
	}
	

	/**
	 * Function to set which messages were watche by auth user and messages id.
	 * @param messagesWatchedId - watched messages' id.
	 * @return chat with info updated.
	 */
	@PutMapping(value="/messagesWatched" ,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> messagesWatched(@RequestBody Set<String> messagesWatchedId){
		return ResponseEntity.ok().body(msgService.messagesWatched(messagesWatchedId));
	}
	
	/**
	 *Function to set as watched by authUser all messages not watched in a specific chat.
	 * @param chatId the ID of the chat
	 * @return a ResponseEntity containing a response message
	 */
	@PutMapping(value ="/watchedAll/{chatId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResMessage> watchedAll(@PathVariable Long chatId){
		msgService.setAllMessagesNotWatchedAsWatchedByChatId(chatId);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("message.watched-all-in-chat")));
	}
}











