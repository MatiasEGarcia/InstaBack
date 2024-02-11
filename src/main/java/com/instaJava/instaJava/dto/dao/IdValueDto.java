package com.instaJava.instaJava.dto.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 
 * @author matia
 *	To get values from database with the signature id , value
 *
 * @param <T>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IdValueDto<T> {

	private Long id;
	
	private T value;
}
