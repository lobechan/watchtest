package com.bluefay.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class BLLog {

	public final static int LEVEL_ALL = 0;
	public final static int LEVEL_DEBUG = 1;
	public final static int LEVEL_INFO = 2;
	public final static int LEVEL_WARNING = 3;
	public final static int LEVEL_ERROR = 4;
	public final static int LEVEL_OFF = 5;

	public final static int OUTPUT_STDOUT = 0;
	public final static int OUTPUT_LOGGER = 1;
	public final static int OUTPUT_STREAM = 2;
	/**
	 * Default display all level log
	 */
	public static int mLevel = LEVEL_ALL;
	public static int mOutput = OUTPUT_LOGGER;

	private static OutputStream mOutStream;

	private static Logger log = Logger.getLogger("BLLog");

	public static void i(String msg) {
		if (LEVEL_INFO >= mLevel) {
			display(msg);
		}
	}

	public static void w(String msg) {
		if (LEVEL_WARNING >= mLevel) {
			display(msg);
		}
	}

	public static void e(String msg) {
		if (LEVEL_ERROR >= mLevel) {
			display(msg);
		}
	}

	public static void e(String msg, Exception ex) {
		if (LEVEL_ERROR >= mLevel) {
			display(msg + ex);
		}
	}

	public static void d(String format, Object... args) {
		if (LEVEL_DEBUG >= mLevel) {
			if (args.length == 0) {
				display(format);
			} else {
				String msg = String.format(format, args);
				display(msg);
			}
		}
	}

	public static void i(String format, Object... args) {
		if (LEVEL_INFO >= mLevel) {
			if (args.length == 0) {
				display(format);
			} else {
				String msg = String.format(format, args);
				display(msg);
			}
		}
	}

	public static void w(String format, Object... args) {
		if (LEVEL_WARNING >= mLevel) {
			if (args.length == 0) {
				display(format);
			} else {
				String msg = String.format(format, args);
				display(msg);
			}
		}
	}

	private static void display(String msg) {
		StackTraceElement caller = new Throwable().fillInStackTrace()
				.getStackTrace()[2];

		String info = String.format("[%s,%d,%s] %s",
				caller.getFileName(), caller.getLineNumber(),
				caller.getMethodName(), msg);

		switch (mOutput) {
		case OUTPUT_STDOUT: {
			System.out.println(info);
			break;
		}
		case OUTPUT_LOGGER: {
			log.warning(info);
			break;
		}
		case OUTPUT_STREAM: {
			if (mOutStream != null) {
				try {
					byte[] data = info.getBytes("utf-8");
					mOutStream.write(data, 0, data.length);
				} catch (IOException e) {
					log.warning(e.getMessage());
				}
			}
			break;
		}
		}
	}

	public static void setLevel(int level) {
		mLevel = level;
	}

	public static void setTag(String tag) {
		if (tag != null && tag.length() > 0) {
			log = Logger.getLogger(tag);
		}
	}

	public static void setOutput(int output, OutputStream out) {
		mOutput = output;
		mOutStream = out;
	}
}
