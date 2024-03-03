package com.instaback.dto.response;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto to return a list of any type to the client.
 *
 * @param <T> object type of list.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResListG<T> implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private List<T> list;
}
