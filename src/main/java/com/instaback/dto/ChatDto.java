package com.instaback.dto;

import java.io.Serializable;
import java.util.List;

import com.instaback.enums.ChatTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatDto implements Serializable{

	private static final long serialVersionUID = 1L;

	private String id;
	
	private String name;
	
	private ChatTypeEnum type;
	
	private String image;
	
	private List<UserDto> users;
	
	private String messagesNoWatched;//number of messages that the authenticated user didn't watched yet.
	
	private String lastMessage;
	
	public ChatDto(String id) {
		this.id = id;
	}
	
}
