package com.cop4331.networking;

import com.cop4331.networking.User;

import java.io.File;

public class Shot {
	private final User mUser;
	private File mImage = null;
	private final String mShotId;

	private DownloadListener mDLListener = null;

	public interface DownloadListener {
		public void onDownloadCompleted(Shot shot);
		public void onDownloadError(Shot shot);
	}

	public Shot(User user, String shotId) {
		mUser   = user;
		mShotId = shotId;
	}

	public User getUser() {
		return null;
	}

	public String getShotId() {
		return null;
	}

	public void downloadImage(DownloadListener listener) {
	
	}

	public File getImage() {
		return null;
	}
}