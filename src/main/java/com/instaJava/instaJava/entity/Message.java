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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "messages")
public class Message{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Long messageId;
	
	@Column(name = "body")
	private String body;
	
	@Column(name = "user_owner")
	private String userOwner;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat")
	private Chat chat;
	
	@Column(name = "sended_at")
	private ZonedDateTime sendedAt;
	
	@Column(name = "watched_by")
	private String watchedBy;
}
