package com.example.chapter7.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IJKPlayerView extends FrameLayout
{
	private IjkMediaPlayer mPlayer;
	private SurfaceView mSurfaceView;

	private Context mContext;

	private IJKPlayerListener mListener;

	public IJKPlayerView(Context context)
	{
		this(context, null);
	}

	public IJKPlayerView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public IJKPlayerView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		setFocusable(true);
		mContext = context;
	}

	public void setVideo(String path)
	{
		// create new player
		createPlayer();
		// set video path
		try {
			mPlayer.setDataSource(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// create surface view
		createSurfaceView();
	}

	public void setVideo(int resID)
	{
		// create new player
		createPlayer();
		// set video source
		IJKPlayerDataSource provider = IJKPlayerDataSource.getInstance(mContext, resID);
		mPlayer.setDataSource(provider);
		// create surface view
		createSurfaceView();
	}

	public void setOnVideoPlayerListener(IJKPlayerListener listener)
	{
		mListener = listener;

		if(mPlayer != null) {
			mPlayer.setOnPreparedListener(listener);
			mPlayer.setOnInfoListener(listener);
			mPlayer.setOnSeekCompleteListener(listener);
			mPlayer.setOnBufferingUpdateListener(listener);
			mPlayer.setOnErrorListener(listener);
			mPlayer.setOnCompletionListener(listener);
			mPlayer.setOnPreparedListener(listener);
			mPlayer.setOnVideoSizeChangedListener(listener);
		}
	}

	public void start()
	{
		if(mPlayer != null)
			mPlayer.start();
	}

	public void pause()
	{
		if(mPlayer != null)
			mPlayer.pause();
	}

	public int getVideoWidth()
	{
		return mPlayer != null? mPlayer.getVideoWidth(): -1;
	}

	public int getVideoHeight()
	{
		return mPlayer != null? mPlayer.getVideoHeight(): -1;
	}

	public void stop()
	{
		if(mPlayer != null)
			mPlayer.stop();
	}

	public void release()
	{
		if (mPlayer != null) {
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
	}

	public boolean isPlaying()
	{
		return mPlayer != null && mPlayer.isPlaying();
	}

	public void seekTo(long now)
	{
		if(mPlayer != null)
			mPlayer.seekTo(now);
	}

	public long getDuration()
	{
		return mPlayer != null? mPlayer.getDuration(): 0;
	}

	public long getCurrentPosition()
	{
		return mPlayer != null? mPlayer.getCurrentPosition(): 0;
	}

	private void createPlayer()
	{
		// remove exist player
		if(mPlayer != null)
		{
			mPlayer.stop();
			mPlayer.setDisplay(null);
			mPlayer.release();
			mPlayer = null;
		}

		// create new player
		mPlayer = new IjkMediaPlayer();
		mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
		mPlayer.setSpeed(1f);

		// reset listener
		setOnVideoPlayerListener(mListener);
	}

	private void createSurfaceView()
	{
		if (mSurfaceView != null)
		{
			// reset surface view
			mPlayer.setDisplay(mSurfaceView.getHolder());
			mPlayer.prepareAsync();
		}
		else
		{
			mSurfaceView = new SurfaceView(mContext);
			mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback()
			{
				@Override
				public void surfaceCreated(SurfaceHolder holder)
				{
					mPlayer.setDisplay(holder);
					mPlayer.prepareAsync();
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
				{

				}

				@Override
				public void surfaceDestroyed(SurfaceHolder holder)
				{

				}
			});
			// set layout parameters --> match parent
			ViewGroup.LayoutParams layoutParams =
					new ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.MATCH_PARENT);
			mSurfaceView.setLayoutParams(layoutParams);

			this.addView(mSurfaceView);
		}
	}
}
