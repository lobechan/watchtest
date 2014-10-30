package com.ElasticScrollView.view;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fastfox.watchtest.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class BarChartView extends View {

	private int[] data_total ;
	private HashMap<Double, Double> map;
	private HashMap<Double, Double> map2;
	private String[] xstr;
	private String[] ystr;
	private int margin ;
	private int marginbottom = 40;
	private int margintop = 15;
	private Chart chart ;
	private Paint paint ;	
	private int xtotal = 4;//y轴刻度个数
	private int ytotal = 3;
	int xmarginleft = 100;//y轴左边偏移距离
	int xmarginright = 100;//y轴左边偏移距离
	int xmarginr = 50;//x轴右边偏移距离
	int xstrmarginleft = 50;
	int width;
	int height;
	int yvalue; //y轴平均距离
	int xvalue; //x轴平均值
	double chartwidt = 110.0f; //直方图宽度半径
	double xStep = 110.0f;
	int yheight;//y轴的长度
	int xwidth;//x轴的长度
	private int hour = 0;
	private double lightsleep = 0;
	private double deepsleep =0;
	ArrayList<Double> dlk;
	String ystr2;//y轴单位
	
	public static final int RECT_SIZE = 6;  
    private Point mSelectedPoint;  
     
    //枚举实现坐标桌面的样式风格
    public static enum Mstyle
    {
        Line,Curve
    }
    private String dataType;
    private Mstyle mstyle=Mstyle.Line;
    private Point[] mPoints = new Point[100];  
    private Point[] mPoints2 = new Point[100]; 
       
    Context context;
    int bheight=0;
    ArrayList<Double> dlkother;
    ArrayList<Double> dlkother2;
//    int totalvalue=30;
//    private int pjvalue=1;
    String ystrother="";//纵纵坐标的属性
    String xstrother[];//横坐标单位
    int margint=15;
    int marginb=40;
    int marginr = 80;
    int c=0;
    int resid=0;
    int xmargin;
    Boolean isylineshow;
    int xcount = 5;
    int xvalueother;//x平均值
    int datasum = 24/6;
    int datacount = 24;//x坐标等分24份
    int xtype = 1;
    /**
    * @param map 需要的数据，虽然key是double，但是只用于排序和显示，与横向距离无关
    * @param totalvalue Y轴的最大值
    * @param pjvalue Y平均值
    * @param xstr X轴的单位
    * @param ystr Y轴的单位
    * @param isylineshow 是否显示纵向网格
     * @return 
    */
    String day[] = new String[] {
  		   "00:00","06:00","12:00","18:00","24:00"
     };
    DecimalFormat df = new DecimalFormat("0.0");
	public BarChartView(Context context) {
		super(context) ;
		this.context=context;
		ystr = new String[] {
				"",context.getString(R.string.ystr1),context.getString(R.string.ystr2),context.getString(R.string.ystr3)
		};
		ystrother = context.getString(R.string.hour);
		ystr2 = context.getString(R.string.hour);
		margin = 0 ;
		chart = new Chart() ;
////		data_total = new int[] {90,65,80/*,115,11,11,10,12,17,18,19,14,15,16,17,24,25,28,26,28*/};
//		xstr = new String[] {"20:00","24:00","4:00","8:00","12:00"};
//		ystr = new String[] {
//				"","浅睡","深睡","睡眠"
//		};
//		this.map = map;
////		this.data_total = data_total;
//		this.xstr = xstr;
		paint = new Paint() ;
		paint.setAntiAlias(true) ;
	}
	public void SetTuView(HashMap<Double, Double> map,HashMap<Double, Double> map2,double lightsleep,double deepsleep,String dataType,String xstr[]) 
    {
       
        
        
		
//		data_total = new int[] {90,65,80/*,115,11,11,10,12,17,18,19,14,15,16,17,24,25,28,26,28*/};
//		xstr = new String[] {"20:00","24:00","4:00","8:00","12:00"};
//		ystr = new String[] {
//				"","浅睡","深睡","睡眠"
//		};
	 	this.dataType = dataType;
		this.map = map;
		this.map2 = map2;
		this.lightsleep = lightsleep;
		this.deepsleep = deepsleep;
//		this.data_total = data_total;
		this.xstr = xstr;
		if(dlk != null) {
			dlk.clear();
		}
		

    }
	public void drawAxis(Canvas canvas) {
		//x，y 轴
		paint.setColor(Color.GRAY) ;
		paint.setStrokeWidth(1) ;
		//x轴线
		canvas.drawLine(xmarginleft, yheight+margint, xmarginleft+xwidth, yheight+margint, paint);
		//y轴线
		canvas.drawLine(xmarginleft, yheight+margint, xmarginleft, yheight+margint+margint-yvalue*3, paint);	
		Log.d("tt18","yheight:"+yheight);
		
	//	x轴上的刻度参数设置
		int x = xmarginleft ;//x轴上文字起始位置=图的起始位置+图的宽度/2
		int xy = yheight+margint + marginbottom/2;
		Paint p = new Paint();
		p.setColor(Color.GRAY);
        p.setAlpha(0x0000ff);   
        p.setTextSize(dip2px(context,7));   
//        String familyName = "宋体";   
//        Typeface font = Typeface.create(familyName,Typeface.ITALIC);   
//        p.setTypeface(font);   
        p.setTextAlign(Paint.Align.CENTER); 
		for (int i = 0; i <= xtotal; i++) {
				canvas.drawText(xstr[i]  + "", x+(int)((double)i*xStep), xy, p) ;
			if(i == xtotal){
				Log.d("tt5"," last kedu "+(x+(i*xStep)) +" text "+xstr[i]  + "");
			}
			
		}
		int ydata = 50;//y轴参数设置
//		String ystr2 = "小时";//y轴单位
		
		int yy =yheight+margint ;
//		canvas.drawText(10 + "", 25, y, p) ;
		p.setTextSize(dip2px(context,6));
		for (int i = 0; i <= ytotal; i++) {
			
			switch (i){
//			case 0:canvas.drawText(ystr[i] + lightsleep  + ystr2, xmarginleft - xstrmarginleft, yy, p) ;break;
			case 1:
				if(lightsleep == 0){
					canvas.drawText(ystr[i] + 0  + ystr2, xmarginleft - xstrmarginleft, yy, p) ;
				}else{
					canvas.drawText(ystr[i] + df.format(lightsleep)  + ystr2, xmarginleft - xstrmarginleft, yy, p) ;
				}
				break;
			case 2:
				if(deepsleep == 0){
					canvas.drawText(ystr[i] + 0 + ystr2, xmarginleft - xstrmarginleft, yy, p) ;
				}else{
					canvas.drawText(ystr[i] + df.format(deepsleep)  + ystr2, xmarginleft - xstrmarginleft, yy, p) ;
				}
				break;
			case 3:
				if((lightsleep+deepsleep) == 0){
					canvas.drawText(ystr[i] + 0 + ystr2, xmarginleft - xstrmarginleft, yy, p) ;
				}else{
					canvas.drawText(ystr[i] + df.format(lightsleep+deepsleep)  + ystr2, xmarginleft - xstrmarginleft, yy, p) ;
				}
				break;
			}
//			canvas.drawText(ystr[i] + i  + ystr2, xmarginleft - xstrmarginleft, yy, p) ;
			yy -= yvalue ;
		}
	}

	public void drawChart(Canvas canvas) {			
//			int temp = 40 ;
			int temp = 20 ;//x刻度偏移距离
//			int chartW = 60;
			double chartW = chartwidt;
			chart.setTotal_y(yheight);
			Log.d("tt5","dlk.size:"+dlk.size()+" xmarginleft "+xmarginleft+" chartW "+chartW);
			for (int i = 0; i < dlk.size(); i++) {	
				chart.setY(yheight+margint);
//				chart.setH((data_total[i] - yheight + marginbottom)/yvalue) ;
//				chart.setH(yheight- marginbottom - yvalue * map.get(dlk.get(i))) ;
				Log.d("tt","dlk.get(i):"+dlk.get(i));
				//if(i == dlk.size()-1)
				{
					Log.d("tt5"," last data x "+(xmarginleft + chartW * i));
				}
				//chartW为图的宽度,margin为偏移值
				chart.setX(xmarginleft + (int)(chartW * (double)i) ) ;
//				chart.setX(temp + margin) ;
				if(map.get(dlk.get(i))%3 == 0){
					chart.setH(yheight+margint - yvalue * 2) ;
					chart.setW(chartW);
//					paint.setColor(Color.RED) ;
					paint.setARGB(255, 135, 11, 46);
				}else if(map.get(dlk.get(i))%3 == 1){
					chart.setH(yheight+margint - yvalue * 1) ;
					chart.setW(chartW);
//					paint.setColor(Color.GREEN) ;
					paint.setARGB(255, 220, 157, 190);
				}else{
					chart.setH(yheight+margint - yvalue * 1) ;
					chart.setW(chartW);
					paint.setColor(Color.WHITE) ;
				}
				chart.drawSelf(canvas, paint) ;
				margin = 50 ;//图与图之间的距离
				temp = chart.getX() ;
				System.out.println("temp:"+temp);
			}
	}
	
	public void drawline(Canvas canvas){
		
//			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
//			paint.setStyle(Style.STROKE);
			paint.setColor(Color.GRAY);
			paint.setStrokeWidth(1);
			//虚线
//			PathEffect effects = new DashPathEffect(new float[] { 5, 5, 5, 5}, 1);
//			paint.setPathEffect(effects);
//			canvas.drawLine(30, 290, 550, 290, paint);
			int y = yheight+margint;
			for (int i = 0; i <= ytotal; i++) {				
				canvas.drawLine(xmarginleft, y, xmarginleft+xwidth, y, paint);
				y -= yvalue ;
			}
			int x = xmarginleft ;
			for (int i = 0; i <= xtotal; i++) {
				canvas.drawLine(x,yheight+margint,x,yheight+margint-yvalue*3, paint);//Y坐标
				x += xStep ;//刻度间隔距离
			}
			
	}
	
	@Override
	public void onDraw(Canvas canvas) {
//		canvas.drawColor(Color.BLACK) ;
//		canvas.drawColor(R.color.ball_white) ;
//		width=getWidth();
//		height= getHeight();
//		Log.d("tt5","w:"+width+" h: "+height);
//		
//		yheight = height - margintop;
//		xvalue = width/(xtotal+1);
//		yvalue = yheight/(ytotal+1);
//		Log.d("tt","map:"+map);
//		dlk = getintfrommap(map);
//		chartwidt = (double)(width - xmarginright-xmarginleft) /(double)dlk.size();//(24 *12);
//		xStep =  (double)(width - xmarginright-xmarginleft)/4.0f;
//		Log.d("debug3","dlk.size:"+dlk.size());
//		drawAxis(canvas) ;
//		drawChart(canvas) ;
//		drawline(canvas);
		
		if(dataType.equals("day")){
			xmarginleft=dip2px(context,30);
			xstrmarginleft = dip2px(context,50/3);
			Log.d("debugmychart","xmarginleft:"+xmarginleft);
			width=getWidth();
			height= getHeight();
			Log.d("tt5","w:"+width+" h: "+height);
			xwidth = width -width/8;
			yheight = height - marginb;
			xvalue = width/(xtotal+1);
			yvalue = yheight/ytotal;
			Log.d("tt","map:"+map);
			dlk = getintfrommap(map);
			chartwidt = (double)(xwidth /*- xmarginright-xmarginleft*/) /(double)dlk.size();//(24 *12);
			xStep =  (double)(xwidth /*- xmarginright-xmarginleft*/)/4.0f;
			Log.d("debug3","dlk.size:"+dlk.size());
			drawAxis(canvas) ;
			drawChart(canvas) ;
			drawline(canvas);
        }else if(dataType.equals("week")){
        	this.xstrother=xstr;
        	this.xcount = xstr.length;
        	dlkother = getintfrommap(map);
        	dlkother2 = getintfrommap(map2);
        	datasum = 7;
        	datacount = 30;
        	marginr = dip2px(context,100);
        	xtype = 7;
        	MyOnDraw(canvas);
        	
        }else {
        	this.xstrother=xstr;
        	this.xcount = xstr.length;
        	dlkother = getintfrommap(map);
        	dlkother2 = getintfrommap(map2);
        	datasum = 5;
        	datacount = 31;
        	marginr = 5;
        	xtype = 1;
        	MyOnDraw(canvas);
        }
		
	}
	
	 @SuppressWarnings("rawtypes")
	    public ArrayList<Double> getintfrommap(HashMap<Double, Double> map)
	    {
	        ArrayList<Double> dlk=new ArrayList<Double>();
	        int position=0;
	        if(map==null)
	            return null;
	        Set set= map.entrySet();   
	        Iterator iterator = set.iterator();
	  
	         while(iterator.hasNext())
	        {   
	            @SuppressWarnings("rawtypes")
	            Map.Entry mapentry  = (Map.Entry)iterator.next();   
	            Log.d("debug3","mapentry.getKey():"+mapentry.getKey());
	            dlk.add((Double) mapentry.getKey());
	        } 
	         for(int i=0;i<dlk.size();i++)
	         {
	             int j=i+1;  
	                position=i;  
	                Double temp=dlk.get(i);  
	                for(;j<dlk.size();j++)
	                {  
	                    if(dlk.get(j)<temp)
	                    {  
	                        temp=dlk.get(j);  
	                        position=j;  
	                    }  
	                }  
	                 
	                dlk.set(position,dlk.get(i)); 
	                dlk.set(i,temp);  
	         }
	        return dlk;
	         
	    }
	 
	 private void MyOnDraw(Canvas canvas){
		 if(c!=0)
	            this.setbg(c);
	        if(resid!=0)
	            this.setBackgroundResource(resid);
	        int height=getHeight();
	        if(bheight==0)
	            bheight=height-marginb;
	        Log.d("tt","height:"+height+" bheight:"+bheight);
	        int width=getWidth();
	        int blwidh=dip2px(context,30);
	        Log.d("debugmychart","blwidh:"+blwidh);
//	        int pjsize=totalvalue/pjvalue +1;//界面布局的尺寸的比例
	        int pjsize= 5;
	        xmargin =(width-marginr-blwidh)/datacount ;//点与点在x轴上的间隔
	        Log.d("MyChart","xmargin:"+xmargin+"  (width-marginr-blwidh)/24="+(width-marginr-blwidh)/24);
	        xvalue = (width-marginr-blwidh)/(datacount);
//	        int pjsize=8;//界面布局的尺寸的比例
//	        totalvalue = pjsize * 1000;
	        // set up paint  
	        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);  
	        paint.setColor(Color.GRAY);  
	        paint.setStrokeWidth(1);  
	        paint.setStyle(Style.STROKE);
	        for(int i=0;i<= pjsize;i++)//将顶点的线变为红色的  警戒线
	        {
//	            if(i==pjsize){
//	            	paint.setColor(Color.RED);
////	            	continue;
//	            }
//	            pjvalue = 1;
	            canvas.drawLine(blwidh,bheight-(bheight/pjsize)*i+margint,blwidh+xvalue*(xcount-1)*datasum,bheight-(bheight/pjsize)*i+margint, paint);//Y坐标
	            drawline(deepsleep*i/60+ystrother, blwidh/2, bheight-(bheight/pjsize)*i+margint, canvas);
	        }
	        ArrayList<Integer> xlist=new ArrayList<Integer>();//记录每个x的值
	        ArrayList<Integer> xlist2=new ArrayList<Integer>();//记录每个x的值
	        //画直线（纵向）
	        paint.setColor(Color.GRAY);
	        if(dlkother==null)
	            return;
	        if(dlkother2==null)
	            return;
	        canvas.drawLine(0,margint,0,bheight+margint, paint);
	        Log.d("tt","dlkother.size:"+dlkother.size());
	        Log.d("tt","dlkother.size:"+dlkother.size());
	        for(int i=0;i<dlkother.size();i++)
	        {
//	            xlist.add((blwidh+(width-blwidh)/dlk.size())*i);
	            xlist.add(blwidh+xmargin*i*xtype);
//	            if(isylineshow)
//	            {
//	            	if(i % 6 == 0){
//	            		canvas.drawLine(blwidh+xvalue*i,margint,blwidh+xvalue*i,bheight+margint, paint);
//	            	}
//	                
//	            }
////	            if(i % 2 == 0){
//	            	drawline(dlk.get(i) * 6+xstr, blwidh+(width-marginr-blwidh)/(xcount-1)*i, bheight+marginb/2, canvas);//X坐标参数
////	            }
	            
	        }
	        for(int i=0;i<dlkother2.size();i++)
	        {
//	            xlist.add((blwidh+(width-blwidh)/dlk.size())*i);
	            xlist2.add(blwidh+xmargin*i*xtype);
//	            if(isylineshow)
//	            {
//	            	if(i % 6 == 0){
//	            		canvas.drawLine(blwidh+xvalue*i,margint,blwidh+xvalue*i,bheight+margint, paint);
//	            	}
//	                
//	            }
////	            if(i % 2 == 0){
//	            	drawline(dlk.get(i) * 6+xstr, blwidh+(width-marginr-blwidh)/(xcount-1)*i, bheight+marginb/2, canvas);//X坐标参数
////	            }
	            
	        }
	        for(int i=0;i<= xcount-1;i++){
	        	if(true){
	        		
	            		canvas.drawLine(blwidh+xvalue*i*datasum,margint,blwidh+xvalue*i*datasum,bheight+margint, paint);
	            		
	            }
	        	drawline(xstrother[i], blwidh+xvalue*i*datasum, bheight+marginb/2, canvas);//X坐标参数
	        }
	        //点的操作设置
	        Log.d("debug_sleep","dlkother:"+dlkother+"  map:"+map+" lightsleep:"+lightsleep);
	        mPoints=getpoints(dlkother, map, xlist, lightsleep, bheight);
	        mPoints2=getpoints(dlkother2, map2, xlist2, lightsleep, bheight);

//	        paint.setColor(Color.GRAY);  
	        paint.setARGB(255, 122, 80, 140);
	        paint.setStyle(Style.STROKE);
	        paint.setStrokeWidth(3);
	        drawscrollline(mPoints, canvas, paint);
	        
	        paint.setARGB(255, 135, 11, 46);
//	        paint.setColor(Color.RED);  
	        paint.setStyle(Style.STROKE);
	        paint.setStrokeWidth(3);
	        drawscrollline(mPoints2, canvas, paint);
//	        if(mstyle==Mstyle.Curve)
//	            drawscrollline(mPoints, canvas, paint);
//	        else
//	            drawline(mPoints, canvas, paint);
	         
//	        paint.setColor(Color.RED);  
//	        paint.setStyle(Style.FILL);  
	        for (int i=0; i<mPoints.length; i++)
	        {  	
	        	paint.setARGB(255, 122, 80, 140);
	        	paint.setStyle(Style.FILL);
	            canvas.drawRect(pointToRect(mPoints[i]),paint);  
	        }  
	        for (int i=0; i<mPoints2.length; i++)
	        {  
	        	paint.setARGB(255, 135, 11, 46);
	        	paint.setStyle(Style.FILL);
	            canvas.drawRect(pointToRect(mPoints2[i]),paint);  
	        } 
	 }
	 
//	 @Override 
//	    public boolean onTouchEvent(MotionEvent event) 
//	    {  
//	        switch (event.getAction()) 
//	        {  
//	        case MotionEvent.ACTION_DOWN:  
//	            for (int i=0; i<mPoints.length; i++)
//	            {  
//	                if (pointToRect(mPoints[i]).contains(event.getX(),event.getY()))
//	                {  
//	                    System.out.println("-yes-"+i);
//	                    mSelectedPoint = mPoints[i];  
//	                }  
//	            }  
//	            break;  
//	        case MotionEvent.ACTION_MOVE:  
//	            if ( null != mSelectedPoint)
//	            {   
////	                mSelectedPoint.x = (int) event.getX();  
//	                mSelectedPoint.y = (int) event.getY();  
////	                invalidate();  
//	            }  
//	            break;  
//	        case MotionEvent.ACTION_UP:  
//	            mSelectedPoint = null;  
//	            break;  
//	        default:  
//	            break;  
//	        }         
//	        return true;  
//	           
//	    }  
	      
	    
	    private RectF pointToRect(Point p)
	    {  
	        return new RectF(p.x -RECT_SIZE/2, p.y - RECT_SIZE/2,p.x + RECT_SIZE/2, p.y + RECT_SIZE/2);             
	    }  
	 
	     
	    private void drawscrollline(Point[] ps,Canvas canvas,Paint paint)
	    {
	        Point startp=new Point();
	        Point endp=new Point();
	        for(int i=0;i<ps.length-1;i++)
	        {
	            startp=ps[i];
	            endp=ps[i+1];
	            int wt=(startp.x+endp.x)/2;
	            Point p3=new Point();
	            Point p4=new Point();
	            p3.y=startp.y;
	            p3.x=wt;
	            p4.y=endp.y;
	            p4.x=wt;
	             
	            Path path = new Path();  
	            path.moveTo(startp.x,startp.y); 
	            path.cubicTo(p3.x, p3.y, p4.x, p4.y,endp.x, endp.y); 
	            canvas.drawPath(path, paint);
	             
	        }
	    }
	 
	     
	    private void drawline(Point[] ps,Canvas canvas,Paint paint)
	    {
	        Point startp=new Point();
	        Point endp=new Point();
	        for(int i=0;i<ps.length-1;i++)
	        {   
	        startp=ps[i];
	        endp=ps[i+1];
	        canvas.drawLine(startp.x,startp.y,endp.x,endp.y, paint);
	        }
	    }
	     
	   
	    private Point[] getpoints(ArrayList<Double> dlk,HashMap<Double, Double> map,ArrayList<Integer> xlist,double max,double h)
	    {
	        Point[] points=new Point[dlk.size()];
	        for(int i=0;i<dlk.size();i++)
	        {
	        	Log.d("debug7"," mapget "+map.get(dlk.get(i))+" h "+h+" max "+max);
	        	double value = map.get(dlk.get(i));
	        	int ph=(int) (h-(int)(h*value/max));
	            Log.d("debug7","ph "+ph);
	            points[i]=new Point(xlist.get(i),ph+margint);
	        }
	        return points;
	    }
	     
	    
	    private void drawline(String text,int x,int y,Canvas canvas)
	    {
	        Paint p = new Paint();
	        p.setColor(Color.GRAY);
	        p.setAlpha(0x0000ff);   
	        p.setTextSize(dip2px(context,7));   
//	        String familyName = "宋体";   
//	        Typeface font = Typeface.create(familyName,Typeface.ITALIC);   
//	        p.setTypeface(font);   
	        p.setTextAlign(Paint.Align.CENTER);     
	        canvas.drawText(text, x, y, p);
	    }
	 
	 
	    public  int dip2px(Context context, float dpValue) 
	    {
	        final float scale = context.getResources().getDisplayMetrics().density;
	        return (int) (dpValue * scale + 0.5f);
	    }
	     
	    
	    public  int px2dip(Context context, float pxValue) 
	    {
	        final float scale = context.getResources().getDisplayMetrics().density;
	        return (int) (pxValue / scale + 0.5f);
	    }
	     
	     
	     
	    @SuppressWarnings("rawtypes")
	    public ArrayList<Integer> getintfrommapother(HashMap<Integer, Integer> map)
	    {
	        ArrayList<Integer> dlk=new ArrayList<Integer>();
	        int position=0;
	        if(map==null)
	            return null;
	        Set set= map.entrySet();   
	        Iterator iterator = set.iterator();
	  
	         while(iterator.hasNext())
	        {   
	            @SuppressWarnings("rawtypes")
	            Map.Entry mapentry  = (Map.Entry)iterator.next();   
	            Log.d("tt","mapentry.getKey():"+mapentry.getKey());
	            dlk.add((Integer)mapentry.getKey());
	        } 
	         for(int i=0;i<dlk.size();i++)
	         {
	             int j=i+1;  
	                position=i;  
	                Integer temp=dlk.get(i);  
	                for(;j<dlk.size();j++)
	                {  
	                    if(dlk.get(j)<temp)
	                    {  
	                        temp=dlk.get(j);  
	                        position=j;  
	                    }  
	                }  
	                 
	                dlk.set(position,dlk.get(i)); 
	                dlk.set(i,temp);  
	         }
	        return dlk;
	         
	    }
	 public void setbg(int c)
	    {
	        this.setBackgroundColor(c);
	    }
	     
	    public HashMap<Double, Double> getMap() {
	        return map;
	    }
	 
	    public void setMap(HashMap<Double, Double> mapother) {
	        this.map = mapother;
	    }
	 
	    public double getTotalvalue() {
	        return lightsleep;
	    }
	 
	    public void setTotalvalue(int totalvalue) {
	        this.lightsleep = totalvalue;
	    }
	 
	    public double getPjvalue() {
	        return deepsleep;
	    }
	 
	    public void setPjvalue(int pjvalue) {
	        this.deepsleep = deepsleep;
	    }
	 
	 
	    public String getYstr() {
	        return ystrother;
	    }
	 
	    public void setYstr(String ystrother) {
	        this.ystrother = ystrother;
	    }
	 
	    public int getMargint() {
	        return margint;
	    }
	 
	    public void setMargint(int margint) {
	        this.margint = margint;
	    }
	 
	    public Boolean getIsylineshow() {
	        return isylineshow;
	    }
	 
	    public void setIsylineshow(Boolean isylineshow) {
	        this.isylineshow = isylineshow;
	    }
	 
	    public int getMarginb() {
	        return marginb;
	    }
	 
	    public void setMarginb(int marginb) {
	        this.marginb = marginb;
	    }
	 
	    public Mstyle getMstyle() {
	        return mstyle;
	    }
	 
	    public void setMstyle(Mstyle mstyle) {
	        this.mstyle = mstyle;
	    }
	 
	    public int getBheight() {
	        return bheight;
	    }
	 
	    public void setBheight(int bheight) {
	        this.bheight = bheight;
	    }
	 
	    public int getC() {
	        return c;
	    }
	 
	    public void setC(int c) {
	        this.c = c;
	    }
	 
	    public int getResid() {
	        return resid;
	    }
	 
	    public void setResid(int resid) {
	        this.resid = resid;
	    }
}
