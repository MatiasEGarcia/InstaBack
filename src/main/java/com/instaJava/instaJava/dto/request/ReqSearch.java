package com.instaJava.instaJava.dto.request;

import java.io.Serializable;
import java.util.List;

import com.instaJava.instaJava.dto.SearchRequestDto;
import com.instaJava.instaJava.enums.GlobalOperationEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqSearch implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@NotNull(message="{vali.searchRequestDtos-list-not-null}")
	private List<@Valid SearchRequestDto> searchRequestDtos;
	@NotNull(message = "{vali.globalOperator-not-null}")
	private GlobalOperationEnum globalOperator;
}
