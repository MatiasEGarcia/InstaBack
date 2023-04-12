package com.instaJava.instaJava.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecificationServiceImpl<T> implements SpecificationService<T> {
	
	private final MessagesUtils messUtils;
	
	private List<Predicate> getPredicates(List<ReqSearch> reqSearchList,
			Root<T> root, CriteriaBuilder criteriaBuilder) {
		List<Predicate> predicates = new ArrayList<>();

		for (ReqSearch reqSearch : reqSearchList) {

			switch (reqSearch.getOperation()) {
			case EQUAL:
				if(reqSearch.getDateValue().booleanValue()) {
					predicates.add(criteriaBuilder.equal(root.get(reqSearch.getColumn()), ZonedDateTime.parse(reqSearch.getValue())));
				}
				predicates.add(criteriaBuilder.equal(root.get(reqSearch.getColumn()), reqSearch.getValue()));
				break;
			case LIKE:
				predicates.add(criteriaBuilder.like(root.get(reqSearch.getColumn()), "%" + reqSearch.getValue() + "%")); 
				break;
			case IN:
				String[] splitIn = reqSearch.getValue().split(",");
				if(reqSearch.getDateValue().booleanValue()) {
					List<ZonedDateTime> values = new ArrayList<>();
					for(String value : splitIn) {
						values.add(ZonedDateTime.parse(value));
					}
					predicates.add(root.get(reqSearch.getColumn()).in(values));
				}
				predicates.add(root.get(reqSearch.getColumn()).in(Arrays.asList(splitIn)));
				break;
			case GREATER_THAN:
				if(reqSearch.getDateValue().booleanValue()) {
					predicates.add(criteriaBuilder.greaterThan(root.get(reqSearch.getColumn()), ZonedDateTime.parse(reqSearch.getValue())));
				}
				predicates.add(criteriaBuilder.greaterThan(root.get(reqSearch.getColumn()),
						Long.parseLong(reqSearch.getValue())));
				break;
			case LESS_THAN:
				if(reqSearch.getDateValue().booleanValue()) {
					predicates.add(criteriaBuilder.lessThan(root.get(reqSearch.getColumn()), ZonedDateTime.parse(reqSearch.getValue())));
				}
				predicates.add(criteriaBuilder.lessThan(root.get(reqSearch.getColumn()),
						Long.parseLong(reqSearch.getValue())));
				break;
			case BETWEEN:
				String[] splitBetween = reqSearch.getValue().split(",");
				if(reqSearch.getDateValue().booleanValue()) {
					predicates.add(criteriaBuilder.between(root.get(reqSearch.getColumn()), ZonedDateTime.parse(splitBetween[0]),ZonedDateTime.parse(splitBetween[1])));
				}
				
				predicates.add(criteriaBuilder.between(root.get(reqSearch.getColumn()), Long.parseLong(splitBetween[0]),
						Long.parseLong(splitBetween[1])));
				break;
			default:
				throw new IllegalStateException(messUtils.getMessage("exception.operationEnum-no-exist"));
			}
		}

		return predicates;
	}

	private List<Predicate> getPredicatesJoin(List<ReqSearch> reqSearchList,
			Root<T> root, CriteriaBuilder criteriaBuilder) {
		List<Predicate> predicates = new ArrayList<>();
		
		for (ReqSearch reqSearch : reqSearchList) {
			
			switch (reqSearch.getOperation()) {
			case EQUAL:
				if(reqSearch.getDateValue().booleanValue()) {
					predicates.add(criteriaBuilder.equal(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()), ZonedDateTime.parse(reqSearch.getValue())));
				}
				predicates.add(criteriaBuilder.equal(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()), reqSearch.getValue()));
				break;
			case LIKE:
				predicates.add(
						criteriaBuilder.like(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()), "%" + reqSearch.getValue() + "%"));
				break;
			case IN:
				String[] splitIn = reqSearch.getValue().split(",");
				if(reqSearch.getDateValue().booleanValue()) {
					List<ZonedDateTime> values = new ArrayList<>();
					for(String value : splitIn) {
						values.add(ZonedDateTime.parse(value));
					}
					predicates.add(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).in(values));
				}
				predicates.add(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).in(Arrays.asList(splitIn)));
				break;
			case GREATER_THAN:
				if(reqSearch.getDateValue().booleanValue()) {
					predicates.add(criteriaBuilder.greaterThan(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()), ZonedDateTime.parse(reqSearch.getValue())));
				}
				predicates.add(criteriaBuilder.greaterThan(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()),
						Long.parseLong(reqSearch.getValue())));
				break;
			case LESS_THAN:
				if(reqSearch.getDateValue().booleanValue()) {
					predicates.add(criteriaBuilder.lessThan(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()), ZonedDateTime.parse(reqSearch.getValue())));
				}
				predicates.add(criteriaBuilder.lessThan(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()),
						Long.parseLong(reqSearch.getValue())));
				break;
			case BETWEEN:
				String[] splitBetween = reqSearch.getValue().split(",");
				if(reqSearch.getDateValue().booleanValue()) {
					predicates.add(criteriaBuilder.between(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()), ZonedDateTime.parse(splitBetween[0]),
							ZonedDateTime.parse(splitBetween[1])));
				}
				predicates.add(criteriaBuilder.between(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()), Long.parseLong(splitBetween[0]),
						Long.parseLong(splitBetween[1])));
				break;
			default:
				throw new IllegalStateException(messUtils.getMessage("exception.operationEnum-no-exist"));
			}
		}
		
		return predicates;
	}

	@Override
	public Specification<T> getSpecification(List<ReqSearch> reqSearchList,
			GlobalOperationEnum globalOperator) {
		if(reqSearchList == null || reqSearchList.isEmpty() || globalOperator == null ) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		return (root, query, criteriaBuilder) -> {
			List<Predicate> allPredicates = new ArrayList<>();
			List<Predicate> predicates;
			List<Predicate> predicatesJoin;
			java.util.function.Predicate<ReqSearch> partition = dto-> dto.getJoinTable() == null || dto.getJoinTable().isBlank();
			var map = reqSearchList.stream().collect(Collectors.partitioningBy(partition));
			predicates = this.getPredicates(map.get(true), root, criteriaBuilder);
			predicatesJoin = this.getPredicatesJoin(map.get(false), root, criteriaBuilder);
			allPredicates.addAll(predicates);
			allPredicates.addAll(predicatesJoin);
			if (globalOperator.equals(GlobalOperationEnum.AND)) {
				return criteriaBuilder.and(allPredicates.toArray(new Predicate[0]));
			} else {
				return criteriaBuilder.or(allPredicates.toArray(new Predicate[0]));
			}
			
		};
	}

	@Override
	public Specification<T> getSpecification(ReqSearch reqSearch) {
		if(reqSearch == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null"));
		return (root ,query , criteriaBuilder) -> {
			//the two methods with I get predicates recibe a list and return a list, I can create them for 1 object but is not necessary  
			List<Predicate> predicates;
			if(reqSearch.getJoinTable() == null | reqSearch.getJoinTable().isBlank()) {
				predicates = getPredicates(List.of(reqSearch), root, criteriaBuilder);
			}else {
				predicates = getPredicatesJoin(List.of(reqSearch), root, criteriaBuilder);
			}
			return predicates.get(0); //I get the only predicate that predicates have
		};
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

}
