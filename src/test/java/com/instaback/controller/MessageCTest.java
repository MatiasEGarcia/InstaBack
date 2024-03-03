package com.instaback.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

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
import com.instaback.dto.request.ReqNewMessage;
import com.instaback.entity.User;
import com.instaback.enums.RolesEnum;
import com.instaback.service.JwtService;
import com.instaback.util.MessagesUtils;

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
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.user.3}")
	private String sqlAddUser3;
	@Value("${sql.script.create.chat.group.1}")
	private String sqlAddChatGroup1;
	@Value("${sql.script.create.chatUsers.1}")
	private String sqlAddChatUsers1;
	@Value("${sql.script.create.chatUsers.2}")
	private String sqlAddChatUsers2;
	@Value("${sql.script.create.message.1}")
	private String sqlAddMessage1;
	@Value("${sql.script.create.message.2}")
	private String sqlAddMessage2;
	@Value("${sql.script.delete.message.1}")
	private String sqlDeleteMessage1;
	@Value("${sql.script.delete.message.2}")
	private String sqlDeleteMessage2;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.chats}")
	private String sqlTruncateChats;
	@Value("${sql.script.truncate.chatUsers}")
	private String sqlTruncateChatUsers;
	@Value("${sql.script.truncate.messages}")
	private String sqlTruncateMessages;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	// this user is in the bdd , because we save it with sqlAddUser1
	private User matiasUserAuth = User.builder().id(1L).username("matias").password("123456")
				.role(RolesEnum.ROLE_USER).build();
	
	@BeforeEach
	void dbbSetUp() {
		//adding users.
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddUser2);
		jdbc.update(sqlAddUser3);
		//adding chat 1
		jdbc.update(sqlAddChatGroup1);
		//adding users to chat 1
		jdbc.update(sqlAddChatUsers1);
		jdbc.update(sqlAddChatUsers2);
		//adding messages in chat 1
		jdbc.update(sqlAddMessage1);
		jdbc.update(sqlAddMessage2);
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
	
	//getMessagesByChatId
	@Test
	void getMessagesByChatWhithoutParamsThereIsMatchesOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		String chatId= "1";
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/messages/getMessagesByChatId/{id}",chatId)
				.header("Authorization", "Bearer "+ token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.list" , hasSize(2)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(2))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("id"))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString()))); //default value if the user don't pass any param
	}
	@Test
	void getMessagesByChatWhithoutParamsThereIsNotMatchesNoContent() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		jdbc.update(sqlDeleteMessage1);
		jdbc.update(sqlDeleteMessage2);
		String chatId= "1";
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/messages/getMessagesByChatId/{id}",chatId)
				.header("Authorization", "Bearer "+ token))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"), messUtils.getMessage("message.group-not-found")));
	}
	
	//messagesWatched
	@Test
	void putMessagesWatchedStatusOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		Set<String> setIds = Set.of("2");
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/messages/messagesWatched")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(setIds))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("numberOfMessagesNoWatched", is("0"))); //now , there are not messages in this chat not watched by the auth user.
		
	}
	
	//watchedAllByChatId
	@Test
	void putWatchedAllStatusOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/messages/watchedAllByChatId/{id}", 1)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.message" , is(messUtils.getMessage("message.watched-all-in-chat"))));
	}
	
	@AfterEach
	void truncateTables() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncateChats);
		jdbc.execute(sqlTruncateChatUsers);
		jdbc.execute(sqlTruncateMessages);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
