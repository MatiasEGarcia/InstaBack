package com.instaJava.instaJava.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "comments")
public class Comment{

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name= "id")
	private Long commentId;
	
	@Column(name = "body")
	private String body;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_user")
	private User ownerUser;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "img")
	private PublicatedImage associatedImg;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent")
	private Comment parent;
	
	@Column(name = "created_At")
	private ZonedDateTime createdAt;

	@Transient
	private Long associateCN; //number of commentaries associated

	public Comment(Long id) {
		this.commentId = id;
	}

	/**
	 * Constructor  with associated commentaries number.
	 * @param c - Comment record.
	 * @param associateCN - associate commentaries number.
	 */
	public Comment(Comment c, Long associateCN) {
		this.commentId = c.getCommentId();
		this.body = c.getBody();
		this.ownerUser = c.getOwnerUser();
		this.associatedImg = c.getAssociatedImg();
		this.parent = c.getParent();
		this.createdAt = c.getCreatedAt();
		this.associateCN = associateCN;
	}
	
}
