package com.cop4331.networking;

import com.cop4331.networking.User;

public class Relationship {
	public static final int STATUS_PENDING  = 0;
	public static final int STATUS_ACCEPTED = 1;
	public static final int STATUS_DENIED   = 2;

	private final User 	   mUser;
	private final boolean  mSentByMe;
	private final int	   mStatus;
	

	public Relationship(User user, boolean sentFromMe, int status) {
		mUser 	  = user;
		mSentByMe = sentFromMe;
		mStatus   = status;
	}

	public User getUser() {
		return mUser;
	}

	public boolean isSentByMe() {
		return mSentByMe;
	}

	public int getStatus() {
		return mStatus;
	}
}