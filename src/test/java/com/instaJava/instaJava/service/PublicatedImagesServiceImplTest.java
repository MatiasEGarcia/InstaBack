package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.RolesEnum;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class PublicatedImagesServiceImplTest {

	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private Clock clock;
	@Mock private MessagesUtils messUtils;
	@Mock private PublicatedImagesDao publicatedImagesDao;
	@InjectMocks PublicatedImagesServiceImpl publicatedImagesService;
	private final User user = User.builder()
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
	void deleteByIdNoExist() {
		Long id = 1L;
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.deleteById(id));
		verify(publicatedImagesDao).findById(id);
		verify(publicatedImagesDao,never()).deleteById(id);
	}
	
	@Test
	void deleteByIdExist() {
		Long id = 1L;
		PublicatedImage publicatedImage = PublicatedImage.builder().pubImaId(id).build(); 
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		publicatedImagesService.deleteById(id);
		verify(publicatedImagesDao).findById(id);
		verify(publicatedImagesDao).deleteById(id);
	}
	
	@Test
	void findByIdNoExist() {
		Long id = 1L;
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService.findById(id));
		verify(publicatedImagesDao).findById(id);
	}
	
	@Test
	void findByIdExist() {
		Long id = 1L;
		PublicatedImage publicatedImage = PublicatedImage.builder().pubImaId(id).build(); 
		when(publicatedImagesDao.findById(id)).thenReturn(Optional.of(publicatedImage));
		assertEquals(publicatedImage, publicatedImagesService.findById(id));
		verify(publicatedImagesDao).findById(id);
	}

	@Test
	void findPublicatedImagesByOwnerSorted() {
		int pageNo = 1;
		int pageSize = 2;
		String sortField = "random";
		String sortDir = "asc";
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
		Pageable pag= PageRequest.of(pageNo - 1, pageSize,sort);
		Page<PublicatedImage> publicatedImages = new PageImpl<>(List.of(new PublicatedImage()));
		when(publicatedImagesDao.findPublicatedImagesByOwner(user, pag)).thenReturn(publicatedImages);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertNotNull(publicatedImagesService.findPublicatedImagesByOwnerSorted(pageNo, pageSize, sortField, sortDir));
		verify(publicatedImagesDao).findPublicatedImagesByOwner(user, pag);
	}

	@Test
	void findPublicatedImagesByOwnerSortedArgNull() {
		int pageNo = 1;
		int pageSize = 2;
		String sortField = null;
		String sortDir = null;
		assertThrows(IllegalArgumentException.class, () -> publicatedImagesService
				.findPublicatedImagesByOwnerSorted(pageNo, pageSize, sortField, sortDir));
	}
	
	@Test
	void findPublicatedImagesByOwnerSortedReturnEmptyPage() {
		int pageNo = 1;
		int pageSize = 2;
		String sortField = "random";
		String sortDir = "asc";
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
		Pageable pag= PageRequest.of(pageNo - 1, pageSize,sort);
		Page<PublicatedImage> publicatedImages = Page.empty();
		when(publicatedImagesDao.findPublicatedImagesByOwner(user, pag)).thenReturn(publicatedImages);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertNotNull(publicatedImagesService.findPublicatedImagesByOwnerSorted(pageNo, pageSize, sortField, sortDir));
		verify(publicatedImagesDao).findPublicatedImagesByOwner(user, pag);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
