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
import com.instaJava.instaJava.service.FollowerService;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.Image;
import com.instaJava.instaJava.validator.IsEnum;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/publicatedImages")
@RequiredArgsConstructor
@Validated
public class PublicatedImageC {

	private final FollowerService followerService;
	private final PublicatedImageMapper publicImaMapper;
	private final PublicatedImageService publicatedImageService;
	private final MessagesUtils messUtils;

	@PostMapping("/save")
	public ResponseEntity<ResPublicatedImage> save(@RequestParam("img") @Image MultipartFile file,
			@RequestParam("description") String description) {
		ResPublicatedImage resPublicatedImage = publicImaMapper.publicatedImageAndUserToResPublicatedImage(
				publicatedImageService.save(description, file),
				(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return ResponseEntity.ok().body(resPublicatedImage);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id) {
		publicatedImageService.deleteById(id);
		return ResponseEntity.ok()
				.body(ResMessage.builder().message(messUtils.getMessage("mess.publi-image-deleted")).build());
	}

	//test all below
	
	@GetMapping
	public ResponseEntity<ResPaginationG<ResPublicatedImage>> searchByUser(
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "pubImaId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "asc") @IsEnum(enumSource = Direction.class) String sortDir) {
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
	 * Return status ok if there are publicated images. 
	 * Return status noContent with a info header if there are not publicated images
	 */
	@GetMapping("/byVisiblesOwners")
	public ResponseEntity<ResPaginationG<ResPublicatedImage>> getAllByOwnerVisible(
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "pubImaId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "asc") @IsEnum(enumSource = Direction.class) String sortDir) {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		Page<PublicatedImage> pagePubliImages = publicatedImageService.getAllByOwnersVisibles(pageInfoDto);
		if (pagePubliImages.getContent().isEmpty()) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.not-publi-image")).build();
		}
		return ResponseEntity.ok()
				.body(publicImaMapper.pageAndPageInfoDtoToResPaginationG(pagePubliImages, pageInfoDto));
	}

	/*
	 * We ask the follow status and only if the follow status is accepted we ask for
	 * the publicated images
	 * 
	 * Return status ok if there are publicated images ,Return status noContent with
	 * header info if any of the next conditions is met - THere are not publicated
	 * images - The follow status is NOT_ASKED - The follow status is IN_PROCESS -
	 * The follow status is REJECTED
	 */
	@GetMapping("/byOwnerId/{ownerId}")
	public ResponseEntity<ResPaginationG<ResPublicatedImage>> getAllByOwnerId(@PathVariable("ownerId") Long ownerId,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "pubImaId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "asc") @IsEnum(enumSource = Direction.class) String sortDir) {
		FollowStatus followStatus = followerService.getFollowStatusByFollowedId(ownerId);
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
