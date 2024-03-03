package com.instaback.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.instaback.dao.InvTokenDao;
import com.instaback.entity.InvToken;
import com.instaback.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class InvTokenServiceImplTest {

	@Mock private Clock clock;
	@Mock private MessagesUtils messUtils;
	@Mock private InvTokenDao invTokenDao;
	@InjectMocks
	private InvTokenServiceImpl invTokenService;

	@Test
	void invalidateTokensArgumentNull() {
		List<String> tokens = null;
		assertThrows(IllegalArgumentException.class, () -> {
			invTokenService.invalidateTokens(tokens);
		});
		verify(invTokenDao, never()).saveAll(null);
	}

	@Test
	void invalidateTokensArgumentEmpty() {
		List<String> tokens = Collections.emptyList();
		assertThrows(IllegalArgumentException.class, () -> {
			invTokenService.invalidateTokens(tokens);
		});
		verify(invTokenDao, never()).saveAll(null);
	}

	@Test
	void invalidateTokensArgumentNoEmptyOrNullReturnInvTokenList() {
		List<String> tokens = List.of("someToken", "someRefreshToken");
		List<InvToken> invTokens = new ArrayList<>();
		when(clock.getZone()).thenReturn(
				ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(
				Instant.parse("2020-12-01T10:05:23.653Z"));
		//I have to do it after when(clock) because throw nullPoint otherwise
		tokens.forEach((token) -> {
			invTokens.add(InvToken.builder()
					.token(token)
					.invalidateDate(ZonedDateTime.now(clock))
					.build());
		});
		when(invTokenDao.saveAll(invTokens)).thenReturn(invTokens);
		assertIterableEquals(invTokens,invTokenService.invalidateTokens(tokens));
		verify(invTokenDao).saveAll(invTokens);
	}
	
	@Test
	void existByTokenReturnFalse() {
		String token = "someToken";
		when(invTokenDao.existsByToken(token)).thenReturn(false);
		assertFalse(invTokenService.existByToken(token));
		verify(invTokenDao).existsByToken(token);
	}
	
	@Test
	void existByTokenReturnTrue() {
		String token = "someToken";
		when(invTokenDao.existsByToken(token)).thenReturn(true);
		assertTrue(invTokenService.existByToken(token));
		verify(invTokenDao).existsByToken(token);
	}
	
}
