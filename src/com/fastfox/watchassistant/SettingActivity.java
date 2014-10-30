package com.fastfox.watchassistant;

import com.ElasticScrollView.view.ElasticScrollView;
import com.ElasticScrollView.view.ElasticScrollView.OnRefreshListener;
import com.bluefay.android.BLUtils;
import com.fastfox.watchtest.R;
import com.excheer.until.AddressData;
import com.excheer.wheel.widget.OnWheelChangedListener;
import com.excheer.wheel.widget.OnWheelScrollListener;
import com.excheer.wheel.widget.WheelView;
import com.excheer.wheel.widget.adapters.AbstractWheelTextAdapter;
import com.excheer.wheel.widget.adapters.ArrayWheelAdapter;

import android.app.Activity;  
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;  
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;  
import android.widget.Toast;
 
public class SettingActivity extends Activity {  
	private ImageView sync;
	private Button button_ok;
	private TextView setting_male,setting_height,setting_weight;
	private LinearLayout setting,user_name_linear,setting_male_linear,setting_height_linear,setting_weight_linear;
	private EditText userName;
	int width,height;
//	ElasticScrollView elasticScrollView;
	
	private EditText mSportTarget, mSleepTarget;
	private boolean phone_flag = false;
	private Button mSavedButton;
    public void onCreate(Bundle savedInstanceState) {  
           	super.onCreate(savedInstanceState);  
           	Log.d("tt","ThirdActivity onCreate");
           	setContentView(R.layout.setting);
           	setting = (LinearLayout)findViewById(R.id.setting);
//            TabDemo tab = (TabDemo) getParent();
//     	   	 tab.handler.obtainMessage(104).sendToTarget();
         // 获取屏幕的高度和宽度
    		Display display = this.getWindowManager().getDefaultDisplay();
    		width = display.getWidth();
    		height = display.getHeight();
//            LayoutInflater flater = LayoutInflater.from(this);
//            elasticScrollView = (ElasticScrollView)findViewById(R.id.scrollview1);
            
            FrameLayout layout = new FrameLayout(this);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            layout.setLayoutParams(param);         
//            final View setting = flater.inflate(R.layout.setting, null);
            setting_male = (TextView)findViewById(R.id.setting_male);
            userName = (EditText)findViewById(R.id.user_name);
            userName.setText(User.getUserName(SettingActivity.this));
            String sex = User.getSex(SettingActivity.this);
            if(sex.equalsIgnoreCase("Male")) {
            	setting_male.setText(R.string.male);
            } else {
            	setting_male.setText(R.string.female);
            }
           	setting_male_linear = (LinearLayout)findViewById(R.id.setting_male_linear);
           	setting_height = (TextView)findViewById(R.id.setting_height);
           	setting_height.setText(User.getHeight(SettingActivity.this)+"");
           	
           	setting_height_linear = (LinearLayout)findViewById(R.id.setting_height_linear);
           	setting_weight = (TextView)findViewById(R.id.setting_weight);
           	setting_weight.setText(User.getWeight(SettingActivity.this)+"");
           	user_name_linear = (LinearLayout)findViewById(R.id.user_name_linear);
           	setting_weight_linear = (LinearLayout)findViewById(R.id.setting_weight_linear);
           	
           	mSportTarget = (EditText)findViewById(R.id.sport_target);
           	mSleepTarget = (EditText)findViewById(R.id.sleep_target);
           	
           	int sporttarget = User.getStepTarget(SettingActivity.this);
           	int sleeptarget = User.getSleepTarget(SettingActivity.this);
           	sleeptarget = sleeptarget/60;
           	mSportTarget.setText(Integer.toString(sporttarget));
           	mSleepTarget.setText(Integer.toString(sleeptarget));

           	mSavedButton = (Button)findViewById(R.id.save_btn);
           	mSavedButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					// save target
					String sportstr = mSportTarget.getText().toString();
					String sleepstr = mSleepTarget.getText().toString();
					String usernamestr = userName.getText().toString();
					String heightstr = setting_height.getText().toString();
					String weightstr = setting_weight.getText().toString();
					if(usernamestr != null && !usernamestr.isEmpty()){
						User.setUserName(SettingActivity.this, usernamestr);
					}else {
						User.setUserName(SettingActivity.this, SettingActivity.this.getResources().getString(R.string.fastfox_user));
					}
					if(heightstr != null && !heightstr.isEmpty()){
						User.setHeight(SettingActivity.this, Integer.parseInt(heightstr));
					}else {
						User.setHeight(SettingActivity.this, 0);
					}
					if(weightstr != null && !weightstr.isEmpty()){
						User.setWeight(SettingActivity.this, Integer.parseInt(weightstr));
					}else {
						User.setHeight(SettingActivity.this, 0);
					}
					
					
					
					int sleep = Integer.parseInt(sleepstr);
					if(sportstr != null && !sportstr.isEmpty()) {
						int sport = Integer.parseInt(sportstr);
						if(sport >100000 || sport == 0) {
							Toast.makeText(SettingActivity.this, 
									getResources().getString(R.string.sport_data_invalid), 
										Toast.LENGTH_LONG)
										.show();
							int sporttarget = User.getStepTarget(SettingActivity.this);
							mSportTarget.setText(Integer.toString(sporttarget));
							return;
						} else {
							User.setStepTarget(SettingActivity.this, sport);
						}
					} else {
						
						Toast.makeText(SettingActivity.this, 
								getResources().getString(R.string.sport_data_invalid), 
									Toast.LENGTH_LONG)
									.show();
						int sporttarget = User.getStepTarget(SettingActivity.this);
						mSportTarget.setText(Integer.toString(sporttarget));
						return;
					}
					
					if(sleepstr != null && !sleepstr.isEmpty()) {
						if(sleep > 18 || sleep == 0) {
							Toast.makeText(SettingActivity.this, 
									getResources().getString(R.string.sleep_data_invalid), 
									Toast.LENGTH_LONG)
									.show();

				           	int sleeptarget = User.getSleepTarget(SettingActivity.this);
				           	mSleepTarget.setText(Integer.toString(sleeptarget));
				           	return;
						} else {
							User.setSleepTarget(SettingActivity.this, sleep*60);
						}
					} else {
						Toast.makeText(SettingActivity.this, 
								getResources().getString(R.string.sleep_data_invalid), 
								Toast.LENGTH_LONG)
								.show();

			           	int sleeptarget = User.getSleepTarget(SettingActivity.this);
			           	mSleepTarget.setText(Integer.toString(sleeptarget));
			           	return;
					}
					
					
					Toast.makeText(SettingActivity.this,
							getResources().getString(R.string.already_saved),
							Toast.LENGTH_LONG)
						.show();
				}
           		
           	});
           	
//            layout.addView(setting);
//            elasticScrollView.addChild(layout,1);
//            elasticScrollView.smoothScrollTo(0, 0);        
//            final Handler handler = new Handler() {
//            	public void handleMessage(Message message) {
//            		String str = (String)message.obj;
//            		OnReceiveData(str);
//            	}
//            };
            setting.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					user_name_linear.setFocusable(true);
					user_name_linear.setFocusableInTouchMode(true);
					user_name_linear.requestFocus();
					return false;
				}
			});
//            elasticScrollView.setonRefreshListener(new OnRefreshListener() {
//     			
//     			@Override
//     			public void onRefresh() {
//     				Thread thread = new Thread(new Runnable() {
//     					
//     					@Override
//     					public void run() {
//     						try {
//     							Thread.sleep(2000);
//     						} catch (InterruptedException e) {
//     							e.printStackTrace();
//     						}
//     						Message message = handler.obtainMessage(0, "new Text");
//     						handler.sendMessage(message);
//     					}
//     				});
//     				thread.start();
//     			}
//     		});
            setting_male_linear.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					createMalesDialog(SettingActivity.this).show();
				}
            	
            });
//            setting_height_linear.setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View arg0) {
//					// 显示 popupWindow
//					PopupWindow popupWindow = makePopupWindow(
//							SettingActivity.this,setting_height,100,"cm", true);
//					int[] xy = new int[2];
//					setting.getLocationOnScreen(xy);
//					popupWindow.showAtLocation(setting,Gravity.CENTER|Gravity.BOTTOM, 0, -height); 
//				}
//            	
//            });
//            setting_weight_linear.setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View arg0) {
//					// 显示 popupWindow
//					PopupWindow popupWindow = makePopupWindow(SettingActivity.this,setting_weight,30,"kg",false);
//					int[] xy = new int[2];
//					setting.getLocationOnScreen(xy);
//					popupWindow.showAtLocation(setting,Gravity.CENTER|Gravity.BOTTOM, 0, -height); 
//				}
//            	
//            });
            
            TextView titleView = (TextView)findViewById(R.id.title);
            titleView.setText(R.string.personal_title);
            LinearLayout data_linearlayout = (LinearLayout)findViewById(R.id.data_linearlayout);
            LinearLayout sync_linearlayout = (LinearLayout)findViewById(R.id.sync_linearlayout);
            data_linearlayout.setVisibility(View.GONE);
			sync_linearlayout.setVisibility(View.GONE);
         }  
//     	protected void OnReceiveData(String str) {
////     		TextView textView =  new TextView(this);
////     		textView.setText(str);
////     		elasticScrollView.addChild(textView, 1);
//     		elasticScrollView.onRefreshComplete();
//     	} 
     	public  Dialog createMalesDialog(final Context context) {  
            LayoutInflater inflater = LayoutInflater.from(context);  
            View v = inflater.inflate(R.layout.alter_usermale, null);// 得到加载view  
            LinearLayout layout = (LinearLayout) v.findViewById(R.id.alter_usermale_view);// 加载布局  
            // main.xml中的ImageView  
            LinearLayout select_male = (LinearLayout)v.findViewById(R.id.select_male);
            LinearLayout select_female = (LinearLayout)v.findViewById(R.id.select_female);
            final ImageView select_icon1 = (ImageView)v.findViewById(R.id.select_icon1);
            final ImageView select_icon2 = (ImageView)v.findViewById(R.id.select_icon2);
            String sex = User.getSex(SettingActivity.this);
            if(sex.equalsIgnoreCase("Male")) {
            	select_icon1.setVisibility(View.VISIBLE);
            	select_icon2.setVisibility(View.GONE);
            } else {
            	select_icon1.setVisibility(View.GONE);
            	select_icon2.setVisibility(View.VISIBLE);
            }
//            String gender = BLUtils.getStringValue(SettingActivity.this, "Gender", "");
//            if(gender.equals("1")){
//            	select_icon1.setVisibility(View.GONE);
//            	select_icon2.setVisibility(View.VISIBLE);
//            }else if(gender.equals("2")){
//            	select_icon1.setVisibility(View.VISIBLE);
//            	select_icon2.setVisibility(View.GONE);
//            }else if(gender.equals("null")){
//            	select_icon1.setVisibility(View.GONE);
//            	select_icon2.setVisibility(View.GONE);
//            }
            
            final Dialog devicesgDialog = new Dialog(context, R.style.devices_dialog);// 创建自定义样式dialog  
      
            devicesgDialog.setCancelable(true);// 可以用“返回键”取消  
            devicesgDialog.setContentView(layout, new LinearLayout.LayoutParams(  
                    LinearLayout.LayoutParams.WRAP_CONTENT,  
                    LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局  
            select_male.setOnClickListener(new OnClickListener(){

    			@Override
    			public void onClick(View arg0) {
    				phone_flag = true;
    				BLUtils.setBooleanValue(SettingActivity.this, "phone_flag", phone_flag);
    				setting_male.setText(R.string.male);
    				User.setSex(SettingActivity.this, "Male");
    				select_icon1.setVisibility(View.VISIBLE);
    				select_icon2.setVisibility(View.GONE);
    	        	devicesgDialog.dismiss();
    			}
            	
            });
            select_female.setOnClickListener(new OnClickListener(){

    			@Override
    			public void onClick(View arg0) {
    				phone_flag = false;
    				BLUtils.setBooleanValue(SettingActivity.this, "phone_flag", phone_flag);
    				setting_male.setText(R.string.female);
    				User.setSex(SettingActivity.this, "Female");
    				select_icon1.setVisibility(View.GONE);
    				select_icon2.setVisibility(View.VISIBLE);
    				devicesgDialog.dismiss();
    			}
            });
            return devicesgDialog;  

    	}  
     	private boolean scrolling = false; 
        private TextView tv;
    	// 创建一个包含自定义view的PopupWindow
    	private PopupWindow makePopupWindow(Context cx,final TextView tv,
    			final int dataint ,final String datastr,
    			final boolean is_height)
    	{
    		final PopupWindow window;
     		window = new PopupWindow(cx); 
     		 
            View contentView = LayoutInflater.from(this).inflate(R.layout.cities_layout, null);
            window.setContentView(contentView);
                        
            final WheelView country = (WheelView) contentView.findViewById(R.id.country);
            country.setVisibleItems(3);
            country.setViewAdapter(new HeightAdapter(this,dataint,datastr));

            country.addChangingListener(new OnWheelChangedListener() {
    			public void onChanged(WheelView wheel, int oldValue, int newValue) {
    			    if (!scrolling) {
//    			        updateCities(city, cities, newValue);
    			    }
    			}
    		});
            
            country.addScrollingListener( new OnWheelScrollListener() {
                public void onScrollingStarted(WheelView wheel) {
                    scrolling = true;
                }
                public void onScrollingFinished(WheelView wheel) {
                    scrolling = false;
//                    updateCities(city, cities, country.getCurrentItem());
//                    tv.setText( AddressData.PROVINCES[country.getCurrentItem()] ); 
                    Log.d("tt","country.getCurrentItem():"+country.getCurrentItem());
                    int currentItem = country.getCurrentItem();
//                    setting_height.setText( AddressData.HEIGHT[currentItem] ); 
                    tv.setText( (currentItem + dataint)  + datastr );
                    if(is_height) {
                    	User.setHeight(SettingActivity.this, currentItem + dataint);
                    } else {
                    	User.setWeight(SettingActivity.this, currentItem + dataint);
                    }
                }
            });
             
            country.setCurrentItem(60);//设置初始选定位置
            
//            // 点击事件处理
//            button_ok = (Button)contentView.findViewById(R.id.button_ok);
//        	button_ok.setOnClickListener(new OnClickListener()
//    		{
//    			@Override
//    			public void onClick(View v)
//    			{ 
////    				tt.setText(AddressData.PROVINCES[country.getCurrentItem()] + "-" + 
////    	            		   AddressData.CITIES[country.getCurrentItem()][city.getCurrentItem()] + "-" + 
////    	            		   AddressData.COUNTIES[country.getCurrentItem()][city.getCurrentItem()][ccity.getCurrentItem()]);
//    				window.dismiss(); // 隐藏
//    			}
//    		});
             
            
     		window.setWidth(width);
     		window.setHeight(height/3);
            
    		// 设置PopupWindow外部区域是否可触摸
    		window.setFocusable(true); //设置PopupWindow可获得焦点
    		window.setTouchable(true); //设置PopupWindow可触摸
    		window.setOutsideTouchable(true); //设置非PopupWindow区域可触摸
    		return window;
    	}        
        /**
         * Adapter for countries
         */
        private class HeightAdapter extends AbstractWheelTextAdapter {
            // Countries names
//            private String countries[] = AddressData.HEIGHT;  
            private String countries[] = new String[151];  
            /**
             * Constructor
             */
            protected HeightAdapter(Context context,int dataint,String datastr) {
                super(context, R.layout.country_layout, NO_RESOURCE);
                for (int i = 0;i<= 150;i++){
                	countries[i] = (dataint +i) + datastr;
                }
                
                setItemTextResource(R.id.country_name);
            }

            @Override
            public View getItem(int index, View cachedView, ViewGroup parent) {
                View view = super.getItem(index, cachedView, parent); 
                return view;
            }
            
            @Override
            public int getItemsCount() {
            	
                return countries.length;
            }
            
            @Override
            protected CharSequence getItemText(int index) {
                return countries[index];
            }
        }
}  