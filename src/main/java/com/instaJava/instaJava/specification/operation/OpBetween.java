package com.instaJava.instaJava.specification.operation;

import com.instaJava.instaJava.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpBetween implements OpI{

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
