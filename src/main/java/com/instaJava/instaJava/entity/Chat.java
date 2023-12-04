package com.instaJava.instaJava.entity;

import java.util.List;

import com.instaJava.instaJava.enums.ChatTypeEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "chats_users", joinColumns = @JoinColumn(name = "chat", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "associate_user", referencedColumnName = "id"))
	private List<User> users;

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "chats_admins", joinColumns = @JoinColumn(name = "chat", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "associate_user", referencedColumnName = "id"))
	private List<User> admins;

	public Chat(Long chatId) {
		this.chatId = chatId;
	}
	
	
}
