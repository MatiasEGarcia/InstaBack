package com.instaback.entity;

import java.time.ZonedDateTime;

import com.instaback.enums.TypeItemLikedEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "item_type")
	private TypeItemLikedEnum itemType;
	
	@Column(name = "item_id")
	private Long itemId;
	
	@Column(name = "decision")
	private boolean decision;
	
	@ManyToOne
	@JoinColumn(name = "owner_like")
	private User ownerLike;
	
	@Column(name = "liked_at")
	private ZonedDateTime likedAt;

	public Like(TypeItemLikedEnum itemType, Long itemId, boolean decision, User ownerLike, ZonedDateTime likedAt) {
		super();
		this.itemType = itemType;
		this.itemId = itemId;
		this.decision = decision;
		this.ownerLike = ownerLike;
		this.likedAt = likedAt;
	}

	
	
}
