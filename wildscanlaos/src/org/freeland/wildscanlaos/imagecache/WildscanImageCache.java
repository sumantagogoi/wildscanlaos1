package org.freeland.wildscanlaos.imagecache;

import org.freeland.wildscanlaos.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.widget.ImageView;

public class WildscanImageCache {

	public static final String DISK_CACHE_DIR = "thumbs";

	private ImageFetcher mEventPhotoImageFetcher;
	private ImageFetcher mEventThumbnailImageFetcher;
	private ImageFetcher mContactAvatarImageFetcher;
	private ImageFetcher mSpeciesThumbnailImageFetcher;
	private ImageFetcher mSpeciesMainPhotoImageFetcher;

	private ImageCache mImageCache;

	private Context mContext;

	private static WildscanImageCache sImageCache;

	public void loadEventPhoto(Object data, ImageView imageView) {
		// event photos are downloaded from the net, cache them on disk
		mImageCache.setEnableDiskCache(true);
		mEventPhotoImageFetcher.loadImage(data, imageView, true);
	}
	public void pauseEventPhoto() {
		mEventPhotoImageFetcher.setPauseWork(false);
		mEventPhotoImageFetcher.setExitTasksEarly(true);
		mEventPhotoImageFetcher.flushCache();			
	}
	public void resumeEventPhoto() {
		mEventPhotoImageFetcher.setExitTasksEarly(false);
	}

	public void loadEventThumbnail(Object data, ImageView imageView) {
		// event photos are downloaded from the net, cache them on disk
		mImageCache.setEnableDiskCache(true);
		mEventPhotoImageFetcher.loadImage(data, imageView, true);
	}
	public void pauseEventThumbnail() {
		mEventPhotoImageFetcher.setPauseWork(false);
		mEventPhotoImageFetcher.setExitTasksEarly(true);
		mEventPhotoImageFetcher.flushCache();			
	}
	public void resumeEventThumbnail() {
		mEventPhotoImageFetcher.setExitTasksEarly(false);
	}

	public void loadContactAvatar(Object data, ImageView imageView) {
		// contact avatars are on local disk, don't re-cache them
		mImageCache.setEnableDiskCache(false);
		mContactAvatarImageFetcher.loadImage(data, imageView, false);
	}
	public void pauseContactAvatar() {
		mContactAvatarImageFetcher.setPauseWork(false);
		mContactAvatarImageFetcher.setExitTasksEarly(true);
		mContactAvatarImageFetcher.flushCache();			
	}
	public void resumeContactAvatar() {
		mContactAvatarImageFetcher.setExitTasksEarly(false);
	}

	public void loadSpeciesThumbnail(Object data, ImageView imageView) {
		// species images are on local disk, don't re-cache them
		mImageCache.setEnableDiskCache(false);
		mSpeciesThumbnailImageFetcher.loadImage(data, imageView, false);
	}
	public void pauseSpeciesThumbnail() {
		mSpeciesThumbnailImageFetcher.setPauseWork(false);
		mSpeciesThumbnailImageFetcher.setExitTasksEarly(true);
		mSpeciesThumbnailImageFetcher.flushCache();			
	}
	public void resumeSpeciesThumbnail() {
		mSpeciesThumbnailImageFetcher.setExitTasksEarly(false);
	}

	public void loadSpeciesMainPhoto(Object data, ImageView imageView) {
		// species images are on local disk, don't re-cache them
		mImageCache.setEnableDiskCache(false);
		mSpeciesMainPhotoImageFetcher.loadImage(data, imageView, false);
	}
	public void pauseSpeciesMainPhoto() {
		mSpeciesMainPhotoImageFetcher.setPauseWork(false);
		mSpeciesMainPhotoImageFetcher.setExitTasksEarly(true);
		mSpeciesMainPhotoImageFetcher.flushCache();			
	}
	public void resumeSpeciesMainPhoto() {
		mSpeciesMainPhotoImageFetcher.setExitTasksEarly(false);
	}

	public static WildscanImageCache getInstance(Activity a) {

		if (sImageCache==null || !sImageCache.mContext.equals(a.getApplicationContext())) {
			sImageCache = new WildscanImageCache(a);
		}

		return sImageCache;
	}

	private WildscanImageCache(Activity a) {
		mContext = a.getApplicationContext();

		FragmentManager fm = a.getFragmentManager();

		// user avatar images - enable disk cache
		ImageCache.ImageCacheParams icp = new ImageCache.ImageCacheParams(mContext, DISK_CACHE_DIR);
		icp.setMemCacheSizePercent(0.25f); // 1/4 of app mem
		icp.compressQuality = 100; //max quality

		mImageCache = ImageCache.getInstance(fm, icp);

		int eventPhotoSize = mContext.getResources().getDimensionPixelSize(R.dimen.event_details_image_height);
		mEventPhotoImageFetcher = new ImageFetcher(mContext, eventPhotoSize);
		mEventPhotoImageFetcher.setLoadingImage(R.drawable.empty_photo);
		icp.diskCacheEnabled = true;
		mEventPhotoImageFetcher.addImageCache(fm, icp);

		int eventThumbnailSize = mContext.getResources().getDimensionPixelSize(R.dimen.event_list_thumbnail_width);
		mEventThumbnailImageFetcher = new ImageFetcher(mContext, eventThumbnailSize*2);
		mEventThumbnailImageFetcher.setLoadingImage(R.drawable.empty_photo);
		icp.diskCacheEnabled = true;
		mEventThumbnailImageFetcher.addImageCache(fm, icp);

		int contactAvatarSize = mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_avatar_width);
		mContactAvatarImageFetcher = new ImageFetcher(mContext, contactAvatarSize*2);
		mContactAvatarImageFetcher.setLoadingImage(R.drawable.empty_avatar);
		icp.diskCacheEnabled = false;
		mContactAvatarImageFetcher.addImageCache(fm, icp);

		int speciesThumbnailSize = mContext.getResources().getDimensionPixelSize(R.dimen.species_gallery_thumbnail_width);
		mSpeciesThumbnailImageFetcher = new ImageFetcher(mContext, speciesThumbnailSize*2);
		mSpeciesThumbnailImageFetcher.setLoadingImage(R.drawable.empty_species);
		icp.diskCacheEnabled = false;
		mSpeciesThumbnailImageFetcher.addImageCache(fm, icp);			

		int speciesMainPhotoSize = mContext.getResources().getDisplayMetrics().widthPixels;
		mSpeciesMainPhotoImageFetcher = new ImageFetcher(mContext, speciesMainPhotoSize);
		mSpeciesMainPhotoImageFetcher.setLoadingImage(R.drawable.empty_species);
		icp.diskCacheEnabled = false;
		mSpeciesMainPhotoImageFetcher.addImageCache(fm, icp);			
	}
}
