package com.instaback.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.instaback.dto.NotificationDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Notification;
import com.instaback.mapper.NotificationMapper;
import com.instaback.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationApplicationImpl implements NotificationApplication{

	private final NotificationService notiService;
	private final NotificationMapper notiMapper;
	
	@Override
	public ResPaginationG<NotificationDto> getNotificationsByAuthUser(int pageNo, int pageSize, String sortField,
			Direction sortDir) {
		Page<Notification> pageNotification;
		PageInfoDto pageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		pageNotification = notiService.getNotificationsByAuthUser(pageInfoDto);
		return notiMapper.pageAndPageInfoDtoToResPaginationG(pageNotification, pageInfoDto);
	}

	@Override
	public NotificationDto deleteNotificationById(Long notiId) {
		Notification notiDeleted = notiService.deleteNotificationById(notiId);
		return notiMapper.notificationToNotificationDtoWithToWho(notiDeleted);
	}

	@Override
	public void deleteAllByAuthUser() {
		notiService.deleteAllByAuthUser();
	}

}
