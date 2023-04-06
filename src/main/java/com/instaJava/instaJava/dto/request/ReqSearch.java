package com.instaJava.instaJava.dto.request;

import java.io.Serializable;
import java.util.List;

import com.instaJava.instaJava.dto.SearchRequestDto;
import com.instaJava.instaJava.enums.GlobalOperationEnum;

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

	private List<SearchRequestDto> searchRequestDtos;
	
	private GlobalOperationEnum globalOperator;
}
