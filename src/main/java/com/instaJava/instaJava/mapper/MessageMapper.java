package com.instaJava.instaJava.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Message;

@Mapper(componentModel = "spring")
public interface MessageMapper {

	MessageDto messageToMessageDto(Message msg);
	
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<MessageDto> pageAndPageInfoDtoToResPaginationG(Page<Message> page,PageInfoDto pageInfoDto);
}
