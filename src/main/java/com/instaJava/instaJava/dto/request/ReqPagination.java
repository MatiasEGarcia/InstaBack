package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ReqPagination implements Serializable{

	private static final long serialVersionUID = 1L;
	private int page; //not null
	private int pageSize; //not null
	private String sortField; // optional
	private String sortDir; // optional
}
