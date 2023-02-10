package com.instaJava.instaJava.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.instaJava.instaJava.entity.User;

public interface UserDao extends JpaRepository<User, Long>{

	User findByUsername(String username);
}
