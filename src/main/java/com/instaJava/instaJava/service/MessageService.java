package com.instaJava.instaJava.service;

import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;

public interface MessageService {

	MessageDto create(String message, Long chatId);
	
	ResPaginationG<MessageDto> getMessagesByChat(Long chatId, PageInfoDto pageInfoDto);
	
}
