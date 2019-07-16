package com.example.chapter7.video;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class IJKPlayerListener implements
		IMediaPlayer.OnBufferingUpdateListener,
		IMediaPlayer.OnCompletionListener,
		IMediaPlayer.OnPreparedListener,
		IMediaPlayer.OnInfoListener,
		IMediaPlayer.OnVideoSizeChangedListener,
		IMediaPlayer.OnErrorListener,
		IMediaPlayer.OnSeekCompleteListener
{
	// default listener callback

	@Override
	public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i)
	{

	}

	@Override
	public void onCompletion(IMediaPlayer iMediaPlayer)
	{

	}

	@Override
	public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1)
	{
		return false;
	}

	@Override
	public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1)
	{
		return false;
	}

	@Override
	public void onPrepared(IMediaPlayer iMediaPlayer)
	{

	}

	@Override
	public void onSeekComplete(IMediaPlayer iMediaPlayer)
	{

	}

	@Override
	public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3)
	{

	}
}
