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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.request.ReqLogout;
import com.instaJava.instaJava.entity.PersonalDetails;
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
	private PersonalDetailsMapper personalDetailsMapper;
	@MockBean
	private UserMapper userMapper;
	@MockBean
	private UserDao userDao;
	@MockBean
	private InvTokenServiceImpl invTokenService;
	@MockBean
	private UserServiceImpl userService;
	
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
	void postUploadImageStatusOk() throws Exception {
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
	void postUploadImageStatusBadRequst() throws Exception {
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
	void getDownloadImageStatusOk() throws Exception {
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
	void postLogoutStatusOk() throws Exception {
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
	
	@Test
	void postLogoutStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
		ReqLogout reqLogout=ReqLogout.builder().build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/logout")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqLogout)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.token",is(messUtils.getMessage("vali.token-not-blank"))))
				.andExpect(jsonPath("$.refreshToken", is(messUtils.getMessage("vali.refreshToken-not-blank"))));
		
		verify(invTokenService,never()).invalidateTokens(null);
	}
	
	@Test
	void postSavePersonalDetailsStatusOk() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		Byte age = 23;
		PersonalDetailsDto perDetDto = PersonalDetailsDto.builder()
				.name("Mati")
				.lastname("Gar")
				.age(age)
				.email("matig@gmail.com")
				.build();
		PersonalDetails perDet = PersonalDetails.builder()
				.name(token)
				.lastname(token)
				.age(age)
				.email("matig@gmail.com")
				.build();
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
		when(userService.savePersonalDetails(perDetDto)).thenReturn(perDet);
		when(personalDetailsMapper.personalDetailsToPersonalDetailsDto(perDet)).thenReturn(perDetDto);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(perDetDto)))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name",is(perDetDto.getName())))
				.andExpect(jsonPath("$.lastname",is(perDetDto.getLastname())))
				.andExpect(jsonPath("$.age",is(23)))
				.andExpect(jsonPath("$.email",is(perDetDto.getEmail())));
		
		verify(userService).savePersonalDetails(perDetDto);
		verify(personalDetailsMapper).personalDetailsToPersonalDetailsDto(perDet);
		
	}
	
	@Test
	void postSavePersonalDetailsStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		PersonalDetailsDto perDetDto = PersonalDetailsDto.builder().build();
		PersonalDetails perDet = PersonalDetails.builder().build();
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(perDet)))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.name",is(messUtils.getMessage("vali.name-not-blank"))))
				.andExpect(jsonPath("$.lastname",is(messUtils.getMessage("vali.lastname-not-blank"))))
				.andExpect(jsonPath("$.age",is(messUtils.getMessage("vali.age-range"))))
				.andExpect(jsonPath("$.email",is(messUtils.getMessage("vali.email-not-blank"))));
		
		verify(userService,never()).savePersonalDetails(perDetDto);
		verify(personalDetailsMapper,never()).personalDetailsToPersonalDetailsDto(perDet);
	}
	
	@Test
	void getGetPersonalDetailsStatusOk() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		Byte age = 23;
		PersonalDetailsDto perDetDto = PersonalDetailsDto.builder()
				.name("Mati")
				.lastname("Gar")
				.age(age)
				.email("matig@gmail.com")
				.build();
		PersonalDetails perDet = PersonalDetails.builder()
				.name(token)
				.lastname(token)
				.age(age)
				.email("matig@gmail.com")
				.build();
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
		when(userService.getPersonalDetailsByUser()).thenReturn(perDet);
		when(personalDetailsMapper.personalDetailsToPersonalDetailsDto(perDet)).thenReturn(perDetDto);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name",is(perDetDto.getName())))
				.andExpect(jsonPath("$.lastname",is(perDetDto.getLastname())))
				.andExpect(jsonPath("$.age",is(23)))
				.andExpect(jsonPath("$.email",is(perDetDto.getEmail())));
		
		verify(userService).getPersonalDetailsByUser();
		verify(personalDetailsMapper).personalDetailsToPersonalDetailsDto(perDet);
		
	}
	
	void getGetUserForUsernameLikeStatusOk() {
		String token = jwtService.generateToken(USER_AUTH);
		//for authentication filter
		when(userService.loadUserByUsername(USER_AUTH.getUsername())).thenReturn(USER_AUTH);
	}
	
	
	
	
}
