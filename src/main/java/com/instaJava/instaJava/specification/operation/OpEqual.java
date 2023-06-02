package com.instaJava.instaJava.specification.operation;

import com.instaJava.instaJava.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpEqual implements OpI {

	private Root<?> root;
	private CriteriaBuilder cb;
	private ReqSearch reqSearch;

	public OpEqual(Root<?> root, CriteriaBuilder cb, ReqSearch reqSearch) {
		super();
		this.root = root;
		this.cb = cb;
		this.reqSearch = reqSearch;
	}

	@Override
	public Predicate getPredicate() {
		if (reqSearch.getJoinTable() != null) {
			return cb.equal(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).as(String.class),
					reqSearch.getValue());
		} else {
			return cb.equal(root.get(reqSearch.getColumn()).as(String.class),
					reqSearch.getValue());
		}

	}

}