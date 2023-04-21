package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
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
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.IllegalActionException;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class PublicatedImagesServiceImplTest {

	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private Clock clock;
	@Mock private MessagesUtils messUtils;
	@Mock private PublicatedImagesDao publicatedImagesDao;
	@Mock private SpecificationService<PublicatedImage> specService;
	@InjectMocks PublicatedImagesServiceImpl publicatedImagesService;
	private final User user = User.builder()
			.userId(1L)
			.username("random")
			.password("random")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	@Test
	void saveMultipartFileNullThrows() {
		MockMultipartFile img = null;
		String description = "someDescription";
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.save(description, img));
	}
	
	@Test
	void saveMultipartFileEmptyThrows() {
		MockMultipartFile img =new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
				 new byte[0]
			      );
		String description = "someDescription";
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.save(description, img));
	}
	
	@Test
	void saveReturnPublicatedImage() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String description = "someDescription";
		
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		when(clock.getZone()).thenReturn(
				ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(
				Instant.parse("2020-12-01T10:05:23.653Z"));
		
		PublicatedImage publicatedImage = PublicatedImage.builder()
				.description(description)
				.image(Base64.getEncoder().encodeToString(img.getBytes()))
				.userOwner(user)
				.createdAt(ZonedDateTime.now(clock))
				.build();
		
		when(publicatedImagesDao.save(publicatedImage)).thenReturn(publicatedImage);
		assertEquals(publicatedImage, publicatedImagesService.save(description, img));
		verify(publicatedImagesDao).save(publicatedImage);
	}
	
	
	@Test
	void deleteByIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class , ()-> publicatedImagesService.deleteById(null));
	}
	@Test
	void deleteByIdNoExists() {
		Long id = 1L;
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.empty());
		publicatedImagesService.deleteById(id);
		verify(publicatedImagesDao).findById(id);
		verify(publicatedImagesDao,never()).deleteById(id);
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
		if(opt.isPresent()) fail("should return empty optional if the record not exist");
		verify(publicatedImagesDao).findById(id);
	}
	@Test
	void getByIdExistReturnPresentOptional() {
		Long id = 1L;
		PublicatedImage publicatedImage = PublicatedImage.builder().pubImaId(id).build(); 
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		Optional<PublicatedImage> opt = publicatedImagesService.getById(id);
		if(opt.isEmpty()) fail("should return present optional if the record exist");
		verify(publicatedImagesDao).findById(id);
	}

	@Test
	void getAllByUserArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByUser(null));
	}
	@Test
	void getAllByUserPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSortField").build();
		assertThrows(IllegalArgumentException.class,() -> publicatedImagesService.getAllByUser(pageInfoDto));
	}
	@Test
	void getAllByUserPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir("randomSortDir").build();
		assertThrows(IllegalArgumentException.class,() -> publicatedImagesService.getAllByUser(pageInfoDto));
	}
	@Test
	void getAllByUserReturnNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir("asc")
				.sortField("pubImaId").build();
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		//spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("random"), "someRandom");
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		when(publicatedImagesDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		assertNotNull(publicatedImagesService.getAllByUser(pageInfoDto));
		verify(specService).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao).findAll(eq(spec), any(Pageable.class));
	}
	
	
	@Test
	void getAllByOwnersVisiblesArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnersVisibles(null));
	}
	@Test
	void getAllByOwnersVisiblesPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSortField").build();
		assertThrows(IllegalArgumentException.class,() -> publicatedImagesService.getAllByOwnersVisibles(pageInfoDto));
	}
	@Test
	void getAllByOwnersVisiblesPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir("randomSortDir").build();
		assertThrows(IllegalArgumentException.class,() -> publicatedImagesService.getAllByOwnersVisibles(pageInfoDto));
	}
	@Test
	void getAllByOwnersVisiblesReturnNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir("asc")
				.sortField("pubImaId").build();
		//spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("random"), "someRandom");
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		when(publicatedImagesDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		assertNotNull(publicatedImagesService.getAllByOwnersVisibles(pageInfoDto));
		verify(specService).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao).findAll(eq(spec), any(Pageable.class));
	}
	
	
	@Test
	void getAllByOwnerIdPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnerId(null,1L));
	}
	@Test
	void getAllByOwnerIdOwnerIdNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir("asc")
				.sortField("pubImaId").build();
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.getAllByOwnerId(pageInfoDto,null));
	}
	@Test
	void getAllByOwnerIdPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSortField").build();
		assertThrows(IllegalArgumentException.class,() -> publicatedImagesService.getAllByOwnerId(pageInfoDto,user.getUserId()));
	}
	@Test
	void getAllByOwnerIdPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir("randomSortDir").build();
		assertThrows(IllegalArgumentException.class,() -> publicatedImagesService.getAllByOwnerId(pageInfoDto,user.getUserId()));
	}
	@Test
	void getAllByOwnerIdReturnNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir("asc")
				.sortField("pubImaId").build();
		//spec for example only, does not match reqSearch
		Specification<PublicatedImage> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("random"), "someRandom");
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		when(publicatedImagesDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		assertNotNull(publicatedImagesService.getAllByOwnerId(pageInfoDto,user.getUserId()));
		verify(specService).getSpecification(any(ReqSearch.class));
		verify(publicatedImagesDao).findAll(eq(spec), any(Pageable.class));
	}
	
	
	
	
	
	
	
	
	
}
