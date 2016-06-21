
package appdefinedbuttonexample.odg.com.appdefinedbuttonexample.videoplayer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.osterhoutgroup.api.ext.OdgIntent;
import com.osterhoutgroup.api.ext.ReticleRemoteExt;

import java.io.File;

import appdefinedbuttonexample.odg.com.appdefinedbuttonexample.R;

public class VideoPlayer extends Activity {
    static String TAG = "VideoPlayer";

    //private static final int REQUEST_CODE_PICK_VIDEO = 1;
    private static final int REQUEST_CODE_PICK_SHORTCUT = 2;

    public static final String PLAY = "play";
    public static final String STOP = "stop";
    public static final String CHOOSE = "choose";
    public static final String PREFERENCES = "preferences";
    public static final String QUIT = "quit";

    VideoView video_view;
    private Uri mUri;

    boolean playing = false;
    boolean start = true;
    boolean paused = false;
    boolean setting_up_shortcut = false;
    static boolean mLoop = false;
	private VideoController mediaController;
	private LinearLayout mVideoControllerLayout;
	private TextView mTitle;

    private IntentFilter mIntentFilter;

    private ReticleRemoteExt mReticleRemoteExt;

    private int mLastPosition;

    public class PlayerCallback {
        public void doOnBack() {
            onBackPressed();
        }
        
        public void doSelectLoop(boolean loop) {
            updateLoop(loop);
        }
            
        public void doCallback(boolean isPlaying) {
            if (mReticleRemoteExt != null) {
                if (isPlaying) {
                    mReticleRemoteExt.launchVideoPlayerController(OdgIntent.COMMAND_TYPES.PLAY,
                            mFileName,
                            mArtistName, mDuration);
                } else {
                    mReticleRemoteExt.launchVideoPlayerController(OdgIntent.COMMAND_TYPES.PAUSE,
                            null, null, null);
                }
            }
        }
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReticleRemoteExt = new ReticleRemoteExt(this);

		ScreenRes.init(this);
        try {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
                setupShortcut();
                return;
            }
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								 WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.main);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mVideoControllerLayout = (LinearLayout) findViewById(R.id.video_controller);
            video_view = (VideoView) findViewById(R.id.video_view);
            mTitle = (TextView) findViewById(R.id.title);
            LinearLayout seekControl = (LinearLayout)findViewById(R.id.seekControl);
            mediaController = new VideoController(this, mVideoControllerLayout, seekControl, new PlayerCallback());
            video_view.setOnPreparedListener(new PreparedListener());
            video_view.setOnCompletionListener(new CompletionListener());
            video_view.setOnErrorListener(new ErrorListener());
            attachMediaController();

            //mUri = getIntent().getData();
            mUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.awe_armovie);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mIntentFilter = new IntentFilter(OdgIntent.REMOTE_CONTROL_COMMAND_INTENT);
    }

	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    if (intent != null) {
	    	doPause();
	    	mLastPosition = 0;
	    	start = true;
	    	paused = false;
   	        mUri = intent.getData();
	    }
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		ScreenRes.configurationChanged();
	}

	private void attachMediaController() {
        if (mediaController != null) {
            mediaController.setMediaPlayer(video_view);
            mediaController.setEnabled(true);
        }
    }

//	private void toggleMediaControlsVisiblity() {
//        if (mediaController.isShowing()) {
//            mediaController.hide();
//        } else {
//            mediaController.show();
//        }
//    }

	protected void setMediaControllerVisibility(boolean show) {
		if (show) {
			mediaController.show();
		} else {
			mediaController.hide();
		}
	}

    @Override
    protected void onDestroy() {

        mReticleRemoteExt.launchVideoPlayerController(OdgIntent.COMMAND_TYPES.STOP,
                null, null, null);
                
        if (video_view != null) {
            try {
                video_view.stopPlayback();
            } catch (IllegalStateException ie) {
                // ignore
            }
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        doPause();
        unregisterReceiver(mRemoteReceiver);
    }

    private void doPause() {
    	if (video_view != null && video_view.isPlaying()) {
            //video_view.pause();
        	mMediaPlayer.pause();
        	mLastPosition = mMediaPlayer.getCurrentPosition();
            //start = true;
            paused = true;
            playing = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (start) {
            playVideo();
            mTitle.setText(mFileName);
        }
        start = false;

        setting_up_shortcut = false;
        if (mediaController != null) {
            mediaController.hide();
        }
        if (paused) {
        	//mMediaPlayer.resume();
        	//video_view.resume();
        	mediaController.show(TIME_OUT);
        	mMediaPlayer.seekTo(mLastPosition);
        	mMediaPlayer.start();
        	paused = false;
        }

        registerReceiver(mRemoteReceiver, mIntentFilter);
    }

    private String mFileName = "";
    private String mArtistName = "Unknown";
    private String mDuration = "Unknown";
    private void playVideo() {
		if (video_view == null) {
			return;
		}
        try {
            if (mUri == null) {
                Toast.makeText(this, "Video not found", Toast.LENGTH_LONG).show();
            } else {
                video_view.setVideoURI(mUri);
				//Preferences.setStringValue(Preference.PREF_VIDEO, mUri.getPath());
                video_view.start();
                playing = true;
                getMediaTitle();

                mReticleRemoteExt.launchVideoPlayerController(OdgIntent.COMMAND_TYPES.PLAY,
                        mFileName,
                        mArtistName, mDuration);
                        
            }
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
            video_view.stopPlayback();
        }
    }

    private void getMediaTitle() {
        String scheme = mUri.getScheme();
        if (scheme.equals("file")) {
            mFileName = mUri.getLastPathSegment();
        } else if (scheme.equals("content")) {
            String[] columns = {
                    MediaStore.Video.VideoColumns._ID,
                    MediaStore.Video.VideoColumns.TITLE,
                    MediaStore.Video.VideoColumns.ARTIST,
                    MediaStore.Video.VideoColumns.DURATION,
                };
            Cursor cursor = getContentResolver().query(mUri, columns, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                int columnIndex = 0;
                try {
                    cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TITLE);
                    mFileName = cursor.getString(columnIndex);
                } catch (IllegalArgumentException e) {
                }
                try {
                    columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.ARTIST);
                    mArtistName = cursor.getString(columnIndex);
                } catch (IllegalArgumentException e) {
                }
                cursor.moveToFirst();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        
    }

    private int TIME_OUT = 5000; //5 sec timeout
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        } else {
            mediaController.show(TIME_OUT);
            return false;
        }
        return true;
    }

    private void setupShortcut() {
        startPickFolder(REQUEST_CODE_PICK_SHORTCUT);
    }

    private void startPickFolder(int request_code) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        try {
            startActivityForResult(intent, request_code);
            setting_up_shortcut = true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_filemanager_installed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
//            case REQUEST_CODE_PICK_VIDEO:
//                finishPickVideo(resultCode, data);
//                break;
            case REQUEST_CODE_PICK_SHORTCUT:
                finishPickShortcut(resultCode, data);
                break;
        }
    }

    // create shortcut intent using folder from result
    private void finishPickShortcut(int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) {
			setResult(RESULT_CANCELED);
			finish();
            return;
		}
        // obtain the filename
        Uri file_uri = data.getData();
        if (file_uri == null) {
			setResult(RESULT_CANCELED);
			finish();
            return;
		}
        String file_path = file_uri.getPath();
        if (file_path == null) {
			setResult(RESULT_CANCELED);
			finish();
            return;
		}

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClass(this, this.getClass());
        shortcutIntent.setData(file_uri);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        File file = new File(file_path);
        String name = file.getName().replaceFirst("[.][^.]+$", "");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);

        // if an image file exists with same base name as video file,
        // use it as the shortcut icon
        File shortcut_file = new File(changeExtension(file_path, ".jpg"));
        if (!shortcut_file.exists())
            shortcut_file = new File(changeExtension(file_path, ".png"));

        if (shortcut_file.exists()) {
            final int ICON_WIDTH = 128;
            final int ICON_HEIGHT = 128;
            Bitmap bitmap = this.decodeBitmap(shortcut_file.getPath(), ICON_WIDTH, ICON_HEIGHT);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
        } else {
            Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this,
                    R.drawable.video_player);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        }
        Log.d(TAG, "finishPickShortcut - " + name);
        setResult(RESULT_OK, intent);
        finish();
    }

    // changes extension to new extension
    static String changeExtension(String originalName, String newExtension) {
        int lastDot = originalName.lastIndexOf(".");
        if (lastDot != -1) {
            return originalName.substring(0, lastDot) + newExtension;
        } else {
            return originalName + newExtension;
        }
    }
    
    private void updateLoop(boolean loop) {
        if (mediaController != null) {
            mediaController.setEnabled(true);
            //Preferences.setBooleanValue(Preference.PREF_LOOP, loop);
            //mLoop = loop;
            //Log.d(TAG, "OnPrepared - looping: " + Boolean.toString(mLoop));
            mMediaPlayer.setLooping(mLoop);
        }
    }

    private Bitmap decodeBitmap(String path, int targetWidth, int targetHeight) {
        // First, get the dimensions of the image
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        final int BUFFER_SIZE = 16384;
        Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math
                .abs(options.outWidth - targetWidth);
        if (options.outHeight * options.outWidth * 2 >= BUFFER_SIZE) {
            // scale to smallest power of 2 that'll get it <= desired dimensions
            double sampleSize = scaleByHeight ? options.outHeight / targetHeight : options.outWidth
                    / targetWidth;
            options.inSampleSize = (int) Math.pow(2d,
                    Math.floor(Math.log(sampleSize) / Math.log(2d)));
        }

        // Do the actual decoding
        Bitmap bitmap = null;
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[BUFFER_SIZE];
        try {
            bitmap = BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            Log.w(TAG, "showImage failed: " + path);
            e.printStackTrace();
        }
        return bitmap;
    }
    private MediaPlayer mMediaPlayer;
    class PreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            mMediaPlayer = mp;
            //mLoop = Preferences.getBooleanValue(Preference.PREF_LOOP);
        }
    }

    class CompletionListener implements MediaPlayer.OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mp) {
			VideoPlayer.this.setMediaControllerVisibility(false);
			Log.d(TAG, "onCompletion");
			playing = false;
			finish();
		}
	}

	/**
	 * Catch MediaPlayer Errors
	 */
    class ErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            VideoPlayer.this.setMediaControllerVisibility(false);
            Log.d(TAG, "onError, Code: " + what + " type: " + extra);
            playing = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoPlayer.this, getString(R.string.media_error),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
    }

    BroadcastReceiver mRemoteReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaController != null) {
                int command = intent.getIntExtra(OdgIntent.REMOTE_CONTROL_COMMAND_TYPE, -1);

                if (!mediaController.isShowing()) {
                    mediaController.show(TIME_OUT);
                }
                switch(command) {
                case OdgIntent.COMMAND_TYPES.REWIND:
                    mediaController.addPosition(-VideoController.FIVE_SECS);
                    break;

                case OdgIntent.COMMAND_TYPES.FAST_FORWARD:
                    mediaController.addPosition(VideoController.FIVE_SECS);
                    break;

                case OdgIntent.COMMAND_TYPES.PLAY:
                    mediaController.doPauseResume(true);
                    break;
                }
            }
        }
    };
}
