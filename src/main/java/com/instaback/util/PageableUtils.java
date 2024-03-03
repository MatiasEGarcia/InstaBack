package com.instaback.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.instaback.dto.PageInfoDto;

@Component
public class PageableUtils {
	
	/**
	 * Method to create Pageable info with PageInfoDto.
	 * 
	 * @param pageInfoDto. It has pagination info.
	 * @return Pageable object 
	 */
	public Pageable getPageable(PageInfoDto pageInfoDto) {
		Sort sort = pageInfoDto.getSortDir().equals(Sort.Direction.ASC)
				? Sort.by(pageInfoDto.getSortField()).ascending()
				: Sort.by(pageInfoDto.getSortField()).descending();
		 return  PageRequest.of(
				pageInfoDto.getPageNo(),
				pageInfoDto.getPageSize(), sort);
	}

}
