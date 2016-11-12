package com.cop4331.networking;

import com.cop4331.networking.User;

public class Relationship {
	private static final int STATUS_PENDING  = 0;
	private static final int STATUS_ACCEPTED = 1;
	private static final int STATUS_DENIED   = 2;

	private final User mUser;
	private final boolean  mSentByMe;
	private final int	   mStatus;
	

	public Relationship(User user, boolean sentFromMe, int status) {
		mUser 	  = user;
		mSentByMe = sentFromMe;
		mStatus   = status;
	}

	public User getUser() {
		return null;
	}

	public boolean isSentByMe() {
		return false;
	}
}