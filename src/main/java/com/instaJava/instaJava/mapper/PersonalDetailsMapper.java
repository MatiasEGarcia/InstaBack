package com.instaJava.instaJava.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;



@Mapper(componentModel = "spring")
public interface PersonalDetailsMapper {

	PersonalDetailsDto personalDetailsToPersonalDetailsDto(PersonalDetails per);
	
	PersonalDetails personalDetailsDtoToPersonalDetails(PersonalDetailsDto per);
	
	@Mapping(target="user", source="user")
	@Mapping(target="id", source="user.personalDetails.id")
	PersonalDetails personalDetailsDtoAndUserToPersonalDetails(PersonalDetailsDto per, User user);
}
