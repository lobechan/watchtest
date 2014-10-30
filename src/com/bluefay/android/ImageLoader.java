package com.bluefay.android;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bluefay.core.BLLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class ImageLoader {
	private ExecutorService mExecutorService; // 固定五个线程来
	private ImageMemoryCache mMemoryCache;// 内存缓存
	private ImageFileCache mFileCache;// 文件缓存
	private Map<String, ImageView> mTaskMap;// 存放任务
	private Context mContext;
	private int mScaleBitmapWidth;
	private int mScaleBitmapHeight;

	public ImageLoader(Context context) {
		mExecutorService = Executors.newFixedThreadPool(5);
		mMemoryCache = new ImageMemoryCache();
		mFileCache = new ImageFileCache();
		mTaskMap = new HashMap<String, ImageView>();
		mContext = context;
		mScaleBitmapWidth = 120;
		mScaleBitmapHeight = 214;
	}

	public ImageLoader(Context context, int width, int height) {
		mExecutorService = Executors.newFixedThreadPool(5);
		mMemoryCache = new ImageMemoryCache();
		mFileCache = new ImageFileCache();
		mTaskMap = new HashMap<String, ImageView>();
		mContext = context;
		mScaleBitmapWidth = width;
		mScaleBitmapHeight = height;
	}

	public Bitmap getMemoryCacheBitmap(String url) {
		return mMemoryCache.getBitmapFromCache(url);
	}

	public void addTask(String url, ImageView img) {
		Bitmap bitmap = mMemoryCache.getBitmapFromCache(url);
		if (bitmap != null) {
			img.setImageBitmap(bitmap);
		} else {
			BLLog.d("addTask url:" + url);
			synchronized (mTaskMap) {
				mTaskMap.put(Integer.toString(img.hashCode()), img);
			}
		}
	}

	public void doTask() {
		// BLLog.d("doTask start");
		synchronized (mTaskMap) {
			Collection<ImageView> con = mTaskMap.values();
			for (ImageView i : con) {
				if (i != null) {
					if (i.getTag() != null) {
						loadImage((String) i.getTag(), i);
					}
				}
			}
			mTaskMap.clear();
		}
	}

	private void loadImage(String url, ImageView img) {
		/*** 加入新的任务 ***/
		mExecutorService.submit(new TaskWithResult(new TaskHandler(url, img),
				url));
	}

	/*** 获得一个图片,从三个地方获取,首先是内存缓存,然后是文件缓存,最后从网络获取 ***/
	private Bitmap getBitmap(String url) {
		// 从内存缓存中获取图片
		Bitmap result;
		result = mMemoryCache.getBitmapFromCache(url);
		if (result == null) {
			// 文件缓存中获取
			// result = mFileCache.getImage(url);
			if (result == null) {
				// 从网络获取
				if (url.startsWith(ImageResource.SCHEME_FILE)) {
					result = BLUtils.scaleBitmap(mContext, url,
							mScaleBitmapWidth, mScaleBitmapHeight);
				} else if (url.startsWith(ImageResource.SCHEME_HTTP)) {
					result = ImageHttp.downloadBitmap(url);
				} else if (url
						.startsWith(ImageResource.SCHEME_ANDROID_RESOURCE)) {
					result = ImageResource.getImageStandard(mContext, url,
							mScaleBitmapWidth, mScaleBitmapHeight);
				} else if (url
						.startsWith(ImageResource.SCHEME_CONTENT)) {
					result = ImageResource.getImageStandard(mContext, url,
							mScaleBitmapWidth, mScaleBitmapHeight);
				}
				if (result != null) {
					mMemoryCache.addBitmapToCache(url, result);
					// mFileCache.saveBmpToSd(result, url);
				}
			} else {
				// 添加到内存缓存
				mMemoryCache.addBitmapToCache(url, result);
			}
		}
		return result;
	}

	/*** 完成消息 ***/
	private class TaskHandler extends Handler {
		private String url;
		private ImageView img;

		public TaskHandler(String url, ImageView img) {
			this.url = url;
			this.img = img;
		}

		@Override
		public void handleMessage(Message msg) {
			/*** 查看imageview需要显示的图片是否被改变 ***/
			if (img.getTag().equals(url)) {
				if (msg.obj != null) {
					Bitmap bitmap = (Bitmap) msg.obj;
					img.setImageBitmap(bitmap);
				}
			}
		}
	}

	/*** 子线程任务 ***/
	private class TaskWithResult implements Callable<String> {
		private String url;
		private Handler handler;

		public TaskWithResult(Handler handler, String url) {
			this.url = url;
			this.handler = handler;
		}

		@Override
		public String call() throws Exception {
			Message msg = new Message();
			msg.obj = getBitmap(url);
			if (msg.obj != null) {
				handler.sendMessage(msg);
			}
			return url;
		}

	}
}
