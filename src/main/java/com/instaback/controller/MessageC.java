package com.instaback.controller;

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

import com.instaback.application.MessageApplication;
import com.instaback.dto.MessageDto;
import com.instaback.dto.request.ReqNewMessage;
import com.instaback.dto.response.ResMessage;
import com.instaback.dto.response.ResNumberOfMessagesNoWatched;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.util.MessagesUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Validated
public class MessageC {

	private final MessagesUtils messUtils;
	private final MessageApplication mApplication;
	
	/**
	 * To create a message.
	 * @param reqNewMessage
	 * @return the messsage created.
	 */
	@PostMapping(consumes= MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MessageDto> create(@Valid @RequestBody ReqNewMessage reqNewMessage){
		MessageDto messageDto= mApplication.create(reqNewMessage);
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
	@GetMapping(value="/getMessagesByChatId/{id}" , produces= MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<MessageDto>> getMessagesByChatId(@PathVariable("id") Long chatId,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir){
		ResPaginationG<MessageDto> res = mApplication.getMessagesByChat(chatId, Integer.parseInt(pageNo), Integer.parseInt(pageSize), sortField,
				sortDir);
		return ResponseEntity.ok().body(res);
	}
	

	/**
	 * Function to set which messages were watche by auth user and messages id.
	 * @param messagesWatchedId - watched messages' id.
	 * @return number of messages not watched yet.
	 */
	@PutMapping(value="/messagesWatched" ,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResNumberOfMessagesNoWatched> messagesWatched(@RequestBody Set<String> messagesWatchedId){
		return ResponseEntity.ok().body(new ResNumberOfMessagesNoWatched(mApplication.messagesWatched(messagesWatchedId).toString()));
	}
	
	/**
	 *Function to set as watched by authUser all messages not watched in a specific chat.
	 * @param chatId the ID of the chat
	 * @return a ResponseEntity containing a response message
	 */
	@PutMapping(value ="/watchedAllByChatId/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResMessage> watchedAllByChatId(@PathVariable("id") Long chatId){
		mApplication.setAllMessagesNotWatchedAsWatchedByChatId(chatId);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("message.watched-all-in-chat")));
	}
}











