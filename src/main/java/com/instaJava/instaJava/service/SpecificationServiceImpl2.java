package com.instaJava.instaJava.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.specification.operation.OpI;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecificationServiceImpl2<T> {// later I have to implement SpecificationService

	private final MessagesUtils messUtils;

	// @Override
	public Specification<T> getSpecification(List<ReqSearch> reqSearchList, GlobalOperationEnum globalOperator) {
		if (reqSearchList == null || globalOperator == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		if (reqSearchList.isEmpty())
			return null;

		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			OpI opI = null;

			for (ReqSearch reqSearch : reqSearchList) {
				
				try {
					Class<?> concreteClass = Class.forName(reqSearch.getOperation().getDirection());
					Constructor<?> contructor = concreteClass.getConstructor(Root.class, CriteriaBuilder.class,
							ReqSearch.class);
					opI = (OpI) contructor.newInstance(root, criteriaBuilder, reqSearch);
				}
				catch (ClassNotFoundException e) {
					//this is throw when getDirection return wrong class
					e.printStackTrace();
				}catch (NoSuchMethodException | SecurityException e) {
					// this is throw when the class don't have the method, in this case a constructor
					e.printStackTrace();
				}catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
				predicates.add(opI.getPredicate());
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

	// @Override
	public Specification<T> getSpecification(ReqSearch reqSearch) {
		return this.getSpecification(List.of(reqSearch), GlobalOperationEnum.NONE);
	}

}