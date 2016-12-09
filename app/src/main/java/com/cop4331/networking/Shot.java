package com.cop4331.networking;

import com.cop4331.networking.User;
import com.parse.GetFileCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.File;

/**
 * Class to manage a Game's shot
 */
public class Shot {
	private final User mUser;
	private File mImage = null;
	private final ParseFile mShotFile;

	private DownloadListener mDLListener = null;

	/**
	 * OBSERVER Pattern
	 * Listener to notify the download status of a Shot's image file
	 */
	public interface DownloadListener {
		/**
		 * Callback provided to notify when the Shot's image has been downloaded
		 * @param shot the shot that the image belongs to
		 */
		public void onDownloadCompleted(Shot shot);

		/**
		 * Callback provided to notify of an error while downloading a Shot's image
		 * @param shot the shot that the error belongs to
		 */
		public void onDownloadError(Shot shot);
	}

	/**
	 * Instantiates a new Shot.
	 * @param user     the user that submitted the Shot
	 * @param shotFile the ParseFile that points to the Shot's image
	 */
	public Shot(User user, ParseFile shotFile) {
		mUser   = user;
		mShotFile = shotFile;
	}

	/**
	 * Gets the User that submitted the shot
	 * @return the user that submitted the shot
	 */
	public User getUser() {
		return mUser;
	}

	/**
	 * Downloads the shot's image
	 * @param listener the listener to notify once the download has been completed
	 */
	public void downloadImage(final DownloadListener listener) {
        //Perform download
		mShotFile.getFileInBackground(new GetFileCallback() {
			@Override
			public void done(File file, ParseException e) {
				if (e == null) {
                    mImage = file;

                    //Notify listener
                    if (listener != null) {
                        listener.onDownloadCompleted(Shot.this);
                    }
				}
			}
		});
	}

	/**
	 * Gets the file path to the shot's image
	 * @return the File object containing the path to the shot's image. If the shot has not yet
     * been donwloaded then null is returned
	 */
	public File getImage() {
		return mImage;
	}
}