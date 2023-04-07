package com.instaJava.instaJava.dto;

import java.io.Serializable;

import com.instaJava.instaJava.enums.OperationEnum;

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
public class SearchRequestDto implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "{vali.SearchRequestDto.column-not-blank}")
	private String column;
	@NotBlank(message = "{vali.SearchRequestDto.value-not-blank}")
	private String value;
	@NotNull(message = "{vali.SearchRequestDto.dateValue-not-null}")
	private Boolean dateValue; //if is a date type I need to handle differently
	private String joinTable;
	@NotNull(message = "{vali.SearchRequestDto.operation-not-null}")
	private OperationEnum operation;
	
}
