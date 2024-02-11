package com.instaJava.instaJava.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;

public interface PublicatedImageService {
	
	/**
	 * Find PublicatedImage by its id.
	 * 
	 * @return Optional<PublicatedImage>
	 * @throws IllegalArgumentException if @param id is null.
	 */
	Optional<PublicatedImage> findById(Long id);
	
	/**
	 * Method to get how many publication has an user.
	 * 
	 * @param id - owner id.
	 * @return number of publications.
	 */
	Long countPublicationsByOwnerId(Long id);
	
	/**
	 * Find PublicatedImage by its id(pubImaId) with it's root comments and if auth user like or not.
	 * 
	 * @param id - publication's id
	 * @return PublicatedImage record
	 * @throws RecordNotFoundException if PublicatedImage record was not found.
	 * @throws IllegalArgumentException if @param id is null.
	 */
	PublicatedImage getById(Long id);
	
	/* Convert MuliparFile to Base64 and try to save a PublicatedImage in the
	 * database
	 * 
	 * @param description. Description of the image.
	 * @param file.        Image to save.
	 * @return PublicatedImage saved.
	 * @throws IllegalArgumentException if @param file is null or empty.
	 * @throws InvalidImageException           if there was an error when was tried to
	 *                                  encode the image to Base64
	 */
	PublicatedImage save(String description,MultipartFile file); 
	
	/**
	 * Delete publicatedImage record by id.
	 * @param publicatedImageId - publicatedImage's idto delete.
	 * @return PublicatedImage record deleted.
	 * @throws IllegalArgumentException if @param id is null.
	 * @throws RecordNotFoundException if the publication wanted to delete was not found.
	 * @throws InvalidActionException   if the user authenticated is not the same
	 *                                  owner of the PublicatedImage record.
	 */
	PublicatedImage deleteById(Long publicatedImageId); 
	
	/**
	 * PublicatedImages records by User.visible = true.
	 * @param pageInfoDto - pagination info
	 * @return Page with PublicatedImage records.
	 * @throws RecordNotFoundException if none record was found.
	 * @throws IllegalArgumentException if PageInfoDto or pageInfoDto.getSortDir or
	 * pageInfoDto.sortField are null.
	 */
	Page<PublicatedImage> getAllByOwnerVisible(PageInfoDto pageInfoDto);
	
	/**
	 * PublicatedImages records by ownerId.
	 * @param ownerId - publicatedImage 's onwer's id.
	 * @param pageInfoDto - pagination info
	 * @return Page with PublicatedImage records.
	 * @throws RecordNotFoundException if none record was found.
	 * @throws IllegalArgumentException if PageInfoDto or pageInfoDto.getSortDir or
	 * pageInfoDto.sortField are null.
	 */
	Page<PublicatedImage> getAllByOnwerId(Long ownerId,PageInfoDto pageInfoDto);
	
	/**
	 * Function to get all publications where the owner user is followed by authenticated user.
	 * 
	 * @param pageInfoDto - has pagination info.
	 * @return Page with Publicated images and pagination info.
	 * @throws RecordNotFoundException if there was not found any publicated image.
	 */
	Page<PublicatedImage> getPublicationsFromUsersFollowed(PageInfoDto pageInfoDto);
	
}
