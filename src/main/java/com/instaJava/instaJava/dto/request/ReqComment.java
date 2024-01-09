package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqComment implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "{vali.body-not-blank}")
	private String body; // comment content.
	
	private String parentId; // root comment
	
	@NotBlank(message = "{vali.publImgId-not-blank}")
	private String publImgId; // publicated image where the comment was written.
}
