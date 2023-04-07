package com.instaJava.instaJava.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.util.List;

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
import com.instaJava.instaJava.dto.SearchRequestDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.enums.RolesEnum;
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

	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.follower}")
	private String sqlAddFollower;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.followers}")
	private String sqlTruncateFollowers;
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
		jdbc.update(sqlAddFollower);
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
	
	
	@Test
	void postGetFollowersBadRequestReqSearchDidntPassed() throws Exception {
		String token = jwtService.generateToken(userAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follower/findAllBy")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message",instanceOf(String.class)));
	}
	
	@Test
	void postGetFollowersBadRequestReqSearchSearchRequestDtoWithBlankOrNullValues() throws Exception {
		String token = jwtService.generateToken(userAuth);
		SearchRequestDto searchRequestDto = SearchRequestDto.builder()
				.column("")
				.value("")
				.build();
		ReqSearch reqSearch = ReqSearch.builder()
				.searchRequestDtos(List.of(searchRequestDto))
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follower/findAllBy")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqSearch)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.['searchRequestDtos[0].column']",is(messUtils.getMessage("vali.SearchRequestDto.column-not-blank"))))
				.andExpect(jsonPath("$.['searchRequestDtos[0].value']",is(messUtils.getMessage("vali.SearchRequestDto.value-not-blank"))))
				.andExpect(jsonPath("$.['searchRequestDtos[0].dateValue']",is(messUtils.getMessage("vali.SearchRequestDto.dateValue-not-null"))))
				.andExpect(jsonPath("$.['searchRequestDtos[0].operation']",is(messUtils.getMessage("vali.SearchRequestDto.operation-not-null"))));
	}
	
	@Test
	void postGetFolowersOk() throws Exception {
		String token = jwtService.generateToken(userAuth);
		SearchRequestDto searchRequestDto = SearchRequestDto.builder()
				.column("userId")
				.value("2")
				.dateValue(false)
				.joinTable("userFollower")
				.operation(OperationEnum.EQUAL)
				.build();
		ReqSearch reqSearch = ReqSearch.builder()
				.searchRequestDtos(List.of(searchRequestDto))
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follower/findAllBy")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqSearch)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(1))); 
	}
	
	@Test
	void postGetFollowersNoContent() throws Exception {
		String token = jwtService.generateToken(userAuth);
		SearchRequestDto searchRequestDto = SearchRequestDto.builder()
				.column("userId")
				.value("5")
				.dateValue(false)
				.joinTable("userFollower")
				.operation(OperationEnum.EQUAL)
				.build();
		ReqSearch reqSearch = ReqSearch.builder()
				.searchRequestDtos(List.of(searchRequestDto))
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follower/findAllBy")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqSearch)))
				.andExpect(status().isNoContent())
				.andExpect(header().string("Info-header", messUtils.getMessage("mess.not-followers")));
	}
	
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncateFollowers);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
	
}
