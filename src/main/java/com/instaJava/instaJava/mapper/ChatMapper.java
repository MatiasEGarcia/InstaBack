package com.instaJava.instaJava.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.ChatUser;

@Mapper(componentModel = "spring")
public interface ChatMapper {

	@Mapping(target = "userId" , source = "chatUser.user.userId")
	@Mapping(target = "username" , source = "chatUser.user.username")
	@Mapping(target = "image" , source = "chatUser.user.image")
	@Mapping(target = "visible" , source = "chatUser.user.visible")
	@Mapping(target = "admin" , source = "chatUser.admin")
	UserDto chatUserToUserDto(ChatUser chatUser);
	
	List<UserDto> chatUserListToUserDtoList(List<ChatUser> chatUserList);
	
	@Mapping(target ="chatId", source = "chat.chatId")
	@Mapping(target ="name", source = "chat.name")
	@Mapping(target ="type", source = "chat.type")
	@Mapping(target ="image", source = "chat.image")
	@Mapping(target ="users", source = "chat.chatUsers")
	ChatDto chatToChatDto(Chat chat);

	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<ChatDto> pageAndPageInfoDtoToResPaginationG(Page<Chat> page, PageInfoDto pageInfoDto);
}
