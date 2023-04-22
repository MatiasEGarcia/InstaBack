package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResFollowStatus;
import com.instaJava.instaJava.dto.response.ResFollow;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.mapper.FollowMapper;
import com.instaJava.instaJava.service.FollowService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.IsEnum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/follow")
@RequiredArgsConstructor
public class FollowC {
	
	private final FollowService follService;
	private final FollowMapper follMapper;
	private final MessagesUtils messUtils;

	@PostMapping
	public ResponseEntity<ResFollowStatus> save(@RequestParam(name = "followed") String followed){
		Follow fol = follService.save(Long.parseLong(followed)); 
		return ResponseEntity.ok().body(follMapper.followToResFollowStatus(fol));
	}
	
	@PostMapping("/findAllBy")
	public ResponseEntity<ResPaginationG<ResFollow>> getAllFollowBy(
			@Valid @RequestBody ReqSearchList reqSearchList,
			@RequestParam(name ="page", defaultValue = "1") String pageNo,
			@RequestParam(name = "pageSize" , defaultValue ="20") String pageSize,
			@RequestParam(name = "sortField", defaultValue="followId") String sortField,
			@RequestParam(name = "sortDir" , defaultValue = "asc")@IsEnum(enumSource = Direction.class) String sortDir){
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		HttpHeaders headers;
		Page<Follow> followPage = follService.search(pageInfoDto,reqSearchList);
		
		if(!followPage.isEmpty()) {
			return ResponseEntity.ok().body(follMapper
					.pageAndPageInfoDtoToResPaginationG(followPage, pageInfoDto));
		}
		headers = new HttpHeaders();
		headers.add("Info-header", messUtils.getMessage("mess.not-follow"));		
		return new ResponseEntity<> (headers, HttpStatus.NO_CONTENT);
	}
	
	@PutMapping("/followStatus")
	public ResponseEntity<ResFollow> updateFollowStatus(@RequestParam(name = "followStatus") FollowStatus followStatus,
			@RequestParam(name = "followId")Long id){
		Follow fol  = follService.updateFollowStatusById(id, followStatus);
		return ResponseEntity.ok().body(follMapper.followToResFollow(fol));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id){
		follService.deleteById(id);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("mess.record-delete")));
	}

}