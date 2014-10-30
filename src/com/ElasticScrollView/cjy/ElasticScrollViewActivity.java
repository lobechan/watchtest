package com.ElasticScrollView.cjy;

import com.ElasticScrollView.view.DrawView;
import com.ElasticScrollView.view.ElasticScrollView;
import com.ElasticScrollView.view.ElasticScrollView.OnRefreshListener;
import com.fastfox.watchtest.R;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ElasticScrollViewActivity extends Activity {
	ElasticScrollView elasticScrollView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        elasticScrollView = (ElasticScrollView)findViewById(R.id.scrollview1);
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        Paint p = new Paint();  
        p.setARGB(255, 122, 103, 104);
        final DrawView view = new DrawView(this,p);//,170.0);
        view.setMinimumHeight(500);
        view.setMinimumWidth(300);
        view.invalidate();
        layout.addView(view);
        elasticScrollView.addChild(layout,1);
//        for(int i=1;i<=50;i++){
//			TextView tempTextView = new TextView(this);
//			tempTextView.setText("Text:" + i);
//			elasticScrollView.addChild(tempTextView,1);
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
    }

	protected void OnReceiveData(String str) {
		TextView textView =  new TextView(this);
		textView.setText(str);
		elasticScrollView.addChild(textView, 1);
		elasticScrollView.onRefreshComplete();
	}
}