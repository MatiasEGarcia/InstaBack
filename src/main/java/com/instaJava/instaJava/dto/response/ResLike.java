package com.instaJava.instaJava.dto.response;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto to return {@link com.instaJava.instaJava.entity.Like} data to the client.
 * With likedAt as string instead ZonedDateTIme
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResLike implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String likeId;
	
	private TypeItemLikedEnum itemType;
	
	private String itemId;
	
	private boolean decision;
	
	private ResUser ownerLike;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
	private ZonedDateTime likedAt;
}
