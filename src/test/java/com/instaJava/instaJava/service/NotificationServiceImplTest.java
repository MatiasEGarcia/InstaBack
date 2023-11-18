package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.NotificationDao;
import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.response.ResUser;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
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
		when(userMapper.UserToResUser(followerUser)).thenReturn(new ResUser());
		when(notiDao.save(any(Notification.class))).thenReturn(notiSaved);

		service.saveNotificationOfFollow(follow, "some message");

		verify(notiDao).save(any(Notification.class));
		verify(messTemplate).convertAndSendToUser(eq(followedUser.getUserId().toString()), eq("/private"),
				any(NotificationDto.class));
	}

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
	void getNotificationsByAuthUser() {
		PageInfoDto pag = PageInfoDto.builder().sortDir(Direction.ASC).sortField("random").build();
		// spec for example only, does not match reqSearch
		Specification<Notification> spec = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("random"), "someRandom");

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(pageUtils.getPageable(pag)).thenReturn(Pageable.unpaged());
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		when(notiDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		
		assertNotNull(service.getNotificationsByAuthUser(pag));
		
	}

}
