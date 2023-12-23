package com.instaJava.instaJava.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dto.request.ReqAddUserChat;
import com.instaJava.instaJava.dto.request.ReqCreateChat;
import com.instaJava.instaJava.dto.request.ReqDelUserFromChat;
import com.instaJava.instaJava.dto.request.ReqUserChat;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.ChatTypeEnum;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class ChatCTest {

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
	@Value("${sql.script.create.user.3}")
	private String sqlAddUser3;
	@Value("${sql.script.create.user.4}")
	private String sqlAddUser4;
	@Value("${sql.script.create.follow.statusAccepted.1}")
	private String sqlAddFollowStatusAccepted1;
	@Value("${sql.script.create.follow.statusAccepted.2}")
	private String sqlAddFollowStatusAccepted2;
	@Value("${sql.script.create.chat.group.1}")
	private String sqlAddChatGroup1;
	@Value("${sql.script.create.chatUsers.1}")
	private String sqlAddChatUsers1;
	@Value("${sql.script.create.chatUsers.2}")
	private String sqlAddChatUsers3;
	@Value("${sql.script.create.chatUsers.3}")
	private String sqlAddChatUsers4;
	@Value("${sql.script.delete.chat.group.1}")
	private String sqlDeleteChat1;
	@Value("${sql.script.delete.chat.group.1.users}")
	private String sqlDeleteChatGroupAllUsers1;
	@Value("${sql.script.delete.chatUsers.3}")
	private String sqlDeleteChatUser3;
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
	@Value("${sql.script.truncate.follow}")
	private String sqlTruncateFollow;
	@Value("${sql.script.truncate.chats}")
	private String sqlTruncateChats;
	@Value("${sql.script.truncate.messages}")
	private String sqlTruncateMessages;
	@Value("${sql.script.truncate.chatUsers}")
	private String sqlTruncateChatUsers;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;

	// this user is in the bdd , because we save it with sqlAddUser1
	private User matiasUserAuth = User.builder().userId(1L).username("matias").password("123456")
			.role(RolesEnum.ROLE_USER).build();
	private User julioUserAuth = User.builder().userId(3L).username("julio").password("123456")
			.role(RolesEnum.ROLE_USER).build();

	@BeforeEach
	void dbbSetUp() {
		// users entities
		jdbc.execute(sqlAddUser1);
		jdbc.execute(sqlAddUser2);
		jdbc.execute(sqlAddUser3);
		jdbc.execute(sqlAddUser4);
		// user1 follow user3 and user4, is accepted.
		jdbc.execute(sqlAddFollowStatusAccepted1);
		jdbc.execute(sqlAddFollowStatusAccepted2);
		// chat group with user1 , user3 and user4
		jdbc.execute(sqlAddChatGroup1);
		jdbc.execute(sqlAddChatUsers1);
		jdbc.execute(sqlAddChatUsers3);
		jdbc.execute(sqlAddChatUsers4);
		//ading messages in chat 1
		jdbc.execute(sqlAddMessage1);
		jdbc.execute(sqlAddMessage2);
	}

	// getChatsByAuthUser
	@Test
	void getGetChatsByAuthUserResponseStatusOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chats/{pageNo}/{pageSize}",0,20).header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.list", hasSize(1)));
	}
	
	@Test
	void getGetChatsByAuthUserResponseStatusNoContent() throws Exception {
		// for some reason, delete on cascade doesn't work in h2 ,so I need to delete
		// manually.
		jdbc.update(sqlDeleteMessage1);
		jdbc.update(sqlDeleteMessage2);
		jdbc.update(sqlDeleteChatGroupAllUsers1);
		jdbc.update(sqlDeleteChat1);// we delete the only chat that user1(matias) has
		String token = jwtService.generateToken(matiasUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chats/{pageNo}/{pageSize}",0,20).header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"),
						is(messUtils.getMessage("chat.group-not-found"))));
	}

	// setImage
	@Test
	void putSetImageWorngFileTypeResponseStatusBadRequest() throws Exception {
		// THe type of the archive is wrong
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", "text/plain", "Hello, World!".getBytes());
		String token = jwtService.generateToken(matiasUserAuth);
		// don't send new image so response should be bad request
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/chats/image/{chatId}", 1).file(img)
				.header("Authorization", "Bearer " + token).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.type-incorrect"))))
				.andExpect(jsonPath("$.details.file", is(messUtils.getMessage("vali.image"))));
	}

	@Test
	void putSetImageResponseStatusOk() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE,
				"Hello, World!".getBytes());
		String token = jwtService.generateToken(matiasUserAuth);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/chats/image/{chatId}", 1).file(img)
				.header("Authorization", "Bearer " + token).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.chatId", is("1")));
	}

	// create
	@Test
	void postCreateBodyinfoNullResponseStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		// we don't send type attribute either attribute usersToAdd
		ReqCreateChat reqChat = ReqCreateChat.builder().build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/chats").header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqChat)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.type", is(messUtils.getMessage("vali.chat-type-not-null"))))
				.andExpect(jsonPath("$.details.usersToAdd", is(messUtils.getMessage("vali.users-list-not-null"))));
	}

	@Test
	void postCreateResponseStatusOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		ReqUserChat reqUserChat = ReqUserChat.builder().username(julioUserAuth.getUsername()).admin(false).build();
		// we don't send type attribute either attribute usersToAdd
		ReqCreateChat reqChat = ReqCreateChat.builder().type(ChatTypeEnum.PRIVATE).usersToAdd(List.of(reqUserChat))
				.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/chats").header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqChat)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.chatId", is("2"))); // is the second chat to be created, it depends of how many I
															// add in dbbSetUp for each.
	}

	// setName
	@Test
	void putSetNameResponseStatusOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		String newName = "newGroupName";

		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/chats/name/{name}/{id}", newName, 1).header("Authorization",
				"Bearer " + token)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.chatId", is("1"))).andExpect(jsonPath("$.name", is(newName)));
	}

	@Test
	void putSetNameChatNotFoundResponseStatusNotFound() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);
		String newName = "newGroupName";

		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/chats/name/{name}/{id}", newName, 100)
				.header("Authorization", "Bearer " + token)).andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("chat.not-found"))));
	}

	// delete
	@Test
	void deleteResponseStatusOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/v1/chats/{id}", 1).header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("generic.delete-ok"))));
	}

	@Test
	void deleteChatNoExistsResponseStatusNotFound() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/v1/chats/{id}", 100).header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("chat.not-found"))));
	}

	// getUsersByChatId
	@Test
	void getGetUsersByChatIdResponseStatusOk() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chats/{id}", 1).header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$", hasSize(3)));
	}

	@Test
	void getGetUsersByChatIdChatNoExistsResponseStatusNotFound() throws Exception {
		String token = jwtService.generateToken(matiasUserAuth);

		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/v1/chats/{id}", 100).header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("chat.not-found"))));
	}
 
	//addUsers 
	@Test
	void postAddUsersChatIdBlankUsersNullResponseStatusBadRequest() throws Exception{
		String token = jwtService.generateToken(matiasUserAuth);
		ReqAddUserChat reqAddUserChat = new ReqAddUserChat();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/chats/add")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(reqAddUserChat)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.chatId" , is(messUtils.getMessage("vali.chatId-not-blank"))))
				.andExpect(jsonPath("$.details.users", is(messUtils.getMessage("vali.users-list-not-null"))));
	}
	 
	@Test
	void postAddUsersResponseStatusOk() throws Exception{
			jdbc.execute(sqlDeleteChatUser3); //we delete the Elda user who is already in chat, now we can add it.
			String token = jwtService.generateToken(matiasUserAuth);
			ReqUserChat reqUserChat = new ReqUserChat("Elda",true);
			ReqAddUserChat reqAddUserChat = new ReqAddUserChat("1", List.of(reqUserChat));
			
			mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/chats/add")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(objectMapper.writeValueAsString(reqAddUserChat)))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
					.andExpect(jsonPath("$.chatId", is("1")))
					.andExpect(jsonPath("$.users" , hasSize(3)));
	}
	
	//quitUsers
	@Test
	void deleteQuitUsersChatIdBlankUsersUsernameNullResponseStatusBadRequest() throws Exception{
			String token = jwtService.generateToken(matiasUserAuth);
			ReqDelUserFromChat reqDelUserFromChat = new ReqDelUserFromChat();
			
			mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/chats/quit")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(objectMapper.writeValueAsString(reqDelUserFromChat)))
					.andExpect(status().isBadRequest())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
					.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
					.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.body-not-fulfilled"))))
					.andExpect(jsonPath("$.details.chatId" , is(messUtils.getMessage("vali.chatId-not-blank"))))
					.andExpect(jsonPath("$.details.usersUsername", is(messUtils.getMessage("vali.usersUsername-list-not-null"))));
	}
	
	@Test
	void deleteQuitUsersResponseStatusOk() throws Exception{
			String token = jwtService.generateToken(matiasUserAuth);
			ReqDelUserFromChat reqDelUserFromChat = new ReqDelUserFromChat("1" , List.of("Elda"));
			
			mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/chats/quit")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(objectMapper.writeValueAsString(reqDelUserFromChat)))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
					.andExpect(jsonPath("$.chatId", is("1")))
					.andExpect(jsonPath("$.users" , hasSize(2)));
	}
	
	@Test
	void putchangeAdminStatusResponseStatusOk() throws Exception{
		String token = jwtService.generateToken(matiasUserAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/chats/adminStatus/{chatId}/{userId}",1,3)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.chatId", is("1")))
				.andExpect(jsonPath("$.users[1].userId",is("3")))//user with id 3 is the second in the list of users
				.andExpect(jsonPath("$.users[1].admin",is(true)));
	}
	
	
	
	
	@AfterEach
	void truncateTables() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateFollow);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncateMessages);
		jdbc.execute(sqlTruncateChatUsers);
		jdbc.execute(sqlTruncateChats);
		jdbc.execute(sqlRefIntegrityTrue);
	}
}
