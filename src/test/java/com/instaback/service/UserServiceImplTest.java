package com.instaback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.instaback.dao.PersonalDetailsDao;
import com.instaback.dao.UserDao;
import com.instaback.dto.PageInfoDto;
import com.instaback.entity.PersonalDetails;
import com.instaback.entity.User;
import com.instaback.exception.InvalidImageException;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.mapper.PersonalDetailsMapper;
import com.instaback.mapper.UserMapper;
import com.instaback.util.MessagesUtils;
import com.instaback.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private UserDao userDao;
	@Mock private PersonalDetailsDao personalDetailsDao;
	@Mock private MessagesUtils messUtils;
	@Mock private PageableUtils pageUtils;
	@Mock private UserMapper userMapper;
	@Mock private PersonalDetailsMapper personalDetailsMapper;
	@Mock private SpecificationService<User> specService;
	@InjectMocks private UserServiceImpl userService;
	 private User authUser = User.builder().id(1L).username("Mati").visible(true).build();
	

	//save
	@Test
	void saveArgumentNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.save(null));
	}
	
	@Test
	void saveReturnNotNull() {
		User user = new User();
		when(userDao.save(user)).thenReturn(user);
		assertNotNull(userService.save(user));
		verify(userDao).save(user);
	}

	//loadByUsername
	@Test
	void loadUserByUsernameUserNull() {
		String username = "randomUsername";
		when(userDao.findByUsername(username)).thenReturn(null);
		assertThrows(UsernameNotFoundException.class,() -> userService.loadUserByUsername(username));
	}

	@Test
	void loadUserByUsernameUserNotNull() {
		String username = "randomUsername";
		User user = new User();
		when(userDao.findByUsername(username)).thenReturn(user);
		assertEquals(user, userService.loadUserByUsername(username));
	}

	//udpateImage
	@Test
	void updateImageArgumentNullThrow() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(authUser);
		assertThrows(InvalidImageException.class,() -> userService.updateImage(null));
		verify(userDao,never()).save(any(User.class));
	}
	
	@Test
	void updateImageArgumentNotNullNotThrow() {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE, new byte[0]);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(authUser);
		userService.updateImage(img);
		verify(userDao).save(any(User.class));
	}
	
	//getImage
	@Test
	void getImageReturnImage() throws IOException {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE, new byte[0]);
		String image64 = Base64.getEncoder().encodeToString(img.getBytes());
		authUser.setImage(image64);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(authUser);
		assertNotNull(userService.getImage());
	}
	
	//getPersonalDetailsByUser
	@Test
	void getPersonalDetailsUserNotHaveThrow() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(authUser);
		assertThrows(RecordNotFoundException.class, () -> userService.getPersonalDetailsByUser());
	}
	
	@Test
	void getPersonalDetailsReturnNotNull() {
		PersonalDetails p = new PersonalDetails();
		authUser.setPersonalDetails(p);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(authUser);
		assertNotNull(userService.getPersonalDetailsByUser());
	}
	
	//savePersonalDetails
	
	@Test
	void savePersonalDetailsReturnsNotNull() {
		byte age = 15;
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(authUser);
		when(personalDetailsDao.save(any(PersonalDetails.class))).thenReturn(new PersonalDetails());
		
		assertNotNull(userService.savePersonalDetails("randomName","randomLastname", age, "randomEmail"));
	}
	
	//changeVisible
	@Test
	void changeVisible() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(authUser);//here the user is visible = true
		authUser.setVisible(false);
		when(userDao.save(authUser)).thenReturn(authUser);
		assertNotNull(userService.changeVisible());
	}

	
	//findById
	void findByIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.findById(null));
	}
	
	@Test
	void findByIdNotFoundThrow() {
		Long id = 1l;
		when(userDao.findById(id)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> userService.findById(id));
	}
	
	@Test
	void findByIdReturnsNotNull() {
		Long id = 1l;
		User user = new User();
		when(userDao.findById(id)).thenReturn(Optional.of(user));
		assertNotNull(userService.findById(id));
	}
	
	
	//getOneUserManyConditions
	@Test
	void getOneUserManyConditionsParamSpecNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.getOneUserManyConditions(null));
	}
	
	@Test
	void getOneUserManyConditionsNotFoundThrow() {
		// spec for example only, does not match 
		Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
				"random");
		when(userDao.findOne(spec)).thenReturn(Optional.empty());	
		assertThrows(RecordNotFoundException.class, () -> userService.getOneUserManyConditions(spec));
	}
	
	@Test
	void getOneUserManyConditionsReturnNotNull() {
		// spec for example only, does not match 
				Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
						"random");
				User user = new User();
		when(userDao.findOne(spec)).thenReturn(Optional.of(user));		
		assertNotNull(userService.getOneUserManyConditions(spec));
	}

	//getOneUserManyConditions
	@Test
	void getManyUsersManyConditionsParamSpecNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.getManyUsersManyConditions(new PageInfoDto(), null));
	}
	
	@Test
	void getManyUsersManyConditionsParamPageInfoDtoNullThrow() {
		// spec for example only, does not match 
				Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
						"random");
		assertThrows(IllegalArgumentException.class, () -> userService.getManyUsersManyConditions(null, spec));
	}
	
	@Test
	void getManyUsersManyConditionsParamPageInfoDtoSortDirNullThrow() {
		PageInfoDto p = PageInfoDto.builder()
				.sortField("Random")
				.build();
		// spec for example only, does not match 
		Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
				"random");
		assertThrows(IllegalArgumentException.class, () -> userService.getManyUsersManyConditions(p, spec));
	}
	
	@Test
	void getManyUsersManyConditionsParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto p = PageInfoDto.builder()
				.sortDir(Direction.ASC)
				.build();
		// spec for example only, does not match 
		Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
				"random");
		assertThrows(IllegalArgumentException.class, () -> userService.getManyUsersManyConditions(p, spec));
	}
	
	@Test
	void getManyUsersManyConditionsNoContentThrow() {
		PageInfoDto p = PageInfoDto.builder()
				.sortDir(Direction.ASC)
				.sortField("randomField")
				.build();
		// spec for example only, does not match 
		Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
				"random");
		Pageable pageable = Pageable.unpaged();
		
		when(pageUtils.getPageable(p)).thenReturn(pageable);
		when(userDao.findAll(spec, pageable)).thenReturn(Page.empty());
		assertThrows(RecordNotFoundException.class, () -> userService.getManyUsersManyConditions(p, spec));
	}
	
	@Test
	void getManyUsersManyConditionsReturnNotNull() {
		PageInfoDto p = PageInfoDto.builder()
				.sortDir(Direction.ASC)
				.sortField("randomField")
				.build();
		// spec for example only, does not match 
		Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
				"random");
		User user = new User();
		Page<User> page = new PageImpl<>(List.of(user));
		Pageable pageable = Pageable.unpaged();
		
		when(pageUtils.getPageable(p)).thenReturn(pageable);
		when(userDao.findAll(spec, pageable)).thenReturn(page);
		assertNotNull(userService.getManyUsersManyConditions(p, spec));
	}
	

	//existByUsername
	@Test
	void existByUsernameArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.existsByUsername(null));
	}
	
	@Test
	void existByUsernameReturnNotNull() {
		assertNotNull(userService.existsByUsername("randomUsername"));
	}
	
	
	//getByUsernameIn
	@Test
	void getByUsernameInParamUsernamelistNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.getByUsernameIn(null),
				"if usernameList is null an Exception should be throw");
		verify(userDao,never()).findByUsernameIn(null);
	}
	
	@Test
	void getByUsernameInParamUsernameSetEmptyThrow() {
		Set<String> emptySet = Collections.emptySet();
		assertThrows(IllegalArgumentException.class,() -> userService.getByUsernameIn(emptySet));
		verify(userDao,never()).findByUsernameIn(emptySet);
	}
	
	@Test
	void getByUsernameInNoUserFoundThrow() {
		Set<String> usernameList = Set.of("Mati");
		when(userDao.findByUsernameIn(anySet())).thenReturn(Collections.emptyList());
		
		assertThrows(RecordNotFoundException.class,() -> userService.getByUsernameIn(usernameList));
		verify(userDao).findByUsernameIn(anySet());
	}
	
	@Test
	void getByUsernameInReturnsNotNull() {
		User user = new User();
		Set<String> usernameList = Set.of("username1", "username2");
		List<User> userList = List.of(user);
		
		when(userDao.findByUsernameIn(usernameList)).thenReturn(userList);
		
		assertNotNull(userService.getByUsernameIn(usernameList));
		verify(userDao).findByUsernameIn(usernameList);
	}
}
