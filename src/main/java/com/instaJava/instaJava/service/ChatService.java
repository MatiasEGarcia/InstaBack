package com.instaJava.instaJava.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqChat;
import com.instaJava.instaJava.entity.Chat;

public interface ChatService {

	Page<Chat> getAuthUserChats(PageInfoDto pageInfoDto);
	
	Chat create(ReqChat reqChat);
	
	Chat setImage(MultipartFile image,Long chatId);
}
