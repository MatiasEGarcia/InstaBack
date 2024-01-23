package com.instaJava.instaJava.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.instaJava.instaJava.entity.IBaseEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchsUtils {
	
	private final MessagesUtils messUtils;
	
	public int bynarySearchById(List<? extends IBaseEntity> elements, Long elementIdToFind) {
		if(elements == null || elements.isEmpty() || elementIdToFind == null) 
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		
		double low = 0;
		double high = elements.size() - 1;
		
		while(low <= high) {
			int middlePosition = (int) Math.ceil((low + high)/2.0);
			IBaseEntity element = elements.get(middlePosition);
			if(element.getBaseEntityId() == elementIdToFind) {
				return middlePosition;
			}else if(element.getBaseEntityId() < elementIdToFind) {
				low = middlePosition + 1.0;
			}else {
				high = middlePosition - 1.0;
			}
		}
		return -1;
	}
}
