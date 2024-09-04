/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.freeland.wildscanlaos.imagecache;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.freeland.wildscanlaos.BuildConfig;
import org.freeland.wildscanlaos.R;
import org.freeland.wildscanlaos.data.WildscanDataManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 */
public class ImageFetcher extends ImageResizer {
    private static final String TAG = "ImageFetcher";
    private static final int DISK_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String DISK_CACHE_DIR = "image_disk_cache";
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private DiskLruCache mDiskCache;
    private File mDiskCacheDir;
    private boolean mDiskCacheStarting = true;
    private final Object mDiskCacheLock = new Object();
    private static final int DISK_CACHE_INDEX = 0;
    
    private static WildscanDataManager sDataManager;

    /**
     * Initialize providing a target image width and height for the processing images.
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        init(context);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageFetcher(Context context, int imageSize) {
        super(context, imageSize);
        init(context);
    }

    private void init(Context context) {
        checkConnection(context);
        mDiskCacheDir = ImageCache.getDiskCacheDir(context, DISK_CACHE_DIR);
        if (sDataManager==null)
        	sDataManager = WildscanDataManager.getInstance(context);
    }

    @Override
    protected void initDiskCacheInternal() {
        super.initDiskCacheInternal();
        initHttpDiskCache();
    }

    private void initHttpDiskCache() {
        if (!mDiskCacheDir.exists()) {
            mDiskCacheDir.mkdirs();
        }
        synchronized (mDiskCacheLock) {
            if (ImageCache.getUsableSpace(mDiskCacheDir) > DISK_CACHE_SIZE) {
                try {
                    mDiskCache = DiskLruCache.open(mDiskCacheDir, 1, 1, DISK_CACHE_SIZE);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "HTTP cache initialized");
                    }
                } catch (IOException e) {
                    mDiskCache = null;
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    @Override
    protected void clearCacheInternal() {
        super.clearCacheInternal();
        synchronized (mDiskCacheLock) {
            if (mDiskCache != null && !mDiskCache.isClosed()) {
                try {
                    mDiskCache.delete();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "HTTP cache cleared");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "clearCacheInternal - " + e);
                }
                mDiskCache = null;
                mDiskCacheStarting = true;
                initHttpDiskCache();
            }
        }
    }

    @Override
    protected void flushCacheInternal() {
        super.flushCacheInternal();
        synchronized (mDiskCacheLock) {
            if (mDiskCache != null) {
                try {
                    mDiskCache.flush();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "HTTP cache flushed");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    @Override
    protected void closeCacheInternal() {
        super.closeCacheInternal();
        synchronized (mDiskCacheLock) {
            if (mDiskCache != null) {
                try {
                    if (!mDiskCache.isClosed()) {
                        mDiskCache.close();
                        mDiskCache = null;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "HTTP cache closed");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "closeCacheInternal - " + e);
                }
            }
        }
    }

    /**
    * Simple network connection check.
    *
    * @param context
    */
    private void checkConnection(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Toast.makeText(context, R.string.no_network_connection_toast, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "checkConnection - no connection found");
        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data) {
        Bitmap bitmap = null;

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + data);
        }

        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        
        if (!TextUtils.isEmpty(mLocalStorageRootUri) && data.startsWith(mLocalStorageRootUri)) {
        	try {
        		String assetPath = data.substring(mLocalStorageRootUri.length());
        		bitmap = decodeSampledBitmapFromObb(sDataManager, assetPath, mImageWidth, mImageHeight, getImageCache());
        		if (bitmap==null) {
        			AssetFileDescriptor fd = sDataManager.expansionFileGetFD(assetPath);
        			if (fd!=null)
        				bitmap = decodeSampledBitmapFromDescriptor(fd.getFileDescriptor(), mImageWidth, mImageHeight, getImageCache());
        		}
			} catch (IOException e) {
				Log.e(TAG, "processBitmap - " + e);
			}
        	if (bitmap==null) {
	        	File local = new File(data.substring("file://".length()));
	        	if (!local.exists()) {
	                if (BuildConfig.DEBUG) {
	                    Log.d(TAG, "processBitmap - persistent file not found, downloading...");
	                }
	        		local.getParentFile().mkdirs();
	        		String remote = data.replace(mLocalStorageRootUri+"/uploads", mRemoteRootUri);
	        		try {
	        			FileOutputStream localOut = new FileOutputStream(local);
						if (downloadUrlToStream(remote, localOut)) {
							fileInputStream = new FileInputStream(local);
							fileDescriptor = fileInputStream.getFD();
						}
	                } catch (IOException e) {
	                    Log.e(TAG, "processBitmap - " + e);
	                } finally {
	                    if (fileDescriptor == null && fileInputStream != null) {
	                        try {
	                            fileInputStream.close();
	                        } catch (IOException e) {}
	                    }
	                }
	        	}
	        	else {
	        		try {
	        			fileInputStream = new FileInputStream(local);
	        			fileDescriptor = fileInputStream.getFD();
	        		} catch (IOException e) {
	        			Log.e(TAG, "processBitmap - " + e);
	        		} finally {
	        			if (fileDescriptor == null && fileInputStream != null) {
	        				try {
	        					fileInputStream.close();
	        				} catch (IOException e) {}
	        			}
	        		}
	        	}
        	}
        }
        else {
	        final String key = ImageCache.hashKeyForDisk(data);
	        DiskLruCache.Snapshot snapshot;
	        synchronized (mDiskCacheLock) {
	            // Wait for disk cache to initialize
	            while (mDiskCacheStarting) {
	                try {
	                    mDiskCacheLock.wait();
	                } catch (InterruptedException e) {}
	            }
	
	            if (mDiskCache != null) {
	                try {
	                    snapshot = mDiskCache.get(key);
	                    if (snapshot == null) {
	                        if (BuildConfig.DEBUG) {
	                            Log.d(TAG, "processBitmap, not found in http cache, downloading...");
	                        }
	                        DiskLruCache.Editor editor = mDiskCache.edit(key);
	                        if (editor != null) {
	                            if (downloadUrlToStream(data,
	                                    editor.newOutputStream(DISK_CACHE_INDEX))) {
	                                editor.commit();
	                            } else {
	                                editor.abort();
	                            }
	                        }
	                        snapshot = mDiskCache.get(key);
	                    }
	                    if (snapshot != null) {
	                        fileInputStream =
	                                (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
	                        fileDescriptor = fileInputStream.getFD();
	                    }
	                } catch (IOException e) {
	                    Log.e(TAG, "processBitmap - " + e);
	                } catch (IllegalStateException e) {
	                    Log.e(TAG, "processBitmap - " + e);
	                } finally {
	                    if (fileDescriptor == null && fileInputStream != null) {
	                        try {
	                            fileInputStream.close();
	                        } catch (IOException e) {}
	                    }
	                }
	            }
            }
        }

        if (bitmap==null && fileDescriptor!=null) {
            bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth, mImageHeight, getImageCache());
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {}
        }
        return bitmap;
    }

    @Override
    protected Bitmap processBitmap(Object data) {
    	try {
    		return processBitmap(String.valueOf(data));
    	} catch (OutOfMemoryError e) {
    		return null;
    	}
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise
     */
    public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        disableConnectionReuseIfNecessary();
        /*Http*/URLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = /*(HttpURLConnection)*/ url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null && HttpURLConnection.class.isInstance(urlConnection)) {
                ((HttpURLConnection)urlConnection).disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {}
        }
        return false;
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
}
