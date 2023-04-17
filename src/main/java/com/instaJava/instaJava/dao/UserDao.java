package com.instaJava.instaJava.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.User;

public interface UserDao extends JpaRepository<User, Long> ,JpaSpecificationExecutor<User>{

	User findByUsername(String username);
	
	boolean existsByUsername(String username);
	
}
