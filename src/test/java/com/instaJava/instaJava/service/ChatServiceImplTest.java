package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.ChatDao;
import com.instaJava.instaJava.dao.ChatUserDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.ChatUser;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.ChatTypeEnum;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
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

	private final User user = User.builder().id(1L).username("Mati").role(RolesEnum.ROLE_USER).build();

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
		Chat chat = new Chat(chatId);
		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		assertNotNull(chatService.getById(chatId));
	}

	// getAuthUserChats
	@Test
	void getAuthUserChatsParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> chatService.getUserChats(null, user.getId(), user.getUsername()));
		verify(chatDao, never()).findByChatUsersUserId(eq(1L), any(Pageable.class));
	}

	@Test
	void getAuthUserChatsParamUserIdNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(20).build();
		assertThrows(IllegalArgumentException.class,
				() -> chatService.getUserChats(pageInfoDto, null, user.getUsername()));
		verify(chatDao, never()).findByChatUsersUserId(eq(1L), any(Pageable.class));
	}

	@Test
	void getAuthUserChatsParamUsernameNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(20).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.getUserChats(pageInfoDto, user.getId(), null));
		verify(chatDao, never()).findByChatUsersUserId(eq(1L), any(Pageable.class));
	}

	@Test
	void getAuthUserChatsNoContentThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(20).build();
		when(chatDao.findChatsByUser(eq(user.getId()), eq(user.getUsername()), any(Pageable.class)))
				.thenReturn(Page.empty());

		assertThrows(RecordNotFoundException.class,
				() -> chatService.getUserChats(pageInfoDto, user.getId(), user.getUsername()));
	}

	@Test
	void getAuthUserChatsReturnNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(20).build();
		Chat chat = new Chat();
		Page<Chat> chatPage = new PageImpl<>(List.of(chat));
		when(chatDao.findChatsByUser(eq(user.getId()), eq(user.getUsername()), any(Pageable.class)))
				.thenReturn(chatPage);
		assertNotNull(chatService.getUserChats(pageInfoDto, user.getId(), user.getUsername()));
	}

	// create

	@Test
	void createParamTypeNullThrow() {
		String name = "grupoRocio";
		User user = User.builder().id(5L).username("rocio").build();
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put(user.getUsername(), false);
		List<User> listUsersToAdd = List.of(user);

		assertThrows(IllegalArgumentException.class, () -> chatService.create(name, null, listUsersToAdd, mapAreAdmin));
		verify(chatDao, never()).save(null);
	}

	@Test
	void createParamListUsersToAddNullThrow() {
		ChatTypeEnum chatType = ChatTypeEnum.PRIVATE;
		String name = "grupoRocio";
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put("rocio", false);

		assertThrows(IllegalArgumentException.class, () -> chatService.create(name, chatType, null, mapAreAdmin));
		verify(chatDao, never()).save(null);
	}

	@Test
	void createParamMapAreAdminNullThrow() {
		ChatTypeEnum chatType = ChatTypeEnum.PRIVATE;
		String name = "grupoRocio";
		User user = User.builder().id(5L).username("rocio").build();
		List<User> listUsersToAdd = List.of(user);

		assertThrows(IllegalArgumentException.class, () -> chatService.create(name, chatType, listUsersToAdd, null));
		verify(chatDao, never()).save(null);
	}

	@Test
	void createPrivateChatWithMoreThan2Users() {
		ChatTypeEnum chatType = ChatTypeEnum.PRIVATE;
		String name = "grupoRocio";
		User user1 = User.builder().id(5L).username("rocio").build();
		User user2 = User.builder().id(3L).username("Franco").build();
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put(user1.getUsername(), false);
		mapAreAdmin.put(user2.getUsername(), true);
		List<User> listUsersToAdd = List.of(user1, user2);

		// AuthUser
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class,
				() -> chatService.create(name, chatType, listUsersToAdd, mapAreAdmin));
		verify(chatDao, never()).save(null);
	}

	@Test
	void createReturnNotNull() {
		ChatTypeEnum chatType = ChatTypeEnum.PRIVATE;
		String name = "grupoRocio";
		User user1 = User.builder().id(5L).username("rocio").build();
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put(user1.getUsername(), false);
		List<User> listUsersToAdd = List.of(user1);
		Chat chat = new Chat();

		// AuthUser
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(chatDao.save(any(Chat.class))).thenReturn(chat);

		assertNotNull(chatService.create(name, chatType, listUsersToAdd, mapAreAdmin));
	}

	// setImage method

	@Test
	void setImageParamImageNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.setImage(null, anyLong()));
	}

	@Test
	void setImageParamChatIdNullThrow() { // just as mock MultipartFile
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		assertThrows(IllegalArgumentException.class, () -> chatService.setImage(multipartFile, null));
	}

	@Test
	void setImageChatNotFoundThrow() { // just as mock MultipartFile
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.setImage(multipartFile, anyLong()));
	}

	@Test
	void setImageChatPrivateThrow() {
		Chat chat = Chat.builder().type(ChatTypeEnum.PRIVATE).build();
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.of(chat));
		assertThrows(InvalidActionException.class, () -> chatService.setImage(multipartFile, anyLong()),
				"If chat is private then cannot set an image");
	}

	@Test
	void setImageReturnsNotNull() {
		// messages not watched = 2
		Long chatId = 1L;
		Chat chat = Chat.builder().id(chatId).type(ChatTypeEnum.GROUP).build();

		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.of(chat));

		assertNotNull(chatService.setImage(multipartFile, chatId));
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
	}

	@Test
	void setChatNameAuthUserNotAdminThrow() {
		Long chatId = 1L;
		String chatName = "chatName";
		ChatUser chatUser = ChatUser.builder().user(user).admin(false).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		assertThrows(InvalidActionException.class, () -> chatService.setChatName(chatId, chatName));
	}

	@Test
	void setChatNameReturnsNotNull() {
		Long chatId = 1L;
		String chatName = "chatName";
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();
		Chat chat = Chat.builder().id(chatId).chatUsers(List.of(chatUser)).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		chat.setName(chatName);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertNotNull(chatService.setChatName(chatId, chatName));
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
	void deleteChatByIdReturnNotNull() {
		Long chatId = 1L;
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		assertNotNull(chatService.deleteChatById(chatId));
		verify(chatDao).delete(any(Chat.class));
	}

	// getAllUsersByChatId
	@Test
	void getAllUsersByChatIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.getAllUsersByChatId(null));
	}

	@Test
	void getAllUsersByChatIdChatNotFoundThrow() {
		Long chatId = 1L;
		when(chatDao.findById(chatId)).thenReturn(Optional.empty());

		assertThrows(RecordNotFoundException.class, () -> chatService.getAllUsersByChatId(chatId));
	}

	@Test
	void getAllUsersByChatIdReturnNotNull() {
		Long chatId = 1L;
		Chat chat = new Chat();
		ChatUser chatUser = ChatUser.builder().chat(chat).user(user).build();
		chat.setChatUsers(List.of(chatUser));
		// dao
		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// mapper
		when(chatMapper.chatUserListToUserList(chat.getChatUsers())).thenReturn(List.of(user));

		assertNotNull(chatService.getAllUsersByChatId(chatId));
	}

	// addUsers
	@Test
	void addUsersParamChatIdNullThrow() {
		User user1 = User.builder().id(2L).username("Rocio").build();
		List<User> listUsersToAdd = List.of(user1);
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put("Rocio", false);

		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(null, listUsersToAdd, mapAreAdmin));
	}

	@Test
	void addUsersParamListUsersToAddNullThrow() {
		Long chatId = 1L;
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put("Rocio", false);

		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(chatId, null, mapAreAdmin));
	}

	@Test
	void addUsersParamChatIdUsersToAddEmptyThrow() {
		Long chatId = 1L;
		List<User> listUsersToAdd = Collections.emptyList();
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put("Rocio", false);

		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(chatId, listUsersToAdd, mapAreAdmin));

	}

	@Test
	void addUsersParamMapAreAdminNullThrow() {
		Long chatId = 1L;
		User user1 = User.builder().id(2L).username("Rocio").build();
		List<User> listUsersToAdd = List.of(user1);

		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(chatId, listUsersToAdd, null));
	}

	@Test
	void addUsersParamMapAreAdminEmptyThrow() {
		Long chatId = 1L;
		User user1 = User.builder().id(2L).username("Rocio").build();
		List<User> listUsersToAdd = List.of(user1);
		Map<String, Boolean> mapAreAdmin = new HashMap<>();

		assertThrows(IllegalArgumentException.class, () -> chatService.addUsers(chatId, listUsersToAdd, mapAreAdmin));
	}

	@Test
	void addUsersChatNotFoundThrow() {
		Long chatId = 1L;
		User user1 = User.builder().id(2L).username("Rocio").build();
		List<User> listUsersToAdd = List.of(user1);
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put("Rocio", false);

		when(chatDao.findById(chatId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.addUsers(chatId, listUsersToAdd, mapAreAdmin));
	}

	@Test
	void addUsersAuthUserNotAdmin() {
		Long chatId = 1L;
		User user1 = User.builder().id(2L).username("Rocio").build();
		List<User> listUsersToAdd = List.of(user1);
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put("Rocio", false);
		Chat chat = new Chat(chatId);
		ChatUser chatUser = ChatUser.builder().chat(chat).user(user).admin(false).build();
		chat.setChatUsers(List.of(chatUser));

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		assertThrows(InvalidActionException.class, () -> chatService.addUsers(chatId, listUsersToAdd, mapAreAdmin));
	}

	@Test
	void addUsersReturnNotNull() {
		Long chatId = 1L;
		User user1 = User.builder().id(2L).username("Rocio").build();
		List<User> listUsersToAdd = List.of(user1);
		Map<String, Boolean> mapAreAdmin = new HashMap<>();
		mapAreAdmin.put("Rocio", false);
		Chat chat = new Chat(chatId);
		ChatUser chatUser = ChatUser.builder().chat(chat).user(user).admin(true).build();
		List<ChatUser> chatUserList = new ArrayList<>();
		chatUserList.add(chatUser);
		chat.setChatUsers(chatUserList);

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		assertNotNull(chatService.addUsers(chatId, listUsersToAdd, mapAreAdmin));
	}

	
	
	// quitUsersFromChatByUsername.
	@Test
	void quitUsersFromChatByUsernameParamChatIdNullThrow() {
		List<String> listUserUseraname = new ArrayList<>();
		listUserUseraname.add("rocio");
		assertThrows(IllegalArgumentException.class,
				() -> chatService.quitUsersFromChatByUsername(null, listUserUseraname));
	}

	@Test
	void quitUsersFromChatByUsernameParamListUserUsernameNullThrow() {
		Long chatId = 1L;
		assertThrows(IllegalArgumentException.class, () -> chatService.quitUsersFromChatByUsername(chatId, null));
	}

	@Test
	void quitUsersFromChatByUsernameParamListUserUsernameEmptyThrow() {
		Long chatId = 1L;
		List<String> listUserUseraname = new ArrayList<>();
		assertThrows(IllegalArgumentException.class,
				() -> chatService.quitUsersFromChatByUsername(chatId, listUserUseraname));
	}

	@Test
	void quitUsersFromChatByUsernameChatNotFoundThrow() {
		Long chatId = 1L;
		List<String> listUserUseraname = new ArrayList<>();
		listUserUseraname.add("rocio");

		when(chatDao.findById(chatId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class,
				() -> chatService.quitUsersFromChatByUsername(chatId, listUserUseraname));
	}

	@Test
	void quitUsersFromChatByUsernameAuthUserNotAdminThrow() {
		Long chatId = 1L;
		List<String> listUserUseraname = new ArrayList<>();
		listUserUseraname.add("rocio");
		ChatUser chatUser = ChatUser.builder().user(user).admin(false).build();
		Chat chat = Chat.builder().chatUsers(List.of(chatUser)).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class,
				() -> chatService.quitUsersFromChatByUsername(chatId, listUserUseraname));
	}

	@Test
	void quitUsersFromChatByUsernameReturnNotNull() {
		Long chatId = 1L;
		List<String> listUserUseraname = new ArrayList<>();
		listUserUseraname.add("rocio");
		ChatUser chatUser = ChatUser.builder().user(user).admin(true).build();
		List<ChatUser> chatUserList = List.of(chatUser);
		Chat chat = Chat.builder().chatUsers(chatUserList).build();

		when(chatDao.findById(chatId)).thenReturn(Optional.of(chat));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(chatUserDao.findByChatId(chatId)).thenReturn(chatUserList);

		assertNotNull(chatService.quitUsersFromChatByUsername(chatId, listUserUseraname));
	}

	// changeAdminStatus
	@Test
	void changeAdminStatusParamChatIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.changeAdminStatus(null, 1L));
	}

	@Test
	void changeAdminStatusParamUserIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.changeAdminStatus(1L, null));
	}

	@Test
	void changeAdminStatusChatUserNotFoundThrow() {
		Long chatId = 1L;
		Long userId = 1L;

		when(chatUserDao.findByChatIdAndUserId(chatId, userId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> chatService.changeAdminStatus(chatId, userId));
	}

	@Test
	void changeAdminStatusUserNoAdminThrow() {
		Long chatId = 1L;
		Long userId = 1L;
		Chat chat = new Chat();
		ChatUser chatUser = ChatUser.builder().chat(chat).user(user).admin(false).build();
		chat.setChatUsers(List.of(chatUser));

		when(chatUserDao.findByChatIdAndUserId(chatId, userId)).thenReturn(Optional.of(chatUser));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class, () -> chatService.changeAdminStatus(chatId, userId));
	}

	@Test
	void changeAdminStatusReturnNotNull() {
		Long chatId = 1L;
		Long userId = 1L;
		Chat chat = new Chat();
		ChatUser chatUser = ChatUser.builder().chat(chat).user(user).admin(true).build();
		chat.setChatUsers(List.of(chatUser));

		when(chatUserDao.findByChatIdAndUserId(chatId, userId)).thenReturn(Optional.of(chatUser));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertNotNull(chatService.changeAdminStatus(chatId, userId));
	}
}
