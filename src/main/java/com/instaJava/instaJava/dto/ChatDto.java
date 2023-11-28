package com.instaJava.instaJava.dto;

import java.io.Serializable;
import java.util.List;

import com.instaJava.instaJava.dto.response.ResUser;
import com.instaJava.instaJava.enums.ChatTypeEnum;

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

	private String chatId;
	
	private String name;
	
	private ChatTypeEnum type;
	
	private String image;
	
	private List<ResUser> users;
	
	private List<ResUser> admins;
}
