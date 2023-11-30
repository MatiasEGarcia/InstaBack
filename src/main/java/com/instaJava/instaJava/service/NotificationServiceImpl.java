package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.NotificationDao;
import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.NotificationType;
import com.instaJava.instaJava.exception.IllegalActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.NotificationMapper;
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
	private final NotificationMapper notificationMapper;
	private final PageableUtils pagUtils;
	private final MessagesUtils messUtils;
	
	
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
				.watched(newNoti.isWatched()).fromWho(userMapper.userToUserDto(follow.getFollower()))
				.notiMessage(customMessage).build();
		messTemplate.convertAndSendToUser(follow.getFollowed().getUserId().toString(), "/private", notiDto);
	}

	//check test
	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<NotificationDto> getNotificationsByAuthUser(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Page<Notification> notiPage = notiDao.findByToWho(user.getUserId(), pagUtils.getPageable(pageInfoDto));
		if(!notiPage.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("mess.no-notifications"), HttpStatus.NO_CONTENT);
		}
		return notificationMapper.pageAndPageInfoDtoToResPaginationG(notiPage, pageInfoDto);
	}


	//check test
	@Override
	@Transactional
	public void deleteNotificationById(Long notiId) {
		if(notiId == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User authUser;
		Optional<Notification> notiToDelete = findNotificationById(notiId);
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!notiToDelete.get().getToWho().equals(authUser)) {
			throw new IllegalActionException(messUtils.getMessage("exception.notif-owner-not-same"),HttpStatus.BAD_REQUEST); 
		}
		notiDao.delete(notiToDelete.get());
	}

	//check test
	@Override
	@Transactional(readOnly = true)
	public Optional<Notification> findNotificationById(Long notiId) {
		if(notiId == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		return notiDao.findById(notiId);
}	
	
}	
	