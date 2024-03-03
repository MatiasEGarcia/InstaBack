package com.instaback.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaback.dao.ChatDao;
import com.instaback.dao.ChatUserDao;
import com.instaback.dto.PageInfoDto;
import com.instaback.entity.Chat;
import com.instaback.entity.ChatUser;
import com.instaback.entity.User;
import com.instaback.enums.ChatTypeEnum;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.InvalidImageException;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.mapper.ChatMapper;
import com.instaback.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

	private final ChatUserDao chatUserDao;
	private final ChatDao chatDao;
	private final MessagesUtils messUtils;
	private final ChatMapper chatMapper;

	@Override
	@Transactional(readOnly = true)
	public Chat getById(Long chatId) {
		if(chatId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return chatDao.findById(chatId).orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"),
				HttpStatus.NOT_FOUND));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<Chat> getUserChats(PageInfoDto pageInfoDto,Long userId, String username) {
		if (pageInfoDto == null || userId == null || username == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Pageable page = PageRequest.of(pageInfoDto.getPageNo(), pageInfoDto.getPageSize());
		Page<Chat> pageChat = chatDao.findChatsByUser(userId, username, page);
		if (pageChat.getContent().isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("chat.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return pageChat;
	}

	@Override
	@Transactional
	public Chat create(String name, ChatTypeEnum type, List<User> listUsersToAdd, Map<String, Boolean> mapAreAdmin) {
		if (listUsersToAdd == null || listUsersToAdd.isEmpty() || type == null || mapAreAdmin == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		User authUser;
		List<ChatUser> listChatUsersToSave;
		Chat chat = Chat.builder().name(name).type(type).build();
		// creating chatUsers (users in chat)
		// creating chatUser objects.
		listChatUsersToSave = new ArrayList<>();
		for (User user : listUsersToAdd) {
			boolean isAdmin = mapAreAdmin.get(user.getUsername());
			ChatUser chatUser = new ChatUser();
			chatUser.setChat(chat);
			chatUser.setUser(user);
			chatUser.setAdmin(isAdmin);
			listChatUsersToSave.add(chatUser);
		}
		
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		// auth user is admin always in creation.
		ChatUser chatUser = new ChatUser();
		chatUser.setChat(chat);
		chatUser.setUser(authUser);
		chatUser.setAdmin(true);
		listChatUsersToSave.add(chatUser);

		if (chat.getType().equals(ChatTypeEnum.PRIVATE) && listChatUsersToSave.size() > 2) {
			throw new InvalidActionException(messUtils.getMessage("chat.private-only-2-users"), HttpStatus.BAD_REQUEST);
		}

		// set chat's users.
		chat.setChatUsers(listChatUsersToSave);

		return chatDao.save(chat);

	}

	@Override
	@Transactional
	public Chat setImage(MultipartFile image, Long chatId) {
		if (image == null || chatId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Chat chatToEdit = chatDao.findById(chatId).orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"),
				HttpStatus.NOT_FOUND));
		// only group chat can edit image attribute.
		if (chatToEdit.getType().equals(ChatTypeEnum.PRIVATE)) {
			throw new InvalidActionException(messUtils.getMessage("chat.private-no-image"), HttpStatus.BAD_REQUEST);
		}
		try {
			chatToEdit.setImage(Base64.getEncoder().encodeToString(image.getBytes()));
		} catch (Exception e) {
			throw new InvalidImageException(messUtils.getMessage("generic.image-base-64"), HttpStatus.BAD_REQUEST, e);
		}		
		return chatToEdit;
	}

	@Override
	@Transactional
	public Chat setChatName(Long chatId, String name) {
		if (chatId == null || name == null || name.isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		authUserIsAdmin(chat);
		chat.setName(name);
		return chat;
	}

	
	@Override
	@Transactional
	public Chat deleteChatById(Long chatId) {
		if (chatId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		authUserIsAdmin(chat);
		chatDao.delete(chat);
		return chat;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<User> getAllUsersByChatId(Long chatId) {
		if (chatId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		return chatMapper.chatUserListToUserList(chat.getChatUsers());
	}
	
	@Override
	@Transactional
	public Chat addUsers(Long chatId, List<User> listUsersToAdd, Map<String, Boolean> mapAreAdmin) {
		if (chatId == null|| listUsersToAdd == null || listUsersToAdd.isEmpty()|| mapAreAdmin == null || mapAreAdmin.isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		Iterator<User> userRemoveIterator;
		List<ChatUser> listChatUsersToSave;
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		// auth user needs to be admin to add new users.
		authUserIsAdmin(chat);

		// we search if there is users wanted to add but that they are already in chat
		// as users
		userRemoveIterator = listUsersToAdd.iterator();
		while (userRemoveIterator.hasNext()) {
			User user = userRemoveIterator.next();
			for (ChatUser chatUser : chat.getChatUsers()) {
				if (chatUser.getUser().equals(user)) {
					// user already is in chat so we remove it from users to add.
					userRemoveIterator.remove();
					break;
				}
			}
		}

		// creating chatUser objects.
		listChatUsersToSave = new ArrayList<>();
		for (User user : listUsersToAdd) {
			boolean isAdmin = mapAreAdmin.get(user.getUsername());
			ChatUser chatUser = new ChatUser();
			chatUser.setChat(chat);
			chatUser.setUser(user);
			chatUser.setAdmin(isAdmin);
			listChatUsersToSave.add(chatUser);
		}
		chat.getChatUsers().addAll(listChatUsersToSave);
		return chat;// now users should be updated.
	}

	@Override
	@Transactional
	public Chat quitUsersFromChatByUsername(Long chatId, List<String> listUserUsername) {
		if (chatId == null || listUserUsername == null || listUserUsername.isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		authUserIsAdmin(chat);
		chatUserDao.deleteByChatIdAndUserUsernameIn(chatId, Set.copyOf(listUserUsername));
		chat.setChatUsers(chatUserDao.findByChatId(chatId));
		return chat;
	}

	@Override
	@Transactional
	public Chat changeAdminStatus(Long chatId, Long userId) {
		if (chatId == null || userId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Chat chat;
		ChatUser ch = chatUserDao.findByChatIdAndUserId(chatId, userId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chatUser.not-found"), HttpStatus.NOT_FOUND));
		chat = ch.getChat();
		authUserIsAdmin(chat);
		ch.setAdmin(ch.isAdmin() ? false : true);
		return chat;
	}

	/**
	 * To know if authenticated user is an admin in chat before doing some action
	 * that need authenticated user as admin.
	 * 
	 * @param chat - chat where to check if has authenticated user as admin.
	 * @throws InvalidActionException if authenticated user is not admin or if is
	 *                                not a user in chat.
	 */
	private void authUserIsAdmin(Chat chat) {
		if (chat == null || chat.getChatUsers() == null || chat.getChatUsers().isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		for (int i = 0; i < chat.getChatUsers().size(); i++) {
			User userInChat = chat.getChatUsers().get(i).getUser();
			if (userInChat.equals(authUser)) {
				if (chat.getChatUsers().get(i).isAdmin()) {
					return; // it can execute the action.
				} else {
					// the user is not admin the cannot execute the action.
					throw new InvalidActionException(messUtils.getMessage("chat.only-admin-action"),
							HttpStatus.BAD_REQUEST);
				}
			}
		}
		// user was not found between the users so
		throw new InvalidActionException(messUtils.getMessage("chat.auth-user-not-in-chat"), HttpStatus.BAD_REQUEST);
	}
}
