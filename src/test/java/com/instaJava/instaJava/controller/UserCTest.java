package com.instaJava.instaJava.controller;


import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.request.ReqLogout;
import com.instaJava.instaJava.entity.RolesEnum;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.service.InvTokenServiceImpl;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.service.UserServiceImpl;
import com.instaJava.instaJava.util.MessagesUtils;
@AutoConfigureMockMvc
@SpringBootTest
class UserCTest {
	
	private static MockHttpServletRequest request;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MessagesUtils messUtils;
	@MockBean
	private UserDao userDao;
	@MockBean
	private InvTokenServiceImpl invTokenService;
	@MockBean
	private UserServiceImpl userService;
	@MockBean
	private PersonalDetailsMapper personalDetailsMapper;
	@MockBean
	private UserMapper userMapper;
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	private final User USER_AUTH = User.builder()
			.username("random")
			.password("random")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	
	@BeforeAll
	static void mockSetup() throws IOException {
		request = new MockHttpServletRequest();
	} 

	@Test
	void uploadImageStatusOk() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		String token = jwtService.generateToken(USER_AUTH);
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
		
		when(userService.getImage()).thenReturn(imgBase64);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/image")
				.file(img)
				.header("Authorization","Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.image64",equalToIgnoringCase(imgBase64)));
		
		verify(userService).loadUserByUsername(USER_AUTH.getUsername());
		verify(userService).getImage();
		
	}
	
	@Test
	void uploadImageStatusBadRequst() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				"text/plain", 
		        "Hello, World!".getBytes()
		      );
		String token = jwtService.generateToken(USER_AUTH);
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/image")
				.file(img)
				.header("Authorization","Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.file",is(messUtils.getMessage("vali.image"))));
		
		verify(userService).loadUserByUsername(USER_AUTH.getUsername());
		verify(userService,never()).getImage();
	}

	@Test
	void downloadImageStatusOk() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		String token = jwtService.generateToken(USER_AUTH);
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
		when(userService.getImage()).thenReturn(imgBase64);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/image")
				.header("Authorization","Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.image64",is(imgBase64)));
		
		verify(userService).getImage();
	}
	
	@Test
	void logoutStatusOk() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
		ReqLogout reqLogout=ReqLogout.builder()
			.token("SomeStringToken")
			.refreshToken("SomeRefreshToken")
			.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/logout")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqLogout)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("mess.successfully-logout"))));
		
		verify(invTokenService)
			.invalidateTokens(List.of(reqLogout.getToken(), reqLogout.getRefreshToken()));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
