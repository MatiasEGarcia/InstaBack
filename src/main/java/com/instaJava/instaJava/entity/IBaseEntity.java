package com.instaJava.instaJava.entity;

public interface IBaseEntity {

	/**
	 * To get entity id.
	 * @return entity's id.
	 */
	Long getBaseEntityId();
	
	/**
	 * To set liked atribute ,should be overrided by entities which can be liked by users.
	 * @param value - true if like, false if not, null if there is no opinion.
	 */
	default void setItemEntityLiked(Boolean value) {
		System.out.println("method not overrided, default message");
	}
}
