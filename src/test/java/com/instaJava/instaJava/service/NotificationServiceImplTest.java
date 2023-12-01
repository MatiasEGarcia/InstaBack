package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
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
	private PageableUtils pageUtils;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private SpecificationService<Notification> specService;
	@InjectMocks
	private NotificationServiceImpl service;
	// As auth user
	private final User user = User.builder().userId(1L).username("random").password("random").role(RolesEnum.ROLE_USER)
			.build();

	@Test
	void saveNotificationOfFollowParamFollowNullThorw() {
		assertThrows(IllegalArgumentException.class, () -> service.saveNotificationOfFollow(null, "some message"));
	}

	@Test
	void saveNotificationOfFollowParamcustomMessageNullThorw() {
		assertThrows(IllegalArgumentException.class, () -> service.saveNotificationOfFollow(new Follow(), null));
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

		service.saveNotificationOfFollow(follow, "some message");

		verify(notiDao).save(any(Notification.class));
		verify(messTemplate).convertAndSendToUser(eq(followedUser.getUserId().toString()), eq("/private"),
				any(NotificationDto.class));
	}

	//getNotificationByAuthUser
	@Test
	void getNotificationsByAuthUserParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> service.getNotificationsByAuthUser(null));
	}

	@Test
	void getNotificationsByAuthUserParamPageInfoDtoSortDirNullThrow() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(null).sortField("random").build();
		assertThrows(IllegalArgumentException.class, () -> service.getNotificationsByAuthUser(pag));
	}

	@Test
	void getNotificationsByAuthUserParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(Direction.ASC).sortField(null).build();
		assertThrows(IllegalArgumentException.class, () -> service.getNotificationsByAuthUser(pag));
	}

	@Test
	void getNotificationsByAuthUserPageEmptyThrow() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(Direction.ASC).sortField("random").build();
		Pageable pageable = Pageable.unpaged();

		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		//pageable
		when(pageUtils.getPageable(pag)).thenReturn(pageable);
		//dao
		when(notiDao.findByToWho(user.getUserId(), pageable)).thenReturn(Page.empty());

		assertThrows(RecordNotFoundException.class,() -> service.getNotificationsByAuthUser(pag));
	}

	@Test
	void getNotificationsByAuthUserReturnsNotNull() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(Direction.ASC).sortField("random").build();
		Pageable pageable = Pageable.unpaged();
		Notification noti = new Notification();
		Page<Notification> page = new PageImpl<>(List.of(noti));
		ResPaginationG<NotificationDto> resPagG= new ResPaginationG<NotificationDto>(); 
		
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		//pageable
		when(pageUtils.getPageable(pag)).thenReturn(pageable);
		//dao
		when(notiDao.findByToWho(user.getUserId(), pageable)).thenReturn(page);
		//mapper
		when(notiMapper.pageAndPageInfoDtoToResPaginationG(page, pag)).thenReturn(resPagG);
		
		assertNotNull(service.getNotificationsByAuthUser(pag));
	}
	
	
	//deleteById
	@Test
	void deleteNotificationByIdNotiIdParamNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> service.deleteNotificationById(null));
	}

	@Test
	void deleteNotificationByIdNotSameReceptorAndAuthUserThrow() {
		Notification notiToDelete = Notification.builder().toWho(new User()).build();//different user.

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		NotificationServiceImpl serviceSpy = spy(service);

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
		NotificationServiceImpl serviceSpy = spy(service);

		doReturn(Optional.of(notiToDelete)).when(serviceSpy).findNotificationById(any(Long.class));

		serviceSpy.deleteNotificationById(1L);
		verify(notiDao).delete(notiToDelete);
	}

	//getNotificationById
	@Test
	void getNotificationByIdNotiIdParamNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> service.findNotificationById(null));
	}

	@Test
	void getNotificationByIdEmptyOptional() {
		when(notiDao.findById(any(Long.class))).thenReturn(Optional.empty());
		assertTrue(service.findNotificationById(1L).isEmpty());
	}

	@Test
	void getNotificationByIdOptionalPresent() {
		Notification notiFounded = new Notification();
		when(notiDao.findById(any(Long.class))).thenReturn(Optional.of(notiFounded));
		assertTrue(service.findNotificationById(1L).isPresent());
	}

}
