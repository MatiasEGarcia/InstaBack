package com.instaJava.instaJava.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.ChatDao;
import com.instaJava.instaJava.dao.ChatUserDao;
import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqAddUserChat;
import com.instaJava.instaJava.dto.request.ReqCreateChat;
import com.instaJava.instaJava.dto.request.ReqDelUserFromChat;
import com.instaJava.instaJava.dto.request.ReqUserChat;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.ChatUser;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.ChatTypeEnum;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.InvalidImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.exception.UserNotApplicableForChatException;
import com.instaJava.instaJava.mapper.ChatMapper;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

	private final ChatUserDao chatUserDao;
	private final ChatDao chatDao;
	private final MessagesUtils messUtils;
	private final UserService userService;
	private final MessageService messageService;
	private final FollowService followService;
	private final ChatMapper chatMapper;

	@Override
	@Transactional(readOnly = true)
	public ChatDto getById(Long chatId) {
		if (chatId == null)
			throw new IllegalArgumentException("generic.arg-not-null");
		ChatDto chatDto;
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;
	}

	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<ChatDto> getAuthUserChats(PageInfoDto pageInfoDto){
		if(pageInfoDto == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		List<ChatDto> listChatDto = null;
		ResPaginationG<ChatDto> resPagChatDto = new ResPaginationG<ChatDto>();
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Pageable page = PageRequest.of(pageInfoDto.getPageNo(), pageInfoDto.getPageSize());
		Page<Chat> pageChat = chatDao.findChatsByUser(authUser.getId(), authUser.getUsername(), page);
		if (pageChat.getContent().isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("chat.group-not-found"), HttpStatus.NO_CONTENT);
		}
		
		listChatDto = new ArrayList<>();
		setMessagesNotWatched(listChatDto, pageChat.getContent(), authUser.getUsername());

		resPagChatDto.setList(listChatDto);
		pageInfoDto.setTotalElements(pageChat.getNumberOfElements());
		pageInfoDto.setTotalPages(pageChat.getTotalPages());
		resPagChatDto.setPageInfoDto(pageInfoDto);
		return resPagChatDto;
	}
	

	@Override
	@Transactional
	public ChatDto create(ReqCreateChat reqCreateChat) {
		if (reqCreateChat == null || reqCreateChat.getUsersToAdd() == null || reqCreateChat.getUsersToAdd().isEmpty()
				|| reqCreateChat.getType() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		Chat chat = Chat.builder().name(reqCreateChat.getName()).type(reqCreateChat.getType()).build();
		// creating chatUsers (users in chat)
		setUsersInNewChat(chat, reqCreateChat.getUsersToAdd());
		return chatMapper.chatToChatDto(chatDao.save(chat));

	}

	@Override
	@Transactional
	public ChatDto setImage(MultipartFile image, Long chatId) {
		if (image == null || chatId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Chat chatUpdated;
		ChatDto chatDto;
		Optional<Chat> chatToEdit = chatDao.findById(chatId);
		if (chatToEdit.isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("chat.not-found"), List.of(chatId.toString()),
					HttpStatus.NOT_FOUND);
		}
		// only group chat can edit image attribute.
		if (chatToEdit.get().getType().equals(ChatTypeEnum.PRIVATE)) {
			throw new InvalidActionException(messUtils.getMessage("chat.private-no-image"), HttpStatus.BAD_REQUEST);
		}

		try {
			chatToEdit.get().setImage(Base64.getEncoder().encodeToString(image.getBytes()));
		} catch (Exception e) {
			throw new InvalidImageException(messUtils.getMessage("generic.image-base-64"), HttpStatus.BAD_REQUEST, e);
		}
		chatUpdated = chatDao.save(chatToEdit.get());
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chatUpdated);
		return chatDto;
	}

	@Override
	@Transactional
	public ChatDto setChatName(Long chatId, String name) {
		if (chatId == null || name == null || name.isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		ChatDto chatDto;
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		authUserIsAdmin(chat);
		chat.setName(name);
		chat = chatDao.save(chat);
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;
	}

	@Override
	@Transactional
	public void deleteChatById(Long chatId) {
		if (chatId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		authUserIsAdmin(chat);
		chatDao.delete(chat);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserDto> getAllUsersByChatId(Long chatId) {
		if (chatId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		return chatMapper.chatUserListToUserDtoList(chat.getChatUsers());
	}

	@Override
	@Transactional
	public ChatDto addUsers(ReqAddUserChat reqAddUserChat) {
		if (reqAddUserChat == null || reqAddUserChat.getChatId() == null || reqAddUserChat.getChatId().isBlank()
				|| reqAddUserChat.getUsers() == null || reqAddUserChat.getUsers().isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		List<User> listUsersToAdd;
		Iterator<User> userRemoveIterator;
		List<ChatUser> listChatUsersToSave;
		ChatDto chatDto;
		Chat chat = chatDao.findById(Long.parseLong(reqAddUserChat.getChatId())).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		// auth user needs to be admin to add new users.
		authUserIsAdmin(chat);

		// getting users' username and isAdmin.
		Map<String, Boolean> mapUsersUsername = new HashMap<>(); // key = username , value = isAdmin.
		for (ReqUserChat reqUserChat : reqAddUserChat.getUsers()) {
			mapUsersUsername.put(reqUserChat.getUsername(), reqUserChat.isAdmin());
		}

		// getting users and checking if all were found and if are applciable
		listUsersToAdd = userService.getByUsernameIn(mapUsersUsername.keySet());
		checkAllFoundedByUsername(listUsersToAdd, mapUsersUsername.keySet());
		areNotApplicable(listUsersToAdd);

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
			boolean isAdmin = mapUsersUsername.get(user.getUsername());
			ChatUser chatUser = new ChatUser();
			chatUser.setChat(chat);
			chatUser.setUser(user);
			chatUser.setAdmin(isAdmin);
			listChatUsersToSave.add(chatUser);
		}
		chat.getChatUsers().addAll(listChatUsersToSave);
		chat = chatDao.save(chat);
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;// now users should be updated.
	}

	@Override
	@Transactional
	public ChatDto quitUsersFromChat(ReqDelUserFromChat reqDelUserFromChat) {
		if (reqDelUserFromChat == null || reqDelUserFromChat.getChatId() == null
				|| reqDelUserFromChat.getChatId().isBlank() || reqDelUserFromChat.getUsersUsername() == null
				|| reqDelUserFromChat.getUsersUsername().isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		ChatDto chatDto;
		Long chatId = Long.parseLong(reqDelUserFromChat.getChatId());
		Chat chat = chatDao.findById(chatId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chat.not-found"), HttpStatus.NOT_FOUND));
		authUserIsAdmin(chat);
		chatUserDao.deleteByChatIdAndUserUsernameIn(chatId, Set.copyOf(reqDelUserFromChat.getUsersUsername()));
		chat.setChatUsers(chatUserDao.findByChatId(chatId));
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;
	}

	@Override
	@Transactional
	public ChatDto changeAdminStatus(Long chatId, Long userId) {
		if (chatId == null || userId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		ChatDto chatDto;
		Chat chat;
		ChatUser ch = chatUserDao.findByChatIdAndUserId(chatId, userId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("chatUser.not-found"), HttpStatus.NOT_FOUND));
		chat = ch.getChat();
		authUserIsAdmin(chat);
		ch.setAdmin(ch.isAdmin() ? false : true);
		chatUserDao.save(ch);
		chatDto = new ChatDto();
		setMessagesNotWatched(chatDto, chat);
		return chatDto;
	}

	@Override
	public int bynarySearchByChatId(List<Chat> chats, Long chatIdToFind) {
		if(chats == null || chats.isEmpty() || chatIdToFind == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		double low = 0;
		double high = chats.size() - 1;
		
		while(low <= high) {
			int middlePosition = (int) Math.ceil((low + high) / 2.0);
			Chat middleChat = chats.get(middlePosition);
			
			if(middleChat.getId() == chatIdToFind) {
				return middlePosition;
			}else if(middleChat.getId() < chatIdToFind) {
				low = middlePosition + 1.0;
			}else {
				high = middlePosition - 1.0;
			}
		}
		return -1;
	}
	
	
	/**
	 * Will add users in chat, will create a ChatUser object for each user wanted to
	 * add in chat and will add auth user too.
	 * 
	 * @param chat                  - chat where users will be added
	 * @param reqCreateChatUserlist - list of users' username and isAdmin
	 *                              information.
	 * @throws InvalidActionException if chat is private and the number of users to
	 *                                add are more than 2.
	 */
	private void setUsersInNewChat(Chat chat, List<ReqUserChat> reqUserChatlist) {
		if (reqUserChatlist == null || reqUserChatlist.isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		List<User> listUsersToAdd;
		List<ChatUser> listChatUsersToSave;
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		// getting users' username and isAdmin property to add. // the auth user is not
		// necessary
		Map<String, Boolean> mapUsersToAdd = new HashMap<>(); // key = username , value = isAdmin
		for (ReqUserChat reqUserChat : reqUserChatlist) {
			if (!reqUserChat.getUsername().equalsIgnoreCase(authUser.getUsername())) {
				mapUsersToAdd.put(reqUserChat.getUsername(), reqUserChat.isAdmin());
			}
		}
		// getting users from database.
		listUsersToAdd = userService.getByUsernameIn(mapUsersToAdd.keySet());
		checkAllFoundedByUsername(listUsersToAdd, mapUsersToAdd.keySet());
		areNotApplicable(listUsersToAdd);

		// creating chatUser objects.
		listChatUsersToSave = new ArrayList<>();
		for (User user : listUsersToAdd) {
			boolean isAdmin = mapUsersToAdd.get(user.getUsername());
			ChatUser chatUser = new ChatUser();
			chatUser.setChat(chat);
			chatUser.setUser(user);
			chatUser.setAdmin(isAdmin);
			listChatUsersToSave.add(chatUser);
		}

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

	}

	/**
	 * Function to check if users are applicable for chat, for example, are visible
	 * or not? the auth user follow it or not?
	 * 
	 * @param users - list off users to check if are applicable or not.
	 */
	@Transactional(readOnly = true)
	private void areNotApplicable(List<User> users) {
		if (users == null) {
			throw new IllegalArgumentException("generic.arg-not-null");
		}
		List<String> usersNotApplicable = null;

		for (User user : users) {
			if (!user.isVisible()) {
				FollowStatus status = followService.getFollowStatusByFollowedId(user.getId());
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
		List<Long[]> countMessages = messageService.getMessagesNotWatchedCountByChatIds(List.of(chat.getId()),
				authUser.getUsername());
		if (!countMessages.isEmpty()) {
			nMessagesNoWatched = countMessages.get(0)[1];
		}
		chatDto.setMessagesNoWatched(nMessagesNoWatched.toString());
		chatMapper.chatToChatDto(chat, chatDto);
	}

	/**
	 * Function to set list of chats info in ChatDtos with number of messages not
	 * watched in that chat(by the authUsername)
	 * 
	 * @param listChatDto  - a list where save chatDto objects.
	 * @param listChat     - a list with Chat objects.
	 * @param authUsername - authenicated user's username.
	 */
	private void setMessagesNotWatched(List<ChatDto> listChatDto, List<Chat> listChat, String authUsername) {
		if (listChatDto == null || listChat == null || listChat.isEmpty() || authUsername == null
				|| authUsername.isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		List<Long[]> countMessages;
		List<Long> listChatsIds = new ArrayList<>();
		chatMapper.chatListToChatDtoList(listChat,listChatDto);

		// getting chats ids.
		for (Chat chat : listChat) {
			listChatsIds.add(chat.getId());
		}
		
		countMessages = messageService.getMessagesNotWatchedCountByChatIds(listChatsIds, authUsername);
		
		for(Long[] array : countMessages) {
			ChatDto chatDto = listChatDto.get(this.bynarySearchByChatId(listChat, array[0]));//listChatDto is sort in the same way of listChat
			chatDto.setMessagesNoWatched(array[1].toString());
		}

	}


}





















