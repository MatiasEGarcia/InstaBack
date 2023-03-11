package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResUser implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String username;
	
	private String image;
}