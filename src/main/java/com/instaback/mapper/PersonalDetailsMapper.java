package com.instaback.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.instaback.dto.PersonalDetailsDto;
import com.instaback.entity.PersonalDetails;
import com.instaback.entity.User;



@Mapper(componentModel = "spring")
public interface PersonalDetailsMapper {

	PersonalDetailsDto personalDetailsToPersonalDetailsDto(PersonalDetails per);
	
	PersonalDetails personalDetailsDtoToPersonalDetails(PersonalDetailsDto per);
	
	@Mapping(target="user", source="user")
	@Mapping(target="id", source="user.personalDetails.id")
	PersonalDetails personalDetailsDtoAndUserToPersonalDetails(PersonalDetailsDto per, User user);
}
