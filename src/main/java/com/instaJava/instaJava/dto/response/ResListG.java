package com.instaJava.instaJava.dto.response;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResListG<T> implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private List<T> list;
}
