package com.bluefay.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.bluefay.core.BLText;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.telephony.SmsManager;
import android.util.Log;

public class BLTelephony {

	private BLTelephony() {
	}

	public static boolean isPhoneNumValid(String phoneNum) {
		return BLText.isNotEmpty(phoneNum);
	}

	public static void call(Context ctx, String dialNum, boolean checkPhoneNum) {
		if (!checkPhoneNum || isPhoneNumValid(dialNum)) {
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ dialNum));
			ctx.startActivity(intent);
		}
	}

	public static void sendTextSms(String phoneNum, String content,
			PendingIntent pi) {
		// Log.d(TAG, "send to: " + phoneNum + " telling: " + content);
		SmsManager.getDefault().sendTextMessage(phoneNum, null, content, pi,
				null);
	}

	public static void saveSentTextSms(Context ctx, String phoneNum,
			String content) {
		// some constants from android.provider.Telephony
		final Uri URI_TELEPHONY_SMS_SENT = Uri.parse("content://sms/sent");
		final String TEXT_SMS_ADDRESS = "address";
		final String TEXT_SMS_BODY = "body";
		ContentValues values = new ContentValues();
		values.put(TEXT_SMS_ADDRESS, phoneNum);
		values.put(TEXT_SMS_BODY, content);
		ctx.getContentResolver().insert(URI_TELEPHONY_SMS_SENT, values);
	}

	public static String getDialNum(Context ctx, String dialNum,
			String contactName) {
		String finalDialNum = dialNum;

		if (BLText.isEmpty(dialNum)) {
			List<ContactRecord> list = getContactsByName(ctx, contactName);
			if (null != list && list.size() > 0) {
				ContactRecord record = list.get(0);
				if (record.phoneNums().size() > 0) {
					finalDialNum = record.phoneNums().get(0).phone();
				}
			}
		}

		return finalDialNum;
	}

	private static final String SEL_NAME_FROM_NUM = String.format(
			"%s='%s' AND %s=?", Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE,
			Phone.NUMBER);

	public static List<String> getContactNameFromPhoneNumber(Context ctx,
			String number) {
		ArrayList<String> contactName = new ArrayList<String>();

		if (BLText.isNotEmpty(number)) {
			Cursor cursor = ctx.getContentResolver().query(Data.CONTENT_URI,
					new String[] { Contacts.DISPLAY_NAME }, SEL_NAME_FROM_NUM,
					new String[] { number }, null);
			// Log.d(TAG, "found " + cursor.getCount() + " match of number: " +
			// number);
			while (cursor.moveToNext()) {
				contactName.add(cursor.getString(0));
			}
			cursor.close();
		}

		return contactName;
	}

	public static List<ContactRecord> getContactsByName(Context ctx,
			String userName) {
		List<ContactRecord> list = new ArrayList<ContactRecord>();

		userName = ((null == userName) ? null : userName.trim());
		String selContactName = null;
		if (BLText.isNotEmpty(userName)) {
			selContactName = String.format("%s='%s' AND %s LIKE '%%%s%%'",
					Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE,
					StructuredName.DISPLAY_NAME, userName);
		} else {
			selContactName = String.format("%s='%s'", Data.MIMETYPE,
					StructuredName.CONTENT_ITEM_TYPE);
		}
		// Log.v(TAG, "selContactName is: " + selContactName);

		Cursor cursor = ctx.getContentResolver().query(
				Data.CONTENT_URI,
				new String[] { Data.RAW_CONTACT_ID,
						StructuredName.DISPLAY_NAME, Data.CONTACT_ID },
				selContactName, null, StructuredName.DISPLAY_NAME + " ASC");
		// Log.v(TAG, "how many matched names found: " + cursor.getCount());
		String strContactCache = "";
		while (cursor.moveToNext()) {
			// Log.v(TAG, "file path: " + ctx.getFilesDir().getAbsolutePath());
			// Log.v(TAG, "valid contact: " + cursor.getString(1));
			String name = cursor.getString(1);
			strContactCache = strContactCache + name + "\r\n";
			ContactRecord record = getContactRecord(ctx, cursor.getInt(0),
					name, cursor.getInt(2));
			if (record.phoneNums().size() > 0) {
				list.add(record);
			} else {
				// Log.v(TAG, "invalid contact: " + record.name());
			}
		}

		writeDataToFile(new File(ctx.getFilesDir().getAbsolutePath(),
				"ContactCache.txt"), strContactCache);

		cursor.close();

		return list;
	}

	private static void writeDataToFile(File file, String data) {
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		FileOutputStream outStream;
		try {
			outStream = new FileOutputStream(file);
			outStream.write(data.getBytes());
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final String SEL_PHONE_NUM = String.format(
			"%s='%s' AND %s=?", Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE,
			Data.RAW_CONTACT_ID);

	private static ContactRecord getContactRecord(Context ctx, int rawId,
			String name, int id) {
		ContactRecord record = new ContactRecord(id, name);

		Cursor cursor = ctx.getContentResolver().query(
				Data.CONTENT_URI,
				new String[] { Phone.NUMBER, Phone.TYPE, Contacts.PHOTO_ID,
						Data.CONTACT_ID }, SEL_PHONE_NUM,
				new String[] { String.valueOf(rawId) }, null);

		while (cursor.moveToNext()) {
			record.addPhoneNum(cursor.getString(0), cursor.getInt(1));
			long contactId = cursor.getLong(3);
			Uri uri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, contactId);
			InputStream input = ContactsContract.Contacts
					.openContactPhotoInputStream(ctx.getContentResolver(), uri);
			if (input != null) {
				Bitmap photo = BitmapFactory.decodeStream(input);
				if (photo != null) {
					Bitmap roundImage = BLBitmap.roundRectBitmap(photo);
					if (roundImage != null) {
						record.setPhoto(roundImage);
					} else {
						record.setPhoto(photo);
					}
				}
			}

			// Log.v(TAG, name + " : " + cursor.getString(0));
		}

		cursor.close();
		return record;
	}

	public static class ContactRecord implements Serializable {
		private static final long serialVersionUID = -8260901841404467973L;

		private final int _id;
		private final String _name;
		private final List<ContactPhone> _phoneNums = new ArrayList<ContactPhone>();
		private Bitmap photo;

		public ContactRecord(int id, String name) {
			_id = id;
			_name = name;
		}

		public ContactRecord() {
			_id = 0;
			_name = "";
		}

		public int id() {
			return _id;
		}

		public String name() {
			return _name;
		}

		public List<ContactPhone> phoneNums() {
			return _phoneNums;
		}

		public Bitmap getPhoto() {
			return photo;
		}

		public void setPhoto(Bitmap photo) {
			this.photo = photo;
		}

		public void addPhoneNum(String phoneNum, int type) {
			_phoneNums.add(new ContactPhone(phoneNum, type));
		}

		public static class ContactPhone implements Serializable {
			private static final long serialVersionUID = -5355097248288732969L;

			public final String phone;
			public final int type;

			public ContactPhone(String aPhone, int aType) {
				phone = aPhone;
				type = aType;
			}

			public String phone() {
				return phone;
			}

			public String type(Context ctx) {
				return ctx.getString(typeResId());
			}

			private int typeResId() {
				return Phone.getTypeLabelResource(type);
			}
		}
	}

}
