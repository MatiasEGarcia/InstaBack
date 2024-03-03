package com.instaback.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.instaback.util.MessagesUtils;
import com.instaback.util.RSAUtils;

@TestPropertySource("/application-test.properties")
@Import({RSAUtils.class , MessagesUtils.class, ReloadableResourceBundleMessageSource.class})
@DataJpaTest
class MessageDaoTest {

	@Autowired MessageDao messageDao;
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
	
	//countByUserNoWatched
	@Test
	void countByUserNoWatchedAndChatIdSearchByJulioChat1() {
		List<Long[]> data = messageDao.countByUserNoWatchedAndChatId(List.of(1L), "julio");//sqlAddUser3 username
		Long[] longArray = data.get(0); //julio only has 1 chat
		assertEquals(1L, longArray[0]); //chat id
		assertEquals(2L, longArray[1]); //number of messages not watched by user (julio).
	}
	
	@Test
	void countByUserNoWatchedAndChatIdSearchByJulioChat1And2() {
		List<Long[]> data = messageDao.countByUserNoWatchedAndChatId(List.of(1L,2L), "julio");//sqlAddUser1 username
		//fist chat
		Long[] firstChat = data.get(0);
		assertEquals(1L, firstChat[0]); //chat id
		assertEquals(2L, firstChat[1]); //number of messages not watched by user (julio).
		
		//second chat
		Long[] secondChat = data.get(1); 
		assertEquals(2L, secondChat[0]); //chat id
		assertEquals(1L, secondChat[1]); //number of messages not watched by user (julio).
	}
	
	@Test
	void countByUserNoWatchedAndChatIdEmpty() {
		List<Long[]> data = messageDao.countByUserNoWatchedAndChatId(List.of(2L), "rocio");//sqlAddUser2 username
		if(!data.isEmpty()) {
			System.out.println(data);
			fail();
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


























