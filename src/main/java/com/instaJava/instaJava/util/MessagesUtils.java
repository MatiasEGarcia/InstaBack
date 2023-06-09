package com.instaJava.instaJava.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * 
 * @author matia
 * Component to retrieve properties messages.
 */
@Component
public class MessagesUtils {

	@Autowired
	private ReloadableResourceBundleMessageSource messageSource;
	
	public String getMessage(String messageId) {
		return messageSource.getMessage(messageId,null,LocaleContextHolder.getLocale());
	}
}
