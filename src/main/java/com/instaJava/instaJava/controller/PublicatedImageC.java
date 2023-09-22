package com.instaJava.instaJava.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;
import com.instaJava.instaJava.service.FollowService;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.Image;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/publicatedImages")
@RequiredArgsConstructor
@Validated
public class PublicatedImageC {

	private final FollowService followService;
	private final PublicatedImageMapper publicImaMapper;
	private final PublicatedImageService publicatedImageService;
	private final MessagesUtils messUtils;

	/**
	 * Save a PublicatedImage record.
	 * 
	 * @param file. image to save
	 * @param description. A just text to save with the image
	 * @return  publicatedImage saved
	 */
	@PostMapping(value="/save", consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResPublicatedImage> save(@RequestParam("img") @Image MultipartFile file,
			@RequestParam("description") String description) {
		ResPublicatedImage resPublicatedImage = publicImaMapper.publicatedImageAndUserToResPublicatedImage(
				publicatedImageService.save(description, file),
				(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return ResponseEntity.ok().body(resPublicatedImage);
	}

	/**
	 * Delete a PublicatedImage by id
	 * 
	 * @param id. id of the PublicatedImage record.
	 * @return a message telling that was successfully deleted
	 */
	@DeleteMapping(value="/{id}", produces = "application/json")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id) {
		publicatedImageService.deleteById(id);
		return ResponseEntity.ok()
				.body(ResMessage.builder().message(messUtils.getMessage("mess.publi-image-deleted")).build());
	}
	
	/**
	 * 
	 * Search authenticated user publicated images, that is why don't have a value, only with /api/v1/publicatedImages works
	 * I don't use ReqSearch with postMapping because we already know that is by user equal to authenticated user.
	 * 
	 * @param pageNo. For pagination, number of the page.
	 * @param pageSize. For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir. In what direction is sorted, asc or desc.
	 * @return a pagination collection with the PublicatedImages records, else a message that there are not records.
	 */
	@GetMapping(produces = "application/json")
	public ResponseEntity<ResPaginationG<ResPublicatedImage>> searchByUser(
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "pubImaId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		Page<PublicatedImage> pagePubliImages = publicatedImageService.getAllByUser(pageInfoDto);
		if (pagePubliImages.getContent().isEmpty()) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.not-publi-image")).build();
		}
		return ResponseEntity.ok()
				.body(publicImaMapper.pageAndPageInfoDtoToResPaginationG(pagePubliImages, pageInfoDto));
	}

	
	/**
	 * Get PublicatedImages from any user with User.visible = true.(public account)
	 * I don't use ReqSearch with postMapping because I already know that is for onwer visible.
	 * 
	 * @param pageNo. For pagination, number of the page.
	 * @param pageSize. For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir. In what direction is sorted, asc or desc.
	 * @return pagination collection  with the PublicatedImages records, else a message that there are not records.
	 */
	@GetMapping(value="/byVisiblesOwners", produces = "application/json")
	public ResponseEntity<ResPaginationG<ResPublicatedImage>> getAllByOwnerVisible(
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "pubImaId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		Page<PublicatedImage> pagePubliImages = publicatedImageService.getAllByOwnersVisibles(pageInfoDto);
		if (pagePubliImages.getContent().isEmpty()) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.not-publi-image")).build();
		}
		return ResponseEntity.ok()
				.body(publicImaMapper.pageAndPageInfoDtoToResPaginationG(pagePubliImages, pageInfoDto));
	}

	/**
	 * 
	 * Get all PublicatedImages by owner id of the record.
	 * I don't use ReqSearch with postMapping because I already know that is owner id.
	 * 
	 * @param ownerId
	 * @param pageNo. For pagination, number of the page.
	 * @param pageSize. For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir. In what direction is sorted, asc or desc.
	 * @return If followStatus is any of these : NOT_ASKED, IN_PROCESS, REJECTED then a message explaining why cannot be
	 * possible to see the records, if is ACCEPTED and there is records a pagination collection with PublicatedImages records, 
	 * else a message that there are not records.
	 * @throws IllegalArgumentException if follow status no exists.
	 */
	@GetMapping(value="/byOwnerId/{ownerId}", produces = "application/json")
	public ResponseEntity<ResPaginationG<ResPublicatedImage>> getAllByOwnerId(@PathVariable("ownerId") Long ownerId,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "pubImaId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		FollowStatus followStatus = followService.getFollowStatusByFollowedId(ownerId);
		switch (followStatus) {
		case NOT_ASKED:
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.followStatus-not-asked"))
					.build();
		case IN_PROCESS:
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.followStatus-in-process"))
					.build();
		case REJECTED:
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.followStatus-rejected"))
					.build();
		case ACCEPTED:
			PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
					.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
			Page<PublicatedImage> pagePubliImages = publicatedImageService.getAllByOwnerId(pageInfoDto, ownerId);
			if (pagePubliImages.getContent().isEmpty()) {
				return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.not-publi-image"))
						.build();
			}
			return ResponseEntity.ok()
					.body(publicImaMapper.pageAndPageInfoDtoToResPaginationG(pagePubliImages, pageInfoDto));
		default:
			throw new IllegalArgumentException("Unexpected follow status: " + followStatus);
		}
	}

}
