package com.instaback.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String id;
	
	private String body;
	
	private String userOwner;
	
	@JsonFormat(shape= Shape.STRING , pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private ZonedDateTime sendedAt;
}
