package com.instaback.application;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

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
