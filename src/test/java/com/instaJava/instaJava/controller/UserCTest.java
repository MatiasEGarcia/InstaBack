package com.instaJava.instaJava.controller;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.io.IOException;
import java.util.Base64;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dao.InvTokenDao;
import com.instaJava.instaJava.dao.PersonalDetailsDao;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.request.ReqLogout;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.RolesEnum;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.service.UserService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class UserCTest {
	
	private static MockHttpServletRequest request;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private MessagesUtils messUtils;
	@Autowired
	private JdbcTemplate jdbc;
	@Autowired
	private UserDao userDao;
	@Autowired
	private InvTokenDao invTokenDao;
	@Autowired
	private PersonalDetailsDao personalDetailsDao;
	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.personalDetails}")
	private String sqlTruncatePersonalDetails;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	//this user is in the bdd , because we save it with sqlAddUser1
	private User USER_AUTH = User.builder()
			.userId(1L)
			.username("matias")
			.password("123456")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	
	@BeforeAll
	static void mockSetup() throws IOException {
		request = new MockHttpServletRequest();
	}

	@BeforeEach
	void dbbSetUp() {
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddUser2);
	}

	@Test
	void postUploadImageStatusOk() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		String token = jwtService.generateToken(USER_AUTH);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/image")
				.file(img)
				.header("Authorization","Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.image64",equalToIgnoringCase(imgBase64)));
	}	
	

	@Test
	void postUploadImageStatusBadRequst() throws Exception {
		//THe type of the archive is wrong
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				"text/plain", 
		        "Hello, World!".getBytes()
		      );
		String token = jwtService.generateToken(USER_AUTH);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/image")
				.file(img)
				.header("Authorization","Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.file",is(messUtils.getMessage("vali.image"))));
	}
	
	@Test
	void getDownloadImageStatusOk() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		String token = jwtService.generateToken(USER_AUTH);
		USER_AUTH.setImage(imgBase64);
		userDao.save(USER_AUTH);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/image")
				.header("Authorization","Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.image64",is(imgBase64)));
	}
	
	@Test
	void postLogoutStatusOk() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
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
		
		assertTrue(invTokenDao.existsByToken(reqLogout.getToken()));
		assertTrue(invTokenDao.existsByToken(reqLogout.getRefreshToken()));
	}
	
	@Test
	void postLogoutStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		ReqLogout reqLogout=ReqLogout.builder().build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/logout")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqLogout)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.token",is(messUtils.getMessage("vali.token-not-blank"))))
				.andExpect(jsonPath("$.refreshToken", is(messUtils.getMessage("vali.refreshToken-not-blank"))));
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
		
		assertNotNull(personalDetailsDao.findByUser(USER_AUTH));
	}
	
	@Test
	void postSavePersonalDetailsStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		PersonalDetailsDto perDetDto = PersonalDetailsDto.builder().build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(perDetDto)))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.name",is(messUtils.getMessage("vali.name-not-blank"))))
				.andExpect(jsonPath("$.lastname",is(messUtils.getMessage("vali.lastname-not-blank"))))
				.andExpect(jsonPath("$.age",is(messUtils.getMessage("vali.age-range"))))
				.andExpect(jsonPath("$.email",is(messUtils.getMessage("vali.email-not-blank"))));
	
		assertNull(personalDetailsDao.findByUser(USER_AUTH));
	}
	
	@Test
	void getGetPersonalDetailsStatusOk() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		Byte age = 23;
		PersonalDetails perDet = PersonalDetails.builder()
				.name(token)
				.lastname(token)
				.age(age)
				.email("matig@gmail.com")
				.user(USER_AUTH)
				.build();
		personalDetailsDao.save(perDet);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name",is(perDet.getName())))
				.andExpect(jsonPath("$.lastname",is(perDet.getLastname())))
				.andExpect(jsonPath("$.age",is(23)))
				.andExpect(jsonPath("$.email",is(perDet.getEmail())));
	}
	
	@Test
	void getGetUserForUsernameLikeStatusOk() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/like")
				.header("Authorization", "Bearer " + token)
				.param("username", "mat")
				.param("limit", "5"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$",hasSize(1)));
	}
	
	@Test
	void getGetUserForUsernameLikeStatusNoContent() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/like")
				.header("Authorization", "Bearer " + token)
				.param("username", "Jul")
				.param("limit", "5"))
				.andExpect(status().isNoContent())
				.andExpect(header().string("moreInfo",messUtils.getMessage("mess.there-no-users")));
	}
	
	@Test
	void getGetUserForUsernameLikeStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(USER_AUTH);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/like")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.field", is("username")));
	}
	
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncatePersonalDetails);
		jdbc.execute(sqlRefIntegrityTrue);
	}

}
