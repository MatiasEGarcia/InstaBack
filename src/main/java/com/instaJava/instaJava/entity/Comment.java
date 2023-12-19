package com.instaJava.instaJava.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
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
	
	@Column(name = "status")
	private String body;
	
	@Column(name = "user_owner")
	private String associatedUser;
	
	@ManyToOne
	@JoinColumn(name = "img")
	private PublicatedImage associatedImg;
	
	@Column(name = "parent_id")
	private Long parentId;
	
	@Column(name = "created_At")
	private ZonedDateTime createdAt;

}
