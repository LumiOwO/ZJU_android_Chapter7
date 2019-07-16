package com.example.chapter7;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.chapter7.image.ImageActivity;
import com.example.chapter7.video.VideoActivity;

public class MainActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.btn_image).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ImageActivity.launch(MainActivity.this);
			}
		});

		findViewById(R.id.btn_video).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				VideoActivity.launch(MainActivity.this);
			}
		});

	}

}
