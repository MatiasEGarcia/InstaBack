package com.instaJava.instaJava.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;



@Mapper(componentModel = "spring")
public interface PersonalDetailsMapper {

	@Mapping(target="perDetId" , source = "perDetId")
	PersonalDetailsDto personalDetailsToPersonalDetailsDto(PersonalDetails per);
	
	@Mapping(target="perDetId" , source = "perDetId")
	PersonalDetails personalDetailsDtoToPersonalDetails(PersonalDetailsDto per);
	
	@Mapping(target="user", source="user")
	@Mapping(target="perDetId", source="user.personalDetails.perDetId")
	PersonalDetails personalDetailsDtoAndUserToPersonalDetails(PersonalDetailsDto per, User user);
}
