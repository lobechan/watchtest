package com.fastfox.watchassistant;

public class Contant {

	public static final String UPLOAD_STEPS = "http://wx.fastfox.cn/uploadstepsres-json.jsp";
	public static final String  SYNC_OK_INTENT = 
				"com.excheer.watchassistant.SYNC_OK";
	public static final String  CALL_REMIND = 
			"com.excheer.watchassistant.CALL_REMIND";
	public static final String  RESYNC_INTENT = 
			"com.excheer.watchassistant.RESYNC_INTENT";
	public static final String  SHOW_LOADING = 
			"com.excheer.watchassistant.SHOW_LOADING";
	public static final String  TEST_SUCCEEDED = 
			"com.excheer.watchassistant.TEST_SUCCEEDED";
	public static final String  TEST_FAILED = 
			"com.excheer.watchassistant.TEST_FAILED";
	
	public static final String URL_CHECK_UPDATE = ""
			+ "http://dm.fastfox.cn/DM/apk.do?method=query&";

	public static final String PRE_LAST_NOTIFIED_UPDATE_TIME = "last_notified_update_time";
	public static final String PRE_IS_NOTIFY_UPDATE_TODAY = "is_notify_update_today";
	public static final String PRE_LAST_DOWNLOAD_ID = "last_download_id";
	public static final String PRE_IS_SEND_USER_INFO = "is_send_user_info";

	public static final int NOTIFY_ID = 7001;
	public static final int NOTIFY_UPDATE_ID = 7002;
	
	public final static byte EXCHEER_PROTOCOL_VERSION = 0x00;
    
    public static final short CMD_RES_OK = 0;
    
    public final static int EXCHEER_CmdId__VERSION = 1;
    public final static int EXCHEER_CmdId__BATTERY = 2;
    public final static int EXCHEER_CmdId__TIME =  3;
    public final static int	EXCHEER_CmdId__FIRMWARELEN = 4;
    public final static int	EXCHEER_CmdId__STEP = 5;
    public final static int	EXCHEER_CmdId__FIRMWAREREQUEST = 6;
    public final static int	EXCHEER_CmdId__FIRMWARE = 7;
    public final static int EXCHEER_CmdId__REQUESTTIME = 8;
    public final static int EXCHEER_CmdId__LOG = 9;
    public final static int EXCHEER_CmdId__SETTING = 10;
    public final static int EXCHEER_CmdId__DEBUG = 11;
    public final static int EXCHEER_CmdId__DEBUGTOSERVER = 12;
    public final static int EXCHEER_CmdId__LIGHT = 14;
    public final static int EXCHEER_CmdId__STEP2 = 13;
    public final static int EXCHEER_CmdId__READSTEP = 15;
    
}
