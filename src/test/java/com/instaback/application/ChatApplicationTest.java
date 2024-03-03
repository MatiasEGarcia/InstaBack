package com.instaback.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaback.dto.ChatDto;
import com.instaback.dto.UserDto;
import com.instaback.dto.request.ReqAddUserChat;
import com.instaback.dto.request.ReqCreateChat;
import com.instaback.dto.request.ReqDelUserFromChat;
import com.instaback.dto.request.ReqUserChat;
import com.instaback.entity.Chat;
import com.instaback.entity.User;
import com.instaback.enums.ChatTypeEnum;
import com.instaback.enums.FollowStatus;
import com.instaback.enums.RolesEnum;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.exception.UserNotApplicableForChatException;
import com.instaback.mapper.ChatMapper;
import com.instaback.mapper.UserMapper;
import com.instaback.service.ChatService;
import com.instaback.service.FollowService;
import com.instaback.service.MessageService;
import com.instaback.service.UserService;
import com.instaback.util.MessagesUtils;
import com.instaback.util.SearchsUtils;

@ExtendWith(MockitoExtension.class)
class ChatApplicationTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private ChatService chService;
	@Mock
	private MessageService mService;
	@Mock
	private FollowService fService;
	@Mock
	private UserService uService;
	@Mock
	private ChatMapper chMapper;
	@Mock
	private UserMapper uMapper;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private SearchsUtils searchUtils;
	@InjectMocks
	ChatApplicationImpl chatApplication;

	private final User authUser = User.builder().id(1L).username("Mati").role(RolesEnum.ROLE_USER).build();

	// getAuhtUserChats
	@Test
	void getAuhtUserChatsReturnsNotNull() {
		// pending
	}

	// create
	@Test
	void createParamReqCreateChatNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatApplication.create(null));
		verify(chService, never()).create(null, null, null, null);
	}

	@Test
	void createReqCreateChatUsersToAddNullThrow() {
		ReqCreateChat reqCreateChat = ReqCreateChat.builder().name("random").type(ChatTypeEnum.PRIVATE).build();
		assertThrows(IllegalArgumentException.class, () -> chatApplication.create(reqCreateChat));
		verify(chService, never()).create(null, null, null, null);
	}

	@Test
	void createReqCreateChatUsersToAddEmptyThrow() {
		ReqCreateChat reqCreateChat = ReqCreateChat.builder().name("random").type(ChatTypeEnum.PRIVATE)
				.usersToAdd(Collections.emptyList()).build();
		assertThrows(IllegalArgumentException.class, () -> chatApplication.create(reqCreateChat));
		verify(chService, never()).create(null, null, null, null);
	}

	@Test
	void createAllNotFoundThrow() {
		User onlyUserFound = User.builder().username("Rocio").build();
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		ReqUserChat reqUserChat2 = new ReqUserChat("Franco", false);
		ReqCreateChat reqCreateChat = ReqCreateChat.builder().name("random").type(ChatTypeEnum.PRIVATE)
				.usersToAdd(List.of(reqUserChat1, reqUserChat2)).build();
		List<User> listUsersToAdd = List.of(onlyUserFound);

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// check if all the possible additions are found.
		when(uService.getByUsernameIn(anySet())).thenReturn(listUsersToAdd);

		assertThrows(RecordNotFoundException.class, () -> chatApplication.create(reqCreateChat));
		verify(chService, never()).create(null, null, null, null);
	}

	@Test
	void createUserFoundVisibleFalseFollowStatusInProcessThrow() {
		User userFoundVisibleFalse = User.builder().id(1L).username("Rocio").visible(false).build();
		FollowStatus fStatus = FollowStatus.IN_PROCESS;
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		ReqCreateChat reqCreateChat = ReqCreateChat.builder().name("random").type(ChatTypeEnum.PRIVATE)
				.usersToAdd(List.of(reqUserChat1)).build();
		List<User> listUsersToAdd = List.of(userFoundVisibleFalse);

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// check if all the possible additions are found.
		when(uService.getByUsernameIn(anySet())).thenReturn(listUsersToAdd);
		// check follow status between auth user and user to add in chat.
		when(fService.getFollowStatusByFollowedId(anyLong())).thenReturn(fStatus);
		assertThrows(UserNotApplicableForChatException.class, () -> chatApplication.create(reqCreateChat));
		verify(chService, never()).create(null, null, null, null);
	}

	@Test
	void createUserFoundVisibleFalseFollowStatusNotAskedThrow() {
		User userFoundVisibleFalse = User.builder().id(1L).username("Rocio").visible(false).build();
		FollowStatus fStatus = FollowStatus.NOT_ASKED;
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		ReqCreateChat reqCreateChat = ReqCreateChat.builder().name("random").type(ChatTypeEnum.PRIVATE)
				.usersToAdd(List.of(reqUserChat1)).build();
		List<User> listUsersToAdd = List.of(userFoundVisibleFalse);

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// check if all the possible additions are found.
		when(uService.getByUsernameIn(anySet())).thenReturn(listUsersToAdd);
		// check follow status between auth user and user to add in chat.
		when(fService.getFollowStatusByFollowedId(anyLong())).thenReturn(fStatus);
		assertThrows(UserNotApplicableForChatException.class, () -> chatApplication.create(reqCreateChat));
		verify(chService, never()).create(null, null, null, null);
	}

	@Test
	void createUserFoundVisibleFalseFollowStatusRejectedThrow() {
		User userFoundVisibleFalse = User.builder().id(1L).username("Rocio").visible(false).build();
		FollowStatus fStatus = FollowStatus.REJECTED;
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		ReqCreateChat reqCreateChat = ReqCreateChat.builder().name("random").type(ChatTypeEnum.PRIVATE)
				.usersToAdd(List.of(reqUserChat1)).build();
		List<User> listUsersToAdd = List.of(userFoundVisibleFalse);

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// check if all the possible additions are found.
		when(uService.getByUsernameIn(anySet())).thenReturn(listUsersToAdd);
		// check follow status between auth user and user to add in chat.
		when(fService.getFollowStatusByFollowedId(anyLong())).thenReturn(fStatus);
		assertThrows(UserNotApplicableForChatException.class, () -> chatApplication.create(reqCreateChat));
		verify(chService, never()).create(null, null, null, null);
	}

	@Test
	void createUserFoundVisibleFalseFollowStatusAcceptedReturnNotNull() {
		User userFoundVisibleFalse = User.builder().id(1L).username("Rocio").visible(false).build();
		FollowStatus fStatus = FollowStatus.ACCEPTED;
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		ReqCreateChat reqCreateChat = ReqCreateChat.builder().name("random").type(ChatTypeEnum.PRIVATE)
				.usersToAdd(List.of(reqUserChat1)).build();
		List<User> listUsersToAdd = List.of(userFoundVisibleFalse);
		Chat chatCreated = new Chat();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// check if all the possible additions are found.
		when(uService.getByUsernameIn(anySet())).thenReturn(listUsersToAdd);
		// check follow status between auth user and user to add in chat.
		when(fService.getFollowStatusByFollowedId(anyLong())).thenReturn(fStatus);
		when(chService.create(eq(reqCreateChat.getName()), eq(reqCreateChat.getType()), eq(listUsersToAdd), anyMap()))
				.thenReturn(chatCreated);
		// mapper
		when(chMapper.chatToChatDto(chatCreated)).thenReturn(new ChatDto());

		assertNotNull(chatApplication.create(reqCreateChat));
	}

	@Test
	void createUserFoundVisibleTrueReturnNotNull() {
		User userFoundVisibleFalse = User.builder().id(1L).username("Rocio").visible(true).build();
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		ReqCreateChat reqCreateChat = ReqCreateChat.builder().name("random").type(ChatTypeEnum.PRIVATE)
				.usersToAdd(List.of(reqUserChat1)).build();
		List<User> listUsersToAdd = List.of(userFoundVisibleFalse);
		Chat chatCreated = new Chat();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// check if all the possible additions are found.
		when(uService.getByUsernameIn(anySet())).thenReturn(listUsersToAdd);
		when(chService.create(eq(reqCreateChat.getName()), eq(reqCreateChat.getType()), eq(listUsersToAdd), anyMap()))
				.thenReturn(chatCreated);
		// mapper
		when(chMapper.chatToChatDto(chatCreated)).thenReturn(new ChatDto());

		assertNotNull(chatApplication.create(reqCreateChat));
	}

	// setImage
	@Test
	void setImageReturnNotNull() {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE,
				"Hello, World!".getBytes());
		Long chatId = 1L;
		Chat chatUpdated = new Chat(chatId);
		Long[] countMessages = { 1L, 2L };
		List<Long[]> listCountMessages = new ArrayList<>();
		listCountMessages.add(countMessages);

		// updating chat
		when(chService.setImage(img, chatId)).thenReturn(chatUpdated);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// getting messages not watched
		when(mService.getMessagesNotWatchedCountByChatIds(List.of(chatId), authUser.getUsername()))
				.thenReturn(listCountMessages);

		assertNotNull(chatApplication.setImage(img, chatId));

		verify(chMapper).chatToChatDto(eq(chatUpdated), any(ChatDto.class));
	}

	// setChatName
	@Test
	void setChatNameReturnNotNull() {
		Long chatId = 1L;
		String chatName = "random";
		Chat chatUpdated = new Chat(chatId);
		Long[] countMessages = { 1L, 2L };
		List<Long[]> listCountMessages = new ArrayList<>();
		listCountMessages.add(countMessages);

		// updating chat
		when(chService.setChatName(chatId, chatName)).thenReturn(chatUpdated);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// getting messages not watched
		when(mService.getMessagesNotWatchedCountByChatIds(List.of(chatId), authUser.getUsername()))
				.thenReturn(listCountMessages);

		assertNotNull(chatApplication.setChatName(chatId, chatName));
		verify(chMapper).chatToChatDto(eq(chatUpdated), any(ChatDto.class));
	}

	// deleteChatById
	@Test
	void deleteChatByIdReturnNotNull() {
		Long chatId = 1L;
		Chat chatDeleted = new Chat();
		when(chService.deleteChatById(chatId)).thenReturn(chatDeleted);
		when(chMapper.chatToChatDto(chatDeleted)).thenReturn(new ChatDto());
		assertNotNull(chatApplication.deleteChatById(chatId));
	}

	// getAllUsersByChatId
	@Test
	void getAllUsersByChatIdReturnNotNull() {
		Long chatId = 1l;
		List<User> userList = new ArrayList<>();
		List<UserDto> userDtoList = new ArrayList<>();
		when(chService.getAllUsersByChatId(chatId)).thenReturn(userList);
		when(uMapper.userListToUserDtoList(userList)).thenReturn(userDtoList);

		assertNotNull(chatApplication.getAllUsersByChatId(chatId));

	}

	// addUsers
	@Test
	void addUsersReqAddUserChatNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatApplication.addUsers(null));
	}

	@Test
	void addUsersReqAddUserChatUsersNullThrow() {
		ReqAddUserChat req = new ReqAddUserChat();
		assertThrows(IllegalArgumentException.class, () -> chatApplication.addUsers(req));
	}

	@Test
	void addUsersNotAllUsersFoundThrow() {
		String chatIdString = "1";
		User userFound = User.builder().id(1L).username("Rocio").visible(true).build();
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		ReqUserChat reqUserChat2 = new ReqUserChat("Franco", false);
		List<ReqUserChat> reqUserChatList = List.of(reqUserChat1, reqUserChat2);
		List<User> usersFound = new ArrayList<>();
		usersFound.add(userFound);
		ReqAddUserChat req = new ReqAddUserChat(chatIdString, reqUserChatList);

		when(uService.getByUsernameIn(anySet())).thenReturn(usersFound);
		assertThrows(RecordNotFoundException.class, () -> chatApplication.addUsers(req));

		verify(chService, never()).addUsers(eq(Long.parseLong(chatIdString)), eq(usersFound), anyMap());
	}

	@Test
	void addUsersVisibleFalseStatusInProcessThrow() {
		String chatIdString = "1";
		FollowStatus fStatus = FollowStatus.IN_PROCESS;
		User userFound = User.builder().id(1L).username("Rocio").visible(false).build();
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		List<ReqUserChat> reqUserChatList = List.of(reqUserChat1);
		List<User> usersFound = new ArrayList<>();
		usersFound.add(userFound);
		ReqAddUserChat req = new ReqAddUserChat(chatIdString, reqUserChatList);

		when(uService.getByUsernameIn(anySet())).thenReturn(usersFound);
		when(fService.getFollowStatusByFollowedId(userFound.getId())).thenReturn(fStatus);

		assertThrows(UserNotApplicableForChatException.class, () -> chatApplication.addUsers(req));

		verify(chService, never()).addUsers(eq(Long.parseLong(chatIdString)), eq(usersFound), anyMap());
	}

	@Test
	void addUsersVisibleFalseStatusNotAskedThrow() {
		String chatIdString = "1";
		FollowStatus fStatus = FollowStatus.NOT_ASKED;
		User userFound = User.builder().id(1L).username("Rocio").visible(false).build();
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		List<ReqUserChat> reqUserChatList = List.of(reqUserChat1);
		List<User> usersFound = new ArrayList<>();
		usersFound.add(userFound);
		ReqAddUserChat req = new ReqAddUserChat(chatIdString, reqUserChatList);

		when(uService.getByUsernameIn(anySet())).thenReturn(usersFound);
		when(fService.getFollowStatusByFollowedId(userFound.getId())).thenReturn(fStatus);

		assertThrows(UserNotApplicableForChatException.class, () -> chatApplication.addUsers(req));

		verify(chService, never()).addUsers(eq(Long.parseLong(chatIdString)), eq(usersFound), anyMap());
	}

	@Test
	void addUsersVisibleFalseStatusRejectedThrow() {
		String chatIdString = "1";
		FollowStatus fStatus = FollowStatus.REJECTED;
		User userFound = User.builder().id(1L).username("Rocio").visible(false).build();
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		List<ReqUserChat> reqUserChatList = List.of(reqUserChat1);
		List<User> usersFound = new ArrayList<>();
		usersFound.add(userFound);
		ReqAddUserChat req = new ReqAddUserChat(chatIdString, reqUserChatList);

		when(uService.getByUsernameIn(anySet())).thenReturn(usersFound);
		when(fService.getFollowStatusByFollowedId(userFound.getId())).thenReturn(fStatus);

		assertThrows(UserNotApplicableForChatException.class, () -> chatApplication.addUsers(req));

		verify(chService, never()).addUsers(eq(Long.parseLong(chatIdString)), eq(usersFound), anyMap());
	}

	@Test
	void addUsersVisibleFalseStatusAcceptedReturnNotNull() {
		String chatIdString = "1";
		Long chatIdLong = 1L;
		FollowStatus fStatus = FollowStatus.ACCEPTED;
		User userFound = User.builder().id(1L).username("Rocio").visible(false).build();
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		List<ReqUserChat> reqUserChatList = List.of(reqUserChat1);
		List<User> usersFound = new ArrayList<>();
		usersFound.add(userFound);
		ReqAddUserChat req = new ReqAddUserChat(chatIdString, reqUserChatList);
		Chat chatUdpated = new Chat(chatIdLong);
		Long[] countMessagesA = { chatIdLong, 1L };
		List<Long[]> countMessagesList = new ArrayList<>();
		countMessagesList.add(countMessagesA);

		// getting users.
		when(uService.getByUsernameIn(anySet())).thenReturn(usersFound);
		// checking if can be added.
		when(fService.getFollowStatusByFollowedId(userFound.getId())).thenReturn(fStatus);
		// adding users.
		when(chService.addUsers(eq(chatIdLong), eq(usersFound), anyMap())).thenReturn(chatUdpated);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// get messages not watched
		when(mService.getMessagesNotWatchedCountByChatIds(List.of(chatIdLong), authUser.getUsername()))
				.thenReturn(countMessagesList);

		assertNotNull(chatApplication.addUsers(req));
		verify(chMapper).chatToChatDto(eq(chatUdpated), any(ChatDto.class));
	}

	@Test
	void addUsersVisibleTrueReturnNotNull() {
		String chatIdString = "1";
		Long chatIdLong = 1L;
		User userFound = User.builder().id(1L).username("Rocio").visible(true).build();
		ReqUserChat reqUserChat1 = new ReqUserChat("Rocio", true);
		List<ReqUserChat> reqUserChatList = List.of(reqUserChat1);
		List<User> usersFound = new ArrayList<>();
		usersFound.add(userFound);
		ReqAddUserChat req = new ReqAddUserChat(chatIdString, reqUserChatList);
		Chat chatUdpated = new Chat(chatIdLong);
		Long[] countMessagesA = { chatIdLong, 1L };
		List<Long[]> countMessagesList = new ArrayList<>();
		countMessagesList.add(countMessagesA);

		// getting users.
		when(uService.getByUsernameIn(anySet())).thenReturn(usersFound);
		// adding users.
		when(chService.addUsers(eq(chatIdLong), eq(usersFound), anyMap())).thenReturn(chatUdpated);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// get messages not watched
		when(mService.getMessagesNotWatchedCountByChatIds(List.of(chatIdLong), authUser.getUsername()))
				.thenReturn(countMessagesList);

		assertNotNull(chatApplication.addUsers(req));
		verify(chMapper).chatToChatDto(eq(chatUdpated), any(ChatDto.class));
	}

	// quitUsersFromChat
	@Test
	void quitUsersFromChatReqDelUserFromChatNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatApplication.quitUsersFromChat(null));
	}

	@Test
	void quitUsersFromChatReqDelUserFromChatChatIdNullThrow() {
		ReqDelUserFromChat req = new ReqDelUserFromChat();
		assertThrows(IllegalArgumentException.class, () -> chatApplication.quitUsersFromChat(req));
	}

	@Test
	void quitUsersFromChatReturnNotNull() {
		String chatId = "1";
		Long chatIdLong = 1L;
		Long[] countMessagesA = { chatIdLong, 1L };
		List<Long[]> countMessagesList = new ArrayList<>();
		countMessagesList.add(countMessagesA);
		ReqDelUserFromChat req = new ReqDelUserFromChat(chatId, Collections.emptyList());
		Chat chatUpdated = new Chat(1L);
		// quiting users.
		when(chService.quitUsersFromChatByUsername(eq(chatIdLong), anyList())).thenReturn(chatUpdated);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// get messages not watched
		when(mService.getMessagesNotWatchedCountByChatIds(List.of(chatIdLong), authUser.getUsername()))
				.thenReturn(countMessagesList);

		assertNotNull(chatApplication.quitUsersFromChat(req));
		verify(chMapper).chatToChatDto(eq(chatUpdated), any(ChatDto.class));
	}

	// changeAdminStatus
	@Test
	void changeAdminStatus() {
		Long chatId = 1L;
		Long userId = 1L;
		Chat chatUpdated = new Chat(chatId);
		Long[] countMessagesA = { chatId, 1L };
		List<Long[]> countMessagesList = new ArrayList<>();
		countMessagesList.add(countMessagesA);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(chService.changeAdminStatus(chatId, userId)).thenReturn(chatUpdated);

		assertNotNull(chatApplication.changeAdminStatus(chatId, userId));
		
		verify(chMapper).chatToChatDto(eq(chatUpdated), any(ChatDto.class));

		
	}

}
