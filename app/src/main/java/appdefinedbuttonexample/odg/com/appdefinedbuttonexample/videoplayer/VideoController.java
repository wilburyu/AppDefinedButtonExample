package appdefinedbuttonexample.odg.com.appdefinedbuttonexample.videoplayer;

import appdefinedbuttonexample.odg.com.appdefinedbuttonexample.R;
import appdefinedbuttonexample.odg.com.appdefinedbuttonexample.videoplayer.VideoPlayer.PlayerCallback;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;


/**
 * This class shows a window with video controls for playing, pausing, rewinding or forwarding.
 * Lots of code taken from MediaController.
 */
public class VideoController {
	private static String TAG = "VideoController";
    private Context mContext;
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean showing = false;
    private boolean dragging;
    private static final int defaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    protected static final int THIRTY_SECS = 30000;
    public static final int FIVE_SECS = 5000;
    private boolean useFastForward;
    private Formatter formatter;
    private ImageButton ffwdButton;
    private ImageButton rewButton;
	private ImageButton playButton;
	private ImageButton galleryButton;
	private ImageButton loopButton;
	private LinearLayout mControlLayout;
	private MediaController.MediaPlayerControl mPlayer;
	private StringBuilder formatBuilder;

	private PlayerCallback mCallback;
	private LinearLayout mSeekControl;

	/**
	 * Constructors.
	 * @param videoPlayer
	 */
	public VideoController(Context context, LinearLayout layout, LinearLayout seekControl, PlayerCallback cb) {
        mContext = context;
		useFastForward = true;
		mControlLayout = layout;
		mCallback = cb;
		mSeekControl = seekControl;
		initControllerView(layout);
    }
	
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

	/**
	 * Setup Controllers
	 * @param parent
	 */
    private void initControllerView(View parent) {
        galleryButton = (ImageButton) mControlLayout.findViewById(R.id.gallery);
        loopButton = (ImageButton) mControlLayout.findViewById(R.id.loop);
        ffwdButton = (ImageButton) mControlLayout.findViewById(R.id.ffd);
        rewButton = (ImageButton) mControlLayout.findViewById(R.id.rew);
        playButton = (ImageButton) mControlLayout.findViewById(R.id.play);
        mProgress = (SeekBar) mSeekControl.findViewById(R.id.mediacontroller_progress);
        mCurrentTime = (TextView) mSeekControl.findViewById(R.id.time_current);
        mEndTime = (TextView) mSeekControl.findViewById(R.id.time);
        
		//playButton = (ImageButton) parent.findViewById(R.id.play);
        if (playButton != null) {
            playButton.setOnClickListener(mPlayListener);
			playButton.requestFocus();
        }

        //ffwdButton = (ImageButton) parent.findViewById(R.id.ffd);
        if (ffwdButton != null) {
            ffwdButton.setOnClickListener(mFfwdListener);
        }

        //rewButton = (ImageButton) parent.findViewById(R.id.rew);
        if (rewButton != null) {
            rewButton.setOnClickListener(mRewListener);
        }
        
        if (galleryButton != null) {
            galleryButton.setOnClickListener(mGalleryListener);
        }
        
        if (loopButton != null) {
            loopButton.setOnClickListener(mLoopListener);
        }


        //mProgress = (SeekBar) parent.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }
        mProgress.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mProgress.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.progress_bar_drawable_selected));
                } else {
                    mProgress.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.progress_bar_drawable));
                }
            }
            
        });
        
        updateLoop();

        //mEndTime = (TextView) parent.findViewById(R.id.time);
        //mCurrentTime = (TextView) parent.findViewById(R.id.time_current);
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(0);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (rewButton != null && !mPlayer.canSeekBackward()) {
                rewButton.setEnabled(false);
            }
            if (ffwdButton != null && !mPlayer.canSeekForward()) {
                ffwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }
    
    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     * @param timeout The timeout in milliseconds. Use 0 to show
     * the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!showing) {
            updatePausePlay();
            setProgress();
			mControlLayout.setVisibility(View.VISIBLE);
			mSeekControl.setVisibility(View.VISIBLE);
            showing = true;
            setEnabled(true);
        }
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
		mHandler.removeMessages(FADE_OUT);
        if (timeout != 0) {
			Message msg = mHandler.obtainMessage(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return showing;
    }

	/**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (showing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                mControlLayout.setVisibility(View.INVISIBLE);
                mSeekControl.setVisibility(View.INVISIBLE);
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "already removed");
            }
            showing = false;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!dragging && showing && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    protected int setProgress() {
        if (mPlayer == null || dragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    private View.OnClickListener mPlayListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (showing) {
                doPauseResume(false);
            }
            show(0);
        }
    };

    private void updatePausePlay() {
        if (mPlayer.isPlaying()) {
			if (playButton != null) {
	            playButton.setImageResource(R.drawable.button_pause_selector);
			}
        } else {
			if (playButton != null) {
				playButton.setImageResource(R.drawable.button_play_selector);
			}
        }
    }
    
    private void updateLoop() {
        boolean loop = !VideoPlayer.mLoop;
        mCallback.doSelectLoop(loop);
        if (VideoPlayer.mLoop) {
            if (loopButton != null) {
                loopButton.setImageResource(R.drawable.button_loop_selector_on);
            }
        } else {
            if (loopButton != null) {
                loopButton.setImageResource(R.drawable.button_loop_selector);
            }
        }
        //oopButton.setSelected(true);
    }

    public void doPauseResume(boolean fromRemote) {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            if (!fromRemote && mCallback != null) {
                mCallback.doCallback(false);
            }
        } else {
            mPlayer.start();
            if (!fromRemote && mCallback != null) {
                mCallback.doCallback(true);
            }
        }
        updatePausePlay();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            dragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated
                // changes to
                // the progress bar's position.
                return;
            }
            if (showing) {
                long duration = mPlayer.getDuration();
                long newposition = (duration * progress) / 1000L;
                mPlayer.seekTo((int) newposition);
                if (mCurrentTime != null) {
                    mCurrentTime.setText(stringForTime((int) newposition));
                }
            }

        }

        public void onStopTrackingTouch(SeekBar bar) {
            dragging = false;
            setProgress();
            updatePausePlay();
            show(defaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    public void setEnabled(boolean enabled) {
        if (playButton != null) {
            playButton.setEnabled(enabled);
        }
        if (ffwdButton != null) {
            ffwdButton.setEnabled(enabled);
        }
        if (rewButton != null) {
            rewButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        mControlLayout.setEnabled(enabled);
        mSeekControl.setEnabled(enabled);
        mControlLayout.requestFocus();
        playButton.requestFocus();
    }

    private View.OnClickListener mRewListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (showing) {
                addPosition(-FIVE_SECS);
            }
            show(0);
			//rewButton.startAnimation(rewButtonFadeAnimation);
        }
    };
    
    private View.OnClickListener mLoopListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (showing) {
                updateLoop();
            }
            show(0);
        }
    };
    
    private View.OnClickListener mGalleryListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (showing) {
                //do back
                mCallback.doOnBack();
            }
            show(0);
            //rewButton.startAnimation(rewButtonFadeAnimation);
        }
    };

    private View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (showing) {
                addPosition(FIVE_SECS);             
            }
            show(0);
        }
    };

	public void addPosition(int position) {
		int duration = mPlayer.getDuration();
		int newPosition = Math.min(duration, Math.max(0, mPlayer.getCurrentPosition() + position));
		mPlayer.seekTo(newPosition);
		setProgress();
	}
}
