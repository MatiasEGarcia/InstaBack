package com.instaJava.instaJava.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.dao.IdValueDto;
import com.instaJava.instaJava.dto.request.ReqNewMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.Message;
import com.instaJava.instaJava.exception.CryptoException;
import com.instaJava.instaJava.mapper.MessageMapper;
import com.instaJava.instaJava.service.ChatService;
import com.instaJava.instaJava.service.MessageService;
import com.instaJava.instaJava.service.NotificationService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.RSAUtils;
import com.instaJava.instaJava.util.SearchsUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageApplicationImpl implements MessageApplication{

	private final MessageService mService;
	private final NotificationService notiService;
	private final ChatService chService;
	private final MessageMapper mMapper;
	private final MessagesUtils messUtils;
	private final SearchsUtils searchUtils;
	private final RSAUtils rsaUtils;
	
	//check tests
	@Override
	public MessageDto create(ReqNewMessage reqNewMessage) {
		if(reqNewMessage == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		Chat chat;
		MessageDto newMessageDto;
		Message messageCreated;
		String encryptMessage;
		String decryptMessage;
		chat = chService.getById(Long.parseLong(reqNewMessage.getChatId()));
		//encrypt message
		try {
			encryptMessage = rsaUtils.encrypt(reqNewMessage.getMessage());
		} catch (Exception e) {
			throw new CryptoException(messUtils.getMessage("message.encrypt-fail") , HttpStatus.BAD_REQUEST, e);
		} 
		messageCreated = mService.create(encryptMessage, chat);
		//decrypt the message created.
		try {
			decryptMessage = rsaUtils.decrypt(messageCreated.getBody());
		}catch(Exception e) {
			throw new CryptoException(messUtils.getMessage("message.decrypt-fail") , HttpStatus.BAD_REQUEST, e);
		}
		newMessageDto = mMapper.messageEAndMessageDecryptToMessageDto(messageCreated, decryptMessage);
		notiService.saveNotificationOfMessage(chat, newMessageDto);
		return newMessageDto;
	}

	//check tests
	@Override
	public ResPaginationG<MessageDto> getMessagesByChat(Long chatId, int pageNo, int pageSize, String sortField,
			Direction sortDir) {
		Page<Message> pageMessages;
		Chat chat;
		List<IdValueDto<String>> idValuesDtoList;
		PageInfoDto pageInfoDto = new PageInfoDto(pageNo, pageSize,0,0, sortField,sortDir);
		chat = chService.getById(chatId);
		pageMessages = mService.getMessagesByChat(chat, pageInfoDto);
		
		//decript messages.
		idValuesDtoList = new ArrayList<>();
		for(Message message : pageMessages.getContent()) {
			IdValueDto<String> idValueDto = new IdValueDto<>(message.getId(),message.getBody());
			idValuesDtoList.add(idValueDto);
		}
		try {
			rsaUtils.decryptAll(idValuesDtoList);
		} catch (Exception e) {
			throw new CryptoException(messUtils.getMessage("message.decrypt-fail") , HttpStatus.BAD_REQUEST, e);
		}
		
		//Assigning the correct decrypted message to each message entity. 
		for(IdValueDto<String> idValueDto : idValuesDtoList) {
			//I use linear and not binary because maybe the sort field is not the id.
			int itemIndex = searchUtils.linealSearch(pageMessages.getContent(), idValueDto.getId());
			pageMessages.getContent().get(itemIndex).setBody(idValueDto.getValue());
		}
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
