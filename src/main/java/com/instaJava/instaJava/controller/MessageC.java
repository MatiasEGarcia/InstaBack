package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqNewMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Validated
public class MessageC {

	private final MessageService msgService;
	
	@PostMapping(consumes= MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MessageDto> create(@Valid @RequestBody ReqNewMessage reqNewMessage){
		MessageDto messageDto= msgService.create(reqNewMessage.getMessage(), Long.parseLong(reqNewMessage.getChatId()));
		return ResponseEntity.ok().body(messageDto);
	}
	
	@GetMapping(value="/{chatId}" , produces= MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<MessageDto>> getMessagesByChat(@PathVariable("chatId") Long chatId,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "messageId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir){
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		return ResponseEntity.ok().body(msgService.getMessagesByChat(chatId, pageInfoDto));
	}
}
