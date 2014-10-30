package com.ElasticScrollView.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.widget.ImageView;

public class DrawView extends ImageView{
	private Paint p;
	private int mPercent = 0;
	private int mTotal = 30;
	public void setPercent(int percent, int total){
		mPercent = percent;
		mTotal = total;
	}
	public DrawView(Context context,Paint p) {  
        super(context);  
        this.p = p;
       // this.R = R;
    }  
	@Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);
        double percent = mPercent*30/mTotal;
        DrawCycle(percent,30,canvas,p);
    }  
	private void DrawCycle(double finished,double total,Canvas canvas,Paint p){
		int gap = 30;
		int x = gap, y = gap ;
		double a = 0;
	    double r = getWidth()/2-gap;
	    Paint p1 = p;
	    for (int i = 0; i < finished; ++i) {
			a = i*360/total;
			a = a*Math.PI/180;
			if (a>=180) {
				x = gap+(int)(r*(1-Math.sin(a)));
				y = gap+(int)(r*(1+Math.cos(a)));
			} else {
				x = gap+(int)(r*(1+Math.sin(a)));
				y = gap+(int)(r*(1-Math.cos(a)));
			}
//			Paint p = new Paint();  
////	        p.setColor(Color.RED);// 设置红色  
//	        p.setARGB(255, 231, 140, 48);
			canvas.drawCircle(x, y, 10, p1);// 大圆  
//			canvas.drawPoint(x, y, p);//画一个点  
		}
		
		for (int i = 0; i < total - finished; ++i) {
			a = (i+finished)*360/total;
			a = a*Math.PI/180;
			if (a>=180) {
				x = gap+(int)(r*(1-Math.sin(a)));
				y = gap+(int)(r*(1+Math.cos(a)));
			} else {
				x = gap+(int)(r*(1+Math.sin(a)));
				y = gap+(int)(r*(1-Math.cos(a)));
			}
			Paint p2 = new Paint();  
			p2.setARGB(255, 180, 180, 180);
	        canvas.drawCircle(x, y, 10, p2);// 大圆  
//	        canvas.drawPoint(x, y, p);//画一个点  
		}

	}
}
