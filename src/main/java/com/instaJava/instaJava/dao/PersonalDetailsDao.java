package com.instaJava.instaJava.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;

public interface PersonalDetailsDao extends JpaRepository<PersonalDetails, Long>{

	PersonalDetails findByUser(User user);
}
