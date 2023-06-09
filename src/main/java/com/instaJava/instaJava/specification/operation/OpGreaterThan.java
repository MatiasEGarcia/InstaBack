package com.instaJava.instaJava.specification.operation;

import com.instaJava.instaJava.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpGreaterThan implements OpI{

	/**
	 * Predicate to search records that column values(casted to string) is greater than @param reqSearch.value.
	 * 
	 * @param root. Type object: User, Like , etc...
	 * @param cb. object to create predicates.
	 * @param reqSearch. has the condition to search.
	 * @return Greater than predicate
	 */
	@Override
	public Predicate getPredicate(Root<?> root, CriteriaBuilder cb,ReqSearch reqSearch){
		if(reqSearch.getJoinTable() != null) {
			return cb.greaterThan(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).as(String.class),
					reqSearch.getValue());
		}else {
			return cb.greaterThan(root.get(reqSearch.getColumn()).as(String.class),
					reqSearch.getValue());
		}
	}

	
	
	
}
