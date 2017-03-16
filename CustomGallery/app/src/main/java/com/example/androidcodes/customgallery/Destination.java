package com.example.androidcodes.customgallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class Destination extends Activity {

	private ImageView iv_image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.destination);

		iv_image = (ImageView) findViewById(R.id.iv_image);
		iv_image.setImageBitmap(BitmapFactory.decodeFile(MyGallery.selected_image_path));
	}
}
