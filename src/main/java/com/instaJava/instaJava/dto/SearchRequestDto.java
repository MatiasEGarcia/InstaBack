package com.instaJava.instaJava.dto;

import java.io.Serializable;

import com.instaJava.instaJava.enums.OperationEnum;

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
	
	String column;
	String value;
	boolean dateValue; //if is a date type I need to handle differently
	String joinTable;
	OperationEnum operation;
	
}
