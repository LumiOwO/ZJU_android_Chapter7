package com.example.chapter7.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.chapter7.R;

import java.util.Locale;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity extends AppCompatActivity
{
	enum Orientation {
		PORTRIAT, LANDSCAPE
	};

	// UI
	private View mLoadingUI;
	private View mPlayingUI;
	// video player
	private IJKPlayerView mVideoPlayer;
	// orientation
	private Orientation mOrientation;
	// widgets
	private Button mPauseBtn;
	private Button mFullScreenBtn;
	private SeekBar mProgressBar;
	private SeekBar mVolumnBar;
	private TextView mPlayTimeView;
	private View mToolBar;

	// handler for progress bar
	private static final int REFRESH_DELAY = 50;
	private static Handler mProgressHandler = new Handler();
	private boolean isAuto = false;
	private Runnable mRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			if(mVideoPlayer.isPlaying()) {
				isAuto = true;
				synchonizeProgress();
				isAuto = false;
				mProgressHandler.postDelayed(this, REFRESH_DELAY);
			}
		}
	};

	private static final int HIDE_DELAY = 5000;
	private static Handler mHideHandler = new Handler();
	private Runnable mHideRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			mToolBar.setVisibility(View.INVISIBLE);
			mHideHandler.removeCallbacksAndMessages(null);
		}
	};

	public static void launch(Activity activity)
	{
		Intent intent = new Intent(activity, VideoActivity.class);

		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		getSupportActionBar().hide();
		hideSystemUI();

		initWidgets();
		initIJKPlayer();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		startAutoProgress();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mVideoPlayer != null && mVideoPlayer.isPlaying()) {
			mVideoPlayer.stop();
		}
		IjkMediaPlayer.native_profileEnd();
		stopAutoProgress();
	}

	@Override
	protected void onDestroy() {
		if (mVideoPlayer != null) {
			mVideoPlayer.stop();
			mVideoPlayer.release();
			mVideoPlayer = null;
		}

		super.onDestroy();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus && Build.VERSION.SDK_INT >= 19) {
			hideSystemUI();
		}
	}

	private void initWidgets()
	{
		// get widgets
		mLoadingUI = findViewById(R.id.loadingUI);
		mPlayingUI = findViewById(R.id.playingUI);
		mToolBar = findViewById(R.id.tool_bar);

		mVideoPlayer = findViewById(R.id.ijkPlayer);

		mProgressBar = findViewById(R.id.seekbar_progress);
		mVolumnBar = findViewById(R.id.seekbar_volumn);

		mFullScreenBtn = findViewById(R.id.btn_fullscreen);
		mPauseBtn = findViewById(R.id.btn_pause);

		mPlayTimeView = findViewById(R.id.text_time);

		// init widgets
		mVolumnBar.setProgress((int)(mVolumnBar.getMax() * 0.3));
		mPlayingUI.setVisibility(View.INVISIBLE);
		mOrientation = Orientation.PORTRIAT;

		// set callbacks
		mFullScreenBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resetHideHandler();
				if(mOrientation == Orientation.LANDSCAPE)
					toPortrait();
				else if(mOrientation == Orientation.PORTRIAT)
					toLandscape();
			}
		});
		mPauseBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resetHideHandler();
				if(mVideoPlayer.isPlaying()) {
					mVideoPlayer.pause();
					mPauseBtn.setText(getString(R.string.start));
				} else {
					mVideoPlayer.start();
					mPauseBtn.setText(getString(R.string.pause));
				}
			}
		});
		mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			private boolean isPlaying;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if(!isAuto) {
					resetHideHandler();

					double ratio = 1.0 * seekBar.getProgress() / seekBar.getMax();
					mVideoPlayer.seekTo((long) (mVideoPlayer.getDuration() * ratio));
					synchonizeProgress();
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				stopAutoProgress();

				isPlaying = mVideoPlayer.isPlaying();
				mVideoPlayer.pause();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				if(isPlaying)
					mVideoPlayer.start();

				startAutoProgress();
				isAuto = false;
			}
		});
		mVolumnBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				resetHideHandler();

				AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				double ratio = 1.0 * mVolumnBar.getProgress() / mVolumnBar.getMax();
				int index = (int)(ratio * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, AudioManager.FLAG_PLAY_SOUND);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}
		});

		mPlayingUI.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mToolBar.getVisibility() == View.VISIBLE) {
					mToolBar.setVisibility(View.INVISIBLE);
					mHideHandler.removeCallbacksAndMessages(null);
				}
				else if(mToolBar.getVisibility() == View.INVISIBLE) {
					mToolBar.setVisibility(View.VISIBLE);
					resetHideHandler();
				}
			}
		});

		startAutoProgress();
	}

	private void initIJKPlayer()
	{
		// load native library
		try {
			IjkMediaPlayer.loadLibrariesOnce(null);
			IjkMediaPlayer.native_profileBegin("libijkplayer.so");
		} catch (Exception e) {
			this.finish();
		}

		// init video
		mVideoPlayer.setVideo(R.raw.big_buck_bunny);
		mVideoPlayer.setOnVideoPlayerListener(new IJKPlayerListener() {
			@Override
			public void onPrepared(IMediaPlayer iMediaPlayer)
			{
				super.onPrepared(iMediaPlayer);

				// delay a few moment
				// to show the loading UI
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// init screen
				if(mOrientation == Orientation.LANDSCAPE)
					toLandscape();
				else if(mOrientation == Orientation.PORTRIAT)
					toPortrait();

				mLoadingUI.setVisibility(View.INVISIBLE);
				mPlayingUI.setVisibility(View.VISIBLE);

				mHideHandler.postDelayed(mHideRunnable, 3000);
				startAutoProgress();
				iMediaPlayer.start();
			}

			@Override
			public void onCompletion(IMediaPlayer iMediaPlayer)
			{
				super.onCompletion(iMediaPlayer);
				stopAutoProgress();

				mProgressBar.setProgress(0);
				mPauseBtn.setText(getString(R.string.start));
			}
		});

	}

	private void hideSystemUI()
	{
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	private void toPortrait()
	{
		// get video size
		int videoWidth = mVideoPlayer.getVideoWidth();
		int videoHeight = mVideoPlayer.getVideoHeight();

		boolean isPlaying = mVideoPlayer.isPlaying();
		mVideoPlayer.pause();
		mOrientation = Orientation.PORTRIAT;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// get window size
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float windowWidth  = metrics.widthPixels;
		float windowHeight = metrics.heightPixels;
		if(windowWidth > windowHeight)
			windowWidth = windowHeight;

		float ratio = windowWidth / videoWidth;

		// resize video view
		FrameLayout.LayoutParams layoutParams =
				(FrameLayout.LayoutParams) mPlayingUI.getLayoutParams();
		layoutParams.height = (int) (videoHeight * ratio);
		layoutParams.width = (int) (videoWidth * ratio);
		mPlayingUI.setLayoutParams(layoutParams);

		mFullScreenBtn.setText(getString(R.string.fullscreen));

		if(isPlaying)
			mVideoPlayer.start();
		mVolumnBar.setVisibility(View.INVISIBLE);
	}

	private void toLandscape()
	{
		boolean isPlaying = mVideoPlayer.isPlaying();
		mVideoPlayer.pause();
		mOrientation = Orientation.LANDSCAPE;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// resize video view to fill the window
		FrameLayout.LayoutParams layoutParams =
				(FrameLayout.LayoutParams) mPlayingUI.getLayoutParams();
		layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
		layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
		mPlayingUI.setLayoutParams(layoutParams);

		mFullScreenBtn.setText(getString(R.string.smallscreen));

		if(isPlaying)
			mVideoPlayer.start();
		mVolumnBar.setVisibility(View.VISIBLE);
	}

	private void synchonizeProgress()
	{
		// get time text
		long current_ms = mVideoPlayer.getCurrentPosition();
		long duration_ms = mVideoPlayer.getDuration();

		long current_s =  current_ms / 1000;
		long duration_s = duration_ms / 1000;

		long current_sec = current_s % 60;
		long current_min = current_s / 60;
		long total_second = duration_s % 60;
		long total_minute = duration_s / 60;

		String time =
				String.format(Locale.getDefault(), "%02d:%02d/%02d:%02d",
						current_min, current_sec, total_minute, total_second);

		mPlayTimeView.setText(time);
		if (isAuto && duration_s != 0) {
			mProgressBar.setProgress((int)
					(1.0 * mProgressBar.getMax() * current_ms / duration_ms));
		}
	}

	private void startAutoProgress()
	{
		mProgressHandler.postDelayed(mRunnable, REFRESH_DELAY);
	}

	private void stopAutoProgress()
	{
		mProgressHandler.removeCallbacksAndMessages(null);
	}

	private void resetHideHandler()
	{
		mHideHandler.removeCallbacksAndMessages(null);
		mHideHandler.postDelayed(mHideRunnable, HIDE_DELAY);
	}
}
