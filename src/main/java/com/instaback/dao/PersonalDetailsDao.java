package com.instaback.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.instaback.entity.PersonalDetails;
import com.instaback.entity.User;

public interface PersonalDetailsDao extends JpaRepository<PersonalDetails, Long>{

	PersonalDetails findByUser(User user);
}
