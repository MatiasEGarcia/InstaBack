package com.instaback.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaback.entity.User;

public interface UserDao extends JpaRepository<User, Long> ,JpaSpecificationExecutor<User>{

	User findByUsername(String username);
	
	List<User> findByUsernameIn(Set<String> username);
	
	boolean existsByUsername(String username);
	
}
