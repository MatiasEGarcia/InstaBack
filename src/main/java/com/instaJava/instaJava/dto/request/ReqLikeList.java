package com.instaJava.instaJava.dto.request;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto to create many Like records.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqLikeList implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@NotNull
	@Valid
	List<ReqLike> reqLikeList;
}
