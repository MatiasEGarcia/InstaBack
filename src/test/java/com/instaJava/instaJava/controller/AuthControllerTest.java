package com.instaJava.instaJava.controller;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.request.ReqLogin;
import com.instaJava.instaJava.dto.request.ReqRefreshToken;
import com.instaJava.instaJava.dto.request.ReqUserRegistration;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.service.AuthService;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

	private static MockHttpServletRequest request;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JdbcTemplate jdbc;
	@Autowired
	private AuthService authService;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserDao userDao;
	@Autowired
	private MessagesUtils messUtils;
	@Autowired
	private JwtService jwtService;
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;

	@BeforeAll
	static void mockSetap() {
		request = new MockHttpServletRequest();
	}
	
	@BeforeEach
	void dbbSetUp() {
		jdbc.execute(sqlAddUser1);
	}
	
	@Test
	void postRegisterStatusOk() throws Exception {
		ReqUserRegistration reqUserRegistration = ReqUserRegistration.builder()
				.username("random")
				.password("random")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqUserRegistration)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.token",instanceOf(String.class)))
				.andExpect(jsonPath("$.refreshToken",instanceOf(String.class)));
		
		assertNotNull(userDao.findByUsername("random"));
	}
	
	@Test
	void postRegisterStatusBadRequest() throws Exception {
		ReqUserRegistration reqUserRegistration = ReqUserRegistration.builder().build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqUserRegistration)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.username",is(messUtils.getMessage("vali.username-not-blank"))))
				.andExpect(jsonPath("$.password",is(messUtils.getMessage("vali.password-not-blank"))));
	}
	
	@Test
	void postAuthenticateStatusOk() throws Exception {
		//this user data is in the -> application-test.properties
		ReqLogin reqLogin = ReqLogin.builder()
				.username("matias")
				.password("123456")
				.build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqLogin)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.token",instanceOf(String.class)))
				.andExpect(jsonPath("$.refreshToken",instanceOf(String.class)));
	}
	
	@Test
	void getrefreshTokenStatusOk() throws Exception {
		UserDetails user = User.builder()
				.username("matias") //username from -> sqlAddUser1
				.build(); 
		ReqRefreshToken reqRefreshToken = ReqRefreshToken.builder()
				.token(jwtService.generateToken(user))
				.refreshToken(jwtService.generateRefreshToken(user))
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/refreshToken")
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqRefreshToken)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.token",instanceOf(String.class)))
				.andExpect(jsonPath("$.refreshToken",instanceOf(String.class)));
		
	}
	
	@Test
	void getrefreshTokenStatusBadRequest() throws Exception { 
		ReqRefreshToken reqRefreshToken = ReqRefreshToken.builder().build();
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/refreshToken")
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqRefreshToken)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.token",is(messUtils.getMessage("vali.token-not-blank"))))
				.andExpect(jsonPath("$.refreshToken",is(messUtils.getMessage("vali.refreshToken-not-blank"))));
		
	}
	
	
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
}
