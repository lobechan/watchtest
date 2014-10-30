package com.bluefay.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BLFile {

	public final static int BUFSIZE = 8 * 1024;

	public static boolean copy(String input, String output) {
		if (input == null || output == null) {
			return false;
		}
		if (input.startsWith("file://")) {
			input = input.substring(7);
		}
		if (output.startsWith("file://")) {
			output = output.substring(7);
		}
		return copy(new File(input), new File(output));
	}

	public static boolean copy(File input, File output) {
		if (input == null || output == null) {
			return false;
		}
		try {
			FileInputStream localFileInputStream = new FileInputStream(input);
			FileOutputStream localFileOutputStream = new FileOutputStream(
					output);
			return copy(localFileInputStream, localFileOutputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean copy(InputStream input, OutputStream output) {

		if (input == null || output == null) {
			return false;
		}
		boolean result = false;
		try {
			byte[] arrayOfByte = new byte[BUFSIZE];
			input = makeInputBuffered(input);
			output = makeOutputBuffered(output);
			int count;
			while ((count = input.read(arrayOfByte, 0, BUFSIZE)) != -1) {
				output.write(arrayOfByte, 0, count);
			}
			output.flush();
		} catch (IOException ex) {
			BLLog.e("Exception while copying: " + ex);
		} finally {
			try {
				if (output != null) {
					output.close();
					result = true;
				}
				if (input != null) {
					input.close();
					result = true;
				}
			} catch (IOException ex) {
				BLLog.e("Exception while closing the stream: " + ex);
			}
		}
		return result;
	}

	public static boolean copyWithoutOutputClosing(InputStream input,
			OutputStream output) {

		if (input == null || output == null) {
			return false;
		}
		boolean result = false;
		try {
			byte[] data = new byte[BUFSIZE];
			input = makeInputBuffered(input);
			output = makeOutputBuffered(output);
			int count;
			while ((count = input.read(data, 0, BUFSIZE)) != -1) {
				output.write(data, 0, count);
			}
			output.flush();
		} catch (IOException ex) {
			BLLog.e("Exception while copying: " + ex);
		} finally {
			try {
				if (input != null) {
					input.close();
					result = true;
				}
			} catch (IOException ex) {
				BLLog.e("Exception while closing the stream: " + ex);
			}
		}
		return result;
	}

	public static InputStream makeInputBuffered(String filePath) {
		try {
			FileInputStream in = new FileInputStream(filePath);
			return makeInputBuffered(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static OutputStream makeOutputBuffered(String filePath) {
		try {
			FileOutputStream out = new FileOutputStream(filePath);
			return makeOutputBuffered(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream makeInputBuffered(InputStream paramInputStream) {
		if (paramInputStream == null) {
			return null;
		}
		if (paramInputStream instanceof BufferedInputStream) {
			return paramInputStream;
		}
		return new BufferedInputStream(paramInputStream, BUFSIZE);

	}

	public static OutputStream makeOutputBuffered(OutputStream paramOutputStream) {
		if (paramOutputStream == null) {
			return null;
		}
		if (paramOutputStream instanceof BufferedOutputStream) {
			return paramOutputStream;
		}
		return new BufferedOutputStream(paramOutputStream, BUFSIZE);
	}

	public static FileOutputStream getEmptyFileOutputStream(File file) {
		if (file == null) {
			return null;
		}
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
		}

		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static FileOutputStream getEmptyFileOutputStream(String filePath) {
		if (filePath == null || filePath.length() == 0) {
			return null;
		}
		return getEmptyFileOutputStream(new File(filePath));
	}

	public static boolean copyStreams(InputStream is, FileOutputStream fos) {
		BufferedOutputStream os = null;
		boolean result = false;
		try {
			byte data[] = new byte[BUFSIZE];
			int count;
			os = new BufferedOutputStream(fos, BUFSIZE);
			while ((count = is.read(data, 0, BUFSIZE)) != -1) {
				os.write(data, 0, count);
			}
			os.flush();
		} catch (IOException e) {
			BLLog.e("Exception while copying: " + e);
		} finally {
			try {
				if (os != null) {
					os.close();
					result = true;
				}
			} catch (IOException e2) {
				BLLog.e("Exception while closing the stream: " + e2);
			}
		}
		return result;
	}

	public static File getUniqueDestination(String base, String extension) {
		File file = new File(base + "." + extension);

		for (int i = 2; file.exists(); i++) {
			file = new File(base + "_" + i + "." + extension);
		}
		return file;
	}

	public static File getFileByTime(String dir, String extension) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String filename = dateFormat.format(now);
		File file = new File(dir + filename + extension);
		return file;
	}

	public static byte[] readFile(String filePath) {
		try {
			FileInputStream in = new FileInputStream(filePath);
			return getData(in);
		} catch (FileNotFoundException e) {
			BLLog.e("FileNotFoundException:" + e.getMessage());
		} catch (IOException e) {
			BLLog.e("IOException:" + e.getMessage());
		}
		return null;
	}
	public static String readText(String filePath) {
		return getString(new File(filePath),"UTF-8");
		
	}
	public static boolean writeFile(String filePath, String content,
			String charset) {

		if (BLText.isEmpty(content)) {
			return false;
		}

		if (charset == null || charset.length() == 0) {
			charset = "UTF-8";
		}
		byte[] data = null;
		try {
			data = content.getBytes(charset);
			return writeFile(filePath, data);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	public static boolean writeFile(String filePath, byte[] data) {
		try {
			if (filePath.startsWith("file://")) {
				filePath = filePath.substring(7);
			}
			FileOutputStream out = new FileOutputStream(filePath);
			out.write(data);
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			BLLog.e("FileNotFoundException:" + e.getMessage());
		} catch (IOException e) {
			BLLog.e("IOException:" + e.getMessage());
		}
		return false;
	}

	public static boolean writeFile(String filePath, byte[] data,
			boolean makedir) {
		try {
			if (filePath.startsWith("file://")) {
				filePath = filePath.substring(7);
			}
			if (makedir) {
				int index = filePath.lastIndexOf(File.separator);
				if (index >= 0) {
					File destDir = new File(filePath.substring(0, index + 1));
					if (!destDir.exists() && !destDir.mkdirs()) {
						BLLog.e("Make dest dir failed:" + destDir.toString());
						return false;
					}
				}
			}
			FileOutputStream out = new FileOutputStream(filePath);
			out.write(data);
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			BLLog.e("FileNotFoundException:" + e.getMessage());
		} catch (IOException e) {
			BLLog.e("IOException:" + e.getMessage());
		}
		return false;
	}

	public static String getFileExtension(String fileName) {
		if (fileName != null) {
			int index = fileName.lastIndexOf(".");
			if (index >= 0) {
				return fileName.substring(index + 1);
			}
		}
		return "";
	}

	public static String getFileName(String filePath) {
		String pathSep = File.separator;
		if (filePath != null) {
			if (filePath.startsWith("http://")) {
				pathSep = "/";
			}
			int index = filePath.lastIndexOf(pathSep);
			if (index >= 0) {
				return filePath.substring(index + 1);
			}
		}
		return "";
	}

	public static String getFileNameNoExt(String filePath) {
		String pathSep = File.separator;
		if (filePath != null) {
			if (filePath.startsWith("http://")) {
				pathSep = "/";
			}
			int index1 = filePath.lastIndexOf(pathSep);
			if (index1 >= 0) {
				int index2 = filePath.lastIndexOf(".");
				if (index2 > 0 && index2 > index1) {
					return filePath.substring(index1 + 1, index2);
				} else {
					return filePath.substring(index1 + 1);
				}
			}
		}
		return "";
	}

	public static long getFileSize(String uri) {
		if (uri == null || uri.length() == 0) {
			return 0;
		}
		if (uri.startsWith("file://")) {
			uri = uri.substring(7);
		}
		File file = new File(uri);
		return file.length();
	}

	public static String getString(File in, String charset) {

		if (charset == null || charset.length() == 0) {
			charset = "UTF-8";
		}
		try {
			byte[] data = getData(new FileInputStream(in));
			String str = new String(data, charset);
			return str;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static byte[] getData(InputStream in) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[BUFSIZE];
		int len = 0;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		byte[] result = out.toByteArray();
		in.close();
		out.close();
		return result;
	}

	public static void delete(String filePath) {
		File file = new File(filePath);
		if (file.exists() && file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			deleteFolderAllFiles(filePath);
			File folder = new File(filePath);
			folder.delete();
		}
	}

	private static void deleteFolderAllFiles(String folder) {
		File file = new File(folder);
		if (!file.exists()) {
			return;
		}

		File[] fileList = file.listFiles();

		for (int i = 0; i < fileList.length; i++) {
			delete(fileList[i].getAbsolutePath());
		}
	}

	public static boolean exists(String path) {
		File file = new File(path);
		return file.exists();
	}

	public static boolean mkdirs(String path) {
		if (path == null || path.length() == 0) {
			return false;
		}
		return mkdirs(new File(path));
	}

	public static boolean mkdirs(File file) {
		if (file == null) {
			return false;
		}
		if (!file.exists()) {
			return file.mkdirs();
		}
		return true;
	}

	public static InputStream getInputStream(String uri) {
		if (uri.startsWith("file://")) {
			uri = uri.substring(7);
		}
		InputStream input = null;
		try {
			input = new FileInputStream(uri);
		} catch (FileNotFoundException e) {
			BLLog.e("FileNotFoundException:" + e.getMessage());
		}
		return input;
	}

	public static OutputStream getOutputStream(String uri) {
		if (uri.startsWith("file://")) {
			uri = uri.substring(7);
		}
		OutputStream output = null;
		try {
			output = new FileOutputStream(uri);
		} catch (FileNotFoundException e) {
			BLLog.e("FileNotFoundException:" + e.getMessage());
		}
		return output;
	}
}
