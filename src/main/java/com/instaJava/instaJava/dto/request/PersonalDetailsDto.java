package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonalDetailsDto implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String name;
	private String lastname;
	private byte age;
	private String email;
}
