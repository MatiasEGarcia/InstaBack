package com.instaJava.instaJava.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.Image;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/publicatedImages")
@RequiredArgsConstructor
@Validated
public class PublicatedImageC {

	private final PublicatedImageMapper publicImaMapper;
	private final PublicatedImageService publicatedImageService;
	private final MessagesUtils messUtils;

	/**
	 * Save a PublicatedImage record.
	 * 
	 * @param file.        image to save
	 * @param description. A just text to save with the image
	 * @return publicatedImage saved
	 */
	@PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
	public ResponseEntity<ResPublicatedImage> save(@RequestPart("img") @Image MultipartFile file,
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
	@DeleteMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id) {
		publicatedImageService.deleteById(id);
		return ResponseEntity.ok()
				.body(ResMessage.builder().message(messUtils.getMessage("mess.publi-image-deleted")).build());
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
	 * @return pagination collection with the PublicatedImages records, else a
	 *         message that there are not records.
	 */
	@GetMapping(value = "/byVisiblesOwners", produces = "application/json")
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
	 * 
	 * @param ownerId   - id of the publications owner.
	 * @param pageNo    - For pagination, number of the page.
	 * @param pageSize  - For pagination, size of the elements in the same page.
	 * @param sortField - For pagination, sorted by..
	 * @param sortDir   - In what direction is sorted, asc or desc.
	 * @return status ok if there is publications and can be accedidas, status no content if there is no publications to show,
	 * status noContent if the user that did the request don't have the correct followStatus.
	 */
	@SuppressWarnings("unchecked")
	@GetMapping(value = "/byOwnerId/{ownerId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResPaginationG<ResPublicatedImage>> getAllByOwner(@PathVariable("ownerId") Long ownerId,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "pubImaId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		Map<String, Object> mapp = publicatedImageService.getAllByOnwer(ownerId, pageInfoDto);
		Page<PublicatedImage> pagePubliImages;
		String headerMessage;

		if (mapp.containsKey("publications")) {
			pagePubliImages = (Page<PublicatedImage>) mapp.get("publications");
			if (pagePubliImages.getContent().isEmpty()) {
				return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.not-publi-image"))
						.build();
			}
			return ResponseEntity.ok()
					.body(publicImaMapper.pageAndPageInfoDtoToResPaginationG(pagePubliImages, pageInfoDto));
		} else {
			// if there is not publication key, then means that publications cannot be
			// displayed by some reason
			headerMessage = (String) mapp.get("moreInfo");
			return ResponseEntity.noContent().header("moreInfo", headerMessage).build();
		}
	}

}
