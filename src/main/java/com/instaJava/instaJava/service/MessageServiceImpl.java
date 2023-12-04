package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.MessageDao;
import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.Message;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.MessageMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{

	private final Clock clock;
	private final MessageDao msgDao;
	private final MessageMapper msgMapper;
	private final ChatService chatService;
	private final MessagesUtils messUtils;
	private final PageableUtils pagUtils;
	
	@Override
	@Transactional
	public MessageDto create(String message, Long chatId) {
		if(message == null || chatId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}else if(message.isBlank()) {
			throw new InvalidActionException(messUtils.getMessage("message.body-not-blank"),HttpStatus.BAD_REQUEST);
		}
		Message newMessage;
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		isAuthUserAUserFromChat(chatId);
		
		newMessage = Message.builder()
				.body(message)
				.chat(new Chat(chatId))
				.userOnwer(user.getUsername())
				.sendedAt(ZonedDateTime.now(clock))
				.build();
		
		newMessage = msgDao.save(newMessage);
		return msgMapper.messageToMessageDto(newMessage);
	}

	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<MessageDto> getMessagesByChat(Long chatId, PageInfoDto pageInfoDto) {
		if(chatId == null || pageInfoDto == null || pageInfoDto.getSortField() == null || pageInfoDto.getSortDir() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		isAuthUserAUserFromChat(chatId);
		Page<Message> page = msgDao.findByChatChatId(chatId, pagUtils.getPageable(pageInfoDto));
		if(!page.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("message.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return msgMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}

	/**
	 * Method to check if the authenticated user is a user from chat.
	 * @param chatId - chat's id.
	 * @throws InvalidActionException if authenticated user is not a user in the chat.
	 */
	private void isAuthUserAUserFromChat(Long chatId) {
		if(chatId == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ChatDto chatDto= chatService.getById(chatId);
		
		if(!chatDto.getUsers().stream().anyMatch((userDto) -> userDto.getUsername().equalsIgnoreCase(user.getUsername()))) {
			throw new InvalidActionException(messUtils.getMessage("message.only-users"), HttpStatus.BAD_REQUEST);
		}
	}

	
	
	
	
	
	
	
	
	
	
}
