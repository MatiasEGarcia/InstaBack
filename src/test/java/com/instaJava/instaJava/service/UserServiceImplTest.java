package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.instaJava.instaJava.dao.PersonalDetailsDao;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private UserDao userDao;
	@Mock private PersonalDetailsDao personalDetailsDao;
	@Mock private MessagesUtils messUtils;
	@Mock private UserMapper userMapper;
	@Mock private PersonalDetailsMapper personalDetailsMapper;
	@InjectMocks private UserServiceImpl userService;
	static private MockMultipartFile multipartFile;
	static private User user;
	static private PersonalDetails personalDetails;
	static private PersonalDetailsDto personalDetailsDto;
	
	@BeforeAll
	static void entitiesSetUp() throws IOException {
		multipartFile = new MockMultipartFile("file", "test.txt",
			      "text/plain", "testing".getBytes());
		user = User.builder()
				.userId(1L)
				.username("Mati")
				.image(Base64.getEncoder().encodeToString(multipartFile.getBytes()))
				.build();
		
		personalDetails = PersonalDetails.builder().build();
		personalDetailsDto = PersonalDetailsDto.builder().build();
		
	}
	
	@Test
	void loadUserByUsernameUserNull() {
		when(userDao.findByUsername(user.getUsername())).thenReturn(null);
		assertThrows(UsernameNotFoundException.class,() -> {userService.loadUserByUsername(user.getUsername());});
		verify(userDao).findByUsername(user.getUsername());
	}

	@Test
	void loadUserByUsernameUserNotNull() {
		when(userDao.findByUsername(user.getUsername())).thenReturn(user);
		assertEquals(user, userService.loadUserByUsername(user.getUsername()));
		verify(userDao).findByUsername(user.getUsername());
	}
	
	@Test
	void updateImageArgumentNullThrow() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertThrows(ImageException.class,() -> {userService.updateImage(null);});
		verify(userDao,never()).save(user);
	}
	
	@Test
	void updateImageArgumentNotNullNotThrow() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		userService.updateImage(multipartFile);
		verify(userDao).save(user);
	}
	
	@Test
	void getImageReturnImage() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertEquals(user.getImage(), userService.getImage());
	}
	
	@Test
	void getPersonalDetailsByUserNoExist() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		when(personalDetailsDao.findByUser(user)).thenReturn(null);
		assertThrows(IllegalArgumentException.class, () -> {userService.getPersonalDetailsByUser();});
		verify(personalDetailsDao).findByUser(user);
	}
	
	@Test
	void getPersonalDetailsByUserExist() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		when(personalDetailsDao.findByUser(user)).thenReturn(personalDetails);
		userService.getPersonalDetailsByUser();
		verify(personalDetailsDao).findByUser(user);
	}
	
	@Test
	void savePersonalDetailsArgumentNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> {userService.savePersonalDetails(null);});
		verify(personalDetailsMapper,never()).personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user);
		verify(personalDetailsDao,never()).save(personalDetails);
	}
	
	@Test
	void savePersonalDetailsArgumentNotNull() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		when(personalDetailsMapper.
				personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user))
				.thenReturn(personalDetails);
		when(personalDetailsDao.save(personalDetails)).thenReturn(personalDetails);
		
		userService.savePersonalDetails(personalDetailsDto);
		
		verify(personalDetailsMapper)
			.personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user);
		verify(personalDetailsDao).save(personalDetails);
	}
	
	@Test
	void findByUsernameLikeReturnNull() {
		when(userDao.findByUsernameLike(user.getUsername(),10)).thenReturn(Collections.emptyList());
	
		assertNull(userService.findByUsernameLike(user.getUsername(),10));
		
		verify(userDao).findByUsernameLike(user.getUsername(),10);
	}
	
	
	@Test
	void findByUsernameLikeReturnEquals() {
		List<User> users = List.of(user);
		when(userDao.findByUsernameLike(user.getUsername(),10)).thenReturn(users);
	
		assertEquals(users,userService.findByUsernameLike(user.getUsername(),10));
		
		verify(userDao).findByUsernameLike(user.getUsername(),10);
	}
	
	
	
	
}
