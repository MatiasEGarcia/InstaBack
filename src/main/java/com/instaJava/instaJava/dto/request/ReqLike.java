package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto object to create a Like record.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqLike implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@NotNull(message= "{vali.type-not-null}")
	private TypeItemLikedEnum type;
	private long itemId;
	@NotNull(message= "{vali.decision-not-null}")
	private Boolean decision;
	@JsonIgnore
	private boolean valid;
	

}
