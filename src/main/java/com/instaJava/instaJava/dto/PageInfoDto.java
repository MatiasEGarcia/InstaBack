package com.instaJava.instaJava.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PageInfoDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int pageNo;
	
	private int pageSize;
	
	private int totalPages;
	
	private int totalElements;
	
	private String sortField;
	
	private String sortDir;
}

