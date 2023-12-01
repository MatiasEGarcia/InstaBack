package com.instaJava.instaJava.controller;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class NotificationCTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private MessagesUtils messUtils;
	@Autowired
	private JdbcTemplate jdbc;

	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.notification}")
	private String sqlAddNotification;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.notifications}")
	private String sqlTruncateNotifications;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	//this user is in the bdd , because we save it with sqlAddUser1
	private User matiAuth = User.builder()
			.userId(1L)
			.username("matias")
			.password("123456")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	@BeforeEach
	void dbbSetUp() {
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddUser2);
		jdbc.update(sqlAddNotification);
	}
	
	@Test
	void getGetNotificationsByAuthUserWithoutParams() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/notifications/getByAuthUser")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1)));
	
	}
	
	
	@Test
	void deleteDeleteById() throws Exception{
		String token = jwtService.generateToken(matiAuth);
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/notifications/{id}",1)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("generic.delete-ok"))));
	}
	
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncateNotifications);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	

}	