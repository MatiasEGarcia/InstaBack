package com.instaJava.instaJava.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaJava.instaJava.entity.Comment;

public interface CommentDao extends JpaRepository<Comment, Long> {

	/**
	 * Select root comments by associatedImage and how many commentaries has associated that root comment.
	 * @param associatedImage - PublicatedImage's id.
	 * @param pag - pageable to sort
	 * @return Page with Comment records.
	 */
	@Query(value = "SELECT new com.instaJava.instaJava.entity.Comment(c1, COUNT(c2)) "
			+ "FROM Comment c1 "
			+ "LEFT JOIN Comment c2 ON c1.id = c2.parent.id "
			+ "WHERE c1.associatedImg.id = :publImgId AND c1.parent IS NULL "
			+ "GROUP BY c1")
	Page<Comment> getRootCommentsByAssociatedImage(@Param(value="publImgId") Long associatedImage,Pageable pag);
	
	Page<Comment> findByParent(Comment comment,Pageable pag);
	
	
}
