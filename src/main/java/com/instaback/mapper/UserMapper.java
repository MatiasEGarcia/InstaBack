package com.instaback.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaback.dto.PageInfoDto;
import com.instaback.dto.UserDto;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserDto userToUserDto(User user);
	
	List<UserDto> userListToUserDtoList(List<User> users);
	
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<UserDto> pageAndPageInfoDtoToResPaginationG(Page<User> page,PageInfoDto pageInfoDto);
}
