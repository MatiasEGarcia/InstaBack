package com.instaback.application;

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

import com.instaback.dto.MessageDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.dto.request.ReqNewMessage;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Chat;
import com.instaback.entity.Message;
import com.instaback.mapper.MessageMapper;
import com.instaback.service.ChatService;
import com.instaback.service.MessageService;
import com.instaback.service.NotificationService;
import com.instaback.util.MessagesUtils;

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

































