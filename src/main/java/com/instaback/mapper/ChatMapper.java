package com.instaback.mapper;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import com.instaback.dto.ChatDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.dto.UserDto;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Chat;
import com.instaback.entity.ChatUser;
import com.instaback.entity.User;

@Mapper(componentModel = "spring")
public interface ChatMapper {

	@Mapping(target = "id" , source = "chatUser.user.id")
	@Mapping(target = "username" , source = "chatUser.user.username")
	@Mapping(target = "image" , source = "chatUser.user.image")
	@Mapping(target = "visible" , source = "chatUser.user.visible")
	@Mapping(target = "admin" , source = "chatUser.admin")
	UserDto chatUserToUserDto(ChatUser chatUser);
	
	List<User> chatUserListToUserList(List<ChatUser> chatUserList);
	
	@Named("basicChatToChatDto")
	@Mapping(target ="id", source = "chat.id")
	@Mapping(target ="name", source = "chat.name")
	@Mapping(target ="type", source = "chat.type")
	@Mapping(target ="image", source = "chat.image")
	@Mapping(target ="users", source = "chat.chatUsers")
	ChatDto chatToChatDto(Chat chat);
	
	@Mapping(target ="id", source = "chat.id")
	@Mapping(target ="name", source = "chat.name")
	@Mapping(target ="type", source = "chat.type")
	@Mapping(target ="image", source = "chat.image")
	@Mapping(target ="users", source = "chat.chatUsers")
	void chatToChatDto(Chat chat, @MappingTarget ChatDto chatDto);
	
	@Mapping(target ="id", source = "chat.id")
	@Mapping(target ="name", source = "chat.name")
	@Mapping(target ="type", source = "chat.type")
	@Mapping(target ="image", source = "chat.image")
	@Mapping(target ="users", source = "chat.chatUsers")
	@Mapping(target = "messagesNoWatched", source = "messagesNoWatched")
	ChatDto chatAndMessagesNoWatchedToChatDto(Chat chat, Long messagesNoWatched);
	
	@IterableMapping(qualifiedByName = "basicChatToChatDto")
	List<ChatDto> chatListToChatDtoList(List<Chat> chatList);
	
	@IterableMapping(qualifiedByName = "basicChatToChatDto")
	void chatListToChatDtoList(List<Chat> chatList, @MappingTarget List<ChatDto> chatDtoList);
	

	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<ChatDto> pageAndPageInfoDtoToResPaginationG(Page<Chat> page, PageInfoDto pageInfoDto);
	
}
