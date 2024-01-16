package com.instaJava.instaJava.entity;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "publicated_images")
public class PublicatedImage{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "id")
	private Long id;
	
	@Column(name = "img", columnDefinition = "BLOB")
    private String image;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_owner")
	private User userOwner;
	
	@Column(name = "created_at")
	private ZonedDateTime  createdAt;
	
	@Column(name = "description")
	private String description;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "associatedImg" , cascade = {CascadeType.REMOVE})
	private List<Comment> comments;
	
	public PublicatedImage(Long id) {
		this.id = id;
	}
	
}
