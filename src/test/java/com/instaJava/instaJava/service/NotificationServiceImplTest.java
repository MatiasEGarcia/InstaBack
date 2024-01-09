package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.NotificationDao;
import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.NotificationChatDto;
import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.ChatUser;
import com.instaJava.instaJava.entity.Comment;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.NotificationType;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.ChatMapper;
import com.instaJava.instaJava.mapper.NotificationMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Clock clock;
	@Mock
	private NotificationDao notiDao;
	@Mock
	private SimpMessagingTemplate messTemplate;
	@Mock
	private UserMapper userMapper;
	@Mock
	private NotificationMapper notiMapper;
	@Mock
	private ChatMapper chatMapper;
	@Mock
	private PageableUtils pageUtils;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private SpecificationService<Notification> specService;
	@InjectMocks
	private NotificationServiceImpl notiService;
	// As auth user
	private final User user = User.builder().userId(1L).username("random").password("random").role(RolesEnum.ROLE_USER)
			.build();

	// saveNotificationOfFollow
	@Test
	void saveNotificationOfFollowParamFollowNullThorw() {
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfFollow(null, "some message"));
	}

	@Test
	void saveNotificationOfFollowParamcustomMessageNullThorw() {
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfFollow(new Follow(), null));
	}

	@Test
	void saveNotificationOfFollow() {
		User followerUser = User.builder().userId(1L).build();
		User followedUser = User.builder().userId(2L).build();
		Notification notiSaved = Notification.builder().notiId(1L).build();
		Follow follow = Follow.builder().followed(followedUser).follower(followerUser).build();
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		when(userMapper.userToUserDto(followerUser)).thenReturn(new UserDto());
		when(notiDao.save(any(Notification.class))).thenReturn(notiSaved);

		notiService.saveNotificationOfFollow(follow, "some message");

		verify(notiDao).save(any(Notification.class));
		verify(messTemplate).convertAndSendToUser(eq(followedUser.getUserId().toString()), eq("/private"),
				any(NotificationDto.class));
	}

	// saveNotificationOfMessage
	@Test
	void saveNotificationOfMessageParamChatDtoNullThrow() {
		MessageDto message = MessageDto.builder()
				.body("random")
				.build();
		assertThrows(IllegalArgumentException.class,
				() -> notiService.saveNotificationOfMessage(null, message));
	}
	
	@Test
	void saveNotificationOfMessageParamChatUsersEmptyThrow() {
		MessageDto message = MessageDto.builder()
				.body("random")
				.build();
		Chat chat = Chat.builder().chatUsers(Collections.emptyList()).build();
		assertThrows(IllegalArgumentException.class,
				() -> notiService.saveNotificationOfMessage(chat, message));
	}

	@Test
	void saveNotificationOfMessageParamMessageDtoNullThrow() {
		ChatUser chatUser = ChatUser.builder().user(user).build();
		Chat chat = Chat.builder()
				.chatUsers(List.of(chatUser))
				.build();
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfMessage(chat, null));
	}

	@Test
	void saveNotificationOfMessageParamMessageDtoBodyNullThrow() {
		ChatUser chatUser = ChatUser.builder().user(user).build();
		Chat chat = Chat.builder()
				.chatUsers(List.of(chatUser))
				.build();
		MessageDto message = new MessageDto();
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfMessage(chat, message));
	}
	@Test
	void saveNotificationOfMessageParamMessageDtoBodyBlankThrow() {
		ChatUser chatUser = ChatUser.builder().user(user).build();
		Chat chat = Chat.builder()
				.chatUsers(List.of(chatUser))
				.build();
		MessageDto message = MessageDto.builder()
				.body("")
				.build();
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfMessage(chat, message));
	}

	@Test
	void saveNotificationOfMessage() {
		List<String> destinationValues;
		Long userId2 = 2L;
		Long userId3 = 3L;
		UserDto userDto2 = new UserDto("2");
		UserDto userDto3 = new UserDto("3");
		User user2 = new User(userId2);
		User user3 = new User(userId3);
		ChatUser chatUser = ChatUser.builder().user(user).build();//same user than authenticated user, so, it shouldn't get a notif
		ChatUser chatUser2 = ChatUser.builder().user(user2).build();
		ChatUser chatUser3 = ChatUser.builder().user(user3).build();
		Chat chat = Chat.builder()
				.chatUsers(List.of(chatUser,chatUser2,chatUser3))
				.build();
		MessageDto messageDto = MessageDto.builder()
				.body("randomMessage")
				.build();
		NotificationDto notiDto2 = NotificationDto.builder()
				.toWho(userDto2)
				.build();
		NotificationDto notiDto3 = NotificationDto.builder()
				.toWho(userDto3)
				.build();
		List<NotificationDto> notificationListDto = List.of(notiDto2,notiDto3);
		List<Notification> notificationList;
		Notification notification1;
		Notification notification2;

		//argument captor for socket destination
		ArgumentCaptor<String> argumentDestinationCapture = ArgumentCaptor.forClass(String.class);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// clock
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		
		//notifications which will be saved. For userId == 1 there should not be a notification.
		notification1 = Notification.builder()
				.fromWho(user)
				.toWho(new User(userId2))
				.createdAt(ZonedDateTime.now(clock))
				.type(NotificationType.MESSAGE)
				.build();
		notification2 = Notification.builder()
				.fromWho(user)
				.toWho(new User(userId3))
				.createdAt(ZonedDateTime.now(clock))
				.type(NotificationType.MESSAGE)
				.build();
		notificationList = List.of(notification1 ,notification2);
		
		// dao
		when(notiDao.saveAll(notificationList)).thenReturn(Collections.emptyList());
		//mappers
		when(notiMapper.notificationListToNotificationDtoListWithToWho(anyList())).thenReturn(notificationListDto);
		when(chatMapper.chatToChatDto(chat)).thenReturn(new ChatDto());
		
		notiService.saveNotificationOfMessage(chat, messageDto);
		
		//verify
		verify(notiDao).saveAll(anyList());
		verify(messTemplate,times(2)).convertAndSend(argumentDestinationCapture.capture(), any(NotificationChatDto.class));
		verify(messTemplate, times(2)).convertAndSendToUser(argumentDestinationCapture.capture(), eq("/private"), any(NotificationDto.class));
		
		//checking socket destinations.
		destinationValues = argumentDestinationCapture.getAllValues();
		assertFalse(destinationValues.contains("/chat/1"), "auhtenticated user don't need a notification for a new message");
		assertTrue(destinationValues.contains("/chat/2"));
		assertTrue(destinationValues.contains("/chat/3"));
		assertFalse(destinationValues.contains("1"),"auhtenticated user don't need a notification for a new message");
		assertTrue(destinationValues.contains("2"));
		assertTrue(destinationValues.contains("3"));
	}

	//saveNotificationOfComment
	@Test
	void saveNotificationOfCommentParamCommentOwnerUserNullThrow() {	
		PublicatedImage associatedImg = PublicatedImage.builder()
				.userOwner(user)
				.build();
		//comment without owner user.
		Comment comment = Comment.builder()
				.associatedImg(associatedImg)
				.build();
		
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfComment(comment, "randomMessage"));
	}
	
	@Test
	void saveNotificationOfCommentParamCommentOwnerUserIdNullThrow() {	
		PublicatedImage associatedImg = PublicatedImage.builder()
				.userOwner(user)
				.build();		
		//comment with user without id.
		Comment comment = Comment.builder()
				.associatedImg(associatedImg)
				.ownerUser(new User())
				.build();
		
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfComment(comment, "randomMessage"));
	}
	
	@Test
	void saveNotificationOfCommentParamCommentWithoutAssociatedImgNullThrow() {	
		//comment without associatedImage.
		Comment comment = Comment.builder()
				.ownerUser(new User(5L))
				.build();
		
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfComment(comment, "randomMessage"));
	}
	
	@Test
	void saveNotificationOfCommentParamCommentAssociatedImgWithoutUserNullThrow() {	
		//comment with user without id.
		Comment comment = Comment.builder()
				.associatedImg(new PublicatedImage())
				.ownerUser(new User(5L))
				.build();
		
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfComment(comment, "randomMessage"));
	}
	
	@Test
	void saveNotificationOfCommentParamCommentAssociatedImgUserOwnerIdNullThrow() {	
		PublicatedImage associatedImg = PublicatedImage.builder()
				.userOwner(new User())
				.build();		
		//comment with user without id.
		Comment comment = Comment.builder()
				.associatedImg(associatedImg)
				.ownerUser(new User(5L))
				.build();
		
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfComment(comment, "randomMessage"));
	}
	
	@Test
	void saveNotificationOfCommentParamCustomMessageNullThrow() {	
		PublicatedImage associatedImg = PublicatedImage.builder()
				.userOwner(user)
				.build();		
		//comment with user without id.
		Comment comment = Comment.builder()
				.associatedImg(associatedImg)
				.ownerUser(new User(5L))
				.build();
		
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfComment(comment, null));
	}
	
	@Test
	void saveNotificationOfCommentParamCustomMessageBlankThrow() {	
		PublicatedImage associatedImg = PublicatedImage.builder()
				.userOwner(user)
				.build();		
		//comment with user without id.
		Comment comment = Comment.builder()
				.associatedImg(associatedImg)
				.ownerUser(new User(5L))
				.build();
		
		assertThrows(IllegalArgumentException.class, () -> notiService.saveNotificationOfComment(comment, ""));
	}
	
	@Test
	void saveNotification() {	
		PublicatedImage associatedImg = PublicatedImage.builder()
				.userOwner(user)
				.build();		
		//comment with user without id.
		Comment comment = Comment.builder()
				.associatedImg(associatedImg)
				.ownerUser(new User(5L))
				.build();
		UserDto userDto = UserDto.builder().userId(user.getUserId().toString()).build();
		Notification noti = Notification.builder().toWho(user).build();
		NotificationDto notiDto = NotificationDto.builder()
				.toWho(userDto)
				.build();
		
		//clock
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		//dao
		when(notiDao.save(any(Notification.class))).thenReturn(noti);
		//mapper
		when(notiMapper.notificationToNotificationDtoWithToWho(noti)).thenReturn(notiDto);
		
		notiService.saveNotificationOfComment(comment, "customMessage");
		
		verify(messTemplate).convertAndSendToUser(notiDto.getToWho().getUserId(),"/private" , notiDto);
	}
	
	
	// getNotificationByAuthUser
	@Test
	void getNotificationsByAuthUserParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> notiService.getNotificationsByAuthUser(null));
	}

	@Test
	void getNotificationsByAuthUserParamPageInfoDtoSortDirNullThrow() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(null).sortField("random").build();
		assertThrows(IllegalArgumentException.class, () -> notiService.getNotificationsByAuthUser(pag));
	}

	@Test
	void getNotificationsByAuthUserParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(Direction.ASC).sortField(null).build();
		assertThrows(IllegalArgumentException.class, () -> notiService.getNotificationsByAuthUser(pag));
	}

	@Test
	void getNotificationsByAuthUserPageEmptyThrow() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(Direction.ASC).sortField("random").build();
		Pageable pageable = Pageable.unpaged();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// pageable
		when(pageUtils.getPageable(pag)).thenReturn(pageable);
		// dao
		when(notiDao.findByToWhoUserId(user.getUserId(), pageable)).thenReturn(Page.empty());

		assertThrows(RecordNotFoundException.class, () -> notiService.getNotificationsByAuthUser(pag));
	}

	@Test
	void getNotificationsByAuthUserReturnsNotNull() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(Direction.ASC).sortField("random").build();
		Pageable pageable = Pageable.unpaged();
		Notification noti = new Notification();
		Page<Notification> page = new PageImpl<>(List.of(noti));
		ResPaginationG<NotificationDto> resPagG = new ResPaginationG<NotificationDto>();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// pageable
		when(pageUtils.getPageable(pag)).thenReturn(pageable);
		// dao
		when(notiDao.findByToWhoUserId(user.getUserId(), pageable)).thenReturn(page);
		// mapper
		when(notiMapper.pageAndPageInfoDtoToResPaginationG(page, pag)).thenReturn(resPagG);

		assertNotNull(notiService.getNotificationsByAuthUser(pag));
	}

	// deleteById
	@Test
	void deleteNotificationByIdNotiIdParamNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> notiService.deleteNotificationById(null));
	}

	@Test
	void deleteNotificationByIdNotSameReceptorAndAuthUserThrow() {
		Notification notiToDelete = Notification.builder().toWho(new User()).build();// different user.

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		NotificationServiceImpl serviceSpy = spy(notiService);

		doReturn(Optional.of(notiToDelete)).when(serviceSpy).findNotificationById(any(Long.class));

		assertThrows(InvalidActionException.class, () -> serviceSpy.deleteNotificationById(1L));
		verify(notiDao, never()).delete(notiToDelete);
	}

	@Test
	void deleteNotificationById() {
		Notification notiToDelete = Notification.builder().toWho(user).build();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		NotificationServiceImpl serviceSpy = spy(notiService);

		doReturn(Optional.of(notiToDelete)).when(serviceSpy).findNotificationById(any(Long.class));

		serviceSpy.deleteNotificationById(1L);
		verify(notiDao).delete(notiToDelete);
	}

	//deleteAllByAuthUser
	@Test
	void deleteAllByAuthUser() {
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		
		notiService.deleteAllByAuthUser();
		
		verify(notiDao).deleteAllByToWho(user);
	}
	
	
	// getNotificationById
	@Test
	void getNotificationByIdNotiIdParamNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> notiService.findNotificationById(null));
	}

	@Test
	void getNotificationByIdEmptyOptional() {
		when(notiDao.findById(any(Long.class))).thenReturn(Optional.empty());
		assertTrue(notiService.findNotificationById(1L).isEmpty());
	}

	@Test
	void getNotificationByIdOptionalPresent() {
		Notification notiFounded = new Notification();
		when(notiDao.findById(any(Long.class))).thenReturn(Optional.of(notiFounded));
		assertTrue(notiService.findNotificationById(1L).isPresent());
	}

}
