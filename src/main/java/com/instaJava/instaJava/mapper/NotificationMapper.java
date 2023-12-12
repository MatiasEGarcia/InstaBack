package com.instaJava.instaJava.mapper;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

	@Named("toNotiDtoWhithoutToWho")
	@Mapping(target ="notificationType", source ="type")
	@Mapping(target= "toWho" , ignore = true)
	NotificationDto notificationToNotificationDtoWhithoutToWho(Notification noti);

	@Named("toNotiDtoWhithToWho")
	@Mapping(target ="notificationType", source ="type")
	NotificationDto notificationToNotificationDtoWithToWho(Notification noti);
	
	@IterableMapping(qualifiedByName = "toNotiDtoWhithToWho")
	List<NotificationDto> notificationListToNotificationDtoListWithToWho(List<Notification> notiList);
	
	@IterableMapping(qualifiedByName = "toNotiDtoWhithoutToWho")
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<NotificationDto> pageAndPageInfoDtoToResPaginationG(Page<Notification> page,PageInfoDto pageInfoDto);
}
