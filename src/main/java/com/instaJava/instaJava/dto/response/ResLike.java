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

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResLike implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private Long likeId;
	
	private TypeItemLikedEnum itemType;
	
	private Long itemId;
	
	private boolean decision;
	
	private ResUser ownerLike;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
	private ZonedDateTime likedAt;
}
