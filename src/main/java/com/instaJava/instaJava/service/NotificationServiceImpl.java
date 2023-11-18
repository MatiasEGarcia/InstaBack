package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.NotificationDao;
import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.NotificationType;
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final Clock clock;
	private final NotificationDao notiDao;
	private final SimpMessagingTemplate messTemplate;
	private final UserMapper userMapper;
	private final PageableUtils pagUtils;
	private final MessagesUtils messUtils;
	private final SpecificationService<Notification> specService;
	
	/**
	 * Method to save a notification about the follow request of an user to another,
	 * plus doing a websocket message to the user followed , in the case that is
	 * connected, this way will know that it have a follow request.
	 * 
	 * @param follow - follow entity previously saved. (can't be null)
	 * @param customMessage - specific message to this notification, makes this notification more specific. (can't be null)
	 * @return void.
	 */
	@Override
	@Transactional
	public void saveNotificationOfFollow(Follow follow, String customMessage) {
		if(follow == null || follow.getFollowed() == null || follow.getFollower() == null || customMessage == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		NotificationDto notiDto;
		ZonedDateTime znDate = ZonedDateTime.now(clock);
		Notification newNoti = Notification.builder().fromWho(follow.getFollower()).toWho(follow.getFollowed())
				.type(NotificationType.FOLLOW).createdAt(znDate).notiMessage(customMessage).build();
		newNoti = notiDao.save(newNoti);

		// web socket event message.
		notiDto = NotificationDto.builder().notiId(newNoti.getNotiId().toString())
				.notificationType(NotificationType.FOLLOW).createdAt(znDate)
				.watched(newNoti.isWatched()).fromWho(userMapper.UserToResUser(follow.getFollower()))
				.notiMessage(customMessage).build();
		messTemplate.convertAndSendToUser(follow.getFollowed().getUserId().toString(), "/private", notiDto);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Notification> getNotificationsByAuthUser(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ReqSearch search = ReqSearch.builder().column("userId").value(user.getUserId().toString()).dateValue(false)
				.joinTable("toWho").operation(OperationEnum.EQUAL).build();
		Pageable pag = pagUtils.getPageable(pageInfoDto);
		return notiDao.findAll(specService.getSpecification(search), pag);
	}

}
