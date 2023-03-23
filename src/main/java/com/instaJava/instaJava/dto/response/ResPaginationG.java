package com.instaJava.instaJava.dto.response;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ResPaginationG<T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private List<T> list;
	private int actualPage;
	private int pageSize;
	private int totalPages;
	private int totalElements;
	private String sortField;
	private String sortDir;

}
