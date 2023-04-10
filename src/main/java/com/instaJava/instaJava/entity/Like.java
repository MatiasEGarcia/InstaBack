package com.instaJava.instaJava.entity;

import java.time.ZonedDateTime;

import com.instaJava.instaJava.enums.TypeItemLikedEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "likes")
public class Like {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long likeId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "itemType")
	private TypeItemLikedEnum item;
	
	@Column(name = "item_id")
	private Long itemId;
	
	@Column(name = "decision")
	private boolean decision;
	
	@ManyToOne
	@JoinColumn(name = "owner_like")
	private User ownerLike;
	
	@Column(name = "liked_at")
	private ZonedDateTime likedAt;
}
