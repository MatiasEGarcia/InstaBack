package com.instaJava.instaJava.controller;

import static org.hamcrest.Matchers.is;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.mapper.FollowerMapper;
import com.instaJava.instaJava.service.FollowerService;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class FollowerCTest {

	private static MockHttpServletRequest request;
	
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private MessagesUtils messUtils;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JdbcTemplate jdbc;
	@Autowired 
	private FollowerService follService;
	@Autowired
	private FollowerMapper follMapper;

	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	//this user is in the bdd , because we save it with sqlAddUser1
	private  User userAuth = User.builder()
			.userId(1L)
			.username("matias")
			.password("123456")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	@BeforeAll
	static void mockSetup() {
		request = new MockHttpServletRequest();
	}

	@BeforeEach
	void dbbSetUp() {
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddUser2);
	}
	
	@Test
	void postSaveStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(userAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follower/save")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.field", is("followed")));
		
	}
	
	@Test
	void postSave() throws Exception {
		String token = jwtService.generateToken(userAuth);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follower/save")
				.header("Authorization", "Bearer " + token)
				.param("followed", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.followStatus", is(FollowStatus.ACCEPTED.toString()))); //is accepted because user 2 is visible = true
	}
	
	
	
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
	
}
