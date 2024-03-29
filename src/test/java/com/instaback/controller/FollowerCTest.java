package com.instaback.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.instaback.dto.request.ReqSearch;
import com.instaback.dto.request.ReqSearchList;
import com.instaback.entity.User;
import com.instaback.enums.FollowStatus;
import com.instaback.enums.GlobalOperationEnum;
import com.instaback.enums.OperationEnum;
import com.instaback.enums.RolesEnum;
import com.instaback.service.JwtService;
import com.instaback.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class FollowerCTest {

	@SuppressWarnings("unused")
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
	@Value("${sql.script.create.follow.statusInProcess}")
	private String sqlAddFollow;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.follow}")
	private String sqlTruncateFollow;
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
	
	private User rociUserAuth = User.builder()
			.id(2L)
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
		jdbc.update(sqlAddFollow);
	}
	
	@Test
	void postSaveStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follow")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.missing-param"))));
		
	}
	@Test
	void postSave() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follow")
				.header("Authorization", "Bearer " + token)
				.param("followedId", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.followStatus", is(FollowStatus.IN_PROCESS.toString()))); //is accepted because user 2 is visible = false
	}
	
	
	@Test
	void postGetAllFollowByBadRequestReqSearchDidntPassed() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follow/findAllBy")
				.contentType(APPLICATION_JSON_UTF8)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message",instanceOf(String.class)));
	}
	@Test
	void postGetAllFollowByBadRequestReqSearchSearchRequestDtoWithBlankOrNullValues() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		ReqSearch reqSearch = ReqSearch.builder()
				.column("")
				.value("")
				.build();
		ReqSearchList reqSearchList = ReqSearchList.builder()
				.reqSearchs(List.of(reqSearch)) 
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follow/findAllBy")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqSearchList)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.['reqSearchs[0].column']",is(messUtils.getMessage("vali.ReqSearch.column-not-blank"))))
				.andExpect(jsonPath("$.details.['reqSearchs[0].value']",is(messUtils.getMessage("vali.ReqSearch.value-not-blank"))))
				.andExpect(jsonPath("$.details.['reqSearchs[0].dateValue']",is(messUtils.getMessage("vali.ReqSearch.dateValue-not-null"))))
				.andExpect(jsonPath("$.details.['reqSearchs[0].operation']",is(messUtils.getMessage("vali.ReqSearch.operation-not-null"))));
	}
	@Test
	void postGetAllFollowByOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		ReqSearch reqSearch = ReqSearch.builder()
				.column("id")
				.value("2")
				.dateValue(false)
				.joinTable("follower")
				.operation(OperationEnum.EQUAL)
				.build();
		ReqSearchList reqSearchList = ReqSearchList.builder()
				.reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follow/findAllBy")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqSearchList)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(1))); 
	}
	@Test
	void postGetAllFollowByNoContent() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		ReqSearch reqSearch = ReqSearch.builder()
				.column("id")
				.value("5")
				.dateValue(false)
				.joinTable("follower")
				.operation(OperationEnum.EQUAL)
				.build();
		ReqSearchList reqSearchList = ReqSearchList.builder()
				.reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/follow/findAllBy")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqSearchList)))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"), messUtils.getMessage("follow.not-found")));
	}
	
	
	@Test
	void putUpdateFollowStatusOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/follow/updateFollowStatus")
				.header("Authorization", "Bearer " + token)
				.param("followStatus", FollowStatus.ACCEPTED.toString())
				.param("id", "1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is("1")))
				.andExpect(jsonPath("$.followStatus", is(FollowStatus.ACCEPTED.toString())));
		
		
	}
	
	@Test
	void putUpdateFollowStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/follow/updateFollowStatus")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.missing-param"))));
	}
	
	
	@Test
	void deleteDeleteByIdOk() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/follow/{id}",1)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("generic.delete-ok"))));
	}
	@Test
	void deleteDeleteByIdNoExistNotFound() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/follow/{id}",2)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error",is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("follow.not-found"))));
	}
	@Test
	void deleteDeleteByIdNotSameFollowerBadRequest() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/follow/{id}",1)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("follow.follower-not-same"))));
	}
	
	//deleteByFollowedId
	@Test
	void deleteDeleteByFollowedIdOk() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/follow/byFollowedId/{id}",1)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("generic.delete-ok"))));
	}
	@Test
	void deleteDeleteByFollowedIdNotFound() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/follow/byFollowedId/{id}",100)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("follow.not-found"))));
	}
	
	
	
	//updateFollowStatusByFollowerById
	@Test
	void putUpdateFollowStatusByFollowerByIdOk() throws Exception{
		String token = jwtService.generateToken(matiasUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/follow/updateFollowStatus/byFollowerId")
				.header("Authorization", "Bearer " + token)
				.param("id", "2")
				.param("followStatus", "ACCEPTED"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.followStatus", is(FollowStatus.ACCEPTED.toString())));
	}
	
	@Test
	void putUpdateFollowStatusByFollowerByIdBadRequest() throws Exception{
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/follow/updateFollowStatus/byFollowerId")
				.header("Authorization", "Bearer " + token)
				.param("id", "1")
				.param("followStatus", "ACCEPTED"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.error",is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("follow.not-found"))));
	}
	
	
	
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncateFollow);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
	
}
