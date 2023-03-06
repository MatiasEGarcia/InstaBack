package com.instaJava.instaJava.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.instaJava.instaJava.dto.response.ResUser;
import com.instaJava.instaJava.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	List<ResUser> UserToResUser(List<User> users);
	
	ResUser UserToResUser(User user);
}
