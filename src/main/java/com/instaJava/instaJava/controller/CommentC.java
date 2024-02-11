package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.application.CommentApplication;
import com.instaJava.instaJava.dto.CommentDto;
import com.instaJava.instaJava.dto.request.ReqComment;
import com.instaJava.instaJava.dto.request.ReqUpdateComment;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Validated
public class CommentC {

	private final CommentApplication commentApplication;
	private final MessagesUtils messUtils;

	/**
	 * To save a Comment record.
	 * @param reqComment - payload with info to save a comment record.
	 * @return new Comment info created
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommentDto> save(@Valid @RequestBody ReqComment reqComment){
		return ResponseEntity.ok().body(commentApplication.save(reqComment));
	}
	
	/**
	 * TO get comments by publication id.
	 * 
	 * @param id - publication's id
	 * @param pageNo.    For pagination, number of the page.
	 * @param pageSize.  For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir.   In what direction is sorted, asc or desc.
	 * @return ResPaginationG wiht Comment records info and Pagination info.
	 */
	@GetMapping(value="/manyByPublicationId",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<CommentDto>> getManyByPublicationId(
			@RequestParam(name = "id") Long id,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir){
		ResPaginationG<CommentDto> page = commentApplication.getRootCommentsByPublicationImageId(id,Integer.parseInt(pageNo),
				Integer.parseInt(pageSize), sortField, sortDir);
		return ResponseEntity.ok().body(page);
	}
	
	/**
	 * TO get comments by parent id
	 * 
	 * @param id - parent's id
	 * @param pageNo.    For pagination, number of the page.
	 * @param pageSize.  For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir.   In what direction is sorted, asc or desc.
	 * @return ResPaginationG wiht Comment records info and Pagination info.
	 */
	@GetMapping(value="/manyByParentId",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<CommentDto>> getManyByParentId(
			@RequestParam(name = "id") Long id,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir){
		ResPaginationG<CommentDto> page = commentApplication.getAssociatedCommentsByParentCommentId(id,Integer.parseInt(pageNo),
				Integer.parseInt(pageSize), sortField, sortDir);
		return ResponseEntity.ok().body(page);
	}
	
	/**
	 * To delete a comment by id.
	 * @param id - comment's id.
	 * @return a message with information.
	 */
	@DeleteMapping(value="/byId/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResMessage> deleteById(@PathVariable(name = "id") Long id){
		commentApplication.deleteById(id);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("generic.delete-ok")));
	}
	
	/**
	 * To update a comment by id.
	 * @param reqUpdateComment - comment's info.
	 * @return The updated comment.
	 */
	@PutMapping(value="/byId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommentDto> updateById(@RequestBody ReqUpdateComment reqUpdateComment){
		CommentDto commmentUpdated = commentApplication.updateBodyById(reqUpdateComment.getId(),reqUpdateComment.getBody());
		return ResponseEntity.ok().body(commmentUpdated);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
