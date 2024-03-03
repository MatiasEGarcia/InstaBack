package com.instaback.dto.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.instaback.enums.TypeItemLikedEnum;

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
	
	@NotNull(message= "{vali.itemId-not-null}")
	private Long itemId;
	@NotNull(message= "{vali.decision-not-null}")
	private Boolean decision;//I don't false by default, I want the client to sent a value, so I use Boolean wrapper.
	@JsonIgnore
	private boolean valid;
	@NotNull(message= "{vali.type-not-null}")
	private TypeItemLikedEnum type;
	

}
