package com.fastfox.watchassistant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

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
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;  
import android.widget.Toast;
import android.widget.ViewFlipper;
 
public class SportsActivity extends Activity implements OnGestureListener{
	private ImageView button,sync;
	private TextView text;
	private View mPopupWindowView;
	private PopupWindow popupWindow;
	private LinearLayout data_linearlayout,sync_linearlayout;
	private TextView action_daydata,action_weekdata,action_mouthdata;
	private TextView mShowTypeView;
	
	private ElasticScrollView elasticScrollView;
	private ViewFlipper viewFlipper;
    private GestureDetector gestureDetector = null;
    private static final int FLING_MIN_DISTANCE = 100;
    private static final int FLING_MIN_VELOCITY = 200;
	private HashMap<Double, Double> map;
	private HashMap<Double, Double> map1;
	private HashMap<Double, Double> map2;
	private HashMap<Double, Double> map3;
	/////
	private String timeSpan = "day"; 
	private LiveData mLiveData = new LiveData();
	
	private TextView mRunning;
	private TextView mWalking;
	private TextView mDistance;
	private TextView mKiloCalorie;
	
	private TextView mWholeSteps;
	private TextView mStepPercent;
	
	private DrawView mStepCircleView;
	
	private MyChartView mDetailView;
	private double mPosX,mPosY,mCurrentPosX,mCurrentPosY;
	private TextView mTimeTip;
	private boolean nodata = false;
	private long todaystart;
	int maxstep = 0;
	int pjvalue = 0;
	long searchTime;
	public int dip2px(Context context, float dipValue){  
        final float scale = context.getResources().getDisplayMetrics().density;   
        return (int)(dipValue * scale + 0.5f);  
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
		cal.setTime(new Date(searchTime));
		
		if (timeSpan.equalsIgnoreCase("day")) {
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-1, 0, 0, 0);
			searchTime = cal.getTimeInMillis();
		} else if (timeSpan.equalsIgnoreCase("week")) {
			//tmpTime.setDate(currentTime.getDate() + 7);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-7, 0, 0, 0);
			searchTime = cal.getTimeInMillis();
			
		} else if (timeSpan.equalsIgnoreCase("month")) {
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)-1, cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			searchTime = cal.getTimeInMillis();
			
		}
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
				Toast.makeText(SportsActivity.this, SportsActivity.this.getString(R.string.nodata_day), Toast.LENGTH_SHORT).show();
			    return;
			} else if (timeSpan.equalsIgnoreCase("week")) {
				Toast.makeText(SportsActivity.this, SportsActivity.this.getString(R.string.nodata_week), Toast.LENGTH_SHORT).show();
			    return;
			} else if (timeSpan.equalsIgnoreCase("month")) {
				Toast.makeText(SportsActivity.this, SportsActivity.this.getString(R.string.nodata_month), Toast.LENGTH_SHORT).show();
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
		Log.d("debug1"," old timespan "+this.timeSpan+" new "+timeSpan);
		if(!timeSpan.equalsIgnoreCase(this.timeSpan)){
			this.timeSpan = timeSpan;
			searchTime = todaystart;
			refresh();
			return true;
		}
		return false;
	}
	private long refresh(){
		long start = 0;
		mLiveData =  parseSteps();
	    Log.d("debug1","refresh livedata "+mLiveData);
//	    mStepCircleView.setPercent(
//	    		   mLiveData.getWalkSteps()+mLiveData.getRunSteps(), 
//	    		   mLiveData.getTotalTarget());
//	    mStepCircleView.invalidate();
//	    int percentres = (mLiveData.getWalkSteps()+mLiveData.getRunSteps())*100
//				/mLiveData.getTotalTarget();
//	    if(percentres>100) percentres = 100;
//	    mStepPercent.setText((percentres)+"%");
//	   
//	   // Toast.makeText(SportsActivity.this, "total" +mLiveData.getTotalTarget() , Toast.LENGTH_LONG).show();
//	    
//	    mWholeSteps.setText(mLiveData.getWalkSteps()+mLiveData.getRunSteps()+"");
//     
//       
//       //
//       mRunning.setText(mLiveData.getRunSteps()+"");
//       mWalking.setText(mLiveData.getWalkSteps()+"");
//       
//       String gonglishu = ((float)mLiveData.getWalkDistance()+(float)mLiveData.getRunDistance())/1000.0 + "";
//       mDistance.setText(gonglishu);
//       mKiloCalorie.setText(mLiveData.getRunKiloCalorie()+mLiveData.getWalkKiloCalorie()+"");
       
       
       // detail chart
       
       
       

       
       if(mLiveData != null){
    	       //mTimeTip
    	       long now = new Date().getTime();
    	       start = mLiveData.getStartTime();
    	       long end = mLiveData.getEndTime();
    	       Log.d("debug10","now:"+now+"  start:"+start);
    	       Log.d("debug10","nodata:"+nodata);
    	       {
	   				
	   				mStepCircleView.setPercent(
	     	    		   mLiveData.getWalkSteps()+mLiveData.getRunSteps(), 
	     	    		   mLiveData.getTotalTarget());
	 	    	    mStepCircleView.invalidate();
	 	    	    int percentres = (mLiveData.getWalkSteps()+mLiveData.getRunSteps())*100
	 	    				/mLiveData.getTotalTarget();
	 	    	    if(percentres>100) percentres = 100;
	 	    	    mStepPercent.setText((percentres)+"%");
	 	    	   
	 	    	   // Toast.makeText(SportsActivity.this, "total" +mLiveData.getTotalTarget() , Toast.LENGTH_LONG).show();
	 	    	    
	 	    	    mWholeSteps.setText(mLiveData.getWalkSteps()+mLiveData.getRunSteps()+"");
	 	         
	 	           
	 	           //
	 	           mRunning.setText(mLiveData.getRunSteps()+"");
	 	           mWalking.setText(mLiveData.getWalkSteps()+"");
	 	           
	 	           String gonglishu = ((float)mLiveData.getWalkDistance()+(float)mLiveData.getRunDistance())/1000.0 + "";
	 	           mDistance.setText(gonglishu);
	 	           mKiloCalorie.setText(mLiveData.getRunKiloCalorie()+mLiveData.getWalkKiloCalorie()+"");
	 	           
	   			}
    	       
    	       
    	       map=new HashMap<Double, Double>();
    	       
   	   		List<LiveTimeData> sportsdatadetail = mLiveData.getTimeData();
   	    	   int i=0;
   	    	   maxstep = 0;
   	    	   for(LiveTimeData item:sportsdatadetail){
   	    		   map.put((double)item.getTimeIndex(), (double)item.getSteps());
   	    		   Log.d("debug1","timeindex "+ item.getTimeIndex()+" steps:"+item.getSteps());
   	    		   i++;
   	    		   if(item.getSteps() > maxstep){
   	    			   maxstep = item.getSteps() ;
   	    			   Log.d("tt","maxstep:"+maxstep);
   	    		   }
   	           }
   	    	   Log.d("debug"," max step "+maxstep);
   	    	    if(maxstep < 800){
   	    			   maxstep = 800;
   	    			   pjvalue = 200;
   	    		   }else {
   	    			   int sectionSteps = 0;
   	    		   sectionSteps = ((((maxstep + 199)/200)+3)/4);
   	    			   
   	    		   if (sectionSteps == 0) sectionSteps = 1;
   	    		 //  var yticks = [[0,'0K'],[sectionSteps*200,sectionSteps*200],[sectionSteps*400,sectionSteps*400],
   	    			///   				[sectionSteps*600,sectionSteps*600], [sectionSteps*800,sectionSteps*800]];
   	    		   pjvalue = sectionSteps*200;
   	    		   }
   	    	    
    	       if (timeSpan.equalsIgnoreCase("day")) {
	    	   		if (Utils.isSameDay(now, start)) {
	    	   			mTimeTip.setText(R.string.today);
	    	   			
	    	   		} else {
	    	   				mTimeTip.setText((Utils.getMonth(start) + 1) + 
		    	   					"." +
		    	   					Utils.getDay(start) /*+ SportsActivity.this.getString(R.string.day)*/);
	    	   		}

     	       mDetailView.SetTuView(map,pjvalue*4,pjvalue,"day","",true);
     	    
//     	       mDetailView.setMap(map);
     	       mDetailView.invalidate();
    	   	}
    	   	else if (timeSpan.equalsIgnoreCase("week")) {
    	   		
    	   		if (Utils.isSameWeek(now, start)) {
    	   			mTimeTip.setText(R.string.thisweek);
    	   		} else {
    	   			mTimeTip.setText((Utils.getMonth(start) + 1) + "." + Utils.getDay(start) + " - " +
	   						(Utils.getMonth(end) + 1) + "." + Utils.getDay(end));
    	   			
    	   		}

     	       mDetailView.SetTuView(map,pjvalue*4,pjvalue,"week","",true);
     	    
//     	       mDetailView.setMap(map);
     	       mDetailView.invalidate();
    	   	}
    	   	else if (timeSpan.equalsIgnoreCase("month")) {
    	   		
    	   		if (Utils.isSameMonth(now, start)) {
    	   			mTimeTip.setText(R.string.thismonth);
    	   		} else {
    	   			mTimeTip.setText((Utils.getMonth(start) + 1) + "." + Utils.getDay(start) + " - " +
	   						(Utils.getMonth(end) + 1) + "." + Utils.getDay(end));
    	   			
    	   		}
     	       mDetailView.SetTuView(map,pjvalue*4,pjvalue,"month","",true);
     	    
//     	       mDetailView.setMap(map);
     	       mDetailView.invalidate();
    	   	}
    	   	
       }
       return start;
	}
	public void onCreate(Bundle savedInstanceState) {  
       super.onCreate(savedInstanceState);  
       Log.d("tt","FirstActivity onCreate");
        //定义DisplayMetrics 对象  
	   DisplayMetrics  dm = new DisplayMetrics();  
	      //取得窗口属性  
	   getWindowManager().getDefaultDisplay().getMetrics(dm);  
	   setContentView(R.layout.main);
	   int width = dm.widthPixels;     // 屏幕宽度（像素）
	   int height = dm.heightPixels;   // 屏幕高度（像素）
	   float density = dm.density;      // 屏幕密度（0.75 / 1.0 / 1.5）
	   int densityDpi = dm.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
//	   Toast.makeText(SportsActivity.this, "屏幕宽度: " + width + "\n屏幕高度： " + height+"\n屏幕密度："+density+"\n屏幕密度DPI:"+densityDpi, Toast.LENGTH_LONG).show();
//	   Log.d("tt2","屏幕宽度（像素）:"+width+"  屏幕高度（像素 :"+height+"\n屏幕密度:"+density+"\n屏幕密度DPI:"+densityDpi);
	   LayoutInflater flater = LayoutInflater.from(this);
	   
       elasticScrollView = (ElasticScrollView)findViewById(R.id.scrollview1);
       //viewFlipper = (ViewFlipper)findViewById(R.id.viewflipper);
       gestureDetector = new GestureDetector(this); 
       elasticScrollView.setContext(this);
       FrameLayout layout = new FrameLayout(this);
       layout.setBackgroundColor(Color.WHITE);
       ViewGroup.LayoutParams paramlayout = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
       layout.setLayoutParams(paramlayout);
       
       
       // left right
       final TextView previousView = new TextView(this);
//       previousView.setVisibility(View.GONE);
       FrameLayout.LayoutParams previousparam = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
    		   LayoutParams.WRAP_CONTENT);
       previousparam.topMargin = dip2px(SportsActivity.this,70);
       previousparam.gravity = Gravity.LEFT;
       previousView.setText("");
       previousView.setBackgroundResource(R.drawable.left_arrow);
       previousView.setTextSize(30);
       previousView.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View arg0) {
//			Toast.makeText(SportsActivity.this, SportsActivity.this.getResources().getString(R.string.scandata), Toast.LENGTH_SHORT).show();
			previous();
		}
    	   
       });
       layout.addView(previousView,previousparam);
       
       final TextView rightView = new TextView(this);
//       rightView.setVisibility(View.GONE);
       FrameLayout.LayoutParams rightparam = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
    		   LayoutParams.WRAP_CONTENT);
       rightparam.topMargin = dip2px(SportsActivity.this,70);
       rightparam.gravity = Gravity.RIGHT;
       rightView.setTextSize(30);
       rightView.setText("");
       rightView.setBackgroundResource(R.drawable.right_arrow);
       layout.addView(rightView,rightparam);
       rightView.setOnClickListener(new OnClickListener(){

	   		@Override
	   		public void onClick(View arg0) {
//	   			Toast.makeText(SportsActivity.this, SportsActivity.this.getResources().getString(R.string.scandata), Toast.LENGTH_SHORT).show();
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
//       elasticScrollView.setOnTouchListener(new OnTouchListener() {
//   		
//		@Override
//   		public boolean onTouch(View arg0, MotionEvent event) {
//   			
//   			switch (event.getAction()) {
//   			case MotionEvent.ACTION_UP:
//   				Log.d("debug11","ACTION_UP:"+mPosX);
//  	            mCurrentPosX = (int)event.getX()-mPosX;     
//  	            mCurrentPosY = (int)event.getY()-mPosY;        
//  	          	mPosX = (int)event.getX();    
//  	            mPosY = (int)event.getY();
//  	            if(mCurrentPosX != 0){
//  	            	Log.e("debug11", " mCurrentPosX:"+mCurrentPosX+"  mCurrentPosY:"+mCurrentPosY+" tan(a):"+Math.abs(mCurrentPosY )/Math.abs(mCurrentPosX ));
//  	     	       if (mCurrentPosX > 0 && (Math.abs(mCurrentPosY )/Math.abs(mCurrentPosX )) < 0.27){
//  	     	    	     Log.e("debug11", "right:"+mPosX);
//  	     	    	      
////  	     	    	     previous();
////  	     	    	     return true;
//  	     	    	     return false;
//  	     	       }  
//  	     	       else if (mCurrentPosX < 0 && (Math.abs(mCurrentPosY )/Math.abs(mCurrentPosX )) < 0.27 ){
//  	     	               Log.e("debug11", "left:"+mPosX);
////  	     	               rightView.setVisibility(View.VISIBLE);
////  	     	               // 加载动画  
////  	       	    	   Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
////  	     		                SportsActivity.this, R.anim.out);  
////  	       	    	   // 使用ImageView显示动画  
////  	       	    	   rightView.startAnimation(hyperspaceJumpAnimation); 
//////  	     	               previousView.setVisibility(View.GONE);
////  	     	               next();
////  	     	               return true;
//  	     	            return false;
//  	     	       }   
//  	     	      else if (mCurrentPosY > 0 && Math.abs(mCurrentPosX ) < 4){
//  		               Log.e("debug11", "down:"+mPosX+" mCurrentPosX:"+mCurrentPosX); 
//  	     	      } 
//  	     	      else if (mCurrentPosY  < 0 && Math.abs(mCurrentPosX ) < 4){
//  		                Log.e("debug11", "up:"+mPosX+" mCurrentPosX:"+mCurrentPosX); 
//  	     	      }
//  	            }
//     	       break;
//   			case MotionEvent.ACTION_DOWN:
//   				
////   				// 加载动画  
////  	    	    Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
////		                SportsActivity.this, R.anim.out);  
////  	    	    // 使用ImageView显示动画  
////  	    	    previousView.startAnimation(hyperspaceJumpAnimation);
////  	    	    rightView.startAnimation(hyperspaceJumpAnimation);
//   			  
//   			  rightView.setVisibility(View.VISIBLE);
//   			  previousView.setVisibility(View.VISIBLE);
//  	    	  Animation animation = AnimationUtils.loadAnimation(SportsActivity.this, R.anim.out); 
//  	    	  rightView.startAnimation(animation);
//  	    	  previousView.startAnimation(animation);
//  	    	    
//   				mPosX = (int)event.getX();    
//   	            mPosY = (int)event.getY();
//   	            Log.d("debug11","mPosX:"+mPosX);
//   	            
//   	            
//   	            break;
//   			case MotionEvent.ACTION_MOVE:
//   				Log.d("debug11","ACTION_MOVE:"+mPosX);
////   	            mCurrentPosX = (int)event.getX()-mPosX;     
////   	            mCurrentPosY = (int)event.getY()-mPosY;        
////   	          	mPosX = (int)event.getX();    
////   	            mPosY = (int)event.getY();
//   	            break;
//   			}
////   	       if (mCurrentPosX > 10 && Math.abs(mCurrentPosY ) < 4){
////   	    	     Log.e("debug11", "right:"+mPosX+" mCurrentPosX:"+mCurrentPosX);
////   	    	      
////   	    	     next();
////   	    	     return true;
////   	       }  
////   	       else if (mCurrentPosX < -10 && Math.abs(mCurrentPosY) < 4 ){
////   	               Log.e("debug11", "left:"+mPosX+" mCurrentPosX:"+mCurrentPosX);
//////   	               rightView.setVisibility(View.VISIBLE);
//////   	               // 加载动画  
//////     	    	   Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
//////   		                SportsActivity.this, R.anim.out);  
//////     	    	   // 使用ImageView显示动画  
//////     	    	   rightView.startAnimation(hyperspaceJumpAnimation); 
////////   	               previousView.setVisibility(View.GONE);
////   	               previous();
////   	               return true;
////   	       }   
////   	       else if (mCurrentPosY > 5 && Math.abs(mCurrentPosX ) < 4){
////   	               Log.e("debug11", "down:"+mPosX+" mCurrentPosX:"+mCurrentPosX); 
////   	       } 
////   	       else if (mCurrentPosY  < 5 && Math.abs(mCurrentPosX ) < 4){
////   	                Log.e("debug11", "up:"+mPosX+" mCurrentPosX:"+mCurrentPosX); 
////   	       }
//   	       
//           
//   	       return false;
//   		}
//   		
//   	});
       Paint p = new Paint();  
       p.setARGB(255, 231, 140, 48);
       ///////
       
       mStepCircleView = new DrawView(this,p);
      
       mStepCircleView.setBackgroundResource(R.drawable.cycle_bg);
      // View cycleimage = flater.inflate(R.layout.cycle_image, null);
       View cycletext = flater.inflate(R.layout.cycle_text, null);
       View todaydata = flater.inflate(R.layout.today_sportsdata,null);
      // View datatable = flater.inflate(R.layout.data_table,null);
      // layout.addView(cycleimage,param);
       
       FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
       param.gravity = Gravity.CENTER_HORIZONTAL;
       layout.addView(mStepCircleView,param);//,param);
      
       
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
       
//       layout.addView(layout2);
//       elasticScrollView.addChild(layout,1);
//       elasticScrollView.addChild(layout2,1);
       FrameLayout layout3 = new FrameLayout(this);
       FrameLayout.LayoutParams param3 = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
       layout3.setLayoutParams(param3);
//       layout3.setBackgroundResource(R.drawable.sports_chart_bg);
       layout3.setBackgroundColor(Color.WHITE);
//       layout3.addView(datatable);
       
       
     
       FrameLayout.LayoutParams paramdrawchartview = new FrameLayout.LayoutParams(
    		   LayoutParams.WRAP_CONTENT,
    		   LayoutParams.WRAP_CONTENT);
       paramdrawchartview.gravity = Gravity.CENTER;
       mDetailView = new MyChartView(this);
       mDetailView.setMinimumHeight(height/2);
//       mDetailView.setMinimumWidth(width/2);
       Log.d("debug30","width/2:"+width/2);
       mDetailView.setMargint(40);
       mDetailView.setMarginb(150);
       mDetailView.setMstyle(Mstyle.Curve);
      // mDetailView.invalidate();
       layout3.addView(mDetailView,paramdrawchartview);
       elasticScrollView.addChild(layout3,1);
       elasticScrollView.addChild(layout2,1);
       elasticScrollView.addChild(layout,1);
       elasticScrollView.smoothScrollTo(0, 0);

       
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
       
       /*
        * 	private TextView mRunning;
	private TextView mWalking;
	private TextView mDistance;
	private TextView mKiloCalorie;
	private TextView mWholeSteps;
	private TextView mStepPercent;
	
        * */
       mRunning = (TextView)findViewById(R.id.running);
       mWalking = (TextView)findViewById(R.id.walking);
       mDistance = (TextView)findViewById(R.id.gonglishu);
       mKiloCalorie = (TextView)findViewById(R.id.caloriestr);
       
       mStepPercent = (TextView)findViewById(R.id.whole_percent);
       mWholeSteps = (TextView)findViewById(R.id.whole_step);
       
       mTimeTip = (TextView)findViewById(R.id.time_tip);
       searchTime= new Date().getTime();
       todaystart = refresh();
       TextView titleView = (TextView)findViewById(R.id.title);
       titleView.setText(R.string.sportdata);
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
		popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popupbg));

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
				SportsActivity.this.sendBroadcast(intent);
			}
			
		});
    }  
	protected void OnReceiveData(String str) {
//		TextView textView =  new TextView(this);
//		textView.setText(str);
//		elasticScrollView.addChild(textView, 1);
		elasticScrollView.onRefreshComplete();
	}
	private void showPopupWindow(View v){
		Log.d("PopupMenu","popupWindow:"+popupWindow.isShowing());
		if(!popupWindow.isShowing()){
			popupWindow.showAsDropDown(data_linearlayout,Gravity.CENTER_HORIZONTAL,0);
//			popupWindow.showAtLocation(button, Gravity.LEFT , 10, 10);
		}else {
			popupWindow.dismiss();
		}
	}
	
	private LiveData parseSteps(){
		  
	       long startTime = 0,endTime = 0;
	       int maxTimeIndex = 0;
	       int totalDays = 1;
	       SimpleDateFormat bartDateFormat = new 
					SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	       Date now = new Date();
	       Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(searchTime));
			
			if ("day".equalsIgnoreCase(timeSpan)) {
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
				startTime = cal.getTimeInMillis();
				long unixTimeGMT = cal.getTimeInMillis() - TimeZone.getDefault().getRawOffset();//获取标准格林尼治时间下日期时间对应的时间戳
				Log.d("debug14","startTime:"+bartDateFormat.format(cal.getTime())+"  unixTimeGMT:"+unixTimeGMT);
				
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
				endTime = cal.getTimeInMillis();
				Log.d("debug14","startTime:"+bartDateFormat.format(cal.getTime())+"  unixTimeGMT:"+unixTimeGMT);
				
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
				
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
				startTime = cal.getTimeInMillis() - 24*60*60*1000*week;
				
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
				endTime = cal.getTimeInMillis() + 24*60*60*1000*(6-week);
				
				if (endTime > now.getTime()) {
					maxTimeIndex = now.getDay() - 1;
					if (maxTimeIndex < 0) maxTimeIndex = 6;
				} else {
					maxTimeIndex = 6;
				}

				totalDays = maxTimeIndex + 1;
			}
			else if ("month".equalsIgnoreCase(timeSpan)) {	
				long day = cal.get(Calendar.DAY_OF_MONTH) - 1;
				long maxDay = cal.getActualMaximum(Calendar.DATE);
				
				//log.info("Today's date is " + day + ", Max date is " + maxDay);
				
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
				startTime = cal.getTimeInMillis() - 24*60*60*1000*day;
				
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
				endTime = cal.getTimeInMillis() + 24*60*60*1000*(maxDay - day - 1);
				
				if (endTime > now.getTime()) {
					maxTimeIndex = now.getDate() - 1;
				} else {
					maxTimeIndex = (int)maxDay - 1;
				}
				
				totalDays = maxTimeIndex + 1;
			}
			
			Log.d("debug1","Time span is " + timeSpan + ", startTime is " + startTime + ", endTime is " + endTime + ", searchTime is " + searchTime);
			/*if(startTime > now.getTime()){
//				nodata = true;
				if (timeSpan.equalsIgnoreCase("day")) {
					cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-1, 0, 0, 0);
					searchTime = cal.getTimeInMillis();
				} else if (timeSpan.equalsIgnoreCase("week")) {
					//tmpTime.setDate(currentTime.getDate() + 7);
					cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-7, 0, 0, 0);
					searchTime = cal.getTimeInMillis();
					
				} else if (timeSpan.equalsIgnoreCase("month")) {
					cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)-1, cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
					searchTime = cal.getTimeInMillis();
					
				}
				return null;
			}else */{
//				nodata = false;
				mLiveData = new LiveData();
				 ArrayList<Steps> stepList = StepFacade.getStepsList(
				    		SportsActivity.this, startTime - 1000, endTime + 1000);
				    
				    int totalWalkSteps = 0;
					int totalWalkDistance = 0;
					int totalWalkKiloCalorie = 0;
					int totalWalkSeconds = 0;
					int totalRunSteps = 0;
					int totalRunDistance = 0;
					int totalRunKiloCalorie = 0;
					int totalRunSeconds = 0;
					int totalTarget = totalDays * User.getStepTarget(SportsActivity.this);
					int totalWheelSteps = 0;
					int totalWheelSeconds = 0;
					
					
					boolean useHour = "day".equalsIgnoreCase(timeSpan);
					int currentSectionIndex = 0;
					LiveTimeData ltd = new LiveTimeData();
					if (useHour) {
						ltd.init("hour");
					} else {
						ltd.init("day");
					}

					mLiveData.setDataType(LiveData.DATA_LIVE);
					mLiveData.setStartTime(startTime);
					mLiveData.setEndTime(endTime);
					mLiveData.setTimeSpan(timeSpan);
					if (now.getTime() >= startTime && now.getTime() <= endTime) {
						mLiveData.setNow(true);
					} else {
						mLiveData.setNow(false);
					}
					

					for(int i=0;i<stepList.size();i++) {
						Steps s = stepList.get(i);
						if (s.getStepType() == Steps.STEP_TYPE_SLEEPING ||
								s.getStepType() == Steps.STEP_NEW_TYPE_SLEEP) continue;

						boolean run = false;//isRunning(s);
						if (s.getStepType() == Steps.STEP_TYPE_WALKING) {
							run = isRunning(s);
						} else if (s.getStepType() == Steps.STEP_NEW_TYPE_RUN) {
							run = true;
						} else if (s.getStepType() == Steps.STEP_NEW_TYPE_WALK) {
							run = false;
						} else {
							run = false;
						}
						
						if (run) {
							totalRunSteps += s.getSteps();
							if (s.getEndTime() > s.getStartTime()) {
								totalRunSeconds += (s.getEndTime() - s.getStartTime())/1000;
							}
						}else if (s.getStepType() == Steps.STEP_TYPE_WALKING || s.getStepType() == Steps.STEP_NEW_TYPE_WALK){
							totalWalkSteps += s.getSteps();
							if (s.getEndTime() > s.getStartTime()) {
								totalWalkSeconds += (s.getEndTime() - s.getStartTime())/1000;
							}
						} else if (s.getStepType() == Steps.STEP_NEW_TYPE_WHEEL) {
							totalWheelSteps += s.getSteps();
							totalWheelSeconds += (s.getEndTime() - s.getStartTime())/1000;
							continue;
						}
						
						
						/* else {
							totalWalkSteps += s.getSteps();
							if (s.getEndTime() > s.getStartTime()) {
								totalWalkSeconds += (s.getEndTime() - s.getStartTime())/1000;
							}
						}*/
						
						int idx = 0;
						cal.setTime(new Date(s.getEndTime()));
						
						if (useHour) {
							idx = cal.get(Calendar.HOUR_OF_DAY);
						} else {
							if ("week".equalsIgnoreCase(timeSpan)) {
								idx = cal.get(Calendar.DAY_OF_WEEK) - 2;
								if (idx < 0) idx = 6;
							} else {
								idx = cal.get(Calendar.DAY_OF_MONTH) - 1;
							}
						}
						
						if (idx > currentSectionIndex) {
							mLiveData.getTimeData().add(ltd);
							ltd = new LiveTimeData();
							ltd.setSteps(s.getSteps());
							ltd.setTimeIndex(idx);
							ltd.setTimeUint(useHour ? LiveTimeData.TIME_UNIT_HOUR : LiveTimeData.TIME_UNIT_DAY);
						} else {
							ltd.setSteps(ltd.getSteps() + s.getSteps());
						}
						currentSectionIndex = idx;
						
						if ("day".equalsIgnoreCase(timeSpan)) {
							LiveDetailData ldd = new LiveDetailData();
							ldd.setStartTime(s.getStartTime());
							ldd.setEndTime(s.getEndTime());
							ldd.setLiveType(run ? LiveDetailData.LIVE_RUN : LiveDetailData.LIVE_WALK);
							ldd.setSteps(s.getSteps());
							mLiveData.getDetailData().add(ldd);
						}
					}
					// add live data
					mLiveData.getTimeData().add(ltd);
					mLiveData.fixTimeData(maxTimeIndex);
					
					totalRunDistance = (totalRunSteps * 86 * User.getHeight(SportsActivity.this) / (100*170));
					totalWalkDistance = (totalWalkSteps * 65 * User.getHeight(SportsActivity.this)/(100 * 170));
					totalRunKiloCalorie = (totalRunSteps*User.getWeight(SportsActivity.this)*90)/(3600*27);
					totalWalkKiloCalorie = (totalWalkSteps*User.getWeight(SportsActivity.this)*30)/(3600*17);
					
					mLiveData.setRunSteps(totalRunSteps);
					mLiveData.setRunDistance(totalRunDistance);
					mLiveData.setRunKiloCalorie(totalRunKiloCalorie);
					mLiveData.setWalkSteps(totalWalkSteps);
					mLiveData.setWalkDistance(totalWalkDistance);
					mLiveData.setWalkKiloCalorie(totalWalkKiloCalorie);
					mLiveData.setTotalTarget(totalTarget);
					mLiveData.setWalkSeconds(totalWalkSeconds);
					mLiveData.setRunSeconds(totalRunSeconds);
					
					mLiveData.setWheelSteps(totalWheelSteps);
					mLiveData.setWheelSeconds(totalWheelSeconds);
					
					//////// end
					return mLiveData;
			}
		    
		   
	}
	private boolean isRunning(Steps s) {
		if (s.getStepType() != Steps.STEP_TYPE_WALKING) return false;
		// at least 30 seconds
		if (s.getEndTime() - s.getStartTime() < 30*1000) return false;
		if (s.getSteps() <= 0) return false;
		// at least 2 steps per seconds when running
		if (s.getSteps()*10 / ((s.getEndTime() - s.getStartTime() - 25*1000)/1000) >= 22) return true;
		return false;
	}
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent event) {
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			mPosX = (int)event.getX();    
//            mPosY = (int)event.getY();
//            Log.d("debug11","mPosX:"+mPosX);
//            break;
//		case MotionEvent.ACTION_MOVE:
//			Log.d("debug11","ACTION_MOVE:"+mPosX);
//            mCurrentPosX = (int)event.getX()-mPosX;     
//            mCurrentPosY = (int)event.getY()-mPosY;        
//          	mPosX = (int)event.getX();    
//            mPosY = (int)event.getY();
//            break;
//		}
////		 if(MotionEvent.ACTION_DOWN==event.getAction()){
////             mPosX = (int)event.getX();    
////             mPosY = (int)event.getY();
////             Log.d("debug11","mPosX:"+mPosX);
////		 }
////		 if (MotionEvent.ACTION_MOVE == event.getAction()) {
////			 Log.d("debug11","ACTION_MOVE:"+mPosX);
////             mCurrentPosX = (int)event.getX()-mPosX;     
////             mCurrentPosY = (int)event.getY()-mPosY;        
////           	 mPosX = (int)event.getX();    
////             mPosY = (int)event.getY();
////		 }
//       if (mCurrentPosX > 0 /*&& Math.abs(mCurrentPosY - mPosY) < 10*/){
//    	     Log.e("debug11", "right:"+mPosX+" move:"+mCurrentPosX); 
//    	     next();
//    	     return true;
//       }  
//       else if (mCurrentPosX < 0 /*&& Math.abs(mCurrentPosY - mPosY) < 10*/ ){
//               Log.e("debug11", "left:"+mPosX+" move:"+mCurrentPosX); 
//               previous();
//               return true;
//       }   
//       else if (mCurrentPosY - mPosY > 0 /*&& Math.abs(mCurrentPosX - mPosX) < 10*/){
//               Log.e("debug11", "down:"+mPosX+" move:"+mCurrentPosX); 
//       } 
//       else if (mCurrentPosY - mPosY < 0 /*&& Math.abs(mCurrentPosX - mPosX) < 10*/){
//                Log.e("debug11", "up:"+mPosX+" move:"+mCurrentPosX); 
//       }
//       return false;
//	}
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
		/*if (arg0.getX() - arg1.getX() > FLING_MIN_DISTANCE
                && Math.abs(arg2) > FLING_MIN_VELOCITY) {
            // 当像左侧滑动的时候
            //设置View进入屏幕时候使用的动画
			this.viewFlipper.setInAnimation(inFromRightAnimation());
            //设置View退出屏幕时候使用的动画
			this.viewFlipper.setOutAnimation(outToLeftAnimation());
			this.viewFlipper.showNext();
        } else if (arg1.getX() - arg0.getX() > FLING_MIN_DISTANCE
                && Math.abs(arg2) > FLING_MIN_VELOCITY) {
            // 当像右侧滑动的时候
        	this.viewFlipper.setInAnimation(inFromLeftAnimation());
        	this.viewFlipper.setOutAnimation(outToRightAnimation());
        	this.viewFlipper.showPrevious();
        }
        return false;*/
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

	/**
     * 定义从右侧进入的动画效果
     * @return
     */
    protected Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(500);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
 
    /**
     * 定义从左侧退出的动画效果
     * @return
     */
    protected Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(500);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }
 
    /**
     * 定义从左侧进入的动画效果
     * @return
     */
    protected Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromLeft.setDuration(500);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }
 
    /**
     * 定义从右侧退出时的动画效果
     * @return
     */
    protected Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoRight.setDuration(500);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoRight;
    }
}  
