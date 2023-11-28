package com.instaJava.instaJava.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.exception.IllegalActionException;
import com.instaJava.instaJava.exception.ImageException;

public interface PublicatedImageService {
	
	/**
	 * Convert MuliparFile to Base64 and try to save a PublicatedImage in the
	 * database
	 * 
	 * @param description. Description of the image.
	 * @param file.        Image to save.
	 * @return PublicatedImage record that was saved.
	 * @throws IllegalArgumentException if @param file is null or empty.
	 * @throws ImageException           if there was an error when was tried to
	 *                                  encode the image to Base64
	 */
	PublicatedImage save(String Description,MultipartFile file);
	
	/**
	 * Delete a PublicatedImage record by its id(pubImaId). If PublicatedImage none
	 * record will be deleted.
	 * 
	 * @param id. is the pubImaId of the PublicatedImage record wanted to delete.
	 * @throws IllegalArgumentException if @param id is null.
	 * @throws IllegalActionException   if the user authenticated is not the same
	 *                                  owner of the PublicatedImage record.
	 */
	void deleteById(Long id);
	
	/**
	 * Find PublicatedImage by its id(pubImaId)
	 * 
	 * @return Optional of PublicatedImage
	 * @throws IllegalArgumentException if @param id is null.
	 */
	Optional<PublicatedImage> getById(Long id);
	
	/**
	 * Create a ReqSearch to get a specification object and then search
	 * PublicatedImages records by User.visible = true.
	 * 
	 * @param pageInfoDto. It has pagination info.
	 * @return Page of PublicatedImages.
	 * @throws IllegalArgumentException if PageInfoDto or pageInfoDto.getSortDir or
	 *                                  pageInfoDto.sortField are null.
	 */
	Page<PublicatedImage> getAllByOwnersVisibles(PageInfoDto pageInfoDto);
	
	/**
	 * Create a ReqSearch to get a specification object and then search
	 * PublicatedImages records by ownerId.
	 * 
	 * @param pageInfoDto - It has pagination info.
	 * @param ownerId   -   Id of the owner who will have his PublicatedImages
	 *                     records fetched.
	 * @throws IllegalArgumentException - if ownerId or PageInfoDto or
	 *                                  pageInfoDto.getSortDir or
	 *                                  pageInfoDto.sortField are null.
	 * @return Page of PublicatedImages
	 */
	Map<String, Object> getAllByOnwer(Long ownerId, PageInfoDto pageInfoDto);
	
	/**
	 * Method to get how many publication has an user.
	 * 
	 * @param id - owner id.
	 * @return number of publications.
	 */
	Long countPublicationsByOwnerId(Long id);
}
