package com.fastfox.watchassistant;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.ElasticScrollView.view.BarChartView;
import com.ElasticScrollView.view.DrawView;
import com.ElasticScrollView.view.ElasticScrollView;
import com.ElasticScrollView.view.ElasticScrollView.OnRefreshListener;
import com.ElasticScrollView.view.MyChartView;
import com.ElasticScrollView.view.MyChartView.Mstyle;
import com.fastfox.watchtest.R;
import com.excheer.until.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;  
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;  
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;  
import android.widget.Toast;
import android.widget.ViewFlipper;
 
public class SleepActivity extends Activity implements OnGestureListener{  
	
	public static final int SLEEP_START_HOUR = 20;
	public static final int SLEEP_END_HOUR = 11;
	
	
	private ElasticScrollView elasticScrollView;
	private ViewFlipper viewFlipper;
	private GestureDetector gestureDetector = null;
	private ImageView button,sync;
	private TextView text;
	HashMap<Double, Double> map;
	HashMap<Double, Double> map2;
	
	private SleepData sleepData = new SleepData();

	private String timeSpan = "day"; 
	private long searchTime = 0;
	
	// 
	TextView mSleepPercentView;
	TextView mSleepTimeView;
	TextView mSleepAdviseView;
	
	TextView mDeepSleepView;
	TextView mLightSleepView;
	
	private LinearLayout data_linearlayout,sync_linearlayout;
	private TextView action_daydata,action_weekdata,action_mouthdata;
	private TextView mShowTypeView;
	
	private View mPopupWindowView;
	private PopupWindow popupWindow;
	BarChartView barchartView ;
	
	DrawView mSleepCircleView;
	private double mPosX,mPosY,mCurrentPosX,mCurrentPosY;
	private TextView mTimeTip;
	private boolean nodata = false;
	int pjvalue = 200;
	private long todaystart;
	DecimalFormat df = new DecimalFormat("0.0");
	
	private class TimeUnion {
		long start;
		long end;
	}
	private final BroadcastReceiver mSyncReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			refresh();
		}
		
	};
	@Override
    protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Contant.SYNC_OK_INTENT);
		registerReceiver(mSyncReceiver,filter);
        super.onResume();
	}
	 @Override
	    protected void onPause() {
	        super.onPause();
	        unregisterReceiver(mSyncReceiver);
	 }
	 
	private void previous(){
		Calendar cal = Calendar.getInstance();
		Log.d("debug4","begin searchTime "+searchTime);
		cal.setTime(new Date(searchTime));
		SimpleDateFormat bartDateFormat = new 
				SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (timeSpan.equalsIgnoreCase("day")) {
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-1, 0, 0, 0);
			searchTime = cal.getTimeInMillis();
			Log.d("debug15","searchTime:"+bartDateFormat.format(searchTime));
		} else if (timeSpan.equalsIgnoreCase("week")) {
			//tmpTime.setDate(currentTime.getDate() + 7);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-7, 0, 0, 0);
			searchTime = cal.getTimeInMillis();
			
		} else if (timeSpan.equalsIgnoreCase("month")) {
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)-1, cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			searchTime = cal.getTimeInMillis();
			
		}
		Log.d("debug4","searchTime "+searchTime);
		refresh();
	}
	private void next(){
		long now = new Date().getTime();
		long startTime = 0,endTime = 0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(searchTime));
		
		if ("day".equalsIgnoreCase(timeSpan)) {
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			startTime = cal.getTimeInMillis();
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
			endTime = cal.getTimeInMillis();
			
		}
		else if ("week".equalsIgnoreCase(timeSpan)) {
			long week = cal.get(Calendar.DAY_OF_WEEK) - 2;
			if (week < 0) week = 6; // Sunday is 6
			
			//log.info("Today's week is " + week);
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			startTime = cal.getTimeInMillis() - 24*60*60*1000*week;
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
			endTime = cal.getTimeInMillis() + 24*60*60*1000*(6-week);
			
		}
		else if ("month".equalsIgnoreCase(timeSpan)) {	
			long day = cal.get(Calendar.DAY_OF_MONTH) - 1;
			long maxDay = cal.getActualMaximum(Calendar.DATE);
			
			//log.info("Today's date is " + day + ", Max date is " + maxDay);
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			startTime = cal.getTimeInMillis() - 24*60*60*1000*day;
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
			endTime = cal.getTimeInMillis() + 24*60*60*1000*(maxDay - day - 1);
			
		}
		
		if(endTime > now){
			if (timeSpan.equalsIgnoreCase("day")) {
				Toast.makeText(SleepActivity.this, SleepActivity.this.getString(R.string.nodata_sleep_day), Toast.LENGTH_SHORT).show();
			    return;
			} else if (timeSpan.equalsIgnoreCase("week")) {
				Toast.makeText(SleepActivity.this, SleepActivity.this.getString(R.string.nodata_sleep_week), Toast.LENGTH_SHORT).show();
			    return;
			} else if (timeSpan.equalsIgnoreCase("month")) {
				Toast.makeText(SleepActivity.this, SleepActivity.this.getString(R.string.nodata_sleep_month), Toast.LENGTH_SHORT).show();
			    return;
			}
		}else{
			if (timeSpan.equalsIgnoreCase("day")) {
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)+1, 0, 0, 0);
				searchTime = cal.getTimeInMillis();
			} else if (timeSpan.equalsIgnoreCase("week")) {
				//tmpTime.setDate(currentTime.getDate() + 7);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)+7, 0, 0, 0);
				searchTime = cal.getTimeInMillis();
				
			} else if (timeSpan.equalsIgnoreCase("month")) {
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
				searchTime = cal.getTimeInMillis();
				
			}
			refresh();
		}
	}
	private boolean changeTimeSpan(String timeSpan){// day week, month
		
		if(!timeSpan.equalsIgnoreCase(this.timeSpan)){
			this.timeSpan = timeSpan;
			searchTime = todaystart;
			refresh();
			return true;
		}
		return false;
	}

	@SuppressLint("UseSparseArrays")
	private long refresh(){
//		long start1 = 0;
		Log.d("debug4","refresh sleep page");
		long start0 = parseforsleepdata();
	       //detail chart
	       if(sleepData != null){
	    	   long now1 = new Date().getTime();
	    	   long start1 = sleepData.getStartTime();
		       long end1 = sleepData.getEndTime();
					int percent = (int) ((sleepData.getSleepMinutes() - sleepData.getWalkMinutes())*100/sleepData.getTargetMinutes());
					 
					 mSleepCircleView.setPercent(
							 (int)(sleepData.getSleepMinutes() - sleepData.getWalkMinutes()), 
							 (int)sleepData.getTargetMinutes());
					 Log.d("debug3","total "+sleepData.getTargetMinutes()+" com "+(sleepData.getSleepMinutes() - sleepData.getWalkMinutes()));
					 mSleepCircleView.invalidate();
				    
				       
				       if(percent<0) percent = 0;
				       if (timeSpan.equalsIgnoreCase("day")) 
				       {
					   		if (percent == 0) {
					   			mSleepAdviseView.setText(getResources().getString(R.string.sleepadvise1));
					   		} else if (percent <= 85) {
					   			mSleepAdviseView.setText(getResources().getString(R.string.sleepadvise2));
					   		} else if (percent > 85 && percent < 115) {
					   			mSleepAdviseView.setText(getResources().getString(R.string.sleepadvise3));
					   		} else {
					   			mSleepAdviseView.setText(getResources().getString(R.string.sleepadvise4));
					   		}
					   	} else {
					   		
					   		mSleepAdviseView.setText(getResources().getString(R.string.sleeptime));
					   	}
				       if(percent>100) percent = 100;
				       mSleepPercentView.setText(percent+"%");
				       
				       
				       
				       
				       double time /*= (double)(sleepData.getSleepMinutes() - sleepData.getWalkMinutes())/60.0f*/;
				       double lighttime = sleepData.getSleepMinutes() - sleepData.getWalkMinutes()-sleepData.getDeepMinutes();
				       if(lighttime == 0){
				    	   mLightSleepView.setText(0+getResources().getString(R.string.hour));
				       }else{
				    	   mLightSleepView.setText(df.format(lighttime/60)+getResources().getString(R.string.hour));
				       }
				       if(sleepData.getDeepMinutes() == 0){
				    	   mDeepSleepView.setText(0+getResources().getString(R.string.hour));
				       }else{
				    	   mDeepSleepView.setText(df.format(sleepData.getDeepMinutes()/60)+getResources().getString(R.string.hour));
				       }
				       time = lighttime/60 + sleepData.getDeepMinutes()/60;
				       if(time == 0){
				    	   mSleepTimeView.setText("0" +getResources().getString(R.string.hour));
				       }else {
				    	   mSleepTimeView.setText(df.format(time) +getResources().getString(R.string.hour));
				       }
	    	   
				    map = new HashMap<Double,Double>();
	    	   		map2 = new HashMap<Double,Double>();
		       
		      
			       if (timeSpan.equalsIgnoreCase("day")) {
			    	   
			    	   SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				       List<SleepDetailData> detailData = new ArrayList<SleepDetailData>();
				       long mark = (sleepData.getEndTime()-sleepData.getStartTime())/4;
				       int xmark = (int)(sleepData.getEndTime()-sleepData.getStartTime())/(60*60*1000);
				       int markSpan = 5*60*1000;
				       int marks = (int)((sleepData.getEndTime() - sleepData.getStartTime() + markSpan -1)/markSpan);
				       
				       Log.d("debug5"," marks "+marks);
				       detailData = sleepData.getDetailData();
				       int i = 5;
				       Log.d("tt","detailDatasize:"+detailData.size());
				       if( detailData.size() != 0){
				    	   //ArrayList<>
				    	   //TimeUnion
				    	   ArrayList<TimeUnion>  walks = new  ArrayList<TimeUnion> ();
				    	   ArrayList<TimeUnion>  deep = new  ArrayList<TimeUnion> ();
				    	   
				    	   for (SleepDetailData item:detailData){
				    		   if(item.getSleepType().equals("walk")){
				    			   TimeUnion uu = new TimeUnion();
				    			   uu.start = item.getStartTime();
				    			   uu.end = item.getEndTime();
				    			   walks.add(uu);
				    		   } else if(item.getSleepType().equals("deep")){
				    			   TimeUnion uu = new TimeUnion();
				    			   uu.start = item.getStartTime();
				    			   uu.end = item.getEndTime();
				    			   deep.add(uu);
				    		   }
		    			   }
				    	   for (int j=0;j<marks;j++){
			    			   map.put((double)j, (double)1);
			    			   long start = sleepData.getStartTime() + j*markSpan;
			    			   long end = start + markSpan;
			    			   boolean processed = false;
			    			   for (int m = 0; m < walks.size(); ++m) {
			    				   TimeUnion tt = walks.get(m);
			    			  		if ((tt.start>=start && tt.start <=end) || 
			    			  		(tt.end>=start && tt.end<=end) ||
			    			  		(tt.start<=start && tt.end>=end))
			    			  		{
			    			  			map.put((double)j, (double)2);
			    			  			processed = true;
			    			  			break;
			    			  		}
			    			  	}
			    			  	
			    			  	if (!processed) {
			    				  	for (int n = 0; n < deep.size(); ++n) {
			    				  		TimeUnion tt = deep.get(n);
			    				  		if((tt.start>=start && tt.start <=end) || 
			    		    			  		(tt.end>=start && tt.end<=end) ||
			    		    			  		(tt.start<=start && tt.end>=end)) 
			    				  		{
			    				  			map.put((double)j, (double)3);
			    				  			processed = true;
			    				  			break;
			    				  		}
			    				  	}
			    			  	}
			    			  
				    	   }
				    	   
				    	  
				       }else {
				    	   Log.d("tt5","marks:"+marks);
				    	   for (int j=0;j<marks;j++){
				    		   map.put(/*markSpan**/(double)j, (double)1);
				    	   }
				       }
			
				       double lightsleep = lighttime/60;
				       double deepsleep = sleepData.getDeepMinutes()/60;

				       
					       
				       Calendar cal = Calendar.getInstance();
			    	   cal.setTime(new Date(now1));
			   		   long nowstart = 0;
//			   		Calendar cal = Calendar.getInstance();
					cal.setTime(new Date(searchTime));
					cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-1, 0, 0, 0);
					long mTime = cal.getTimeInMillis();
					SimpleDateFormat bartDateFormat = new 
							SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Log.d("debug16","searchTime:"+bartDateFormat.format(mTime));
		   	   		if (/*Utils.isSameDay(now1, mTime)*/false) {
		   	   			mTimeTip.setText(R.string.today);
		   	   			
		   	   		} else {
		   	   			mTimeTip.setText((Utils.getMonth(mTime) + 1) + 
		   	   					"." +
		   	   					Utils.getDay(mTime) /*+ SportsActivity.this.getString(R.string.day)*/);
		   	   		}
		   	   	   String []xstr = new String[5];
			       for(int x=0;x<5;x++){
			    	   xstr[x] = sdf.format(new Date(sleepData.getStartTime()+mark*x));
			       }
			       
			       barchartView.SetTuView(map,map2,lightsleep,deepsleep,"day",xstr);
			       
			       barchartView.invalidate();
			   	}  else {
//			   		long now = new Date().getTime();
//			        long start = sleepData.getStartTime();
//			        long end = sleepData.getEndTime();
			        List<SleepTimeData>  sleepTimeData = 
			        		sleepData.getTimeData();
			        
			        pjvalue = 3*60;
			        int remax = 15*60;
			        for(SleepTimeData item:sleepTimeData){
			        	Log.d("debug6","item sleep mitutes "+item.getSleepMinutes());
			        	map.put(item.getTimeIndex(), item.getSleepMinutes());
			        	Log.d("debug17","sleep_map:"+map);
			        	map2.put(item.getTimeIndex(), item.getSleepMinutes()-item.getDeepSleepMinutes());
			        }
			        
			   	 if (timeSpan.equalsIgnoreCase("week")) {
				   		
				   		if (Utils.isSameWeek(now1, start1)) {
				   			mTimeTip.setText(R.string.thisweek);
				   		} else {
				   			mTimeTip.setText((Utils.getMonth(start1) + 1) + "." + Utils.getDay(start1) + " - " +
			  						(Utils.getMonth(end1) + 1) + "." + Utils.getDay(end1));
				   			
				   		}
				   		String week[]= new String[] {
				        		SleepActivity.this.getString(R.string.monday),SleepActivity.this.getString(R.string.tuesday),SleepActivity.this.getString(R.string.wednesday),
				        		SleepActivity.this.getString(R.string.thursday),SleepActivity.this.getString(R.string.friday),SleepActivity.this.getString(R.string.saturday),
				        		SleepActivity.this.getString(R.string.sunday)
				         };
				        //map.put(1, 20);
				   		barchartView.SetTuView(map,map2,remax,pjvalue,"week",week);
					    barchartView.invalidate();
			
				   	}
				   	else if (timeSpan.equalsIgnoreCase("month")) {
				   		
				   		if (Utils.isSameMonth(now1, start1)) {
				   			mTimeTip.setText(R.string.thismonth);
				   		} else {
				   			mTimeTip.setText((Utils.getMonth(start1) + 1) + "." + Utils.getDay(start1) + " - " +
			  						(Utils.getMonth(end1) + 1) + "." + Utils.getDay(end1));
				   			
				   		}
				   		String month[] = new String[] {
					     		   "1"+SleepActivity.this.getString(R.string.day),"6"+SleepActivity.this.getString(R.string.day),"11"+SleepActivity.this.getString(R.string.day),
					     		   "16"+SleepActivity.this.getString(R.string.day),"21"+SleepActivity.this.getString(R.string.day),"26"+SleepActivity.this.getString(R.string.day)
					     		   ,"31"+SleepActivity.this.getString(R.string.day)
					        };
				   		//map.put(1, 20);
				   		barchartView.SetTuView(map,map2,remax,pjvalue,"month",month);
					    barchartView.invalidate();
				   	}
			   		
			   	}
			   	
	     }
	  return start0;
	}
	public int dip2px(Context context, float dipValue){  
        final float scale = context.getResources().getDisplayMetrics().density;   
        return (int)(dipValue * scale + 0.5f);  
     } 
    @SuppressLint({ "ResourceAsColor", "UseSparseArrays" })
	public void onCreate(Bundle savedInstanceState) {  
       super.onCreate(savedInstanceState);  
//       requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		  //定义DisplayMetrics 对象  
	      DisplayMetrics  dm = new DisplayMetrics();  
	      //取得窗口属性  
	      getWindowManager().getDefaultDisplay().getMetrics(dm);  
	      searchTime= new Date().getTime();
	      
//	      //窗口的宽度  
//	      int screenWidth = dm.widthPixels;  
//	       
//	      //窗口高度  
//	      int screenHeight = dm.heightPixels;  
	      int width = dm.widthPixels;     // 屏幕宽度（像素）
	        int height = dm.heightPixels;   // 屏幕高度（像素）
	        float density = dm.density;      // 屏幕密度（0.75 / 1.0 / 1.5）
	        int densityDpi = dm.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
//	        Toast.makeText(SleepActivity.this, "屏幕宽度: " + width + "\n屏幕高度： " + height+"\n屏幕密度："+density+"\n屏幕密度DPI:"+densityDpi, Toast.LENGTH_LONG).show();
//	      textView.setText("屏幕宽度: " + screenWidth + "\n屏幕高度： " + screenHeight); 
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_sleep);
	   Log.d("tt","SecondActivity onCreate");
	   LayoutInflater flater = LayoutInflater.from(this);
	   elasticScrollView = (ElasticScrollView)findViewById(R.id.scrollview1);
//       viewFlipper = (ViewFlipper)findViewById(R.id.viewflipper);
       gestureDetector = new GestureDetector(this); 
       
       FrameLayout layout = new FrameLayout(this);
       layout.setBackgroundColor(Color.WHITE);
       ViewGroup.LayoutParams paramlayout = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
       layout.setLayoutParams(paramlayout);
       
       // left right
       final TextView previousView = new TextView(this);
//       previousView.setVisibility(View.GONE);
       FrameLayout.LayoutParams previousparam = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
    		   LayoutParams.WRAP_CONTENT);
       previousparam.topMargin = dip2px(this,70);
       previousparam.gravity = Gravity.LEFT;
       previousView.setText("");
       previousView.setBackgroundResource(R.drawable.left_arrow);
       previousView.setTextSize(30);
       previousView.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View arg0) {
//			Toast.makeText(SleepActivity.this, SleepActivity.this.getResources().getString(R.string.scandata), Toast.LENGTH_SHORT).show();
			previous();
		}
    	   
       });
       layout.addView(previousView,previousparam);
       
       final TextView rightView = new TextView(this);
//       rightView.setVisibility(View.GONE);
       FrameLayout.LayoutParams rightparam = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
    		   LayoutParams.WRAP_CONTENT);
       rightparam.topMargin = dip2px(this,70);
       rightparam.gravity = Gravity.RIGHT;
       rightView.setTextSize(30);
       rightView.setText("");
       rightView.setBackgroundResource(R.drawable.right_arrow);
       layout.addView(rightView,rightparam);
       rightView.setOnClickListener(new OnClickListener(){

	   		@Override
	   		public void onClick(View arg0) {
//	   			Toast.makeText(SleepActivity.this, SleepActivity.this.getResources().getString(R.string.scandata), Toast.LENGTH_SHORT).show();
	   			next();
	   		}
       	   
       });
       /*elasticScrollView.setGestureDetector(gestureDetector);
       elasticScrollView.setOnTouchListener(new OnTouchListener() {
		
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			return gestureDetector.onTouchEvent(arg1);
		}
	});*/
       /*elasticScrollView.setOnTouchListener(new OnTouchListener() {
      		
   		@Override
      		public boolean onTouch(View arg0, MotionEvent event) {
      			
      			switch (event.getAction()) {
      			case MotionEvent.ACTION_UP:
//      				rightView.setVisibility(View.GONE);
//      	            previousView.setVisibility(View.GONE);
      				Log.d("debug11","ACTION_UP:"+mPosX);
      	            mCurrentPosX = event.getX()-mPosX;     
      	            mCurrentPosY = event.getY()-mPosY;        
      	          	mPosX = (int)event.getX();    
      	            mPosY = (int)event.getY();
      	          if(mCurrentPosX != 0){
    	            	Log.e("debug11", " mCurrentPosX:"+mCurrentPosX+"  mCurrentPosY:"+mCurrentPosY+" tan(a):"+Math.abs(mCurrentPosY )/Math.abs(mCurrentPosX ));
    	     	       if (mCurrentPosX > 0 && (Math.abs(mCurrentPosY )/Math.abs(mCurrentPosX )) < 0.27){
    	     	    	     Log.e("debug11", "right:"+mPosX);
    	     	    	      
    	     	    	     previous();
    	     	    	     return true;
    	     	       }  
    	     	       else if (mCurrentPosX < 0 && (Math.abs(mCurrentPosY )/Math.abs(mCurrentPosX )) < 0.27 ){
    	     	               Log.e("debug11", "left:"+mPosX);
//    	     	               rightView.setVisibility(View.VISIBLE);
//    	     	               // 加载动画  
//    	       	    	   Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
//    	     		                SportsActivity.this, R.anim.out);  
//    	       	    	   // 使用ImageView显示动画  
//    	       	    	   rightView.startAnimation(hyperspaceJumpAnimation); 
////    	     	               previousView.setVisibility(View.GONE);
    	     	               next();
    	     	               
    	     	               return true;
    	     	       }   
    	     	      else if (mCurrentPosY > 0 && Math.abs(mCurrentPosX ) < 4){
    		               Log.e("debug11", "down:"+mPosX+" mCurrentPosX:"+mCurrentPosX); 
    	     	      } 
    	     	      else if (mCurrentPosY  < 0 && Math.abs(mCurrentPosX ) < 4){
    		                Log.e("debug11", "up:"+mPosX+" mCurrentPosX:"+mCurrentPosX); 
    	     	      }
    	            }
      	            break;
      			case MotionEvent.ACTION_DOWN:
      				Log.d("debug11","ACTION_DOWN:"+mPosX);
//      				// 加载动画  
//     	    	    Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
//   		                SportsActivity.this, R.anim.out);  
//     	    	    // 使用ImageView显示动画  
//     	    	    previousView.startAnimation(hyperspaceJumpAnimation);
//     	    	    rightView.startAnimation(hyperspaceJumpAnimation);
      			  
      			  rightView.setVisibility(View.VISIBLE);
      			  previousView.setVisibility(View.VISIBLE);
     	    	  Animation animation = AnimationUtils.loadAnimation(SleepActivity.this, R.anim.out); 
     	    	  rightView.startAnimation(animation);
     	    	  previousView.startAnimation(animation);
     	    	    
      				mPosX = (int)event.getX();    
      	            mPosY = (int)event.getY();
      	            Log.d("debug11","mPosX:"+mPosX);
      	            
      	            
      	            break;
      			case MotionEvent.ACTION_MOVE:
      				Log.d("debug11","ACTION_MOVE:"+mPosX);
//      	            mCurrentPosX = (int)event.getX()-mPosX;     
//      	            mCurrentPosY = (int)event.getY()-mPosY;        
//      	          	mPosX = (int)event.getX();    
//      	            mPosY = (int)event.getY();
      	            break;
      			}
//      			Log.e("debug11", " mCurrentPosX:"+mCurrentPosX+"  mCurrentPosY:"+mCurrentPosY);
//      	       if (mCurrentPosX > 0 && Math.abs(mCurrentPosY ) < 50){
//      	    	     Log.e("debug11", "right:"+mPosX+" mCurrentPosX:"+mCurrentPosX);
//      	    	      
//      	    	     next();
//      	    	     return true;
//      	       }  
//      	       else if (mCurrentPosX < 0 && Math.abs(mCurrentPosY) < 50 ){
//      	               Log.e("debug11", "left:"+mPosX+" mCurrentPosX:"+mCurrentPosX);
////      	               rightView.setVisibility(View.VISIBLE);
////      	               // 加载动画  
////        	    	   Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
////      		                SportsActivity.this, R.anim.out);  
////        	    	   // 使用ImageView显示动画  
////        	    	   rightView.startAnimation(hyperspaceJumpAnimation); 
//////      	               previousView.setVisibility(View.GONE);
//      	               previous();
//      	               return true;
//      	       }   
//      	       else if (mCurrentPosY > 5 && Math.abs(mCurrentPosX ) < 4){
//      	               Log.e("debug11", "down:"+mPosX+" mCurrentPosX:"+mCurrentPosX); 
//      	       } 
//      	       else if (mCurrentPosY  < 5 && Math.abs(mCurrentPosX ) < 4){
//      	                Log.e("debug11", "up:"+mPosX+" mCurrentPosX:"+mCurrentPosX); 
//      	       }
      	       
              
      	       return false;
      		}
      		
      	});*/
       Paint p = new Paint();  
       p.setARGB(255, 122, 80, 140);
       mSleepCircleView = new DrawView(this,p);//(this,p,r);
       mSleepCircleView.setBackgroundResource(R.drawable.cycle_bg);
    //   View cycleimage = flater.inflate(R.layout.cycle_image, null);
       View cycletext = flater.inflate(R.layout.cycle_sleeptext, null);
       View todaydata = flater.inflate(R.layout.today_sleepdata,null);
      
       FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
       param.gravity = Gravity.CENTER_HORIZONTAL;
       layout.addView(mSleepCircleView,param);//,param);
       
       
       FrameLayout.LayoutParams paramdraw = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
       paramdraw.gravity = Gravity.CENTER_HORIZONTAL;
      // layout.addView(drawview,paramdraw);
       
       FrameLayout.LayoutParams paramtext = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
       paramtext.gravity = Gravity.CENTER;
       layout.addView(cycletext,paramtext);
       
       FrameLayout layout2 = new FrameLayout(this);
       ViewGroup.LayoutParams paramdlinear = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
       layout2.setLayoutParams(paramdlinear);
       layout2.setBackgroundColor(Color.WHITE);
       
       FrameLayout.LayoutParams paramdata = new FrameLayout.LayoutParams(
    		   LayoutParams.WRAP_CONTENT,
    		   LayoutParams.WRAP_CONTENT);
       paramdata.gravity = Gravity.CENTER_HORIZONTAL;
       
       layout2.addView(todaydata,paramdata);
       
       FrameLayout layout3 = new FrameLayout(this);
       FrameLayout.LayoutParams param3 = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
       layout3.setLayoutParams(param3);
       layout3.setBackgroundColor(Color.WHITE);
//       layout3.setBackgroundResource(R.drawable.sleep_chart_bg);
//       layout3.addView(datatable);
       /*final MyChartView drawchartview = new MyChartView(this);
       drawchartview.setMinimumHeight(600);
       drawchartview.setMinimumWidth(250);
       drawchartview.SetTuView(map,50,10,"x","h",false);
       map=new HashMap<Double, Double>();
       map.put(1.0, 0.0);
       map.put(3.0, 25.0);
       map.put(4.0, 32.0);
       map.put(5.0, 41.0);
       map.put(6.0, 16.0);
       map.put(7.0, 36.0);
       map.put(8.0, 26.0);
       drawchartview.setTotalvalue(50);
       drawchartview.setPjvalue(10);
       drawchartview.setMap(map);
//     tu.setXstr("");
//     tu.setYstr("");
       drawchartview.setMargint(20);
       drawchartview.setMarginb(50);
       drawchartview.setMstyle(Mstyle.Line);
       drawchartview.invalidate();
       layout3.addView(drawchartview);*/
      
       
       
       
//       int []data_total = new int[] {1,1,1,1,1,1,2,2,3,3,3,3,3,3,3,3,3,3,1,1,1,1,1,1};
       
       barchartView = new BarChartView(this) ;
//       barchartView.setMinimumHeight(height/2);
//       barchartView.setMinimumWidth(width);
       barchartView.setMinimumHeight(height/2);
       barchartView.setMinimumWidth(width/2);
       barchartView.setMargint(40);
       barchartView.setMarginb(150);
	   layout3.addView(barchartView) ;
       elasticScrollView.addChild(layout3,1);
       elasticScrollView.addChild(layout2,1);
       elasticScrollView.addChild(layout,1);
       elasticScrollView.smoothScrollTo(0, 0);
//       for(int i=1;i<=50;i++){
//			TextView tempTextView1 = new TextView(this);
//			tempTextView1.setText("Text:" + i);
//			elasticScrollView.addChild(tempTextView1,1);
//		}
       
       final Handler handler = new Handler() {
       	public void handleMessage(Message message) {
       		String str = (String)message.obj;
       		OnReceiveData(str);
       	}
       };
       elasticScrollView.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Message message = handler.obtainMessage(0, "new Text");
						handler.sendMessage(message);
					}
				});
				thread.start();
			}
		});
       
       mSleepPercentView = (TextView)findViewById(R.id.sleep_percent);
       mSleepTimeView = (TextView)findViewById(R.id.sleep_time);
       mSleepAdviseView = (TextView)findViewById(R.id.sleep_advise);
       
       mTimeTip = (TextView)findViewById(R.id.sleeptime_tip);
       //else {
	   		//SleepAdvise.innerHTML = "睡眠时间";
	   	//}
       
       mDeepSleepView = (TextView)findViewById(R.id.deep_sleep);
       mLightSleepView = (TextView)findViewById(R.id.light_sleep);
       
       data_linearlayout = (LinearLayout)findViewById(R.id.data_linearlayout);
	   sync_linearlayout = (LinearLayout)findViewById(R.id.sync_linearlayout);
	   mShowTypeView = (TextView)findViewById(R.id.showtype);
	   mPopupWindowView = flater.inflate(R.layout.mypopup, null);
		action_daydata = (TextView)mPopupWindowView.findViewById(R.id.action_daydata);
		action_weekdata = (TextView)mPopupWindowView.findViewById(R.id.action_weekdata);
		action_mouthdata = (TextView)mPopupWindowView.findViewById(R.id.action_mouthdata);
		popupWindow = new PopupWindow(mPopupWindowView,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.black));

		action_daydata.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				showPopupWindow(arg0);
				
				if(changeTimeSpan("day")){
					mShowTypeView.setText(R.string.action_daydata);
				}
				Log.d("tt","---action_daydata---");
			}
			
		});
		action_weekdata.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				showPopupWindow(arg0);
				if(changeTimeSpan("week")){
					mShowTypeView.setText(R.string.action_weekdata);
				}
				
				Log.d("tt","---action_weekdata---");
			}
			
		});
		action_mouthdata.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				showPopupWindow(arg0);
				if(changeTimeSpan("month")){
					mShowTypeView.setText(R.string.action_mouthdata);
				}
			}
			
		});
		data_linearlayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("PopupMenu","---onClick---");
				showPopupWindow(arg0);
			}
			
		});
		sync_linearlayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent intent =new Intent(Contant.RESYNC_INTENT);
				SleepActivity.this.sendBroadcast(intent);
			}
			
		});
      TextView titleView = (TextView)findViewById(R.id.title);
      titleView.setText(R.string.sleepdata);
      todaystart = refresh();
    }  
    private void showPopupWindow(View v){
		Log.d("PopupMenu","popupWindow:"+popupWindow.isShowing());
		if(!popupWindow.isShowing()){
//			popupWindow.showAsDropDown(data_linearlayout,35,0);
			popupWindow.showAsDropDown(data_linearlayout,Gravity.CENTER_HORIZONTAL,0);
//			popupWindow.showAtLocation(button, Gravity.LEFT , 10, 10);
		}else {
			popupWindow.dismiss();
		}
	}
	protected void OnReceiveData(String str) {
//		TextView textView =  new TextView(this);
//		textView.setText(str);
//		elasticScrollView.addChild(textView, 1);
		elasticScrollView.onRefreshComplete();
	}
	
	private long parseforsleepdata(){
		sleepData = new SleepData();
		//long searchTime= new Date().getTime();
		int totalDays = 1;
		int maxTimeIndex = 0;
		
		Date now = new Date();
		//log.info("now: " + now.getMonth() + " - " + now.getDate() + " - " + now.getDay() + "  " + now.getHours() + ":" + now.getMinutes());
		long startTime = 0, endTime = 0;
		SimpleDateFormat bartDateFormat = new 
				SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(searchTime));
		if ("day".equalsIgnoreCase(timeSpan)) {
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-1, SLEEP_START_HOUR, 0, 0);
			startTime = cal.getTimeInMillis();
			long unixTimeGMT = cal.getTimeInMillis() - TimeZone.getDefault().getRawOffset();//获取标准格林尼治时间下日期时间对应的时间戳
			Log.d("debug14","startTime:"+bartDateFormat.format(cal.getTime())+"  unixTimeGMT:"+unixTimeGMT);
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, SLEEP_END_HOUR, 0, 0);
			endTime = cal.getTimeInMillis();
			Log.d("debug14","endTime:"+bartDateFormat.format(cal.getTime()));
			
			if (endTime > now.getTime()) {
				maxTimeIndex = now.getHours();
			} else {
				maxTimeIndex = 23;
			}
			
			totalDays = 1;
		}
		else if ("week".equalsIgnoreCase(timeSpan)) {
			long week = cal.get(Calendar.DAY_OF_WEEK) - 2;
			if (week < 0) week = 6; // Sunday is 6
			
			//log.info("Today's week is " + week);
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1, SLEEP_START_HOUR, 0, 0);
			startTime = cal.getTimeInMillis() - 24*60*60*1000*week;
			Log.d("debug17","startTime:"+bartDateFormat.format(startTime));
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, SLEEP_END_HOUR, 0, 0);
			endTime = cal.getTimeInMillis() + 24*60*60*1000*(6-week);
			Log.d("debug17","endTime:"+bartDateFormat.format(endTime));
			if (endTime > now.getTime()) {
				maxTimeIndex = now.getDay() - 1;
				if (maxTimeIndex < 0) maxTimeIndex = 6;
			} else {
				maxTimeIndex = 6;
			}
			totalDays = maxTimeIndex + 1;
			Log.d("debug17","totalDays:"+totalDays);
		}
		else if ("month".equalsIgnoreCase(timeSpan)) {	
			long day = cal.get(Calendar.DAY_OF_MONTH) - 1;
			long maxDay = cal.getActualMaximum(Calendar.DATE);
			
			//log.info("Today's date is " + day + ", Max date is " + maxDay);
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1, SLEEP_START_HOUR, 0, 0);
			startTime = cal.getTimeInMillis() - 24*60*60*1000*day;
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, SLEEP_END_HOUR, 0, 0);
			endTime = cal.getTimeInMillis() + 24*60*60*1000*(maxDay - day - 1);
			
			if (endTime > now.getTime()) {
				maxTimeIndex = now.getDate() - 1;
			} else {
				maxTimeIndex = (int)maxDay - 1;
			}
			totalDays = maxTimeIndex + 1;
		}
		
		//log.info("Time span is " + timeSpan + ", startTime is " + startTime + ", endTime is " + endTime + ", searchTime is " + searchTime);
		
		List stepList = StepFacade.getStepsList(SleepActivity.this, startTime - 1000, endTime + 1000);
		if (stepList != null) {
			//log.info("Total " + stepList.size() + " step data.");
		}
		
		int totalTarget = totalDays * User.getSleepTarget(SleepActivity.this);
		
		boolean useHour = "day".equalsIgnoreCase(timeSpan);
		int currentSectionIndex = 0;

		sleepData.setDataType(SleepData.DATA_SLEEP);
		sleepData.setStartTime(startTime);
		sleepData.setEndTime(endTime);
		sleepData.setTimeSpan(timeSpan);
		sleepData.setTargetMinutes(totalTarget);
		if (now.getTime() >= startTime && now.getTime() <= endTime) {
			sleepData.setNow(true);
		} else {
			sleepData.setNow(false);
		}
		
		boolean sleepStarted = false;
		long sleepStartTime = 0;
		long sleepEndTime = 0;
		int deepSleepMinutes = 0;
		int walkMinutes = 0;
		boolean todayDone = false;
		int wakeupDataCnt = 0;
		List deepSleepArray = new ArrayList();
		for(Steps s:(List<Steps>)stepList) {	
			int idx = 0;
			cal.setTime(new Date(s.getEndTime()));
			
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			
			if (hour >= SLEEP_END_HOUR && hour < SLEEP_START_HOUR) {
				continue;
			}

			if ("week".equalsIgnoreCase(timeSpan)) {
				idx = cal.get(Calendar.DAY_OF_WEEK) - 2;
				if (idx < 0)
					idx = 6;
				if (hour >= SLEEP_START_HOUR)
					++idx;
				if (idx > totalDays)
					idx = totalDays;
			} else {
				idx = cal.get(Calendar.DAY_OF_MONTH) - 1;
				if (hour >= SLEEP_START_HOUR)
					++idx;
				if (idx > totalDays)
					idx = totalDays;
			}
			
			//log.info("Package: " + s.getId());
			
			// No sleep package yet
			if (!sleepStarted) {
				if (todayDone && idx == currentSectionIndex) continue;
				if (s.getStepType() != Steps.STEP_TYPE_SLEEPING && s.getStepType() != Steps.STEP_NEW_TYPE_SLEEP) continue;
				if (s.getEndTime() - s.getStartTime() < 20*60*1000) continue;
				if (s.getSteps() == 0) continue;
				if ((s.getSteps()*13*60*1000)/(s.getEndTime() - s.getStartTime()) >= 2) {
					//log.info("Wakeup times in ten minutes is more than 2, not sleeping");
					continue;
				}
				
				if ((s.getEndTime() - s.getStartTime())/(60*60*1000) >= 2) {
					//log.info("Wakeup times in ten minutes is more than 10, not sleeping");
					continue;
				}
				
				//log.info("Sleep start!" + s.getStartTime());
				sleepStarted = true;
				sleepStartTime = s.getStartTime();
				sleepEndTime = s.getEndTime();
				deepSleepMinutes = 0;
				walkMinutes = 0;
				currentSectionIndex = idx;
				todayDone = false;
				wakeupDataCnt = 0;
			} else {
				// this is next day
				if (idx > currentSectionIndex) {
					//log.info("Next day");
					if (useHour) {
						sleepData.setStartTime(sleepStartTime);
						sleepData.setEndTime(sleepEndTime);
						sleepData.setDeepMinutes(deepSleepMinutes);
						sleepData.setWalkMinutes(walkMinutes);
						sleepData.setSleepMinutes((int)(sleepEndTime - sleepStartTime + 60*1000 - 1)/(60*1000));
						//log.info("Day: sleep " + sleepData.getSleepMinutes() + ", deep " + sleepData.getSleepMinutes() + ", walk " + sleepData.getWalkMinutes());
					} else {
						SleepTimeData std = new SleepTimeData();
						std.setTimeIndex(currentSectionIndex);
						std.setSleepMinutes((int)(sleepEndTime - sleepStartTime + 60*1000 - 1)/(60*1000) - walkMinutes);
						std.setDeepSleepMinutes(deepSleepMinutes);
						sleepData.getTimeData().add(std);
						sleepData.setSleepMinutes(sleepData.getSleepMinutes() + std.getSleepMinutes());
						sleepData.setDeepMinutes(sleepData.getDeepMinutes() + std.getDeepSleepMinutes());
						sleepData.setWalkMinutes(sleepData.getWalkMinutes() + walkMinutes);
						
						//log.info("Month: sleep " + sleepData.getSleepMinutes() + ", deep " + sleepData.getSleepMinutes() + ", walk " + sleepData.getWalkMinutes());
						//log.info("Single: " + std.getTimeIndex() + " - sleep " + std.getSleepMinutes() + " deep " + std.getDeepSleepMinutes());
					}
					
					sleepStarted = false;
					sleepStartTime = 0;
					sleepEndTime = 0;
					deepSleepMinutes = 0;
					walkMinutes = 0;
					todayDone = false;
					wakeupDataCnt = 0;
				}
				// the same day
				else {
					// Sleep end check
					if ( 
							// Walk 5 minutes
							((s.getStepType() != Steps.STEP_TYPE_SLEEPING && s.getStepType() != Steps.STEP_NEW_TYPE_SLEEP) &&
							(s.getEndTime() - s.getStartTime() > 5*60*1000)) ||
							// Wakeup times over 5/m
							((s.getStepType() == Steps.STEP_TYPE_SLEEPING || s.getStepType() == Steps.STEP_NEW_TYPE_SLEEP) &&
							(s.getEndTime() - s.getStartTime() > 10*60*1000) &&
							((s.getSteps()*60*1000)/(s.getEndTime() - s.getStartTime()) > 2)) ||
							wakeupDataCnt >= 6
							) 
					{
						//log.info("Sleep End");
						// sleep end, but the night is still very long...
						if ((hour <= 23 && hour >= SLEEP_START_HOUR) || (hour < 2) ) {
							//log.info("Got sleep end signal. but time is too early!");
							sleepStarted = false;
							sleepStartTime = 0;
							sleepEndTime = 0;
							deepSleepMinutes = 0;
							walkMinutes = 0;
							todayDone = false;
							wakeupDataCnt = 0;
						} 
						// sleep really end
						else {
							sleepEndTime = s.getStartTime();
							
							if (useHour) {
								sleepData.setStartTime(sleepStartTime);
								sleepData.setEndTime(sleepEndTime);
								sleepData.setDeepMinutes(deepSleepMinutes);
								sleepData.setWalkMinutes(walkMinutes);
								sleepData.setSleepMinutes((int)(sleepEndTime - sleepStartTime + 60*1000 - 1)/(60*1000));
								
								//log.info("Day: sleep " + sleepData.getSleepMinutes() + ", deep " + sleepData.getSleepMinutes() + ", walk " + sleepData.getWalkMinutes());
							} else {
								SleepTimeData std = new SleepTimeData();
								std.setTimeIndex(currentSectionIndex);
								std.setSleepMinutes((int)(sleepEndTime - sleepStartTime + 60*1000 - 1)/(60*1000) - walkMinutes);
								std.setDeepSleepMinutes(deepSleepMinutes);
								sleepData.getTimeData().add(std);
								sleepData.setSleepMinutes(sleepData.getSleepMinutes() + std.getSleepMinutes());
								sleepData.setDeepMinutes(sleepData.getDeepMinutes() + std.getDeepSleepMinutes());
								sleepData.setWalkMinutes(sleepData.getWalkMinutes() + walkMinutes);
								
								//log.info("Month: sleep " + sleepData.getSleepMinutes() + ", deep " + sleepData.getSleepMinutes() + ", walk " + sleepData.getWalkMinutes());
								//log.info("Single: " + std.getTimeIndex() + " - sleep " + std.getSleepMinutes() + " deep " + std.getDeepSleepMinutes());
							}
							sleepStarted = false;
							sleepStartTime = 0;
							sleepEndTime = 0;
							deepSleepMinutes = 0;
							walkMinutes = 0;
							todayDone = true;
							wakeupDataCnt = 0;
						}
					}
					// not end
					else {
						if (todayDone && currentSectionIndex == idx) {
							//log.info("Not end but today's data is done, ignore this package.");
							continue;
						}
						// Walk mode, goto WC?
						if ((s.getStepType() != Steps.STEP_TYPE_SLEEPING && s.getStepType() != Steps.STEP_NEW_TYPE_SLEEP)) {
							++wakeupDataCnt;
							SleepDetailData sdd = new SleepDetailData();
							sdd.setStartTime(s.getStartTime());
							sdd.setEndTime(s.getEndTime());
							sdd.setSleepType(SleepDetailData.SLEEP_WALK);
							sleepData.getDetailData().add(sdd);
							walkMinutes += (s.getEndTime() - s.getStartTime() + 60*1000 - 1)/(60*1000);
							//log.info("Walk data during sleep, minutes " + walkMinutes);
						}
						else if (s.getEndTime() - s.getStartTime() > 5*1000 &&
								(s.getSteps()*60*1000)/(s.getEndTime() - s.getStartTime()) > 1) {
							++wakeupDataCnt;
						}
						// Deep sleep mode
						else if (s.getEndTime() - s.getStartTime() > 20*60*1000 &&
								(s.getSteps()*25*60*1000)/(s.getEndTime() - s.getStartTime()) <= 2) 
						{
							wakeupDataCnt = 0;
							SleepDetailData sdd = new SleepDetailData();
							sdd.setStartTime(s.getStartTime());
							sdd.setEndTime(s.getEndTime());
							sdd.setSleepType(SleepDetailData.SLEEP_DEEP);
							
							sleepData.getDetailData().add(sdd);
							deepSleepMinutes += getDeepSleepMinutes(deepSleepArray, sdd);
							deepSleepArray.add(sdd);
							//log.info("Deep sleep data during sleep, minutes " + deepSleepMinutes);
						} 
						else {
							wakeupDataCnt = 0;
							//log.info("Normal sleep package: minutes: " + (s.getEndTime() - s.getStartTime() + 60*1000 - 1)/(60*1000));
						}
						sleepEndTime = s.getEndTime();
					}
				}
			}
		}
		
		if (sleepStarted) {
			//log.info("Sleep started, but no end signal! Handle the last package.");
			if (useHour) {
				sleepData.setStartTime(sleepStartTime);
				sleepData.setEndTime(sleepEndTime);
				sleepData.setDeepMinutes(deepSleepMinutes);
				sleepData.setWalkMinutes(walkMinutes);
				sleepData.setSleepMinutes((int)(sleepEndTime - sleepStartTime + 60*1000 - 1)/(60*1000));
				
				//log.info("Day: sleep " + sleepData.getSleepMinutes() + ", deep " + sleepData.getSleepMinutes() + ", walk " + sleepData.getWalkMinutes());
			} else {
				SleepTimeData std = new SleepTimeData();
				std.setTimeIndex(currentSectionIndex);
				std.setSleepMinutes((int)(sleepEndTime - sleepStartTime + 60*1000 - 1)/(60*1000) - walkMinutes);
				std.setDeepSleepMinutes(deepSleepMinutes);
				sleepData.getTimeData().add(std);
				sleepData.setSleepMinutes(sleepData.getSleepMinutes() + std.getSleepMinutes());
				sleepData.setDeepMinutes(sleepData.getDeepMinutes() + std.getDeepSleepMinutes());
				sleepData.setWalkMinutes(sleepData.getWalkMinutes() + walkMinutes);
				
				//log.info("Month: sleep " + sleepData.getSleepMinutes() + ", deep " + sleepData.getSleepMinutes() + ", walk " + sleepData.getWalkMinutes());
				//log.info("Single: " + std.getTimeIndex() + " - sleep " + std.getSleepMinutes() + " deep " + std.getDeepSleepMinutes());
			}
		}
		
		if (!useHour) {
			sleepData.fixTimeData(maxTimeIndex);
		}
		return searchTime;
	}
	
	private int getDeepSleepMinutes(List dsa, SleepDetailData sdd) {
		long ts = 0;
		if (dsa == null || dsa.size() == 0) 
			ts = sdd.getEndTime() - sdd.getStartTime();
		else {
			boolean found =false;
			for (SleepDetailData d : (List<SleepDetailData>)dsa) {
				if (d.getStartTime() <= sdd.getStartTime() && 
						d.getEndTime() >= sdd.getEndTime()) {
					ts = 0;
					found = true;
					break;
				} else if (d.getStartTime() >= sdd.getStartTime() && 
						d.getEndTime()<=sdd.getEndTime()) {
					ts = d.getStartTime() - sdd.getStartTime() + sdd.getEndTime() - d.getEndTime();
					found = true;
					break;
				} else if (d.getStartTime() <= sdd.getStartTime() && 
						d.getEndTime() >= sdd.getStartTime() &&
						d.getEndTime() <=sdd.getEndTime()) {
					ts = sdd.getEndTime() - d.getEndTime();
					found = true;
					break;
				} else if (d.getStartTime() >= sdd.getStartTime() &&
						d.getStartTime() <= sdd.getEndTime() &&
						d.getEndTime() >= sdd.getEndTime()) {
					ts = d.getStartTime() - sdd.getStartTime();
					found = true;
					break;
				} else {
					found = false;
				}
			}
			
			if (!found) ts = sdd.getEndTime() - sdd.getStartTime();
		}
		if (ts < 0) return 0;
		return (int)(ts/(60*1000));
	}
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		//对手指滑动的距离进行了计算，如果滑动距离大于120像素，就做切换动作，否则不做任何切换动作。  
        // 从左向右滑动  
        if (arg0.getX() - arg1.getX() > 120)  
        {  
        	Log.d("debug16","---left to right---");
            // 添加动画  
            this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,  
                    R.anim.push_left_in));  
            this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,  
                    R.anim.push_left_out));  
            this.viewFlipper.showNext();  
            next();
            return true;  
        }// 从右向左滑动  
        else if (arg0.getX() - arg1.getX() < -120)  
        {  
        	Log.d("debug16","---right to left---");
            this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,  
                    R.anim.push_right_in));  
            this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,  
                    R.anim.push_right_out));  
            this.viewFlipper.showPrevious();  
            previous();
            return true;  
        }  
        return true;
	}
	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}  
