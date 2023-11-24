package com.instaJava.instaJava.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.User;

public interface UserDao extends JpaRepository<User, Long> ,JpaSpecificationExecutor<User>{

	User findByUsername(String username);
	
	List<User> findByUsernameIn(List<String> username);
	
	boolean existsByUsername(String username);
	
}
