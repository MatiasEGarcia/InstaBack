package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReqLogin implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
}
