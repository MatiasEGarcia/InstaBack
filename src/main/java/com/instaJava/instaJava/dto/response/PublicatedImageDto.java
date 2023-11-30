package com.instaJava.instaJava.dto.response;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.instaJava.instaJava.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 *	Dto to send {@link com.instaJava.instaJava.entity.PublicatedImage} data to the client.
 * With createdAt as String and only the userOwner name.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PublicatedImageDto implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private ZonedDateTime  createdAt;
	
	private String image;
	
	private String description;
	
	private UserDto userOwner;
}