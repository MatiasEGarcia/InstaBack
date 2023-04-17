package com.instaJava.instaJava.dto.request;

import java.io.Serializable;
import java.util.List;

import com.instaJava.instaJava.enums.GlobalOperationEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//this class has a list of ReqSearch
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqSearchList implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@NotNull(message="{vali.reqSearchs-not-null}")
	@Valid
	private List< ReqSearch> reqSearchs;
	@NotNull(message = "{vali.globalOperator-not-null}")
	private GlobalOperationEnum globalOperator;
}
