package com.instaJava.instaJava.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqAddUserChat;
import com.instaJava.instaJava.dto.request.ReqCreateChat;
import com.instaJava.instaJava.dto.request.ReqDelUserFromChat;
import com.instaJava.instaJava.dto.request.ReqUserChat;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.exception.UserNotApplicableForChatException;
import com.instaJava.instaJava.mapper.ChatMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.service.ChatService;
import com.instaJava.instaJava.service.FollowService;
import com.instaJava.instaJava.service.MessageService;
import com.instaJava.instaJava.service.UserService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.SearchsUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatApplicationImpl implements ChatApplication {

	private final ChatService chService;
	private final MessageService mService;
	private final FollowService fService;
	private final UserService uService;
	private final ChatMapper chMapper;
	private final UserMapper uMapper;
	private final MessagesUtils messUtils;
	private final SearchsUtils searchUtils;

	@Override
	public ResPaginationG<ChatDto> getAuhtUserChats(int pageNo, int pageSize) {
		Page<Chat> pageChats;
		List<ChatDto> listChatDto;
		ResPaginationG<ChatDto> resPagChatDto = new ResPaginationG<ChatDto>();
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PageInfoDto pageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, "", Direction.ASC);
		// getting chats
		pageChats = chService.getUserChats(pageInfoDto, authUser.getId(), authUser.getUsername());

		// getting how many messages were not watched.
		listChatDto = new ArrayList<>();
		setMessagesNotWatched(listChatDto, pageChats.getContent(), authUser.getUsername());

		resPagChatDto.setList(listChatDto);
		pageInfoDto.setTotalElements(pageChats.getNumberOfElements());
		pageInfoDto.setTotalPages(pageChats.getTotalPages());
		resPagChatDto.setPageInfoDto(pageInfoDto);

		return resPagChatDto;
	}

	@Override
	public ChatDto create(ReqCreateChat reqChat) {
		if(reqChat == null || reqChat.getUsersToAdd() == null || reqChat.getUsersToAdd().isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		Chat chatCreated;
		List<User> listUsersToAdd;
		Map<String, Boolean> mapUsersToAdd = new HashMap<>(); // key = username , value = isAdmin
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		// getting users' username and isAdmin property to add, the auth user is not
		// necessary
		for (ReqUserChat reqUserChat : reqChat.getUsersToAdd()) {
			if (!reqUserChat.getUsername().equalsIgnoreCase(authUser.getUsername())) {
				mapUsersToAdd.put(reqUserChat.getUsername(), reqUserChat.isAdmin());
			}
		}
		// getting users from database.
		listUsersToAdd = uService.getByUsernameIn(mapUsersToAdd.keySet());
		checkAllFoundedByUsername(listUsersToAdd, mapUsersToAdd.keySet());
		areNotApplicable(listUsersToAdd);
		// creating chat
		chatCreated = chService.create(reqChat.getName(), reqChat.getType(), listUsersToAdd, mapUsersToAdd);
		return chMapper.chatToChatDto(chatCreated);
	}

	@Override
	public ChatDto setImage(MultipartFile image, Long chatId) {
		ChatDto chatDto;
		Chat chatUpdated = chService.setImage(image, chatId);
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chatUpdated);
		return chatDto;
	}

	@Override
	public ChatDto setChatName(Long chatId, String name) {
		ChatDto chatDto;
		Chat chat = chService.setChatName(chatId, name);
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;
	}

	@Override
	public ChatDto deleteChatById(Long chatId) {
		Chat chat = chService.deleteChatById(chatId);
		return chMapper.chatToChatDto(chat);
	}

	@Override
	public List<UserDto> getAllUsersByChatId(Long chatId) {
		List<User> listUser = chService.getAllUsersByChatId(chatId);
		return uMapper.userListToUserDtoList(listUser);
	}

	@Override
	public ChatDto addUsers(ReqAddUserChat reqAddUserChat) {
		if(reqAddUserChat == null || reqAddUserChat.getUsers() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		List<User> listUsersToAdd;
		ChatDto chatDto;
		Chat chat;
		// getting users' username and isAdmin.
		Map<String, Boolean> mapUsersUsername = new HashMap<>(); // key = username , value = isAdmin.
		for (ReqUserChat reqUserChat : reqAddUserChat.getUsers()) {
			mapUsersUsername.put(reqUserChat.getUsername(), reqUserChat.isAdmin());
		}
		// getting users and checking if all were found and if are applciable
		listUsersToAdd = uService.getByUsernameIn(mapUsersUsername.keySet());
		checkAllFoundedByUsername(listUsersToAdd, mapUsersUsername.keySet());
		areNotApplicable(listUsersToAdd);

		chat = chService.addUsers(Long.parseLong(reqAddUserChat.getChatId()), listUsersToAdd, mapUsersUsername);
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;
	}
	
	
	@Override
	public ChatDto quitUsersFromChat(ReqDelUserFromChat reqDelUserFromChat) {
		if(reqDelUserFromChat == null || reqDelUserFromChat.getChatId() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		ChatDto chatDto;
		Chat chat = chService.quitUsersFromChatByUsername(Long.parseLong(reqDelUserFromChat.getChatId()), reqDelUserFromChat.getUsersUsername());
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;
	}
	
	@Override
	public ChatDto changeAdminStatus(Long chatId, Long userId) {
		Chat chat = chService.changeAdminStatus(chatId, userId);
		ChatDto chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;
	}

	/*
	 * Function to set list of chats info in ChatDtos with number of messages not
	 * watched in that chat(by the authUsername)
	 * 
	 * @param listChatDto - a list where save chatDto objects.
	 * 
	 * @param listChat - a list with Chat objects.
	 * 
	 * @param authUsername - authenicated user's username.
	 */
	private void setMessagesNotWatched(List<ChatDto> listChatDto, List<Chat> listChat, String authUsername) {
		if (listChatDto == null || listChat == null || listChat.isEmpty() || authUsername == null
				|| authUsername.isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		List<Long[]> countMessages;
		List<Long> listChatsIds = new ArrayList<>();
		chMapper.chatListToChatDtoList(listChat, listChatDto);

		// getting chats ids.
		for (Chat chat : listChat) {
			listChatsIds.add(chat.getId());
		}

		countMessages = mService.getMessagesNotWatchedCountByChatIds(listChatsIds, authUsername);

		for (Long[] array : countMessages) {
			int index = searchUtils.bynarySearchById(listChat, array[0]);
			ChatDto chatDto = listChatDto.get(index);// listChatDto is sort
													// in the same way of
													// listChat
			chatDto.setMessagesNoWatched(array[1].toString());
		}

	}

	/**
	 * Calls setMessagesNotWatched.
	 * 
	 * @param chatDto - object where save Chat info withmessages not watched count.
	 * @param chat    - chat found.
	 * @throws IllegalArgumentException if some param is null.
	 */
	private void setMessagesNotWatched(ChatDto chatDto, Chat chat) {
		if (chat == null || chatDto == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		Long nMessagesNoWatched = 0L;

		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Long[]> countMessages = mService.getMessagesNotWatchedCountByChatIds(List.of(chat.getId()),
				authUser.getUsername());
		if (!countMessages.isEmpty()) {
			nMessagesNoWatched = countMessages.get(0)[1];
		}
		chatDto.setMessagesNoWatched(nMessagesNoWatched.toString());
		chMapper.chatToChatDto(chat, chatDto);
	}

	/**
	 * Function to check if users are applicable for chat, for example, are visible
	 * or not? the auth user follow it or not?
	 * 
	 * @param users - list off users to check if are applicable or not.
	 */
	private void areNotApplicable(List<User> users) {
		if (users == null) {
			throw new IllegalArgumentException("generic.arg-not-null");
		}
		List<String> usersNotApplicable = null;

		for (User user : users) {
			if (!user.isVisible()) {
				FollowStatus status = fService.getFollowStatusByFollowedId(user.getId());
				// only if auth user follow the user and the status is accepted can add it to
				// the group.
				if (!status.equals(FollowStatus.ACCEPTED)) {
					if (usersNotApplicable == null) {
						usersNotApplicable = new ArrayList<>();
					}
					usersNotApplicable.add(user.getUsername());
				}
			}
		}

		if (usersNotApplicable != null) {
			throw new UserNotApplicableForChatException(messUtils.getMessage("chat.users-not-applicable"),
					HttpStatus.BAD_REQUEST, usersNotApplicable);
		}
	}

	/**
	 * Method to check if all the users needed to add in Chat entity to save were
	 * found.
	 * 
	 * @param usersFound     - users who where found.
	 * @param allUsersString - list of usernames of the users provided by the
	 *                       request (there must be all the usernames that had been
	 *                       needed to be found).
	 * @throws IllegalArgumentExeption if one parameter is null.
	 * @throws RecordNotFoundException if there was some user who couldn't be found
	 */
	private void checkAllFoundedByUsername(List<User> usersFound, Set<String> allUsersString) {
		if (usersFound == null || allUsersString == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		List<String> usersFoundUsernames = usersFound.stream().map(User::getUsername).toList();

		List<String> usersNotFound = allUsersString.stream().filter(username -> !usersFoundUsernames.contains(username))
				.toList();

		if (!usersNotFound.isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("user.group-not-found"), usersNotFound,
					HttpStatus.NOT_FOUND);
		}
	}

	



}
