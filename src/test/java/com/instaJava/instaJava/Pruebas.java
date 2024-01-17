package com.instaJava.instaJava;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.instaJava.instaJava.dao.ChatDao;
import com.instaJava.instaJava.entity.Chat;

@TestPropertySource("/application-test.properties")
@DataJpaTest
class Pruebas {
	@Autowired ChatDao chatDao;
	@Autowired JdbcTemplate jdbc;
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.user.3}")
	private String sqlAddUser3;
	@Value("${sql.script.create.user.4}")
	private String sqlAddUser4;
	@Value("${sql.script.create.chat.group.1}")
	private String sqlAddChatGroup1;
	@Value("${sql.script.create.chat.private.1}")
	private String sqlAddChatPrivate1;
	@Value("${sql.script.create.chatUsers.1}")
	private String sqlAddChatUsers1;
	@Value("${sql.script.create.chatUsers.2}")
	private String sqlAddChatUsers2;
	@Value("${sql.script.create.chatUsers.3}")
	private String sqlAddChatUsers3;
	@Value("${sql.script.create.chatUsers.4}")
	private String sqlAddChatUsers4;
	@Value("${sql.script.create.chatUsers.5}")
	private String sqlAddChatUsers5;
	@Value("${sql.script.create.message.1}")
	private String sqlAddMessage1;
	@Value("${sql.script.create.message.2}")
	private String sqlAddMessage2;
	@Value("${sql.script.create.message.3}")
	private String sqlAddMessage3;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
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
	
	@BeforeEach
	void dbbSetUp() {
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddUser2);
		jdbc.update(sqlAddUser3);
		jdbc.update(sqlAddUser4);
		jdbc.update(sqlAddChatGroup1);
		jdbc.update(sqlAddChatUsers1);
		jdbc.update(sqlAddChatUsers2);
		jdbc.update(sqlAddChatUsers3);
		jdbc.update(sqlAddMessage1); //owner is sqlAddUser1
		jdbc.update(sqlAddMessage2); //owner is sqlAddUser3
		jdbc.update(sqlAddChatPrivate1);
		jdbc.update(sqlAddChatUsers4);
		jdbc.update(sqlAddChatUsers5);
		jdbc.update(sqlAddMessage3); //owner is sqlAddUser2
	}
	
	@Test
	void check() {
		Page<Chat> chatPage = chatDao.findChatsByUser(1L, "matias",PageRequest.of(0, 10));
		
		if(chatPage.getContent().isEmpty()) {
			fail("tambien esta vacio");
		}
		
		
	}

	@AfterEach
	void cleanupDatabase() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncateChats);
		jdbc.execute(sqlTruncateMessages);
		jdbc.execute(sqlTruncateChatUsers);
		jdbc.execute(sqlRefIntegrityTrue);
	};
}
