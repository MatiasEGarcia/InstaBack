package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Notification;
import com.instaJava.instaJava.mapper.NotificationMapper;
import com.instaJava.instaJava.service.NotificationService;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationC {

	private final MessagesUtils messUtils;
	private final NotificationMapper notificationMapper;
	private final NotificationService notiService;

	// tengo que testear

	/**
	 * Handler to get all the notifications by the user that make the request.
	 * 
	 * @param pageNo    - For pagination, number of the page.
	 * @param pageSize  - For pagination, size of the elements in the same page.
	 * @param sortField - For pagination, sorted by..
	 * @param sortDir   - In what direction is sorted, asc or desc.
	 * @return paginated notifications that were found,else a message that there
	 *         wasn't any that meet the conditions.
	 */
	@GetMapping(value = "/getByAuthUser", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<NotificationDto>> getNotificationsByAuthUser(
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "notiId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		Page<Notification> pageNotifs = notiService.getNotificationsByAuthUser(pageInfoDto);
		if (pageNotifs.getContent().isEmpty()) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.no-notifications")).build();
		}
		return ResponseEntity.ok().body(notificationMapper.pageAndPageInfoDtoToResPaginationG(pageNotifs, pageInfoDto));
	}

	/**
	 * Delete notification record by id.
	 * 
	 * @param id id of the notification record
	 * @return a message telling that was successfully deleted
	 */
	@DeleteMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id) {
		notiService.deleteNotificationById(id);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("mess.notif-deleted")));
	}
}
