package com.instaJava.instaJava.dto.request;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 *
 *Payload to delete users form a chat.
 *chatId to know which chat should be updated.
 *usersUsername to know which users delete.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqDelUserFromChat implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "{vali.chatId-not-blank}")
	private String chatId;
	
	@NotNull(message = "{vali.usersUsername-list-not-null}")
	private List<String> usersUsername;
}
