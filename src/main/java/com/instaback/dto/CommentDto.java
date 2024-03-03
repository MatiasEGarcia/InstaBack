package com.instaback.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.instaback.dto.response.PublicatedImageDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 *
 *	CommentDto with comment info but without PublicatedImage.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommentDto implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String body;
	
	private UserDto ownerUser;
	
	private PublicatedImageDto associatedImg;
	
	private CommentDto parent;
	
	@JsonFormat(shape = Shape.STRING , pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private ZonedDateTime createdAt;
	
	private String associateCN; //number of commentaries associated
}
