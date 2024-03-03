package com.instaback.service;

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

import com.instaback.dao.MessageDao;
import com.instaback.dto.PageInfoDto;
import com.instaback.entity.Chat;
import com.instaback.entity.Message;
import com.instaback.entity.User;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.util.MessagesUtils;
import com.instaback.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{

	private final Clock clock;
	private final MessageDao msgDao;
	private final MessagesUtils messUtils;
	private final PageableUtils pagUtils;
	
	@Override
	@Transactional
	public Message create(String message, Chat chat) {
		if(message == null || chat == null || chat.getId() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}else if(message.isBlank()) {
			throw new InvalidActionException(messUtils.getMessage("message.body-not-blank"),HttpStatus.BAD_REQUEST);
		}
		Message newMessage;
		StringBuilder watchedByMessCreator;
		User user;
		user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		isAuthUserAUserFromChat(chat);
		watchedByMessCreator = new StringBuilder(user.getUsername());
		watchedByMessCreator.append(","); //always a come at the end. to separate names.
		newMessage = Message.builder()
				.body(message)
				.chat(chat)
				.userOwner(user.getUsername())
				.sendedAt(ZonedDateTime.now(clock))
				.watchedBy(watchedByMessCreator.toString())
				.build();
		
		newMessage = msgDao.save(newMessage);
		return newMessage;
	}

	
	@Override
	@Transactional(readOnly = true)
	public Page<Message> getMessagesByChat(Chat chat, PageInfoDto pageInfoDto) {
		if(chat == null || chat.getId() == null || pageInfoDto == null || pageInfoDto.getSortField() == null || pageInfoDto.getSortDir() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Page<Message> page;
		isAuthUserAUserFromChat(chat);
		page = msgDao.findByChatId(chat.getId(), pagUtils.getPageable(pageInfoDto));
		if(!page.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("message.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return page;
	}
	
	@Override
	@Transactional
	public Long messagesWatched(Set<String> messageWatchedIds) {
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
		//Add auth user as watcher.
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
		
		messagesNotWatched = this.getMessagesNotWatchedCountByChatIds(List.of(chatOrigin.getId()), authUser.getUsername());
		//check if there some message not watched in origin chat.
		if(!messagesNotWatched.isEmpty()) {
			messagesNotWatchedNumber = messagesNotWatched.get(0)[1];//there should be 1 chat searched
		}
		return messagesNotWatchedNumber;//there should be 1 chat searched
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
	public void setAllMessagesNotWatchedAsWatchedByChatId(Chat chat) {
		if(chat == null || chat.getId() == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Message> listMessages = msgDao.findAllByChatAndUserNoWatched(chat, authUser.getUsername());
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
				if(message.getId() == messageId) {
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




















