package com.cop4331.networking;

import com.cop4331.networking.User;

/**
 * The type Relationship.
 */
public class Relationship {
	/**
	 * The constant STATUS_PENDING.
	 */
	public static final int STATUS_PENDING  = 0;
	/**
	 * The constant STATUS_ACCEPTED.
	 */
	public static final int STATUS_ACCEPTED = 1;
	/**
	 * The constant STATUS_DENIED.
	 */
	public static final int STATUS_DENIED   = 2;

	private final User 	   mUser;
	private final boolean  mSentByMe;
	private final int	   mStatus;


	/**
	 * Instantiates a new Relationship.
	 * @param user       the user in the relationship
	 * @param sentFromMe indicates if the relationship was initiated from the current Account
	 * @param status     the status of the relationship
	 */
	public Relationship(User user, boolean sentFromMe, int status) {
		mUser 	  = user;
		mSentByMe = sentFromMe;
		mStatus   = status;
	}

	/**
	 * Getst the user associated the relationship
	 * @return the user
	 */
	public User getUser() {
		return mUser;
	}

	/**
	 * Gets if the request was sent by the current Account
	 * @return true if sent by the current Account, otherwise false
	 */
	public boolean isSentByMe() {
		return mSentByMe;
	}

	/**
	 * Gets the current status of the relationship
	 * @return the status of the relationship
	 */
	public int getStatus() {
		return mStatus;
	}
}