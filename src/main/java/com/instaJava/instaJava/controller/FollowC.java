package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.application.FollowApplication;
import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/follow")
@RequiredArgsConstructor
public class FollowC {
	
	private final MessagesUtils messUtils;
	private final FollowApplication fApplication;

	/**
	 * Save a follow record with the authenticated user as follower and the @param followed as followed.
	 * 
	 * @param followed. id of the User that will be followed.
	 * @return followStatus.
	 */
	@PostMapping(produces = "application/json")
	public ResponseEntity<FollowDto> save(@RequestParam(name = "followedId") Long followedId){
		return ResponseEntity.ok().body(fApplication.save(followedId));
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
			@RequestParam(name = "sortField", defaultValue="id") String sortField,
			@RequestParam(name = "sortDir" , defaultValue = "ASC")Direction sortDir){
		return ResponseEntity.ok().body(fApplication.search(Integer.parseInt(pageNo),Integer.parseInt(pageSize), sortField, sortDir,reqSearchList));
	}
	
	/**
	 * To update the follow status in Follow records.
	 * 
	 * @param followStatus. Kind of followStatus to update to.
	 * @param followId. follow id from the record to updated.
	 * @return follow record updated
	 */
	@PutMapping(value="/updateFollowStatus", produces = "application/json")
	public ResponseEntity<FollowDto> updateFollowStatus(@RequestParam(name = "followStatus") FollowStatus followStatus,
			@RequestParam(name = "id")Long id){
		return ResponseEntity.ok().body(fApplication.updateFollowStatusById(id, followStatus));
	}

	/**
	 * Delete a Follow record.
	 * 
	 * @param id. Follow id record.
	 * @return message that the record was successfully deleted.
	 */
	@DeleteMapping(value="/{id}", produces = "application/json")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id){
		fApplication.deleteById(id);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("generic.delete-ok")));
	}

	/**
	 * To delete a follow record by it's followed id, and as follower the auth user.
	 * @param id - followed's id
	 * @return message that the record was successfully deleted.
	 */
	@DeleteMapping(value="/byFollowedId/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResMessage> deleteByFollowedId(@PathVariable("id") Long id){
		fApplication.deleteByFollwedId(id);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("generic.delete-ok")));
	}
	
	
	/**
	 * Method to update follow status on follow record by authenticated user and follower or followed id.
	 * @param followerId
	 * @param followedId
	 * @return follow record updated
	 */
	@PutMapping(value = "/updateFollowStatus/byFollowerId" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FollowDto> updateFollowStatusByFollowerById(
			@RequestParam(name = "id") Long id,
			@RequestParam(name = "followStatus") FollowStatus followStatus){
		FollowDto f = fApplication.updateFollowStatusByFollowerId(id, followStatus);
		return ResponseEntity.ok().body(f);
	}
	

	
}












