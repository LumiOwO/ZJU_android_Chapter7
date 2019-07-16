package com.example.chapter7.image;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends PagerAdapter
{
	private List<View> mList = new ArrayList<>();

	@Override
	public int getCount()
	{
		return mList.size();
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object o)
	{
		return view == o;
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position)
	{
		View view = mList.get(position);
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object)
	{
		container.removeView(mList.get(position));
	}

	public void setImages(List<View> list)
	{
		mList = list;
	}
}
