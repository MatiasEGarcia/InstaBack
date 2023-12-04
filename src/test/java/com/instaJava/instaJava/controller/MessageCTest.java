package com.instaJava.instaJava.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dto.request.ReqNewMessage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class MessageCTest {
	
	@Autowired private MockMvc mockMvc;
	@Autowired private JwtService jwtService;
	@Autowired private JdbcTemplate jdbc;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private MessagesUtils messUtils;
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.chat.group.1}")
	private String sqlAddChatGroup1;
	@Value("${sql.script.create.chatUsers.1}")
	private String sqlAddChatUsers1;
	@Value("${sql.script.create.chatAdmins.1}")
	private String sqlAddChatAdmins1;
	@Value("${sql.script.create.message.1}")
	private String sqlAddMessage1;
	@Value("${sql.script.delete.message.1}")
	private String sqlDeleteMessage1;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.chats}")
	private String sqlTruncateChats;
	@Value("${sql.script.truncate.chatUsers}")
	private String sqlTruncateChatUsers;
	@Value("${sql.script.truncate.chatAdmins}")
	private String sqlTruncateChatAdmins;
	@Value("${sql.script.truncate.messages}")
	private String sqlTruncateMessages;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	// this user is in the bdd , because we save it with sqlAddUser1
	private User matiasUserAuth = User.builder().userId(1L).username("matias").password("123456")
				.role(RolesEnum.ROLE_USER).build();
	
	@BeforeEach
	void dbbSetUp() {
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddChatGroup1);
		jdbc.update(sqlAddChatUsers1);
		jdbc.update(sqlAddChatAdmins1);
		jdbc.update(sqlAddMessage1);
	}
	
	//create
	@Test
	void createBodyHasNullValuesBadRequest() throws Exception{
		String token = jwtService.generateToken(matiasUserAuth);
		ReqNewMessage req = new ReqNewMessage();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/messages")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(req))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.message" , is(messUtils.getMessage("vali.message-not-blank"))))
				.andExpect(jsonPath("$.details.chatId" , is(messUtils.getMessage("vali.chatId-not-blank"))));
	}
	@Test
	void createNoBodyBadRequest() throws Exception{
		String token = jwtService.generateToken(matiasUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/messages")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.value-missing-incorrect"))));
	}
	@Test
	void create() throws Exception{
		String token = jwtService.generateToken(matiasUserAuth);
		String message = "randomMessage";
		String chatId = "1";
		ReqNewMessage req = new ReqNewMessage(message ,chatId);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/messages")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(req))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.body", is(message)))
				.andExpect(jsonPath("$.body", is(message)));
	}
	
	//getMessagesByChat
	@Test
	void getMessagesByChatWhithoutParamsThereIsMatchesOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		String chatId= "1";
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/messages/{chatId}",chatId)
				.header("Authorization", "Bearer "+ token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.list" , hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("messageId"))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString()))); //default value if the user don't pass any param
	}
	@Test
	void getMessagesByChatWhithoutParamsThereIsNotMatchesNoContent() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		jdbc.update(sqlDeleteMessage1);
		String chatId= "1";
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/messages/{chatId}",chatId)
				.header("Authorization", "Bearer "+ token))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"), messUtils.getMessage("message.group-not-found")));
	}
	
	
	
	
	@AfterEach
	void truncateTables() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncateChats);
		jdbc.execute(sqlTruncateChatUsers);
		jdbc.execute(sqlTruncateChatAdmins);
		jdbc.execute(sqlTruncateMessages);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
