package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.ChatDao;
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
import com.instaJava.instaJava.mapper.ChatMapper;
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
	private final ChatMapper chatMapper;
	private final ChatDao chatDao;
	private final NotificationService notifService;
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
		Chat chat;
		Message newMessage;
		MessageDto newMessageDto;
		StringBuilder watchedByMessCreator;
		User user;
		chat= chatDao.findById(chatId).orElseThrow(() ->
			new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		isAuthUserAUserFromChat(chat);
		watchedByMessCreator = new StringBuilder(user.getUsername());
		watchedByMessCreator.append(","); //always a come at the end. to separate names.
		newMessage = Message.builder()
				.body(message)
				.chat(new Chat(chatId))
				.userOwner(user.getUsername())
				.sendedAt(ZonedDateTime.now(clock))
				.watchedBy(watchedByMessCreator.toString())
				.build();
		
		newMessage = msgDao.save(newMessage);
		newMessageDto = msgMapper.messageToMessageDto(newMessage);
		notifService.saveNotificationOfMessage(chat, newMessageDto);
		return newMessageDto;
	}

	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<MessageDto> getMessagesByChat(Long chatId, PageInfoDto pageInfoDto) {
		if(chatId == null || pageInfoDto == null || pageInfoDto.getSortField() == null || pageInfoDto.getSortDir() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Page<Message> page;
		Chat chat= chatDao.findById(chatId).orElseThrow(() ->
			new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		isAuthUserAUserFromChat(chat);
		page = msgDao.findByChatChatId(chatId, pagUtils.getPageable(pageInfoDto));
		if(!page.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("message.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return msgMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}
	
	@Override
	@Transactional
	public ChatDto messagesWatched(Set<String> messageWatchedIds) {
		if(messageWatchedIds == null || messageWatchedIds.isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Chat chatOrigin;
		Long messagesNotWatchedNumber = 0L;
		User authUser;
		List<Long[]> messagesNotWatched;
		List<Message> listMessages;
		List<Long> listLongMessageWatchedIds = new ArrayList<>();
		//cast
		for(String id : messageWatchedIds) {
			listLongMessageWatchedIds.add(Long.parseLong(id));
		}
		
		listMessages = msgDao.findAllById(listLongMessageWatchedIds);
		wereAllFoundOrNot(listMessages, listLongMessageWatchedIds);
		
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		for(Message message : listMessages) {
			String str;
			String[] usersUsername;
			boolean alreadyWatchedFlag = false;
			StringBuilder whoWatchedBefore = new StringBuilder(message.getWatchedBy());
			
			//checking auth user is not already in watchedBy list.
			str = whoWatchedBefore.toString();
			usersUsername = str.split(",");
			for(String userUsername : usersUsername) {
				if(userUsername.equalsIgnoreCase(authUser.getUsername())) {
					alreadyWatchedFlag = true;
					break;
				}
			}
			if(alreadyWatchedFlag) break;//if is already watched by auth user break for, and go to next message.
			
			whoWatchedBefore.append(authUser.getUsername());
			whoWatchedBefore.append(",");
			message.setWatchedBy(whoWatchedBefore.toString());
		}
		
		listMessages = msgDao.saveAll(listMessages);
		//all messages should be from the same chat.
		chatOrigin = listMessages.get(0).getChat();
		
		messagesNotWatched = this.getMessagesNotWatchedCountByChatIds(List.of(chatOrigin.getChatId()), authUser.getUsername());
		//check if there some message not watched in origin chat.
		if(!messagesNotWatched.isEmpty()) {
			messagesNotWatchedNumber = messagesNotWatched.get(0)[1];//there should be 1 chat searched
		}
		return chatMapper.chatAndMessagesNoWatchedToChatDto(chatOrigin, messagesNotWatchedNumber);//there should be 1 chat searched
	}	
	
	@Override
	@Transactional(readOnly = true)
	public List<Long[]> getMessagesNotWatchedCountByChatIds(List<Long> chatsId, String username) {
		if(chatsId == null || chatsId.isEmpty() || username == null || username.isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		}
		return msgDao.countByUserNoWatchedAndChatId(chatsId, username);
	}

	@Override
	@Transactional
	public void setAllMessagesNotWatchedAsWatchedByChatId(Long chatId) {
		if(chatId == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		List<Message> listMessages = msgDao.findAllByChatIdAndUserNoWatched(chatId, authUser.getUsername());
		
		if(listMessages.isEmpty()) return;
		
		for(Message message : listMessages) {
			StringBuilder stringBuilder = new StringBuilder(message.getWatchedBy());
			stringBuilder.append(authUser.getUsername());
			stringBuilder.append(",");
			message.setWatchedBy(stringBuilder.toString());
		}
		msgDao.saveAll(listMessages);
	}
	
	
	/**
	 * Method to check if the authenticated user is a user from chat.
	 * @param chatId - chat's id.
	 * @throws InvalidActionException if authenticated user is not a user in the chat.
	 */
	private void isAuthUserAUserFromChat(Chat chat) {
		if(chat == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean flag = false;
		for(User user : chat.getUsers()) {
			if(user.equals(authUser)) {
				flag = true;
				break;
			}
		}
		if(!flag) {
			throw new InvalidActionException(messUtils.getMessage("message.only-users"), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * To check if all mesages were found.
	 * @param messagesFound - list of messages found.
	 * @param allMessagesId - id list of messages which should be found.
	 * @throws RecordNotFoundException if some message was not found.
	 */
	private void wereAllFoundOrNot(List<Message> messagesFound, List<Long> allMessagesId) {
		List<String> messagesNotFound = null;
		
		for(Long messageId : allMessagesId) {
			boolean flagWasFound = false;
			for(Message message : messagesFound) {
				if(message.getMessageId() == messageId) {
					flagWasFound = true;
					break;
				}
			}
			if(!flagWasFound) {
				if(messagesNotFound == null) {
					messagesNotFound = new ArrayList<>();	
				}
				messagesNotFound.add(messageId.toString());
			}
		}
		
		if(messagesNotFound != null) {
			throw new RecordNotFoundException(messUtils.getMessage("message.not-found"), messagesNotFound, HttpStatus.NOT_FOUND);
		}
		
	}

	
}




















