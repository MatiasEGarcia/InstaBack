package com.instaJava.instaJava.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


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
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class LikeCTest {

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
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.publicatedImage}")
	private String sqlAddPublicatedImage;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.publicatedImages}")
	private String sqlTruncatePublicatedImages;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	//this user is in the bdd , because we save it with sqlAddUser1
	private  User matiasUserAuth = User.builder()
			.userId(1L)
			.username("matias")
			.password("123456")
			.role(RolesEnum.ROLE_USER)
			.build();
	//this user is in the bdd , because we save it with sqlAddUser2
	private User rociUserAuth = User.builder()
			.userId(2L)
			.username("rocio")
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
	void testSaveBadRequestParamsNotPassed() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/like")
				.header("Authorization", "Bearer "+ token))
				.andExpect(status().isBadRequest())
		        .andExpect(jsonPath("$.field",is("type")));
		//this is for the first param that the client didn't pass, if this is passed, but not the next param, then the error will be with field == param name
	}
	
	@Test
	void testSaveOk() throws Exception {
		jdbc.execute(sqlAddPublicatedImage); //if the record does not exist in the db, the save request will throw an exception
		String token = jwtService.generateToken(matiasUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/like")
				.header("Authorization", "Bearer "+ token)
				.param("type", TypeItemLikedEnum.PULICATED_IMAGE.toString())
				.param("itemId", "1")
				.param("decision", "true"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.likeId", is(1)))
				.andExpect(jsonPath("$.itemType", is(TypeItemLikedEnum.PULICATED_IMAGE.toString())))
				.andExpect(jsonPath("$.itemId", is(1)))
				.andExpect(jsonPath("$.decision", is(true)))
				.andExpect(jsonPath("$.ownerLike.username", is("matias")));
	}

	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncatePublicatedImages);
		jdbc.execute(sqlRefIntegrityTrue);
	}
}
