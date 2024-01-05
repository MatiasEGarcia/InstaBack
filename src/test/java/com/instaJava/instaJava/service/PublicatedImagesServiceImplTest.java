package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.response.PublicatedImageDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

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
	private FollowService followService;
	@Mock
	private UserService userService;
	@Mock
	private PublicatedImageMapper publicatedImageMapper;
	@Mock
	private SpecificationService<PublicatedImage> specService;
	@InjectMocks
	PublicatedImagesServiceImpl publicatedImagesService;
	private final User user = User.builder().userId(1L).username("random").password("random").role(RolesEnum.ROLE_USER)
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
		UserDto userDto = UserDto.builder().userId("1").username("random").build();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));

		PublicatedImage publicatedImage = PublicatedImage.builder().description(description).image(imgBase64)
				.userOwner(user).createdAt(ZonedDateTime.now(clock)).build();
		PublicatedImageDto publiImageDto = PublicatedImageDto.builder().description(description).image(imgBase64)
				.userOwner(userDto).createdAt(ZonedDateTime.now(clock)).build();

		when(publicatedImagesDao.save(publicatedImage)).thenReturn(publicatedImage);
		when(publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage)).thenReturn(publiImageDto);

		assertNotNull(publicatedImagesService.save(description, img));
		verify(publicatedImagesDao).save(publicatedImage);
	}

	// delete
	@Test
	void deleteByIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.deleteById(null));
	}

	@Test
	void deleteByIdNoExistsThrow() {
		Long id = 1L;
		PublicatedImagesServiceImpl spyPublicatedImageService = spy(publicatedImagesService);
		doThrow(RecordNotFoundException.class).when(spyPublicatedImageService).getById(id);
		assertThrows(RecordNotFoundException.class, () -> spyPublicatedImageService.deleteById(id));
		verify(publicatedImagesDao, never()).deleteById(id);
	}

	@Test
	void deleteByIdExistsNotSameUserThrow() {
		Long id = 1L;
		PublicatedImagesServiceImpl spyPublicatedImageService = spy(publicatedImagesService);
		UserDto userDto = UserDto.builder() // different user than the auth user.
				.userId("3").build();
		PublicatedImageDto publiImageDto = PublicatedImageDto.builder().id("1").userOwner(userDto).build();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		doReturn(publiImageDto).when(spyPublicatedImageService).getById(id);

		assertThrows(InvalidActionException.class, () -> spyPublicatedImageService.deleteById(id));

		verify(publicatedImagesDao, never()).deleteById(id);
	}

	@Test
	void deleteById() {
		Long id = 1L;
		PublicatedImagesServiceImpl spyPublicatedImageService = spy(publicatedImagesService);
		UserDto userDto = UserDto.builder() // same user than the auth user.
				.userId("1").build();
		PublicatedImageDto publiImageDto = PublicatedImageDto.builder().id("1").userOwner(userDto).build();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		doReturn(publiImageDto).when(spyPublicatedImageService).getById(id);
		;

		spyPublicatedImageService.deleteById(id);

		verify(publicatedImagesDao).deleteById(id);
	}

	// getById
	@Test
	void getByIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getById(null));
	}

	@Test
	void getByIdNoExistThrow() {///
		Long id = 1L;
		assertThrows(RecordNotFoundException.class, () -> publicatedImagesService.getById(id));
		verify(publicatedImagesDao).findById(id);
	}

	@Test
	void getByIdOwnerNoVisibleStatusRejectedThrow() {
		Long id = 1L;
		User userOnwer = User.builder().userId(2L).visible(false).build();
		PublicatedImage publicatedImage = PublicatedImage.builder().publImgId(id).userOwner(userOnwer).build();
		// dao
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		// follow
		when(followService.getFollowStatusByFollowedId(userOnwer.getUserId())).thenReturn(FollowStatus.REJECTED);

		assertThrows(InvalidActionException.class, () -> publicatedImagesService.getById(id));
		verify(publicatedImageMapper, never()).publicatedImageToPublicatedImageDto(publicatedImage);
	}

	@Test
	void getByIdOwnerVisibleStatusRejectedReturnNotNull() {
		Long id = 1L;
		User userOnwer = User.builder().userId(2L).visible(true).build();
		PublicatedImage publicatedImage = PublicatedImage.builder().publImgId(id).userOwner(userOnwer).build();
		// dao
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		// follow
		when(followService.getFollowStatusByFollowedId(userOnwer.getUserId())).thenReturn(FollowStatus.REJECTED);
		// mapper
		when(publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage))
				.thenReturn(new PublicatedImageDto());

		assertNotNull(publicatedImagesService.getById(id));
	}

	@Test
	void getByIdOwnerNoVisibleStatusInProcessThrow() {
		Long id = 1L;
		User userOnwer = User.builder().userId(2L).visible(false).build();
		PublicatedImage publicatedImage = PublicatedImage.builder().publImgId(id).userOwner(userOnwer).build();
		// dao
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		// follow
		when(followService.getFollowStatusByFollowedId(userOnwer.getUserId())).thenReturn(FollowStatus.IN_PROCESS);

		assertThrows(InvalidActionException.class, () -> publicatedImagesService.getById(id));
		verify(publicatedImageMapper, never()).publicatedImageToPublicatedImageDto(publicatedImage);
	}

	@Test
	void getByIdOwnerVisibleStatusInProcessReturnNotNull() {
		Long id = 1L;
		User userOnwer = User.builder().userId(2L).visible(true).build();
		PublicatedImage publicatedImage = PublicatedImage.builder().publImgId(id).userOwner(userOnwer).build();
		// dao
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		// follow
		when(followService.getFollowStatusByFollowedId(userOnwer.getUserId())).thenReturn(FollowStatus.IN_PROCESS);

		// mapper
		when(publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage))
				.thenReturn(new PublicatedImageDto());

		assertNotNull(publicatedImagesService.getById(id));
	}

	@Test
	void getByIdOwnerNoVisibleStatusNotAskedThrow() {
		Long id = 1L;
		User userOnwer = User.builder().userId(2L).visible(false).build();
		PublicatedImage publicatedImage = PublicatedImage.builder().publImgId(id).userOwner(userOnwer).build();
		// dao
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		// follow
		when(followService.getFollowStatusByFollowedId(userOnwer.getUserId())).thenReturn(FollowStatus.NOT_ASKED);

		assertThrows(InvalidActionException.class, () -> publicatedImagesService.getById(id));
		verify(publicatedImageMapper, never()).publicatedImageToPublicatedImageDto(publicatedImage);
	}

	@Test
	void getByIdOwnerVisibleStatusNotAskedReturnNotNull() {
		Long id = 1L;
		User userOnwer = User.builder().userId(2L).visible(true).build();
		PublicatedImage publicatedImage = PublicatedImage.builder().publImgId(id).userOwner(userOnwer).build();
		// dao
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		// follow
		when(followService.getFollowStatusByFollowedId(userOnwer.getUserId())).thenReturn(FollowStatus.NOT_ASKED);

		// mapper
		when(publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage))
				.thenReturn(new PublicatedImageDto());

		assertNotNull(publicatedImagesService.getById(id));
	}

	@Test
	void getByIdOwnerNoVisibleReturnsNotNull() {
		Long id = 1L;
		User userOnwer = User.builder().userId(2L).visible(false).build();
		PublicatedImage publicatedImage = PublicatedImage.builder().publImgId(id).userOwner(userOnwer).build();
		// dao
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		// follow
		when(followService.getFollowStatusByFollowedId(userOnwer.getUserId())).thenReturn(FollowStatus.ACCEPTED);
		// mapper
		when(publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage))
				.thenReturn(new PublicatedImageDto());

		assertNotNull(publicatedImagesService.getById(id));
	}
	
	@Test
	void getByIdOwnerVisibleReturnsNotNull() {
		Long id = 1L;
		User userOnwer = User.builder().userId(2L).visible(true).build();
		PublicatedImage publicatedImage = PublicatedImage.builder().publImgId(id).userOwner(userOnwer).build();
		// dao
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		// follow
		when(followService.getFollowStatusByFollowedId(userOnwer.getUserId())).thenReturn(FollowStatus.ACCEPTED);
		// mapper
		when(publicatedImageMapper.publicatedImageToPublicatedImageDto(publicatedImage))
				.thenReturn(new PublicatedImageDto());

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
	void getAllByOwnersVisiblesArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnersVisibles(null));
	}

	@Test
	void getAllByOwnersVisiblesPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSortField").build();
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnersVisibles(pageInfoDto));
	}

	@Test
	void getAllByOwnersVisiblesPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnersVisibles(pageInfoDto));
	}

	@Test
	void getAllByOwnersVisiblesNoneRecordThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		Page<PublicatedImage> publicatedImagePage = Page.empty();

		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(publicatedImagesDao.findByUserOwnerVisible(eq(true), any(Pageable.class))).thenReturn(publicatedImagePage);

		assertThrows(RecordNotFoundException.class, () -> publicatedImagesService.getAllByOwnersVisibles(pageInfoDto));

		verify(publicatedImagesDao).findByUserOwnerVisible(eq(true), any(Pageable.class));
	}

	@Test
	void getAllByOwnersVisiblesReturnsNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		PublicatedImage publiImage = new PublicatedImage();
		ResPaginationG<PublicatedImageDto> resPag = new ResPaginationG<>();
		Page<PublicatedImage> publicatedImagePage = new PageImpl<>(List.of(publiImage));

		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(publicatedImagesDao.findByUserOwnerVisible(eq(true), any(Pageable.class))).thenReturn(publicatedImagePage);
		when(publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(publicatedImagePage, pageInfoDto))
				.thenReturn(resPag);

		assertNotNull(publicatedImagesService.getAllByOwnersVisibles(pageInfoDto));

		verify(publicatedImagesDao).findByUserOwnerVisible(eq(true), any(Pageable.class));
	}

	// getAllByOwner
	@Test
	void getAllByOwnerOwnerIdNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOnwer(null, pageInfoDto));
	}

	@Test
	void getAllByOwnerPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOnwer(1L, null));
	}

	@Test
	void getAllByOwnerPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSortField").build();
		assertThrows(IllegalArgumentException.class,
				() -> publicatedImagesService.getAllByOnwer(user.getUserId(), pageInfoDto));
	}

	@Test
	void getAllByOwnerPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class,
				() -> publicatedImagesService.getAllByOnwer(user.getUserId(), pageInfoDto));
	}

	@Test
	void getAllByOwnerSameThanAuthUserNonePublicationThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		Pageable page = Pageable.unpaged();
		Page<PublicatedImage> publicatedImagePage = Page.empty();

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// pageable
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(page);
		// dao
		when(publicatedImagesDao.findByUserOwnerUserId(user.getUserId(), page)).thenReturn(publicatedImagePage);

		assertThrows(RecordNotFoundException.class,
				() -> publicatedImagesService.getAllByOnwer(user.getUserId(), pageInfoDto));

		verify(followService, never()).getFollowStatusByFollowedId(anyLong());
		verify(publicatedImagesDao).findByUserOwnerUserId(user.getUserId(), page);
	}

	@Test
	void getAllByOwnerSameThanAuthUserReturnsNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		PublicatedImage publicatedImage = new PublicatedImage();
		Pageable page = Pageable.unpaged();
		ResPaginationG<PublicatedImageDto> resPag = new ResPaginationG<PublicatedImageDto>();
		Page<PublicatedImage> publicatedImagePage = new PageImpl<>(List.of(publicatedImage));

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// pageable
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(page);
		// dao
		when(publicatedImagesDao.findByUserOwnerUserId(user.getUserId(), page)).thenReturn(publicatedImagePage);
		when(publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(publicatedImagePage, pageInfoDto))
				.thenReturn(resPag);

		assertNotNull(publicatedImagesService.getAllByOnwer(user.getUserId(), pageInfoDto));

		verify(followService, never()).getFollowStatusByFollowedId(anyLong());
		verify(publicatedImagesDao).findByUserOwnerUserId(user.getUserId(), page);
	}

	@Test
	void getAllByOwnerNotSameThanAuthUserFollowStatusNotAskedThrow() {
		Long idDifferentFromAuthUser = 100L;
		UserDto ownerDifferentFornAuthUserDto = UserDto.builder().userId("100").visible(false).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		Pageable page = Pageable.unpaged();

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(ownerDifferentFornAuthUserDto);
		// asking followService
		when(followService.getFollowStatusByFollowedId(idDifferentFromAuthUser)).thenReturn(FollowStatus.NOT_ASKED);

		assertThrows(InvalidActionException.class,
				() -> publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto));

		verify(userService).getById(idDifferentFromAuthUser);
		verify(followService).getFollowStatusByFollowedId(idDifferentFromAuthUser);
		verify(publicatedImagesDao, never()).findByUserOwnerUserId(idDifferentFromAuthUser, page);
	}

	@Test
	void getAllByOwnerNotSameThanAuthUserFollowStatusInProcess() {
		Long idDifferentFromAuthUser = 100L;
		UserDto ownerDifferentFornAuthUserDto = UserDto.builder().userId("100").visible(false).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		Pageable page = Pageable.unpaged();

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(ownerDifferentFornAuthUserDto);
		// asking followService
		when(followService.getFollowStatusByFollowedId(idDifferentFromAuthUser)).thenReturn(FollowStatus.IN_PROCESS);

		assertThrows(InvalidActionException.class,
				() -> publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto));

		verify(userService).getById(idDifferentFromAuthUser);
		verify(followService).getFollowStatusByFollowedId(idDifferentFromAuthUser);
		verify(publicatedImagesDao, never()).findByUserOwnerUserId(idDifferentFromAuthUser, page);
	}

	@Test
	void getAllByOwnerNotSameThanAuthUserFollowStatusRejected() {
		Long idDifferentFromAuthUser = 100L;
		UserDto ownerDifferentFornAuthUserDto = UserDto.builder().userId("100").visible(false).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		Pageable page = Pageable.unpaged();

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(ownerDifferentFornAuthUserDto);
		// asking followService
		when(followService.getFollowStatusByFollowedId(idDifferentFromAuthUser)).thenReturn(FollowStatus.REJECTED);

		assertThrows(InvalidActionException.class,
				() -> publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto));

		verify(userService).getById(idDifferentFromAuthUser);
		verify(followService).getFollowStatusByFollowedId(idDifferentFromAuthUser);
		verify(publicatedImagesDao, never()).findByUserOwnerUserId(idDifferentFromAuthUser, page);
	}

	@Test
	void getAllByOwnerNotSameThanAuthUserFollowStatusAccepted() {
		Long idDifferentFromAuthUser = 100L;
		UserDto ownerDifferentFornAuthUserDto = UserDto.builder().userId("100").visible(false).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		Pageable page = Pageable.unpaged();
		PublicatedImage publicatedImage = new PublicatedImage();
		ResPaginationG<PublicatedImageDto> resPag = new ResPaginationG<PublicatedImageDto>();
		Page<PublicatedImage> publicatedImagePage = new PageImpl<>(List.of(publicatedImage));

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(ownerDifferentFornAuthUserDto);
		// asking followService
		when(followService.getFollowStatusByFollowedId(idDifferentFromAuthUser)).thenReturn(FollowStatus.ACCEPTED);
		// pageUtils
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(page);
		// dao
		when(publicatedImagesDao.findByUserOwnerUserId(idDifferentFromAuthUser, page)).thenReturn(publicatedImagePage);
		// mapping
		when(publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(publicatedImagePage, pageInfoDto))
				.thenReturn(resPag);

		assertNotNull(publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto));

		verify(followService).getFollowStatusByFollowedId(anyLong());
		verify(publicatedImagesDao).findByUserOwnerUserId(idDifferentFromAuthUser, page);

	}

	// countPublicationsByOwnerId
	@Test
	void countPublicationsByOwnerIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.countPublicationsByOwnerId(null));
		verify(publicatedImagesDao, never()).countByUserOwnerUserId(null);
	}

	@Test
	void countPublicationsByOwnerIdReturnsNotNull() {
		Long ownerId = 1L;
		when(publicatedImagesDao.countByUserOwnerUserId(ownerId)).thenReturn(1L);
		assertNotNull(publicatedImagesService.countPublicationsByOwnerId(ownerId));
		verify(userService).getById(ownerId);
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
		when(publicatedImagesDao.findPublicationsFromUsersFollowed(eq(user.getUserId()), any(Pageable.class)))
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
		ResPaginationG<PublicatedImageDto> resPag = new ResPaginationG<PublicatedImageDto>();

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// pageUtils
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		// dao
		when(publicatedImagesDao.findPublicationsFromUsersFollowed(eq(user.getUserId()), any(Pageable.class)))
				.thenReturn(page);
		// mapper
		when(publicatedImageMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto)).thenReturn(resPag);

		assertNotNull(publicatedImagesService.getPublicationsFromUsersFollowed(pageInfoDto));
	}

}
