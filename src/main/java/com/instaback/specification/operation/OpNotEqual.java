package com.instaback.specification.operation;

import com.instaback.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpNotEqual  implements OpI{

	/**
	 * Predicate to search records that column values(casted to string) is not equal to @param reqSearch.value
	 * 
	 * @param root - Type object: User, Like , etc...
	 * @param cb - object to create predicates.
	 * @param reqSearch - has the condition to search.
	 * @return equal predicate object.
	 */
	@Override
	public Predicate getPredicate(Root<?> root, CriteriaBuilder cb, ReqSearch reqSearch) {
		
		if(reqSearch.getJoinTable() != null) {
			return cb.notEqual(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).as(String.class),
					reqSearch.getValue());
		}else {
			return cb.notEqual(root.get(reqSearch.getColumn()).as(String.class), 
					reqSearch.getValue());
		}
		
	}

}
