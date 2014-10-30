package com.ElasticScrollView.view;

import android.graphics.Canvas ;
import android.graphics.Paint ;
import android.util.Log;

public class Chart {

	private double w = 60 ;
	private int h ;
	private int total_y = 300 ;
	private int x ;
	private int y;
	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x ;
	}

	public void setX(int x) {
		this.x = x ;
	}

	public int getH() {
		return h ;
	}

	public void setH(int h) {
		this.h = h ;
	}
	
	public int getTotal_y() {
		return total_y;
	}
	
	public void setTotal_y(int total_y) {
		this.total_y = total_y;
	}
	
	public double getW() {
		return w;
	}
	
	public void setW(double w) {
		this.w = w;
	}

	public void drawSelf(Canvas canvas, Paint paint) {
//		canvas.drawRect(x, total_y - h, w + x, total_y - 1, paint) ;
		Log.d("zz"," x "+x +" w "+(int)(w + (double)x));
		canvas.drawRect(x, h, (int)(w + (double)x)+1, y, paint) ;
	}

}
