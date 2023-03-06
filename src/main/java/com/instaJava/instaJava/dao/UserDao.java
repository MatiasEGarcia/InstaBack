package com.instaJava.instaJava.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaJava.instaJava.entity.User;

public interface UserDao extends JpaRepository<User, Long>{

	User findByUsername(String username);
	
	@Query(value= "SELECT * FROM users WHERE username LIKE %:username% LIMIT :limit" , nativeQuery = true)
	List<User> findByUsernameLike(@Param("username")String username,@Param("limit") int limit);

}
