package com.instaback.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaback.dto.MessageDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Message;

@Mapper(componentModel = "spring")
public interface MessageMapper {

	MessageDto messageToMessageDto(Message msg);
	
	@Mapping(target = "id" , source = "msg.id")
	@Mapping(target = "userOwner" , source = "msg.userOwner")
	@Mapping(target = "sendedAt" , source = "msg.sendedAt")
	@Mapping(target = "body" , source = "messageDecrypt")
	MessageDto messageEAndMessageDecryptToMessageDto(Message msg, String messageDecrypt);
	
	
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<MessageDto> pageAndPageInfoDtoToResPaginationG(Page<Message> page,PageInfoDto pageInfoDto);
}
