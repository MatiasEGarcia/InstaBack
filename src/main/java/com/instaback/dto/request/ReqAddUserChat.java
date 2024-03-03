package com.instaback.dto.request;

import java.io.Serializable;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Object to add users on chat.
 * chatId - to know where to add users.
 * users - users to add.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Validated
public class ReqAddUserChat implements Serializable{

	private static final long serialVersionUID = 1L;

	@NotBlank(message = "{vali.chatId-not-blank}")
	private String chatId;
	
	@NotNull(message = "{vali.users-list-not-null}")
	@Valid
	private List<ReqUserChat> users;
	
}
