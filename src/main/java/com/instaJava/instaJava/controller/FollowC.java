package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.service.FollowService;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/follow")
@RequiredArgsConstructor
public class FollowC {
	
	private final FollowService follService;
	private final MessagesUtils messUtils;

	/**
	 * Save a follow record with the authenticated user as follower and the @param followed as followed.
	 * 
	 * @param followed. id of the User that will be followed.
	 * @return followStatus.
	 */
	@PostMapping(produces = "application/json")
	public ResponseEntity<FollowDto> save(@RequestParam(name = "followed") Long followed){
		return ResponseEntity.ok().body(follService.save(followed));
	}
	
	/**
	 * Get Follows records by many conditions.
	 * 
	 * @param reqSearchList. object with a collection with conditions to follow search.
	 * @param pageNo. For pagination, number of the page.
	 * @param pageSize. For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir. In what direction is sorted, asc or desc.
	 * @return
	 */
	@PostMapping(value="/findAllBy", consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResPaginationG<FollowDto>> getAllFollowBy(
			@Valid @RequestBody ReqSearchList reqSearchList,
			@RequestParam(name ="page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize" , defaultValue ="20") String pageSize,
			@RequestParam(name = "sortField", defaultValue="followId") String sortField,
			@RequestParam(name = "sortDir" , defaultValue = "ASC")Direction sortDir){
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		return ResponseEntity.ok().body(follService.search(pageInfoDto,reqSearchList));
	}
	
	/**
	 * To update the follow status in Follow records.
	 * 
	 * @param followStatus. Kind of followStatus to update to.
	 * @param id. follow id from the record to updated.
	 * @return follow record updated
	 */
	@PutMapping(value="/updateFollowStatus", produces = "application/json")
	public ResponseEntity<FollowDto> updateFollowStatus(@RequestParam(name = "followStatus") FollowStatus followStatus,
			@RequestParam(name = "followId")Long id){
		return ResponseEntity.ok().body(follService.updateFollowStatusById(id, followStatus));
	}

	/**
	 * Delete a Follow record.
	 * 
	 * @param id. Follow id record.
	 * @return message that the record was successfully deleted.
	 */
	@DeleteMapping(value="/{id}", produces = "application/json")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id){
		follService.deleteById(id);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("generic.delete-ok")));
	}

}
