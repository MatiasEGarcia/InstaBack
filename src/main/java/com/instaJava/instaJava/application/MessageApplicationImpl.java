package com.instaJava.instaJava.application;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageApplicationImpl implements MessageApplication{

	private final MessageService mService;
	private final NotificationService notiService;
	private final ChatService chService;
	private final MessageMapper mMapper;
	private final MessagesUtils messUtils;
	
	@Override
	public MessageDto create(ReqNewMessage reqNewMessage) {
		if(reqNewMessage == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		Chat chat;
		MessageDto newMessageDto;
		Message messageCreated;
		chat = chService.getById(Long.parseLong(reqNewMessage.getChatId()));
		messageCreated = mService.create(reqNewMessage.getMessage(), chat);
		newMessageDto = mMapper.messageToMessageDto(messageCreated);
		notiService.saveNotificationOfMessage(chat, newMessageDto);
		return newMessageDto;
	}

	@Override
	public ResPaginationG<MessageDto> getMessagesByChat(Long chatId, int pageNo, int pageSize, String sortField,
			Direction sortDir) {
		Page<Message> pageMessages;
		Chat chat;
		PageInfoDto pageInfoDto = new PageInfoDto(pageNo, pageSize,0,0, sortField,sortDir);
		chat = chService.getById(chatId);
		pageMessages = mService.getMessagesByChat(chat, pageInfoDto);
		return mMapper.pageAndPageInfoDtoToResPaginationG(pageMessages, pageInfoDto);
	}

	@Override
	public Long messagesWatched(Set<String> messageWatchedIds) {
		return mService.messagesWatched(messageWatchedIds);
	}

	@Override
	public void setAllMessagesNotWatchedAsWatchedByChatId(Long chatId) {
		Chat chat = chService.getById(chatId);
		mService.setAllMessagesNotWatchedAsWatchedByChatId(chat);
	}
}
