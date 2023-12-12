package com.instaJava.instaJava.entity;

import java.util.List;

import com.instaJava.instaJava.enums.ChatTypeEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "chats")
public class Chat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long chatId;

	@Column(name = "name")
	private String name;

	@Column(name = "image", columnDefinition = "BLOB")
	private String image;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private ChatTypeEnum type;

	@OneToMany(fetch = FetchType.LAZY , mappedBy = "chat",cascade = {CascadeType.PERSIST, CascadeType.MERGE,CascadeType.REMOVE})
	private List<ChatUser> chatUsers;

	public Chat(Long chatId) {
		this.chatId = chatId;
	}
	
}
