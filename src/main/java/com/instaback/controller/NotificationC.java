package com.instaback.controller;

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

import com.instaback.application.NotificationApplication;
import com.instaback.dto.NotificationDto;
import com.instaback.dto.response.ResMessage;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationC {

	private final MessagesUtils messUtils;
	private final NotificationApplication notiApplication;


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
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		return ResponseEntity.ok().body(notiApplication.getNotificationsByAuthUser(Integer.parseInt(pageNo), 
				Integer.parseInt(pageSize),sortField, sortDir));
	}

	/**
	 * Delete notification record by id.
	 * 
	 * @param id id of the notification record
	 * @return a message telling that was successfully deleted
	 */
	@DeleteMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id) {
		notiApplication.deleteNotificationById(id);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("generic.delete-ok")));
	}
	
	/**
	 * Delete all the auth user's notifications
	 * @return
	 */
	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResMessage> deleteAll(){
		notiApplication.deleteAllByAuthUser();
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("generic.delete-ok")));
	}
}
