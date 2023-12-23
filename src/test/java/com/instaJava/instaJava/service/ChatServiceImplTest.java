package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.ChatUser;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.ChatTypeEnum;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.exception.UserNotApplicableForChatException;
import com.instaJava.instaJava.mapper.ChatMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private UserService userService;
	@Mock
	private FollowService followService;
	@Mock
	private MessageService messageService;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private PageableUtils pageUtils;
	@Mock
	private ChatMapper chatMapper;
	@Mock
	private ChatUserDao chatUserDao;
	@Mock
	private ChatDao chatDao;
	@InjectMocks
	private ChatServiceImpl chatService;

	private final User user = User.builder().userId(1L).username("Mati").role(RolesEnum.ROLE_USER).build();

	// getById
	@Test
	void getByIdParamChatIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.getById(null));
	}

	@Test
	void getByIdNoExistsThrow() {
		Long id = 1L;
		when(chatDao.findById(id)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.getById(id));
	}

	@Test
	void getByIdExistsNotNull() {
		// messages not watched = 2
		Long chatId = 1L;
		Long messArr[] = { chatId, 2L };
		List<Long[]> listMessArr = new ArrayList<>();
		listMessArr.add(messArr);
		Chat chat = new Chat(chatId);
		ChatDto chatDto = ChatDto.builder().messagesNoWatched("2").build();
		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// get messages not watched count.
		when(messageService.getMessagesNotWatchedCountByChatIds(List.of(chatId), user.getUsername()))
				.thenReturn(listMessArr);

		assertNotNull(chatService.getById(chatId));
		verify(chatMapper).chatToChatDto(chat, chatDto);
	}

	// getAuthUserChats
	@Test
	void getAuthUserChatsParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.getAuthUserChats(null));
		verify(chatDao, never()).findByChatUsersUserUserId(eq(1L), any(Pageable.class));
	}

	@Test
	void getAuthUserChatsNoContentThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(20).build();
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(chatDao.findChatsByUser(eq(user.getUserId()),eq(user.getUsername()),any(Pageable.class))).thenReturn(Page.empty());

		assertThrows(RecordNotFoundException.class, () -> chatService.getAuthUserChats(pageInfoDto));
		
		verify(messageService,never()).getMessagesNotWatchedCountByChatIds(anyList(), eq(user.getUsername()));
	}

	//I cannot test this. A method void called from a private method. the void method change a list content, and I cannot emulate that.
	void getAuthUserChatsReturnsNotNull() {
		
	}

	// create
	@Test
	void createParamReqCreateChatNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.create(null));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createParamReqCreateChatUsersToAddNullThrow() {
		ReqCreateChat reqChat = ReqCreateChat.builder().type(ChatTypeEnum.PRIVATE).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.create(reqChat));
		verify(chatUserDao, never()).saveAll(Collections.emptyList());
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createParamReqChatUsersToAddEmptyThrow() {
		ReqCreateChat reqChat = ReqCreateChat.builder().usersToAdd(Collections.emptyList()).type(ChatTypeEnum.PRIVATE)
				.build();
		assertThrows(IllegalArgumentException.class, () -> chatService.create(reqChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createParamReqChatTypeNullThrow() {
		ReqUserChat reqUserChat = new ReqUserChat("ranomd", false);
		List<ReqUserChat> noEmptyList = List.of(reqUserChat);
		ReqCreateChat reqChat = ReqCreateChat.builder().usersToAdd(noEmptyList).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.create(reqChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createNoAllUsersWereFoundThrow() {
		// users to find and add in chats.
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false);
		List<ReqUserChat> usersToAdd = List.of(reqUserChat1, reqUserChat2);
		Set<String> setUsersUsername = Set.of(reqUserChat1.getUsername(), reqUserChat2.getUsername());
		// two users who where found.
		User user1 = User.builder().username("username1").build();

		ReqCreateChat reqChat = ReqCreateChat.builder().usersToAdd(usersToAdd).type(ChatTypeEnum.PRIVATE).build();
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(userService.getByUsernameIn(setUsersUsername)).thenReturn(List.of(user1));

		assertThrows(RecordNotFoundException.class, () -> chatService.create(reqChat),
				"if one user wasn't found , should throw UserNotApplicableForChatException");

		verify(userService).getByUsernameIn(setUsersUsername);
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createUserNotVisibleFollowStatusIsRejectedThrow() {
		// users to find and add in chats.
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false);
		List<ReqUserChat> usersToAdd = List.of(reqUserChat1, reqUserChat2);
		Set<String> setUsersUsername = Set.of(reqUserChat1.getUsername(), reqUserChat2.getUsername());
		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		ReqCreateChat reqChat = ReqCreateChat.builder().usersToAdd(usersToAdd).type(ChatTypeEnum.PRIVATE).build();
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(userService.getByUsernameIn(setUsersUsername)).thenReturn(List.of(user1, user2));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.REJECTED);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.create(reqChat),
				"if one user to add in chat is not visible and its follow status authUser -> otherUser is rejected , should throw UserNotApplicableForChatException");

		verify(userService).getByUsernameIn(setUsersUsername);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createUserNotVisibleFollowStatusIsInProcessThrow() {
		// users to find and add in chats.
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false);
		List<ReqUserChat> usersToAdd = List.of(reqUserChat1, reqUserChat2);
		Set<String> setUsersUsername = Set.of(reqUserChat1.getUsername(), reqUserChat2.getUsername());

		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		ReqCreateChat reqChat = ReqCreateChat.builder().usersToAdd(usersToAdd).type(ChatTypeEnum.PRIVATE).build();
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(userService.getByUsernameIn(setUsersUsername)).thenReturn(List.of(user1, user2));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.IN_PROCESS);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.create(reqChat),
				"if one user to add in chat is not visible and its follow status authUser -> otherUser is in process , should throw UserNotApplicableForChatException");

		verify(userService).getByUsernameIn(setUsersUsername);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createUserNotVisibleFollowStatusIsNotAskedThrow() {
		// users to find and add in chats.
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false);
		List<ReqUserChat> usersToAdd = List.of(reqUserChat1, reqUserChat2);
		Set<String> setUsersUsername = Set.of(reqUserChat1.getUsername(), reqUserChat2.getUsername());
		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		ReqCreateChat reqChat = ReqCreateChat.builder().usersToAdd(usersToAdd).type(ChatTypeEnum.PRIVATE).build();
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(userService.getByUsernameIn(setUsersUsername)).thenReturn(List.of(user1, user2));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.NOT_ASKED);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.create(reqChat),
				"if one user to add in chat is not visible and its follow status authUser -> otherUser is not asked , should throw UserNotApplicableForChatException");

		verify(userService).getByUsernameIn(setUsersUsername);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createPrivateChatMoreThan2UsersThrow() {
		// users to find and add in chats.
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false);
		List<ReqUserChat> usersToAdd = List.of(reqUserChat1, reqUserChat2);
		Set<String> setUsersUsername = Set.of(reqUserChat1.getUsername(), reqUserChat2.getUsername());
		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		ReqCreateChat reqChat = ReqCreateChat.builder().usersToAdd(usersToAdd).type(ChatTypeEnum.PRIVATE).build();

		when(userService.getByUsernameIn(setUsersUsername)).thenReturn(List.of(user1, user2));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.ACCEPTED);
		assertThrows(InvalidActionException.class, () -> chatService.create(reqChat));

		verify(userService).getByUsernameIn(setUsersUsername);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createPrivateChatReturnsNotNull() {
		// users to find and add in chats.
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		List<ReqUserChat> usersToAdd = List.of(reqUserChat1);
		Set<String> setUsersUsername = Set.of(reqUserChat1.getUsername());
		// Users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		ChatDto chatDto = new ChatDto();
		// chat which will be save.
		Chat chat = Chat.builder().build();

		ReqCreateChat reqChat = ReqCreateChat.builder().usersToAdd(usersToAdd).type(ChatTypeEnum.PRIVATE).build();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(userService.getByUsernameIn(setUsersUsername)).thenReturn(List.of(user1));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.ACCEPTED);
		when(chatDao.save(any(Chat.class))).thenReturn(chat);
		when(chatMapper.chatToChatDto(any(Chat.class))).thenReturn(chatDto);

		assertNotNull(chatService.create(reqChat));

		verify(userService).getByUsernameIn(setUsersUsername);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao).save(any(Chat.class));
	}

	@Test
	void createGroupChatReturnsNotNull() {
		// users to find and add in chats.
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false);
		List<ReqUserChat> usersToAdd = List.of(reqUserChat1, reqUserChat2);
		Set<String> setUsersUsername = Set.of(reqUserChat1.getUsername(), reqUserChat2.getUsername());

		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();
		ChatDto chatDto = new ChatDto();
		// chat which will be save.
		Chat chat = Chat.builder().build();

		ReqCreateChat reqChat = ReqCreateChat.builder().name("randomGroupName").usersToAdd(usersToAdd)
				.type(ChatTypeEnum.GROUP).build();

		when(userService.getByUsernameIn(setUsersUsername)).thenReturn(List.of(user1, user2));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.ACCEPTED);
		when(chatDao.save(any(Chat.class))).thenReturn(chat);
		when(chatMapper.chatToChatDto(any(Chat.class))).thenReturn(chatDto);

		assertNotNull(chatService.create(reqChat));

		verify(userService).getByUsernameIn(setUsersUsername);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao).save(any(Chat.class));

	}

	// setImage method

	@Test
	void setImageParamImageNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.setImage(null, anyLong()));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void setImageParamChatIdNullThrow() { // just as mock MultipartFile
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		assertThrows(IllegalArgumentException.class, () -> chatService.setImage(multipartFile, null));
		verify(chatDao, never()).save(any(Chat.class));

	}

	@Test
	void setImageChatNotFoundThrow() { // just as mock MultipartFile
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.setImage(multipartFile, anyLong()));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void setImageChatPrivateThrow() {
		Chat chat = Chat.builder().type(ChatTypeEnum.PRIVATE).build();
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.of(chat));
		assertThrows(InvalidActionException.class, () -> chatService.setImage(multipartFile, anyLong()),
				"If chat is private then cannot set an image");
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void setImageReturnsNotNull() {
		// messages not watched = 2
		Long chatId = 1L;
		Long messArr[] = { chatId, 2L };
		List<Long[]> listMessArr = new ArrayList<>();
		listMessArr.add(messArr);
		Chat chat = Chat.builder().chatId(chatId).type(ChatTypeEnum.GROUP).build();
		ChatDto chatDto = ChatDto.builder().messagesNoWatched("2").build();

		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(chatDao.save(chat)).thenReturn(chat);
		// get messages not watched count.
		when(messageService.getMessagesNotWatchedCountByChatIds(List.of(chatId), user.getUsername()))
				.thenReturn(listMessArr);

		assertNotNull(chatService.setImage(multipartFile, chatId));
		verify(chatDao).save(any(Chat.class));
		verify(chatMapper).chatToChatDto(chat, chatDto);
	}

	// setChatName
	@Test
	void setChatNameParamChatIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.setChatName(null, "name"));
	}

	@Test
	void setChatNameParamNameNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.setChatName(1L, null));
	}

	@Test
	void setChatNameParamNameBlankThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.setChatName(1L, ""));
	}

	@Test
	void setChatNameChatNotFoundThrow() {
		Long chatId = 1L;
		String chatName = "chatName";
		when(chatDao.findById(chatId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.setChatName(chatId, chatName));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void setChatNameAuthUserNotAdminThrow() {
		Long chatId = 1L;
		String chatName = "chatName";
		ChatUser chatUser = ChatUser.builder().user(user).admin(false).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		assertThrows(InvalidActionException.class, () -> chatService.setChatName(chatId, chatName));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void setChatNameReturnsNotNull() {
		Long chatId = 1L;
		Long messArr[] = { chatId, 2L };
		List<Long[]> listMessArr = new ArrayList<>();
		listMessArr.add(messArr);
		String chatName = "chatName";
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();
		Chat chat = Chat.builder().chatId(chatId).chatUsers(List.of(chatUser)).build();
		ChatDto chatDto = ChatDto.builder().messagesNoWatched("2").build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		chat.setName(chatName);
		when(chatDao.save(chat)).thenReturn(chat);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// get messages not watched count.
		when(messageService.getMessagesNotWatchedCountByChatIds(List.of(chatId), user.getUsername()))
				.thenReturn(listMessArr);

		assertNotNull(chatService.setChatName(chatId, chatName));
		verify(chatDao).save(any(Chat.class));
		verify(chatMapper).chatToChatDto(chat, chatDto);
	}

	// deleteChatId
	@Test
	void deleteChatIdParamChatIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.deleteChatById(null));
	}

	@Test
	void deleteChatIdChatNotFoundThrow() {
		Long chatId = 1L;
		when(chatDao.findById(chatId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.deleteChatById(chatId));
		verify(chatDao, never()).delete(any(Chat.class));
	}

	@Test
	void deleteChatIdAuthUserNotAdminThrow() {
		Long chatId = 1L;
		ChatUser chatUser = ChatUser.builder().user(user).admin(false).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();
		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		assertThrows(InvalidActionException.class, () -> chatService.deleteChatById(chatId));
		verify(chatDao, never()).delete(any(Chat.class));

	}

	@Test
	void deleteChatById() {
		Long chatId = 1L;
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		chatService.deleteChatById(chatId);
		verify(chatDao).delete(any(Chat.class));
	}

	// getAllUsersByChatId
	@Test
	void getAllUsersByChatIdParamChatIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.getAllUsersByChatId(null));
	}

	@Test
	void getAllUsersByChatIdChatNotFoundThrow() {
		Long chatId = 1L;
		when(chatDao.findById(chatId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.getAllUsersByChatId(chatId));
	}

	@Test
	void getAllUsersByChatIdReturnsNotNull() {
		Long chatId = 1L;
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();
		UserDto userDto = UserDto.builder().userId(user.getUserId().toString()).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		when(chatMapper.chatUserListToUserDtoList(chat.getChatUsers())).thenReturn(List.of(userDto));
		assertNotNull(chatService.getAllUsersByChatId(chatId));
	}

	// addUsers

	@Test
	void addUsersParamReqAddUserChatNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(null));
	}

	@Test
	void addUsersParamReqAddUserChatChatIdNullThrow() {
		ReqUserChat reqUserChat = new ReqUserChat();
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().users(List.of(reqUserChat)).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(reqAddUserChat));
	}

	@Test
	void addUsersParamReqAddUserChatChatIdBlankThrow() {
		ReqUserChat reqUserChat = new ReqUserChat();
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId("").users(List.of(reqUserChat)).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(reqAddUserChat));
	}

	@Test
	void addUsersParamReqAddUserChatUsersNullThrow() {
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId("1").build();
		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(reqAddUserChat));
	}

	@Test
	void addUsersParamReqAddUserChatUsersEmptyThrow() {
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId("1").users(Collections.emptyList()).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(reqAddUserChat));
	}

	@Test
	void addUsersChatNotFoundThrow() {
		String chatId = "1";
		ReqUserChat reqUserChat = new ReqUserChat();
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId(chatId).users(List.of(reqUserChat)).build();
		when(chatDao.findById(Long.parseLong(chatId))).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.addUsers(reqAddUserChat));
	}

	@Test
	void addUsersChatAuthUserNotAdminThrow() {
		String chatId = "1";
		ReqUserChat reqUserChat = new ReqUserChat();
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId(chatId).users(List.of(reqUserChat)).build();
		ChatUser chatUser = ChatUser.builder().user(user).admin(false).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();
		when(chatDao.findById(Long.parseLong(chatId))).thenReturn(Optional.of(chat));
		// authUser
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class, () -> chatService.addUsers(reqAddUserChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	// USER NOT IN CHAT
	@Test
	void addUsersNotAllUsersFoundThrow() {
		String chatId = "1";
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false); // will not be found.
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId(chatId)
				.users(List.of(reqUserChat1, reqUserChat2)).build();
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();// auth user is admin
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();
		// only user who was found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();

		when(chatDao.findById(Long.parseLong(chatId))).thenReturn(Optional.of(chat));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(userService.getByUsernameIn(anySet())).thenReturn(List.of(user1));

		assertThrows(RecordNotFoundException.class, () -> chatService.addUsers(reqAddUserChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void addUsersNotApplicableFollowStatusRejectedThrow() {
		String chatId = "1";
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false); // will not be found.
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId(chatId)
				.users(List.of(reqUserChat1, reqUserChat2)).build();
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();// auth user is admin
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();
		// only user who was found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().userId(3L).username("username2").visible(true).build();

		when(chatDao.findById(Long.parseLong(chatId))).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// getting users
		when(userService.getByUsernameIn(anySet())).thenReturn(List.of(user1, user2));
		// checking if users are applicable
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.REJECTED);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.addUsers(reqAddUserChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void addUsersNotApplicableFollowStatusNotAskedThrow() {
		String chatId = "1";
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false); // will not be found.
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId(chatId)
				.users(List.of(reqUserChat1, reqUserChat2)).build();
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();// auth user is admin
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();
		// only user who was found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().userId(3L).username("username2").visible(true).build();

		when(chatDao.findById(Long.parseLong(chatId))).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// getting users
		when(userService.getByUsernameIn(anySet())).thenReturn(List.of(user1, user2));
		// checking if users are applicable
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.NOT_ASKED);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.addUsers(reqAddUserChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void addUsersNotApplicableFollowStatusInProcessThrow() {
		String chatId = "1";
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false); // will not be found.
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId(chatId)
				.users(List.of(reqUserChat1, reqUserChat2)).build();
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();// auth user is admin
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();
		// only user who was found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().userId(3L).username("username2").visible(true).build();

		when(chatDao.findById(Long.parseLong(chatId))).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// getting users
		when(userService.getByUsernameIn(anySet())).thenReturn(List.of(user1, user2));
		// checking if users are applicable
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.IN_PROCESS);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.addUsers(reqAddUserChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void addUsersReturnNotNullAuthUserAlreadyInChatIsNotAddedAgain() {
		String chatIdString = "1";
		Long chatIdLong = 1L;
		Long messArr[] = { chatIdLong, 2L };
		List<Long[]> listMessArr = new ArrayList<>();
		listMessArr.add(messArr);
		ReqUserChat reqUserChatAuthUser = new ReqUserChat(user.getUsername(), false);
		ReqUserChat reqUserChat1 = new ReqUserChat("username1", false);
		ReqUserChat reqUserChat2 = new ReqUserChat("username2", false); // will not be found.
		ReqAddUserChat reqAddUserChat = ReqAddUserChat.builder().chatId(chatIdString)
				.users(List.of(reqUserChatAuthUser, reqUserChat1, reqUserChat2)).build();
		ChatUser chatUserAuthUser = ChatUser.builder().user(user).admin(true).build();// auth user is admin
		List<ChatUser> listChatUsers = new ArrayList<>();
		listChatUsers.add(chatUserAuthUser);
		Chat chat = Chat.builder().chatId(chatIdLong).chatUsers(listChatUsers).build();
		ChatDto chatDto = ChatDto.builder().messagesNoWatched("2").build();
		// only user who was found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().userId(3L).username("username2").visible(true).build();
		List<User> listUsersFound = new ArrayList<>();
		listUsersFound.add(user1);
		listUsersFound.add(user2);
		listUsersFound.add(user);

		when(chatDao.findById(chatIdLong)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// getting users
		when(userService.getByUsernameIn(anySet())).thenReturn(listUsersFound);
		// checking if users are applicable
		when(followService.getFollowStatusByFollowedId(anyLong())).thenReturn(FollowStatus.ACCEPTED);

		// I need to check that Auth user is not added 2 times
		ChatUser chatUser1 = ChatUser.builder().user(user1).admin(false).build();
		ChatUser chatUser2 = ChatUser.builder().user(user2).admin(false).build();
		listChatUsers.add(chatUser1);
		listChatUsers.add(chatUser2);
		chat.getChatUsers().addAll(listChatUsers);

		// dao
		when(chatDao.save(chat)).thenReturn(chat);
		// get messages not watched count.
		when(messageService.getMessagesNotWatchedCountByChatIds(List.of(chatIdLong), user.getUsername()))
				.thenReturn(listMessArr);

		assertNotNull(chatService.addUsers(reqAddUserChat));

		verify(chatMapper).chatToChatDto(chat, chatDto);
	}

	// quitUsersFromChat.
	@Test
	void quitUsersFromChatParamReqDelUserFromChatNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.quitUsersFromChat(null));
	}

	@Test
	void quitUsersFromChatParamReqDelUserFromChatChatIdNullThrow() {
		List<String> listUsername = List.of("username1", "username2");
		ReqDelUserFromChat reqDelUserFromChat = ReqDelUserFromChat.builder().usersUsername(listUsername).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.quitUsersFromChat(reqDelUserFromChat));
	}

	@Test
	void quitUsersFromChatParamReqDelUserFromChatUsersUsernameNullThrow() {
		ReqDelUserFromChat reqDelUserFromChat = ReqDelUserFromChat.builder().chatId("1").usersUsername(null).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.quitUsersFromChat(reqDelUserFromChat));
	}

	@Test
	void quitUsersFromChatParamReqDelUserFromChatUsersUsernameEmptyThrow() {
		ReqDelUserFromChat reqDelUserFromChat = ReqDelUserFromChat.builder().chatId("1")
				.usersUsername(Collections.emptyList()).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.quitUsersFromChat(reqDelUserFromChat));
	}

	@Test
	void quitUsersFromChatChatNotFoundThrow() {
		Long chatId = 1L;
		List<String> listUsername = List.of("username1", "username2");
		ReqDelUserFromChat reqDelUserFromChat = ReqDelUserFromChat.builder().chatId(chatId.toString())
				.usersUsername(listUsername).build();
		when(chatDao.findById(chatId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.quitUsersFromChat(reqDelUserFromChat));
		verify(chatUserDao, never()).deleteByChatIdAndUserUsernameIn(eq(chatId), anySet());
	}

	@Test
	void quitUsersFromChatAuthUserNotAdminThrow() {
		Long chatId = 1L;
		List<String> listUsername = List.of("username1", "username2");
		ReqDelUserFromChat reqDelUserFromChat = ReqDelUserFromChat.builder().chatId(chatId.toString())
				.usersUsername(listUsername).build();
		ChatUser chatUser = ChatUser.builder().user(user).admin(false).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		assertThrows(InvalidActionException.class, () -> chatService.quitUsersFromChat(reqDelUserFromChat));
		verify(chatUserDao, never()).deleteByChatIdAndUserUsernameIn(eq(chatId), anySet());
	}

	@Test
	void quitUsersFromChat() {
		Long chatId = 1L;
		Long messArr[] = { chatId, 2L };
		List<Long[]> listMessArr = new ArrayList<>();
		listMessArr.add(messArr);
		List<String> listUsername = List.of("username1", "username2");
		ReqDelUserFromChat reqDelUserFromChat = ReqDelUserFromChat.builder().chatId(chatId.toString())
				.usersUsername(listUsername).build();
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();
		Chat chat = Chat.builder().chatId(chatId).chatUsers(List.of(chatUser)).build();
		ChatDto chatDto = ChatDto.builder().messagesNoWatched("2").build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// get messages not watched count.
				when(messageService.getMessagesNotWatchedCountByChatIds(List.of(chatId), user.getUsername()))
						.thenReturn(listMessArr);
		assertNotNull(chatService.quitUsersFromChat(reqDelUserFromChat));
		
		verify(chatUserDao).deleteByChatIdAndUserUsernameIn(eq(chatId), anySet());
		verify(chatUserDao).findByChatChatId(chatId);
		verify(chatMapper).chatToChatDto(chat, chatDto);
	}

	// changeAdminStatus
	@Test
	void changeAdminStatusChatIdNullthrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.changeAdminStatus(null, 1L));
	}

	@Test
	void changeAdminStatusUserIdNullthrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.changeAdminStatus(1L, null));
	}

	@Test
	void changeAdminStatusChatUserNotFoundThrow() {
		Long chatId = 1L;
		Long userId = 1L;
		when(chatUserDao.findByChatChatIdAndUserUserId(chatId, userId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.changeAdminStatus(chatId, userId));
		verify(chatUserDao, never()).save(any(ChatUser.class));
	}

	@Test
	void changeAdminStatusAuthUserNoAdminThrow() {
		Long chatId = 1L;
		Long userId = 1L;
		ChatUser chatUserAuthUser = ChatUser.builder().user(user).admin(false).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUserAuthUser)).build();
		ChatUser chatUserFound = ChatUser.builder().chat(chat).admin(false) // at the end, should be true.
				.build();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		when(chatUserDao.findByChatChatIdAndUserUserId(chatId, userId)).thenReturn(Optional.of(chatUserFound));

		assertThrows(InvalidActionException.class, () -> chatService.changeAdminStatus(chatId, userId));

		verify(chatUserDao, never()).save(chatUserFound);
	}

	@Test
	void changeAdminStatusAuthUserNoInChat() {
		Long chatId = 1L;
		Long userId = 1L;
		ChatUser chatUserAuthUser = ChatUser.builder().user(new User()).admin(false).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUserAuthUser)).build();
		ChatUser chatUserFound = ChatUser.builder().chat(chat).admin(false) // at the end, should be true.
				.build();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(chatUserDao.findByChatChatIdAndUserUserId(chatId, userId)).thenReturn(Optional.of(chatUserFound));

		assertThrows(InvalidActionException.class, () -> chatService.changeAdminStatus(chatId, userId));

		verify(chatUserDao, never()).save(chatUserFound);
	}

	@Test
	void changeAdminStatusReturnsNotNull() {
		Long chatId = 1L;
		Long userId = 1L;
		Long messArr[] = { chatId, 2L };
		List<Long[]> listMessArr = new ArrayList<>();
		listMessArr.add(messArr);
		ChatUser chatUserAuthUser = ChatUser.builder().user(user).admin(true).build();
		Chat chat = Chat.builder().chatId(chatId).chatUsers(List.of(chatUserAuthUser)).build();
		ChatDto chatDto = ChatDto.builder().messagesNoWatched("2").build();
		ChatUser chatUserFound = ChatUser.builder().chat(chat).admin(false) // at the end, should be true.
				.build();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		when(chatUserDao.findByChatChatIdAndUserUserId(chatId, userId)).thenReturn(Optional.of(chatUserFound));
		// get messages not watched count.
		when(messageService.getMessagesNotWatchedCountByChatIds(List.of(chatId), user.getUsername()))
				.thenReturn(listMessArr);

		assertNotNull(chatService.changeAdminStatus(chatId, userId));

		chatUserFound.setAdmin(true);
		verify(chatUserDao).save(chatUserFound);
		verify(chatMapper).chatToChatDto(chat, chatDto);
	}
	
	//bynarySearchById
	@Test
	void bynarySearchByChatIdParamChatsNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.bynarySearchByChatId(null, 1L));
	}
	@Test
	void bynarySearchByChatIdParamChatsEmptyThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.bynarySearchByChatId(Collections.emptyList(), 1L));
	}
	@Test
	void bynarySearchByChatIdParamChatIdToFindNullThrow() {
		Chat chat = new Chat();
		List<Chat> chats = List.of(chat);
		assertThrows(IllegalArgumentException.class, () -> chatService.bynarySearchByChatId(chats, null));
	}
	
	@Test
	void bynarySearchByChatIdReturnCorrectIndex() {
		Chat chat1 = Chat.builder().chatId(1L).build();
		Chat chat2 = Chat.builder().chatId(2L).build();
		Chat chat3 = Chat.builder().chatId(3L).build();
		Chat chat4 = Chat.builder().chatId(4L).build();
		List<Chat> chats = List.of(chat1,chat2,chat3,chat4);
		assertEquals(1, chatService.bynarySearchByChatId(chats, 2L),"should return index 1");
	}
	
	@Test
	void bynarySearchByChatIdReturnCorrectIndex2Items() {
		Chat chat1 = Chat.builder().chatId(1L).build();
		Chat chat2 = Chat.builder().chatId(2L).build();
		List<Chat> chats = List.of(chat1,chat2);
		assertEquals(0, chatService.bynarySearchByChatId(chats, 1L),"should return index 0");
	}
	
	@Test
	void bynarySearchByChatIdReturnNegative1ChatNotFound() {
		Chat chat1 = Chat.builder().chatId(1L).build();
		Chat chat2 = Chat.builder().chatId(2L).build();
		Chat chat3 = Chat.builder().chatId(3L).build();
		Chat chat4 = Chat.builder().chatId(4L).build();
		List<Chat> chats = List.of(chat1,chat2,chat3,chat4);
		assertEquals(-1, chatService.bynarySearchByChatId(chats, 5L),"if chat was not found should return -1");
	}
}




















