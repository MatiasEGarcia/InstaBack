package com.instaJava.instaJava.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqNewMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.Message;
import com.instaJava.instaJava.mapper.MessageMapper;
import com.instaJava.instaJava.service.ChatService;
import com.instaJava.instaJava.service.MessageService;
import com.instaJava.instaJava.service.NotificationService;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class MessageApplicationImplTest {

	@Mock MessageService mService;
	@Mock NotificationService nService;
	@Mock ChatService chService;
	@Mock MessageMapper mMapper;
	@Mock MessagesUtils messUtils;
	@InjectMocks MessageApplicationImpl messageApplication;
	
	//create
	@Test
	void createParamReqNewMessageNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> messageApplication.create(null));
	}
	
	@Test
	void createReturnNotNull() {
		ReqNewMessage reqNewMessage = new ReqNewMessage();
		reqNewMessage.setChatId("1");
		reqNewMessage.setMessage("random");
		Chat chatFound = new Chat();
		Message messageCreated = new Message();
		MessageDto messageDto = new MessageDto();
		
		when(chService.getById(Long.parseLong(reqNewMessage.getChatId()))).thenReturn(chatFound);
		when(mService.create(reqNewMessage.getMessage(), chatFound)).thenReturn(messageCreated);
		when(mMapper.messageToMessageDto(messageCreated)).thenReturn(messageDto);
		
		assertNotNull(messageApplication.create(reqNewMessage));
		
		verify(nService).saveNotificationOfMessage(chatFound, messageDto);
	}
	
	//getMessagesByChat
	
	@Test
	void getMessagesByChatReturnNotNull() {
		Long chatId = 1L;
		Chat chatFound = new Chat();
		Page<Message> messagePage = Page.empty();
		ResPaginationG<MessageDto> res = new ResPaginationG<MessageDto>();
		
		when(chService.getById(chatId)).thenReturn(chatFound);
		when(mService.getMessagesByChat(eq(chatFound), any(PageInfoDto.class))).thenReturn(messagePage);
		when(mMapper.pageAndPageInfoDtoToResPaginationG(eq(messagePage), any(PageInfoDto.class))).thenReturn(res);
		assertNotNull(messageApplication.getMessagesByChat(chatId, 0, 0, null, null));
	}
	
	//messagesWatched
	
	@Test
	void messagesWatchedReturnNotNull() {
		Set<String> set = Collections.emptySet();
		when(mService.messagesWatched(set)).thenReturn(1L);
		assertNotNull(messageApplication.messagesWatched(set));
	}
	
	//setAllMessagesNotWatchedAsWatchedByChatId
	
	@Test
	void setAllMessagesNotWatchedAsWatchedByChatId() {
		Chat chatFound = new Chat(1L);
		when(chService.getById(chatFound.getId())).thenReturn(chatFound);
		
		messageApplication.setAllMessagesNotWatchedAsWatchedByChatId(chatFound.getId());
		
		verify(mService).setAllMessagesNotWatchedAsWatchedByChatId(chatFound);
		
	}
	
	
}

































