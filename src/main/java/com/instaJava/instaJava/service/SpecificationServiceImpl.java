package com.instaJava.instaJava.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.specification.operation.OpI;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecificationServiceImpl<T> implements SpecificationService<T> {

	private final MessagesUtils messUtils;

	
	@Override
	public Specification<T> getSpecification(List<ReqSearch> reqSearchList, GlobalOperationEnum globalOperator) {
		if (reqSearchList == null || globalOperator == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		if (reqSearchList.isEmpty())
			return null;

		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			OpI opI = null;

			for (ReqSearch reqSearch : reqSearchList) {
				opI = OPERATIONS.get(reqSearch.getOperation().getOperation());
				predicates.add(opI.getPredicate(root,criteriaBuilder,reqSearch));
			}

			switch (globalOperator) {
			case AND:
				return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
			case OR:
				return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
			case NONE:
				return predicates.get(0);// the only predicate
			default:
				throw new IllegalArgumentException("Unexpected value: " + globalOperator);
			}

		};
	}

	
	@Override
	public Specification<T> getSpecification(ReqSearch reqSearch) {
		return this.getSpecification(List.of(reqSearch), GlobalOperationEnum.NONE);
	}

}
