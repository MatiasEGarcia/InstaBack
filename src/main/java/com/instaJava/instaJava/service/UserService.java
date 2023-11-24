package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;

public interface UserService {

	User save(User user);
	
	void updateImage(MultipartFile file); //will return the new image
	
	String getImage();
	
	Optional<PersonalDetails> getPersonalDetailsByUser();
	
	PersonalDetails savePersonalDetails(PersonalDetailsDto personalDetailsDto);
	
	User changeVisible();
	
	User getByPrincipal();
	
	Optional<User> getById(Long id);
	
	List<User> getByUsernameIn(List<String> usernameList);
	
	Optional<User> getByUsername(String username);
	
	Optional<User> getOneUserOneCondition(ReqSearch reqSearch);
	
	Optional<User> getOneUserManyConditions(ReqSearchList reqSearchList);
	
	Page<User> getManyUsersOneCondition(PageInfoDto pageInfoDto,ReqSearch reqSearch);
	
	Page<User> getManyUsersManyConditions(PageInfoDto pageInfoDto,ReqSearchList reqSearchList);
	
	boolean existsByUsername(String username);
	
	boolean existsOneCondition(ReqSearch reqSearch);
	
	boolean existsManyConditions(ReqSearchList reqSearchList);
}
