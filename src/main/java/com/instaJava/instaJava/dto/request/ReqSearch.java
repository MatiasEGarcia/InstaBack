package com.instaJava.instaJava.dto.request;

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
public class ReqSearch implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "{vali.ReqSearch.column-not-blank}")
	private String column;
	@NotBlank(message = "{vali.ReqSearch.value-not-blank}")
	private String value;
	@NotNull(message = "{vali.ReqSearch.dateValue-not-null}")
	private Boolean dateValue; //if is a date type I need to handle differently
	private String joinTable;
	@NotNull(message = "{vali.ReqSearch.operation-not-null}")
	private OperationEnum operation;
	
}
