package com.instaJava.instaJava.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.PublicatedImage;

public interface PublicatedImagesDao extends JpaRepository<PublicatedImage, Long> , JpaSpecificationExecutor<PublicatedImage>{
	
	Page<PublicatedImage> findByUserOwnerVisible(Boolean visible, Pageable pageable);
	
	Page<PublicatedImage> findByUserOwner(Long ownerId, Pageable pageable);
	
	Long countByUserOwner(Long userOwnerId);
}
