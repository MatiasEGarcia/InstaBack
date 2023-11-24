package com.instaJava.instaJava.dto.request;

import java.io.Serializable;
import java.util.List;

import com.instaJava.instaJava.enums.ChatTypeEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *Dto to create a chat record. 
 * @author matia
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqChat implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String name;//can be null
	@NotNull(message = "{vali.chat-type-not-null}")
	private ChatTypeEnum type;
	@NotNull(message = "{vali.users-list-not-null}")
	private List<String> usersToAdd;//users username
	private List<String> usersToAddAsAdmins;//users username
	
}
