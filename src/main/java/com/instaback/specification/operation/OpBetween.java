package com.instaback.specification.operation;

import com.instaback.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpBetween implements OpI{

	/**
	 *  
	 * Create predicate where value wanted to search is casted to String and then searched between 2 values got
	 * in @param reqSearch.value.
	 * 
	 * @param root. Type object: User, Like , etc...
	 * @param cb. object to create predicates.
	 * @param reqSearch. has the condition to search.
	 * @return null if @param reqSearch.value don't have 2 values, else a between predicate.
	 */
	@Override
	public Predicate getPredicate(Root<?> root, CriteriaBuilder cb,ReqSearch reqSearch){
		String[] splitIn = reqSearch.getValue().split(",");
		if(splitIn.length != 2)
			return null;
		if(reqSearch.getJoinTable() != null){
			return cb.between(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).as(String.class),
					splitIn[0],
					splitIn[1]);
		}return cb.between(root.get(reqSearch.getColumn()).as(String.class),
				splitIn[0],
				splitIn[1]);
	}

}
