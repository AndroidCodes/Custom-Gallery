package com.example.androidcodes.customgallery;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class MyGallery extends Activity {

	public static String selected_image_path;

	private ArrayList<Album> list_of_albums;
	private ArrayList<String> list_of_images_paths;

	private ImageLoader imageLoader;
	private DisplayImageOptions defaultOptions;

	private ListView lv_album_list;
	private GridView gv_imgs_according_to_albums;
	private TextView tv_no_images;

	private Albums_List_Adapter album_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		initImageLoader();

		lv_album_list = (ListView) findViewById(R.id.lv_album_list);
		gv_imgs_according_to_albums = (GridView) findViewById(R.id.gv_imgs_according_to_albums);
		tv_no_images = (TextView) findViewById(R.id.tv_no_images);

		list_of_albums = new ArrayList<>();

		Cursor album_cursor = getApplicationContext().getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
						new String[] { "_data", "_id", "bucket_display_name","bucket_id" }, null, null, "datetaken DESC");

		HashSet<String> temp = new HashSet<String>();   //Use to just remove repeatition of same name album
		for (int i = 0; i < album_cursor.getCount(); i++) {
			album_cursor.moveToPosition(i);

			Album album = new Album();
			String albumName = album_cursor.getString(album_cursor.getColumnIndexOrThrow("bucket_display_name"));
			album.setAlbumId(album_cursor.getString(album_cursor.getColumnIndexOrThrow("bucket_id")));
			album.setAlbumName(albumName);
			album.setImagePath(album_cursor.getString(album_cursor.getColumnIndexOrThrow("_data")));

			if (temp.add(albumName)) {

				list_of_albums.add(album);

			}			
		}
		
		album_adapter = new Albums_List_Adapter(MyGallery.this, list_of_albums);
		lv_album_list.setAdapter(album_adapter);
		lv_album_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				refreshAlbums(position);

				fillGallery(list_of_albums.get(position).getAlbumId());

			}
		});

		gv_imgs_according_to_albums.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> adapterView,
							View view, int position, long arg3) {
						// TODO Auto-generated method stub
						selected_image_path = list_of_images_paths.get(position);
						Intent i = new Intent(MyGallery.this, Destination.class);
						startActivity(i);
					}
				});
		
		//To load Images From The First Album If Camera Folder Doesn't Contain Images.If Camera Folder Contain Images
		//Then First Time Load Images From Camera Folder. 
		int index = 0;
		if (list_of_albums != null) {
			for (int i = 0; i < list_of_albums.size(); i++) {
				if (list_of_albums.get(i).getAlbumName().equalsIgnoreCase("camera")) {
					index = i;
					break;
				}
				else{
					int size;
					if (get_count_of_images_inside_single_album(list_of_albums.get(0).getAlbumId()) > 0) {
						size = get_count_of_images_inside_single_album(list_of_albums.get(0).getAlbumId());
						index = 0;
					}
				}
			}
		}
		if (index != -1 && lv_album_list != null) {
			final int position = index;
			refreshAlbums(index);
			fillGallery(list_of_albums.get(index).getAlbumId());
			lv_album_list.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv_album_list.smoothScrollToPosition(position);
				}
			});
		}
	}

	private int get_count_of_images_inside_single_album(String album_id) {
		Cursor image_count_cursor = getApplicationContext().getContentResolver().query(Media.EXTERNAL_CONTENT_URI
				, new String[]{ "_data","_id" }, "bucket_id = ?",new String[]{ album_id }, "datetaken DESC");
				if (image_count_cursor != null) {
					return image_count_cursor.getCount();
				}
		return -1;
	}
	
	private void initImageLoader() {
		defaultOptions = new DisplayImageOptions.Builder().cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(this)
		.defaultDisplayImageOptions(defaultOptions).memoryCache(new WeakMemoryCache());

		ImageLoaderConfiguration config = builder.build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
	}

	private void refreshAlbums(final int position) {
		if (list_of_albums != null && list_of_albums.size() > 0) {
			for (int i = 0; i < list_of_albums.size(); i++) {
				((Album)list_of_albums.get(i)).setSelected(false);
			}
			((Album)list_of_albums.get(position)).setSelected(true);

			if (album_adapter != null && lv_album_list != null) {
				album_adapter.notifyDataSetChanged();
				lv_album_list.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						lv_album_list.smoothScrollToPosition(position);
					}
				});
			}
		}
	}

	private void fillGallery(String album_id) {
		Cursor images_cursor = getApplicationContext().getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
						new String[] { "_data", "_id" },"bucket_id=?",new String[] { album_id }, "datetaken DESC");

		list_of_images_paths = new ArrayList<String>();
		for (int i = 0; i < images_cursor.getCount(); i++) {
			images_cursor.moveToPosition(i);
			if (new File(images_cursor.getString(images_cursor
					.getColumnIndexOrThrow("_data"))).length() != 0) {
				list_of_images_paths.add(images_cursor.getString(images_cursor
						.getColumnIndexOrThrow("_data")));
			}
		}
		if (list_of_images_paths != null && list_of_images_paths.size() > 0) {
			gv_imgs_according_to_albums.setAdapter(new Custom_GridView_Adapter(
					MyGallery.this, list_of_images_paths));
		} else {
			gv_imgs_according_to_albums.setVisibility(View.GONE);
			tv_no_images.setVisibility(View.VISIBLE);
		}
	}
	
	private class Album {

		private String albumId;
		private String albumName;
		private String imagePath;
		private Boolean selected = false;

		public String getAlbumId() {
			return albumId;
		}

		public void setAlbumId(String albumId) {
			this.albumId = albumId;
		}

		public String getAlbumName() {
			return albumName;
		}

		public void setAlbumName(String albumName) {
			this.albumName = albumName;
		}

		public String getImagePath() {
			return imagePath;
		}

		public void setImagePath(String imagePath) {
			this.imagePath = imagePath;
		}

		public void setSelected(Boolean selected) {
			this.selected = selected;
		}

		public boolean isSelected() {
			return selected;
		}
	}
	
	private class Albums_List_Adapter extends BaseAdapter {

		ViewHolder vh;
		private Context context;
		private ArrayList<Album> list_of_albums;
		private LayoutInflater inflater;

		public Albums_List_Adapter(Context context,
				ArrayList<Album> list_of_albums) {
			super();
			
			this.context = context;
			this.list_of_albums = list_of_albums;
			
			inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
			
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list_of_albums.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list_of_albums.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		private class ViewHolder {
			ImageView iv_album_image;
			TextView tv_album_name;
			LinearLayout ll_layout;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			if (convertView == null) {
				vh = new ViewHolder();
				
				convertView = inflater.inflate(R.layout.album_listview_items,null);
				vh.tv_album_name = (TextView) convertView.findViewById(R.id.tv_album_name);
				vh.ll_layout = (LinearLayout) convertView.findViewById(R.id.ll_layout);
				
				convertView.setTag(vh);
			}
			else {
				vh = (ViewHolder) convertView.getTag();
			}
			
			if (((Album) list_of_albums.get(position)).isSelected()) {
				vh.ll_layout.setBackgroundResource(R.drawable.border);
			} else {
				vh.ll_layout.setBackgroundResource(0);
			}
		
			imageLoader.displayImage("file://"+ list_of_albums.get(position).getImagePath(),
					(ImageView) convertView.findViewById(R.id.iv_album_img),defaultOptions, new SimpleImageLoadingListener() {

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							// TODO Auto-generated method stub
							super.onLoadingComplete(imageUri, view, loadedImage);
						}
					});

			Log.i("ALBUM NAME>>>>>>>", ((Album)list_of_albums.get(position)).albumName);

			vh.tv_album_name.setText(((Album)list_of_albums.get(position)).getAlbumName());

			return convertView;

		}
	}

	private class Custom_GridView_Adapter extends BaseAdapter {

		private Context context;
		private LayoutInflater inflater;
		private ArrayList<String> list_of_images;

		public Custom_GridView_Adapter(Context context,
				ArrayList<String> list_of_images) {
			super();

			this.context = context;
			this.list_of_images = list_of_images;

			inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list_of_images.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub

			return list_of_images.get(position);

		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub

			return position;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.custom_gallery_gridview_item, null);
			}

			imageLoader.displayImage("file://" + (String) list_of_images.get(position),
					(ImageView) convertView.findViewById(R.id.iv_gallery_img),defaultOptions, new SimpleImageLoadingListener() {

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							// TODO Auto-generated method stub
							super.onLoadingComplete(imageUri, view, loadedImage);
						}
					});

			return convertView;

		}
	}
}
