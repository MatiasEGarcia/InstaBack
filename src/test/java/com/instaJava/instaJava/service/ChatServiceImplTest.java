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
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.ChatDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqChat;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.ChatTypeEnum;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.exception.UserNotApplicableForChatException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import jakarta.persistence.EntityNotFoundException;

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
	private MessagesUtils messUtils;
	@Mock
	private PageableUtils pageUtils;
	@Mock
	private ChatDao chatDao;
	@InjectMocks
	private ChatServiceImpl chatService;

	private final User user = User.builder().userId(1L).role(RolesEnum.ROLE_USER).build();

	// getAuthUserChats
	@Test
	void getAuthUserChatsParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.getAuthUserChats(null));
	}

	@Test
	void getAuthUserChatsParamPageInfoDtoSortDirNullThrow() {
		PageInfoDto page = PageInfoDto.builder().sortField("random").build();

		assertThrows(IllegalArgumentException.class, () -> chatService.getAuthUserChats(page));
	}

	@Test
	void getAuthUserChatsParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto page = PageInfoDto.builder().sortDir(Direction.ASC).build();

		assertThrows(IllegalArgumentException.class, () -> chatService.getAuthUserChats(page));
	}

	@Test
	void getAuthUserChatsReturnsNotNull() {
		PageInfoDto page = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(pageUtils.getPageable(page)).thenReturn(Pageable.unpaged());
		when(chatDao.findByUsersUserId(eq(user.getUserId()), any(Pageable.class))).thenReturn(Page.empty());

		assertNotNull(chatService.getAuthUserChats(page));

		verify(chatDao).findByUsersUserId(eq(user.getUserId()), any(Pageable.class));
	}

	// create
	@Test
	void createParamReqChatNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.create(null));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createParamReqChatUsersToAddNullThrow() {
		ReqChat reqChat = ReqChat.builder().usersToAddAsAdmins(Collections.emptyList()).type(ChatTypeEnum.PRIVATE)
				.build();
		assertThrows(IllegalArgumentException.class, () -> chatService.create(reqChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createParamReqChatTypeNullThrow() {
		List<String> emptyList = Collections.emptyList();
		ReqChat reqChat = ReqChat.builder().usersToAdd(emptyList).usersToAddAsAdmins(emptyList).build();
		assertThrows(IllegalArgumentException.class, () -> chatService.create(reqChat));
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createNoneUsersFoundByUsernameThrow() {
		List<String> emptyList = Collections.emptyList();
		ReqChat reqChat = ReqChat.builder().usersToAdd(emptyList).usersToAddAsAdmins(emptyList)
				.type(ChatTypeEnum.PRIVATE).build();
		when(userService.getByUsernameIn(emptyList)).thenReturn(Collections.emptyList());

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.create(reqChat));

		verify(userService).getByUsernameIn(emptyList);
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createAllUsersWasntFoundThrow() {
		// users' username to find.
		List<String> usersToAddComplete = List.of("username1", "username2");
		List<String> usersToAddAsAdminsComplete = List.of("username3", "username4");
		List<String> allUsersToFind = new ArrayList<>();
		allUsersToFind.addAll(usersToAddComplete);
		allUsersToFind.addAll(usersToAddAsAdminsComplete);
		// two users who where found.
		User user1 = User.builder().username("username1").build();
		User user2 = User.builder().username("username2").build();

		ReqChat reqChat = ReqChat.builder().usersToAdd(usersToAddComplete)
				.usersToAddAsAdmins(usersToAddAsAdminsComplete).type(ChatTypeEnum.PRIVATE).build();
		when(userService.getByUsernameIn(allUsersToFind)).thenReturn(List.of(user1, user2));

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.create(reqChat),
				"if one user wasn't found , should throw UserNotApplicableForChatException");

		verify(userService).getByUsernameIn(allUsersToFind);
		verify(chatDao, never()).save(any(Chat.class));
	}

	/**
	 * If one of the users to add in chat is not visible and the follow status
	 * authUser -> otherUser is rejected.
	 */
	@Test
	void createUserNotVisibleFollowStatusIsRejectedThrow() {
		// users' username to find.
		List<String> usersToAddComplete = List.of("username1");
		List<String> usersToAddAsAdminsComplete = List.of("username2");
		List<String> allUsersToFind = new ArrayList<>();
		allUsersToFind.addAll(usersToAddComplete);
		allUsersToFind.addAll(usersToAddAsAdminsComplete);
		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		ReqChat reqChat = ReqChat.builder().usersToAdd(usersToAddComplete)
				.usersToAddAsAdmins(usersToAddAsAdminsComplete).type(ChatTypeEnum.PRIVATE).build();
		when(userService.getByUsernameIn(allUsersToFind)).thenReturn(List.of(user1, user2));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.REJECTED);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.create(reqChat),
				"if one user to add in chat is not visible and its follow status authUser -> otherUser is rejected , should throw UserNotApplicableForChatException");

		verify(userService).getByUsernameIn(allUsersToFind);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createUserNotVisibleFollowStatusIsInProcessThrow() {
		// users' username to find.
		List<String> usersToAddComplete = List.of("username1");
		List<String> usersToAddAsAdminsComplete = List.of("username2");
		List<String> allUsersToFind = new ArrayList<>();
		allUsersToFind.addAll(usersToAddComplete);
		allUsersToFind.addAll(usersToAddAsAdminsComplete);
		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		ReqChat reqChat = ReqChat.builder().usersToAdd(usersToAddComplete)
				.usersToAddAsAdmins(usersToAddAsAdminsComplete).type(ChatTypeEnum.PRIVATE).build();
		when(userService.getByUsernameIn(allUsersToFind)).thenReturn(List.of(user1, user2));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.IN_PROCESS);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.create(reqChat),
				"if one user to add in chat is not visible and its follow status authUser -> otherUser is in process , should throw UserNotApplicableForChatException");

		verify(userService).getByUsernameIn(allUsersToFind);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createUserNotVisibleFollowStatusIsNotAskedThrow() {
		// users' username to find.
		List<String> usersToAddComplete = List.of("username1");
		List<String> usersToAddAsAdminsComplete = List.of("username2");
		List<String> allUsersToFind = new ArrayList<>();
		allUsersToFind.addAll(usersToAddComplete);
		allUsersToFind.addAll(usersToAddAsAdminsComplete);
		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		ReqChat reqChat = ReqChat.builder().usersToAdd(usersToAddComplete)
				.usersToAddAsAdmins(usersToAddAsAdminsComplete).type(ChatTypeEnum.PRIVATE).build();
		when(userService.getByUsernameIn(allUsersToFind)).thenReturn(List.of(user1, user2));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.NOT_ASKED);

		assertThrows(UserNotApplicableForChatException.class, () -> chatService.create(reqChat),
				"if one user to add in chat is not visible and its follow status authUser -> otherUser is not asked , should throw UserNotApplicableForChatException");

		verify(userService).getByUsernameIn(allUsersToFind);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao, never()).save(any(Chat.class));
	}

	@Test
	void createPrivateChatReturnsNotNull() {
		// users' username to find.
		List<String> usersToAddComplete = List.of("username1");
		List<String> usersToAddAsAdminsComplete = List.of("username2");
		List<String> allUsersToFind = new ArrayList<>();
		allUsersToFind.addAll(usersToAddComplete);
		allUsersToFind.addAll(usersToAddAsAdminsComplete);
		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		// chat which will be save.
		Chat chat = Chat.builder().build();

		ReqChat reqChat = ReqChat.builder().usersToAdd(usersToAddComplete)
				.usersToAddAsAdmins(usersToAddAsAdminsComplete).type(ChatTypeEnum.PRIVATE).build();

		when(userService.getByUsernameIn(allUsersToFind)).thenReturn(List.of(user1, user2));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.ACCEPTED);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(chatDao.save(any(Chat.class))).thenReturn(chat);

		assertNotNull(chatService.create(reqChat));

		verify(userService).getByUsernameIn(allUsersToFind);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao).save(any(Chat.class));
	}

	@Test
	void createGroupChatReturnsNotNull() {
		// users' username to find.
		List<String> usersToAddComplete = List.of("username1");
		List<String> usersToAddAsAdminsComplete = List.of("username2");
		List<String> allUsersToFind = new ArrayList<>();
		allUsersToFind.addAll(usersToAddComplete);
		allUsersToFind.addAll(usersToAddAsAdminsComplete);

		// two users who where found.
		User user1 = User.builder().userId(2L).username("username1").visible(false).build();
		User user2 = User.builder().username("username2").userId(3L).visible(true).build();

		// chat which will be save.
		Chat chat = Chat.builder().build();

		ReqChat reqChat = ReqChat.builder().name("randomGroupName").usersToAdd(usersToAddComplete)
				.usersToAddAsAdmins(usersToAddAsAdminsComplete).type(ChatTypeEnum.GROUP).build();

		when(userService.getByUsernameIn(allUsersToFind)).thenReturn(List.of(user1, user2));
		// search follow status with user1 , which is the one with visible false
		when(followService.getFollowStatusByFollowedId(user1.getUserId())).thenReturn(FollowStatus.ACCEPTED);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(chatDao.save(any(Chat.class))).thenReturn(chat);

		assertNotNull(chatService.create(reqChat));

		verify(userService).getByUsernameIn(allUsersToFind);
		verify(followService).getFollowStatusByFollowedId(user1.getUserId());
		verify(chatDao).save(any(Chat.class));
	}

	// setImage method
	@Test
	void setImageParamImageNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> chatService.setImage(null, anyLong()));
		verify(chatDao,never()).save(any(Chat.class));
	}

	@Test
	void setImageParamChatIdNullThrow() {
		// just as mock
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		assertThrows(IllegalArgumentException.class, () -> chatService.setImage(multipartFile, null));
		verify(chatDao,never()).save(any(Chat.class));

	}

	@Test
	void setImageChatNotFoundThrow() {
		// just as mock
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> chatService.setImage(multipartFile, anyLong()));
		verify(chatDao,never()).save(any(Chat.class));
	}

	@Test
	void setImageChatPrivateThrow() {
		Chat chat = Chat.builder()
				.type(ChatTypeEnum.PRIVATE)
				.build();
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.of(chat));
		assertThrows(InvalidException.class, () -> chatService.setImage(multipartFile, anyLong()),
				"If chat is private then cannot set an image");
		verify(chatDao,never()).save(any(Chat.class));
	}
	
	@Test
	void setImageReturnsNotNull() {
		Chat chat = Chat.builder()
				.type(ChatTypeEnum.GROUP)
				.build();
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "testing".getBytes());
		when(chatDao.findById(anyLong())).thenReturn(Optional.of(chat));
		when(chatDao.save(chat)).thenReturn(chat);
		assertNotNull(chatService.setImage(multipartFile, anyLong()));
		verify(chatDao).save(any(Chat.class));
	}
	
}
