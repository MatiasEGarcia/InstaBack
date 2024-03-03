package com.instaback.config.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.instaback.util.MessagesUtils;

import jakarta.servlet.http.HttpServletResponse;


@ExtendWith(MockitoExtension.class)
class TimeZoneFilterTest {
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private MockFilterChain filterChain;
	@Mock private MessagesUtils messUtils;
	@InjectMocks private TimeZoneFilter filter;
	
	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		filterChain = new MockFilterChain();
	}

	@Test
	void timeZoneHeaderPresentAndZoneAvailable() throws Exception {
		String timezoneHeader = "America/New_York";
		TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
		ArgumentCaptor<TimeZone> timeZoneCaptor = ArgumentCaptor.forClass(TimeZone.class);
		
		try(MockedStatic<TimeZone> mockedTimeZone = mockStatic(TimeZone.class)){
			mockedTimeZone.when(() -> TimeZone.getTimeZone(timezoneHeader)).thenReturn(timeZone);
			
			request.addHeader("Timezone", timezoneHeader);
			filter.doFilterInternal(request, response, filterChain);
			
			mockedTimeZone.verify(() -> TimeZone.setDefault(timeZoneCaptor.capture()),times(1));
		}
		assertEquals(timeZone, timeZoneCaptor.getValue(),
				"trying to set an incorrect timezone as default time zone,should be America/New_York");
	
	}
	
	@Test
	void timeZoneHeaderNoPresentUtcTimeAsDefault() throws Exception {
		String timeZoneDefault = "UTC";
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneDefault);
		ArgumentCaptor<TimeZone> timeZoneCaptor = ArgumentCaptor.forClass(TimeZone.class);
		
		try(MockedStatic<TimeZone> mockedTimeZone = mockStatic(TimeZone.class)){
			mockedTimeZone.when(() -> TimeZone.getTimeZone(timeZoneDefault)).thenReturn(timeZone);
			filter.doFilterInternal(request, response, filterChain);
			mockedTimeZone.verify(() -> TimeZone.setDefault(timeZoneCaptor.capture()),times(1));
		}
		
		assertEquals(timeZone, timeZoneCaptor.getValue(),
				"trying to set an incorrect timezone as default time zone,should be UTC");
	}
	
	@Test
	void timeZoneHeaderPresentButIsNotAvailable() throws Exception{
		String timezoneHeader = "noExistTimeZone";
		Set<String> availableZoneIds = Collections.emptySet();//empty so, timezoneHeader will no be available
		try(MockedStatic<ZoneId> mockedZoneId = mockStatic(ZoneId.class)){
			mockedZoneId.when(() -> ZoneId.getAvailableZoneIds()).thenReturn(availableZoneIds);
			request.addHeader("Timezone", timezoneHeader);
			filter.doFilterInternal(request, response, filterChain);
		}
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
