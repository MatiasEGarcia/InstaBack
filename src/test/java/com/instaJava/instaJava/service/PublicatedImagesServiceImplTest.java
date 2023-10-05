package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.IllegalActionException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import jakarta.persistence.EntityNotFoundException;

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
	private SpecificationService<PublicatedImage> specService;
	@InjectMocks
	PublicatedImagesServiceImpl publicatedImagesService;
	private final User user = User.builder().userId(1L).username("random").password("random").role(RolesEnum.ROLE_USER)
			.build();

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
	void saveReturnPublicatedImage() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE,
				"Hello, World!".getBytes());
		String description = "someDescription";

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));

		PublicatedImage publicatedImage = PublicatedImage.builder().description(description)
				.image(Base64.getEncoder().encodeToString(img.getBytes())).userOwner(user)
				.createdAt(ZonedDateTime.now(clock)).build();

		when(publicatedImagesDao.save(publicatedImage)).thenReturn(publicatedImage);
		assertEquals(publicatedImage, publicatedImagesService.save(description, img));
		verify(publicatedImagesDao).save(publicatedImage);
	}

	@Test
	void deleteByIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.deleteById(null));
	}

	@Test
	void deleteByIdNoExists() {
		Long id = 1L;
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.empty());
		publicatedImagesService.deleteById(id);
		verify(publicatedImagesDao).findById(id);
		verify(publicatedImagesDao, never()).deleteById(id);
	}

	@Test
	void deleteByIdExistsNotSameUserThrow() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		Long id = 1L;
		PublicatedImage publicatedImage = PublicatedImage.builder().pubImaId(id).userOwner(User.builder().build()).build(); 
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		assertThrows(IllegalActionException.class,()->publicatedImagesService.deleteById(id));
		verify(publicatedImagesDao).findById(id);
		verify(publicatedImagesDao,never()).deleteById(id);
	}

	@Test
	void deleteByIdExistsSameUser() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		Long id = 1L;
		PublicatedImage publicatedImage = PublicatedImage.builder().pubImaId(id).userOwner(user).build(); 
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		publicatedImagesService.deleteById(id);
		verify(publicatedImagesDao).findById(id);
		verify(publicatedImagesDao).deleteById(id);
	}

	@Test
	void getByIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getById(null));
	}

	@Test
	void getByIdNoExistReturnEmptyOptional() {
		Long id = 1L;
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.empty());
		Optional<PublicatedImage> opt = publicatedImagesService.getById(id);
		if (opt.isPresent())
			fail("should return empty optional if the record not exist");
		verify(publicatedImagesDao).findById(id);
	}

	@Test
	void getByIdExistReturnPresentOptional() {
		Long id = 1L;
		PublicatedImage publicatedImage = PublicatedImage.builder().pubImaId(id).build();
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		Optional<PublicatedImage> opt = publicatedImagesService.getById(id);
		if (opt.isEmpty())
			fail("should return present optional if the record exist");
		verify(publicatedImagesDao).findById(id);
	}


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
	void getAllByOwnersVisiblesReturnNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		// spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("random"), "someRandom");
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(publicatedImagesDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		assertNotNull(publicatedImagesService.getAllByOwnersVisibles(pageInfoDto));
		verify(specService).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao).findAll(eq(spec), any(Pageable.class));
	}
	

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
	void getAllByOwnerSameThanAuthUser() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		// spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("random"), "someRandom");

		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// specification
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		// pageable
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		// dao
		when(publicatedImagesDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());

		Map<String, Object> mapp = publicatedImagesService.getAllByOnwer(user.getUserId(), pageInfoDto);
		if (!mapp.containsKey("publications"))
			fail("should return a map with 'publications' key and page publications as value");

		verify(followService, never()).getFollowStatusByFollowedId(anyLong());
		verify(specService).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao).findAll(eq(spec), any(Pageable.class));
	}

	@Test
	void getAllByOwnerNotSameThanUserButNotFoundThrow() {
		Long idDifferentFromAuthUser = 100L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		// spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("random"), "someRandom");
		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class,
				() -> publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto));

		verify(userService).getById(idDifferentFromAuthUser);
		verify(followService, never()).getFollowStatusByFollowedId(idDifferentFromAuthUser);
		verify(specService, never()).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao, never()).findAll(eq(spec), any(Pageable.class));
	}

	@Test
	void getAllByOwnerNotSameThanAuthUserFollowStatusNotAsked() {
		Long idDifferentFromAuthUser = 100L;
		User ownerDifferentFornAuthUser = User.builder().userId(idDifferentFromAuthUser).visible(false).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		// spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("random"), "someRandom");
		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(Optional.of(ownerDifferentFornAuthUser));
		// asking followService
		when(followService.getFollowStatusByFollowedId(idDifferentFromAuthUser)).thenReturn(FollowStatus.NOT_ASKED);

		Map<String, Object> mapp = publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto);
		if (!mapp.containsKey("moreInfo"))
			fail("should return a map with 'moreInfo' key and string message as value");

		verify(userService).getById(idDifferentFromAuthUser);
		verify(followService).getFollowStatusByFollowedId(idDifferentFromAuthUser);
		verify(specService, never()).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao, never()).findAll(eq(spec), any(Pageable.class));
	}

	@Test
	void getAllByOwnerNotSameThanAuthUserFollowStatusInProcess() {
		Long idDifferentFromAuthUser = 100L;
		User ownerDifferentFornAuthUser = User.builder().userId(idDifferentFromAuthUser).visible(false).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		// spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("random"), "someRandom");
		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(Optional.of(ownerDifferentFornAuthUser));
		// asking followService
		when(followService.getFollowStatusByFollowedId(idDifferentFromAuthUser)).thenReturn(FollowStatus.IN_PROCESS);

		Map<String, Object> mapp = publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto);
		if (!mapp.containsKey("moreInfo"))
			fail("should return a map with 'moreInfo' key and string message as value");

		verify(userService).getById(idDifferentFromAuthUser);
		verify(followService).getFollowStatusByFollowedId(idDifferentFromAuthUser);
		verify(specService, never()).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao, never()).findAll(eq(spec), any(Pageable.class));
	}

	@Test
	void getAllByOwnerNotSameThanAuthUserFollowStatusRejected() {
		Long idDifferentFromAuthUser = 100L;
		User ownerDifferentFornAuthUser = User.builder().userId(idDifferentFromAuthUser).visible(false).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		// spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("random"), "someRandom");
		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(Optional.of(ownerDifferentFornAuthUser));
		// asking followService
		when(followService.getFollowStatusByFollowedId(idDifferentFromAuthUser)).thenReturn(FollowStatus.REJECTED);

		Map<String, Object> mapp = publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto);
		if (!mapp.containsKey("moreInfo"))
			fail("should return a map with 'moreInfo' key and string message as value");

		verify(userService).getById(idDifferentFromAuthUser);
		verify(followService).getFollowStatusByFollowedId(idDifferentFromAuthUser);
		verify(specService, never()).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao, never()).findAll(eq(spec), any(Pageable.class));
	}

	@Test
	void getAllByOwnerNotSameThanAuthUserFollowStatusAccepted() {
		Long idDifferentFromAuthUser = 100L;
		User ownerDifferentFornAuthUser = User.builder().userId(idDifferentFromAuthUser).visible(false).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("pubImaId").build();
		// spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("random"), "someRandom");
		// getting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// asking for the ownerUser, to know if is visible
		when(userService.getById(idDifferentFromAuthUser)).thenReturn(Optional.of(ownerDifferentFornAuthUser));
		// asking followService
		when(followService.getFollowStatusByFollowedId(idDifferentFromAuthUser)).thenReturn(FollowStatus.ACCEPTED);
		// specification
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		// pageable
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		// dao
		when(publicatedImagesDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());

		Map<String, Object> mapp = publicatedImagesService.getAllByOnwer(idDifferentFromAuthUser, pageInfoDto);
		if (!mapp.containsKey("publications"))
			fail("should return a map with 'publications' key and page publications as value");

		verify(userService).getById(idDifferentFromAuthUser);
		verify(followService).getFollowStatusByFollowedId(idDifferentFromAuthUser);
		verify(specService).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao).findAll(eq(spec), any(Pageable.class));
	}

}
