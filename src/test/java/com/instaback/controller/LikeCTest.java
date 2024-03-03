package com.instaback.controller;

import static org.hamcrest.Matchers.is;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaback.dto.request.ReqLike;
import com.instaback.entity.User;
import com.instaback.enums.RolesEnum;
import com.instaback.enums.TypeItemLikedEnum;
import com.instaback.service.JwtService;
import com.instaback.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class LikeCTest {

	@SuppressWarnings("unused")
	private static MockHttpServletRequest request;
	
	@Autowired private MockMvc mockMvc;
	@Autowired private JwtService jwtService;
	@Autowired private JdbcTemplate jdbc;
	@Autowired private MessagesUtils messUtils;
	@Autowired private ObjectMapper objectMapper;
	
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
			.id(1L)
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
		jdbc.update(sqlAddPublicatedImage);
	}
	
	
	@Test
	void postSaveNoReqLikeBadRequest() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/like")
				.contentType(APPLICATION_JSON_UTF8)
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.value-missing-incorrect"))));//creo que este mensaje no deberia ir
	}
	@Test
	void postSaveReqLikeValuesNullEmptyBadRequest() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		ReqLike req= ReqLike.builder().build(); 

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/like")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.itemId", is(messUtils.getMessage("vali.itemId-not-null"))))
				.andExpect(jsonPath("$.details.type", is(messUtils.getMessage("vali.type-not-null"))))
				.andExpect(jsonPath("$.details.decision", is(messUtils.getMessage("vali.decision-not-null"))));
	}
	@Test
	void postSaveOkReturnLike() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		ReqLike req= ReqLike.builder()
				.type(TypeItemLikedEnum.PULICATED_IMAGE)
				.itemId(1L)
				.decision(true)
				.build(); 

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/like")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(req))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is("1")));//the publicatedImage id.
	}
	
	
	
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncatePublicatedImages);
		jdbc.execute(sqlRefIntegrityTrue);
	}
}
