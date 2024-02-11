package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.application.PublicatedImageApplication;
import com.instaJava.instaJava.dto.response.PublicatedImageDto;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.Image;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/publicatedImages")
@RequiredArgsConstructor
@Validated
public class PublicatedImageC {

	private final PublicatedImageApplication pImageApplication;
	private final MessagesUtils messUtils;

	/**
	 * Save a PublicatedImage record.
	 * 
	 * @param file.        image to save
	 * @param description. A just text to save with the image
	 * @return publicatedImage saved
	 */
	@PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
	public ResponseEntity<PublicatedImageDto> save(@RequestPart("img") @Image MultipartFile file,
			@RequestParam("description") String description) {
		return ResponseEntity.ok().body(pImageApplication.save(description, file));
	}

	/**
	 * Delete a PublicatedImage by id
	 * 
	 * @param id. id of the PublicatedImage record.
	 * @return a message telling that was successfully deleted
	 */
	@DeleteMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id) {
		pImageApplication.deleteById(id);
		return ResponseEntity.ok()
				.body(ResMessage.builder().message(messUtils.getMessage("generic.delete-ok")).build());
	}

	/**
	 * Get all publication info by id.(with root comments AND if auth user like or not)
	 * @param id - publication's info.
	 * @param pageNo.    For comments pagination, number of the page.
	 * @param pageSize.  For comments pagination, size of the elements in the same page.
	 * @param sortField. For comments pagination, sorted by..
	 * @param sortDir.   In what direction is sorted, asc or desc.
	 * @return all publication info.
	 */
	@GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PublicatedImageDto> getById(@PathVariable("id") Long id ,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir){
		return  ResponseEntity.ok().body(pImageApplication.getById(id,Integer.parseInt(pageNo), Integer.parseInt(pageSize),
				sortField, sortDir));
	}
	
	
	/**
	 * Get PublicatedImages from any user with User.visible = true.(public account)
	 * I don't use ReqSearch with postMapping because I already know that is for
	 * onwer visible.
	 * 
	 * @param pageNo.    For pagination, number of the page.
	 * @param pageSize.  For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir.   In what direction is sorted, asc or desc.
	 * @return ResPaginationG wiht PublicatedImages records info and Pagination info.
	 */
	@GetMapping(value = "/byVisiblesOwners", produces = "application/json")
	public ResponseEntity<ResPaginationG<PublicatedImageDto>> getAllByOwnerVisible(
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		return ResponseEntity.ok()
				.body(pImageApplication.getAllByOwnersVisibles(Integer.parseInt(pageNo), Integer.parseInt(pageSize),
						sortField, sortDir));
	}

	/**
	 * 
	 * Get all PublicatedImages by owner id of the record.
	 * 
	 * @param ownerId   - id of the publications owner.
	 * @param pageNo    - For pagination, number of the page.
	 * @param pageSize  - For pagination, size of the elements in the same page.
	 * @param sortField - For pagination, sorted by..
	 * @param sortDir   - In what direction is sorted, asc or desc.
	 * @return ResPaginationG wiht PublicatedImages records info and Pagination info.
	 */
	@GetMapping(value = "/byOwnerId/{ownerId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<PublicatedImageDto>> getAllByOwner(@PathVariable("ownerId") Long ownerId,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		return ResponseEntity.ok().body(pImageApplication.getAllByOnwerId(ownerId, Integer.parseInt(pageNo), 
				Integer.parseInt(pageSize), sortField, sortDir));
	}
	
	/**
	 * get all publications by users who auth user follow.(status = ACCEPTED)
	 * @param pageNo    - For pagination, number of the page.
	 * @param pageSize  - For pagination, size of the elements in the same page.
	 * @param sortField - For pagination, sorted by..
	 * @param sortDir   - In what direction is sorted, asc or desc.
	 * @return ResPaginationG wiht PublicatedImages records info and Pagination info.
	 */
	@GetMapping(value="/byUsersFollowed" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<PublicatedImageDto>> getAllByUsersFollowed(
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir){
		return ResponseEntity.ok().body(pImageApplication.getPublicationsFromUsersFollowed(Integer.parseInt(pageNo), 
				Integer.parseInt(pageSize), sortField, sortDir));
	}
	

}
