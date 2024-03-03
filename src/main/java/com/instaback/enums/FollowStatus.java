package com.instaback.enums;

public enum FollowStatus {
	ACCEPTED,
	REJECTED,
	IN_PROCESS,
	NOT_ASKED;//this is used when we retrieve the following status to the client, but the following record not exists
}
