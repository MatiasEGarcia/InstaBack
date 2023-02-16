package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import lombok.Data;


@Data
public class ReqLogout implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String token;
}
