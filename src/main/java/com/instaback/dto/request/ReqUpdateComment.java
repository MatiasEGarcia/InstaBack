package com.instaback.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqUpdateComment implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@NotNull(message ="{vali.id-not-null}")
	private Long id;
	@NotBlank(message = "{vali.body-not-blank}")
	private String body;
}
