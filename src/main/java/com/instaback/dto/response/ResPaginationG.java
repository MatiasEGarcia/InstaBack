package com.instaback.dto.response;

import java.io.Serializable;
import java.util.List;

import com.instaback.dto.PageInfoDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 
 * @author matia
 *	Dto similiar to {@link com.instaback.dto.response.ResListG} but in this case with pagination.
 *
 * @param <T> object type of list.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ResPaginationG<T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private List<T> list;
	private PageInfoDto pageInfoDto;

}
