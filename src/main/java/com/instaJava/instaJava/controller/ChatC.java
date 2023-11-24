package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqChat;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.mapper.ChatMapper;
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
	private final ChatMapper chatMapper;
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
		Page<Chat> pageChats = chatService.getAuthUserChats(pageInfoDto);
		if (pageChats.getContent().isEmpty()) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.there-no-chats")).build();
		}
		return ResponseEntity.ok(chatMapper.pageAndPageInfoDtoToResPaginationG(pageChats, pageInfoDto));
	}
	
	/**
	 * To create a chat without image.
	 * @param reqChat - contains chat info to create chat entity.
	 * @return new chat entity created.
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatDto> create(@Valid @RequestBody ReqChat reqChat){
		Chat chat = chatService.create(reqChat);
		return ResponseEntity.ok(chatMapper.chatToChatDto(chat));
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
		Chat chat = chatService.setImage(file, chatId);
		return ResponseEntity.ok(chatMapper.chatToChatDto(chat));
	}
	
}
