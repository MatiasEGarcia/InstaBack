package com.instaJava.instaJava.dao;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource("/application-test.properties")
@DataJpaTest
class InvTokenDaoTest {

	@Autowired
	private InvTokenDao invTokenDao;
	
	@Autowired
	private JdbcTemplate jdbc;
	
	@Value("${sql.script.create.invToken}")
	private String sqlAddInvToken;
	
	@BeforeEach
	void setUpDBData() {
		jdbc.update(sqlAddInvToken);
	}

}
