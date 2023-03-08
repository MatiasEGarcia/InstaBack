package com.instaJava.instaJava.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource("/application-test.properties")
@DataJpaTest
class UserDaoTest {
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private JdbcTemplate jdbc;
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	
	@BeforeEach
	void setUpDBData() {
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddUser2);
	}
	
	@Test
	void usernameLikeReturnNotEmpty() {
		assertTrue(!userDao.findByUsernameLike("mati", 5).isEmpty());
	}
	
	@Test
	void usernameLikeReturnEmpty() {
		assertTrue(userDao.findByUsernameLike("julio", 5).isEmpty());
	}

}
