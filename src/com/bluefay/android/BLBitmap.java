package com.bluefay.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.Gravity;

public class BLBitmap {

	private static final int MAX_UPLOAD_WIDTH = 640;
	private static final int MAX_UPLOAD_HEIGHT = 960;
	private static final int MAX_UPLOAD_SIZE = MAX_UPLOAD_WIDTH
			* MAX_UPLOAD_HEIGHT;

	private static final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static final Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	static {
		mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	}

	public static Bitmap roundRectBitmap(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		try {
			Bitmap bitmap = BitmapFactory
					.decodeStream(new FileInputStream(file));
			Bitmap result = roundRectBitmap(bitmap);
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			System.gc();
		}
		return null;
	}

	public static Bitmap roundRectBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int bWidth = bitmap.getWidth();
		int bHeight = bitmap.getHeight();
		int mWidth = 120;
		int mHeight = 120;
		RectF mTempSrc = new RectF();
		RectF mTempDst = new RectF();
		mTempSrc.set(0, 0, bWidth, bHeight);
		mTempDst.set(0, 0, mWidth, mHeight);
		Matrix mMatrix = new Matrix();
		mMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.FILL);
		Bitmap scaledBitmap = Bitmap.createBitmap(mWidth, mHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(scaledBitmap);
		canvas.drawBitmap(bitmap, mMatrix, null);
		Bitmap result = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		canvas = new Canvas(result);
		canvas.drawRoundRect(mTempDst, 8, 8, mFillPaint);
		canvas.drawBitmap(scaledBitmap, 0, 0, mMaskPaint);
		return result;
	}

	public static Bitmap roundBitmap(Context context, String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		try {
			Bitmap bitmap = BitmapFactory
					.decodeStream(new FileInputStream(file));
			Bitmap result = roundBitmap(context, bitmap);
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			System.gc();
		}
		return null;
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static Bitmap roundBitmap(Context context, Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int size = dip2px(context, 24);
		int bWidth = bitmap.getWidth();
		int bHeight = bitmap.getHeight();
		int mWidth = size;
		int mHeight = size;
		RectF mTempSrc = new RectF();
		RectF mTempDst = new RectF();
		mTempSrc.set(0, 0, bWidth, bHeight);
		mTempDst.set(0, 0, mWidth, mHeight);
		Matrix mMatrix = new Matrix();
		mMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.FILL);
		Bitmap scaledBitmap = Bitmap.createBitmap(mWidth, mHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(scaledBitmap);

		canvas.drawBitmap(bitmap, mMatrix, null);
		Bitmap result = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		canvas = new Canvas(result);
		int radius = size / 2;
		canvas.drawCircle(radius, radius, radius, mFillPaint);
		canvas.drawBitmap(scaledBitmap, 0, 0, mMaskPaint);
		// canvas.drawRoundRect(mTempDst, 8, 8, mFillPaint);

		return result;
	}

	public static Bitmap getBitmapThumb(String imageFile) {
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imageFile, opts);
			opts.inSampleSize = computeSampleSize(opts, 240);
			opts.inJustDecodeBounds = false;
			Bitmap bitmap = BitmapFactory.decodeFile(imageFile, opts);
			// Bitmap bitmap=ThumbnailUtils.createImageThumbnail(imageFile,
			// ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL);
			if (bitmap != null) {
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				if (bitmap.getWidth() > 240 + 50) {
					int dstHeight = 240 * height / width;
					Bitmap target = Bitmap.createScaledBitmap(bitmap, 240,
							dstHeight, true);
					bitmap.recycle();
					bitmap = null;
					return target;
				} else {
					return bitmap;
				}
			} else {
				return null;
			}
		} catch (OutOfMemoryError e) {
			System.gc();
			System.gc();
			return null;
		}
	}

	public static Bitmap getBitmapThumb(String imageFile, int maxWidth,
			int maxHeight) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imageFile, opts);
		opts.inSampleSize = computeSampleSize(opts, 240);
		opts.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(imageFile, opts);
		if (bitmap != null) {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int destWidth = maxWidth;

			if (bitmap.getWidth() > 240 + 50) {
				int dstHeight = 240 * height / width;
				Bitmap target = Bitmap.createScaledBitmap(bitmap, 240,
						dstHeight, true);
				bitmap.recycle();
				bitmap = null;
				return target;
			} else {
				return bitmap;
			}
		} else {
			return null;
		}

	}

	@SuppressWarnings("unused")
	private static void copyFile(String src, String desc) {
		int length = 1048891;
		FileChannel inC = null;
		FileChannel outC = null;
		try {

			FileInputStream in = new FileInputStream(src);
			FileOutputStream out = new FileOutputStream(desc);
			inC = in.getChannel();
			outC = out.getChannel();
			ByteBuffer b = null;
			while (inC.position() < inC.size()) {
				if ((inC.size() - inC.position()) < length) {
					length = (int) (inC.size() - inC.position());
				} else
					length = 1048891;
				b = ByteBuffer.allocateDirect(length);
				inC.read(b);
				b.flip();
				outC.write(b);
				outC.force(false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inC != null && inC.isOpen()) {
					inC.close();
				}
				if (outC != null && outC.isOpen()) {
					outC.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Bitmap decodeBitmap(String imagePath) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imagePath, opts);
			opts.inSampleSize = computeSampleSize(opts, MAX_UPLOAD_SIZE);
			opts.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(imagePath, opts);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public static Bitmap rotateBitmap(Bitmap bitmap, int rotation) {
		Matrix matrix = new Matrix();
		if (rotation != 0) {
			matrix.postRotate(rotation);
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Bitmap destBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return destBitmap;
	}

	public static boolean saveBitmapAs(Bitmap bitmap, String path) {
		FileOutputStream fos = null;
		boolean successed = false;
		try {
			File targetFile = new File(path);
			fos = new FileOutputStream(targetFile);
			successed = bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e2) {
				}
			}
		}
		return successed;
	}

	private static int computeSampleSize(BitmapFactory.Options options,
			int maxSize) {
		int w = options.outWidth;
		int h = options.outHeight;
		int scale = 1;
		while (w * h > maxSize) {
			w = w / 2;
			h = h / 2;
			scale = scale * 2;
		}
		return scale;
	}

	public static boolean saveBitmapToFile(Bitmap bitmap, String targetFile)
			throws Exception {
		FileOutputStream fos = null;
		boolean successed = false;
		try {
			fos = new FileOutputStream(targetFile);
			successed = bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e2) {
				}
			}
		}
		return successed;
	}

	public static Drawable resizeImage(Context ctx, int resId, int iconWidth,
			int iconHeight) {

		// int value;
		Drawable drawable = ctx.getResources().getDrawable(resId);
		drawable = new ScaleDrawable(drawable, Gravity.CENTER, iconWidth,
				iconHeight).getDrawable();
		drawable.setBounds(0, 0, iconWidth, iconHeight);
		return drawable;

	}
}
