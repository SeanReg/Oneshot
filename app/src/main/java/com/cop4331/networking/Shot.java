package com.cop4331.networking;

import com.cop4331.networking.User;
import com.parse.GetFileCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.File;

public class Shot {
	private final User mUser;
	private File mImage = null;
	private final ParseFile mShotFile;

	private DownloadListener mDLListener = null;

	public interface DownloadListener {
		public void onDownloadCompleted(Shot shot);
		public void onDownloadError(Shot shot);
	}

	public Shot(User user, ParseFile shotFile) {
		mUser   = user;
		mShotFile = shotFile;
	}

	public User getUser() {
		return mUser;
	}

	public String getShotId() {
		return null;
	}

	public void downloadImage(final DownloadListener listener) {
		mShotFile.getFileInBackground(new GetFileCallback() {
			@Override
			public void done(File file, ParseException e) {
				if (e == null) {
                    mImage = file;

                    if (listener != null) {
                        listener.onDownloadCompleted(Shot.this);
                    }
				}
			}
		});
	}

	public File getImage() {
		return mImage;
	}
}