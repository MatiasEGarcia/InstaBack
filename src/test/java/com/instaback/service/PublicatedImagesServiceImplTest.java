package com.instaback.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaback.dao.CommentDao;
import com.instaback.dao.PublicatedImagesDao;
import com.instaback.dto.PageInfoDto;
import com.instaback.entity.PublicatedImage;
import com.instaback.entity.User;
import com.instaback.enums.RolesEnum;
import com.instaback.exception.InvalidException;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.mapper.CommentMapper;
import com.instaback.mapper.PublicatedImageMapper;
import com.instaback.util.MessagesUtils;
import com.instaback.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class PublicatedImagesServiceImplTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Clock clock;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private PageableUtils pageUtils;
	@Mock
	private PublicatedImagesDao publicatedImagesDao;
	@Mock
	private CommentDao commentDao;
	@Mock
	private FollowService followService;
	@Mock
	private UserService userService;
	@Mock
	private PublicatedImageMapper publicatedImageMapper;
	@Mock
	private CommentMapper commentMapper;
	@Mock
	private SpecificationService<PublicatedImage> specService;
	@InjectMocks
	PublicatedImagesServiceImpl publicatedImagesService;
	private final User user = User.builder().id(1L).username("random").password("random").role(RolesEnum.ROLE_USER)
			.build();

	// save
	@Test
	void saveMultipartFileNullThrows() {
		MockMultipartFile img = null;
		String description = "someDescription";
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.save(description, img));
	}

	@Test
	void saveMultipartFileEmptyThrows() {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE, new byte[0]);
		String description = "someDescription";
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.save(description, img));
	}

	@Test
	void saveReturnsNotNull() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE,
				"Hello, World!".getBytes());
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());

		String description = "someDescription";

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));

		PublicatedImage publicatedImage = PublicatedImage.builder().description(description).image(imgBase64)
				.userOwner(user).createdAt(ZonedDateTime.now(clock)).build();

		when(publicatedImagesDao.save(publicatedImage)).thenReturn(publicatedImage);

		assertNotNull(publicatedImagesService.save(description, img));
	}

	// delete
	@Test
	void deleteByIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.deleteById(null));
	}

	@Test
	void deleteByIdNoExistsThrow() {
		Long id = 1L;
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> publicatedImagesService.deleteById(id));
		verify(publicatedImagesDao, never()).deleteById(id);
	}

	@Test
	void deleteByIdExistsNotSameUserThrow() {
		Long id = 1L;
		PublicatedImage publiImage = PublicatedImage.builder().id(1L).userOwner(new User(3L)).build();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publiImage));

		assertThrows(InvalidException.class, () -> publicatedImagesService.deleteById(id));

		verify(publicatedImagesDao, never()).deleteById(id);
	}

	@Test
	void deleteByIdReturnNotNull() {
		Long id = 1L;
		PublicatedImage publiImage = PublicatedImage.builder().id(1L).userOwner(user).build();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publiImage));

		assertNotNull(publicatedImagesService.deleteById(id));
	}

	// getById
	@Test
	void getByIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getById(null));
	}

	@Test
	void getByIdNoExistThrow() {
		Long id = 1L;
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> publicatedImagesService.getById(id));
	}
	
	@Test
	void getByIdReturnNotNull() {
		Long id = 1L;
		PublicatedImage p = new PublicatedImage();
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(p));
		assertNotNull(publicatedImagesService.getById(id));
	}

	// findById
	@Test
	void findByIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.findById(null));
	}

	@Test
	void findByIdReturnOptionalEmpty() {
		Long id = 1L;
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.empty());
		assertTrue(publicatedImagesService.findById(id).isEmpty(), "if dao return empty optional, should return empty");
	}

	@Test
	void findByIdReturnOptionalPreset() {
		Long id = 1L;
		PublicatedImage pi = new PublicatedImage();
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(pi));
		assertTrue(publicatedImagesService.findById(id).isPresent(),
				"if dao return present optional, should return present");
	}
	
	// getAllByOnwersVisibles
	@Test
	void getAllByOwnersVisibleArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnerVisible(null));
	}

	@Test
	void getAllByOwnersVisiblePageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSortField").build();
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnerVisible(pageInfoDto));
	}

	@Test
	void getAllByOwnersVisiblePageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnerVisible(pageInfoDto));
	}

	@Test
	void getAllByOwnersVisibleNoneRecordThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		Page<PublicatedImage> publicatedImagePage = Page.empty();

		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(publicatedImagesDao.findByUserOwnerVisible(eq(true), any(Pageable.class))).thenReturn(publicatedImagePage);

		assertThrows(RecordNotFoundException.class, () -> publicatedImagesService.getAllByOwnerVisible(pageInfoDto));

		verify(publicatedImagesDao).findByUserOwnerVisible(eq(true), any(Pageable.class));
	}

	@Test
	void getAllByOwnersVisibleReturnsNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		PublicatedImage publiImage = new PublicatedImage();
		Page<PublicatedImage> publicatedImagePage = new PageImpl<>(List.of(publiImage));

		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(publicatedImagesDao.findByUserOwnerVisible(eq(true), any(Pageable.class))).thenReturn(publicatedImagePage);

		assertNotNull(publicatedImagesService.getAllByOwnerVisible(pageInfoDto));

		verify(publicatedImagesDao).findByUserOwnerVisible(eq(true), any(Pageable.class));
	}

	// getAllByOnwerId
	@Test
	void getAllByOwnerIdOwnerIdNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOnwerId(null, pageInfoDto));
	}

	@Test
	void getAllByOwnerIdPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOnwerId(1L, null));
	}

	@Test
	void getAllByOwnerIdPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSortField").build();
		assertThrows(IllegalArgumentException.class,
				() -> publicatedImagesService.getAllByOnwerId(user.getId(), pageInfoDto));
	}

	@Test
	void getAllByOwnerIdPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class,
				() -> publicatedImagesService.getAllByOnwerId(user.getId(), pageInfoDto));
	}

	@Test
	void getAllByOnwerIdNoContentThrow() {
		Long ownerId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		Pageable pageable =  Pageable.unpaged();
		
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		when(publicatedImagesDao.findByUserOwnerId(ownerId, pageable)).thenReturn(Page.empty());
		assertThrows(RecordNotFoundException.class, () -> publicatedImagesService.getAllByOnwerId(ownerId, pageInfoDto));
	}
	
	@Test
	void getAllByOnwerIdReturnNotNull() {
		Long ownerId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		PublicatedImage p = new PublicatedImage();
		Page<PublicatedImage> page = new PageImpl<>(List.of(p));
		Pageable pageable = Pageable.unpaged();
		
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		when(publicatedImagesDao.findByUserOwnerId(ownerId, pageable)).thenReturn(page);
		assertNotNull(publicatedImagesService.getAllByOnwerId(ownerId, pageInfoDto));
	}
	

	// countPublicationsByOwnerId
	@Test
	void countPublicationsByOwnerIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.countPublicationsByOwnerId(null));
		verify(publicatedImagesDao, never()).countByUserOwnerId(null);
	}

	@Test
	void countPublicationsByOwnerIdReturnsNotNull() {
		Long ownerId = 1L;
		when(publicatedImagesDao.countByUserOwnerId(ownerId)).thenReturn(1L);
		assertNotNull(publicatedImagesService.countPublicationsByOwnerId(ownerId));
	}

	// getPublicationsFromUsersFollowed
	@Test
	void getPublicationsFromUsersFollowedParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> publicatedImagesService.getPublicationsFromUsersFollowed(null));
	}

	@Test
	void getPublicationsFromUsersFollowedParamPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").build();
		assertThrows(IllegalArgumentException.class,
				() -> publicatedImagesService.getPublicationsFromUsersFollowed(pageInfoDto));
	}

	@Test
	void getPublicationsFromUsersFollowedParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.DESC).build();
		assertThrows(IllegalArgumentException.class,
				() -> publicatedImagesService.getPublicationsFromUsersFollowed(pageInfoDto));
	}

	@Test
	void getPublicationsFromUsersFollowedParamPageInfoDtoSortFieldBlankThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.DESC).sortField("").build();
		assertThrows(IllegalArgumentException.class,
				() -> publicatedImagesService.getPublicationsFromUsersFollowed(pageInfoDto));
	}

	@Test
	void getPublicationsFromUsersFollowedNoRecordsFoundThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.DESC).sortField("random").build();
		Page<PublicatedImage> page = Page.empty();

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// pageUtils
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		// dao
		when(publicatedImagesDao.findPublicationsFromUsersFollowed(eq(user.getId()), any(Pageable.class)))
				.thenReturn(page);

		assertThrows(RecordNotFoundException.class,
				() -> publicatedImagesService.getPublicationsFromUsersFollowed(pageInfoDto));

		verify(publicatedImageMapper, never()).pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}

	@Test
	void getPublicationsFromUsersFollowedReturnsNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.DESC).sortField("random").build();
		PublicatedImage publicatedImage = new PublicatedImage();
		Page<PublicatedImage> page = new PageImpl<>(List.of(publicatedImage));

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// pageUtils
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		// dao
		when(publicatedImagesDao.findPublicationsFromUsersFollowed(eq(user.getId()), any(Pageable.class)))
				.thenReturn(page);

		assertNotNull(publicatedImagesService.getPublicationsFromUsersFollowed(pageInfoDto));
	}

}
