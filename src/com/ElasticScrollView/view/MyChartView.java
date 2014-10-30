package com.ElasticScrollView.view;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fastfox.watchtest.R;
 
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
 
/**
 * 
 * 
 *
 */
public class MyChartView extends View{
    public static final int RECT_SIZE = 6;  
    private Point mSelectedPoint;  
     
    //枚举实现坐标桌面的样式风格
    public static enum Mstyle
    {
        Line,Curve
    }
 
    private Mstyle mstyle=Mstyle.Line;
    private Point[] mPoints = new Point[100];  
       
    Context context;
    int bheight=0;
    HashMap<Double, Double> map;
    ArrayList<Double> dlk;
    int totalvalue=30;
    private int pjvalue=1;
    String ystr="";//纵纵坐标的属性
    String xstr[];//横坐标单位
    int margint=15;
    int marginb=40;
    int marginr = 80;
    int c=0;
    int resid=0;
    int xmargin;
    Boolean isylineshow;
    int xcount = 5;
    int xvalue;//x平均值
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
    String week[];/* = new String[] {
 		   "周一","周二","周三","周四","周五","周六","周日"
    };*/
    String month[];/* = new String[] {
    		   "1日","6日","12日","18日","24日","31日"
       };*/
    public void SetTuView(HashMap<Double, Double> map,int totalvalue,int pjvalue,String datatype,String ystr,Boolean isylineshow) 
    {
        this.map=map;
        this.totalvalue=totalvalue;
        this.pjvalue=pjvalue;
        if(datatype.equals("day")){
        	this.xstr=day;
        	this.xcount = day.length;
        	datasum = 24/4;
        	datacount = 24;
        	marginr = 60;
        	xtype = 1;
        }else if(datatype.equals("week")){
        	this.xstr=week;
        	this.xcount = week.length;
        	datasum = 7;
        	datacount = 30;
        	marginr = dip2px(context,100);
        	Log.d("debug30","marginr_px:"+marginr);
        	xtype = 7;
        }else {
        	this.xstr=month;
        	this.xcount = month.length;
        	datasum = 5;
        	datacount = 31;
        	marginr = 5;
        	xtype = 1;
        }
//        this.xstr=xstr;
        this.ystr=ystr;
        this.isylineshow=isylineshow;
        Log.d("tt","pjvalue:"+pjvalue);
        //屏幕横向
//        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }  
 
    public MyChartView(Context ct)
    {
        super(ct);
        this.context=ct;
    }
     
    public MyChartView(Context ct, AttributeSet attrs)
    {
        super( ct, attrs );
        this.context=ct;
    }
     
    public MyChartView(Context ct, AttributeSet attrs, int defStyle) 
    {        
        super( ct, attrs, defStyle );
        this.context=ct;
    }
     
    @SuppressLint("DrawAllocation")
    @Override 
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas); 
        
        int height=getHeight();
        int width=getWidth();
        Log.d("debug30","heigt:"+height+"width:"+width);
        week= new String[] {
        		context.getString(R.string.monday),context.getString(R.string.tuesday),context.getString(R.string.wednesday),
        		context.getString(R.string.thursday),context.getString(R.string.friday),context.getString(R.string.saturday),
        		context.getString(R.string.sunday)
         };
        month = new String[] {
     		   "1"+context.getString(R.string.day),"6"+context.getString(R.string.day),"11"+context.getString(R.string.day),
     		   "16"+context.getString(R.string.day),"21"+context.getString(R.string.day),"26"+context.getString(R.string.day)
     		   ,"31"+context.getString(R.string.day)
        };
        if(c!=0)
            this.setbg(c);
        if(resid!=0)
            this.setBackgroundResource(resid);
        dlk=getintfrommap(map);
        
        
        if(bheight==0)
            bheight=height-marginb;
        Log.d("tt","height:"+height+" bheight:"+bheight);
        
        Log.d("debug30","heigt_dp:"+px2dip(context,height)+"width_dp:"+px2dip(context,width));
        int blwidh=dip2px(context,30);
//        int pjsize=totalvalue/pjvalue +1;//界面布局的尺寸的比例
        int pjsize= 4;
        xmargin =(width-marginr-blwidh)/datacount ;//点与点在x轴上的间隔
        Log.d("MyChart","xmargin:"+xmargin+"  (width-marginr-blwidh)/24="+(width-marginr-blwidh)/24);
        xvalue = (width-marginr-blwidh)/(datacount);
//        int pjsize=8;//界面布局的尺寸的比例
//        totalvalue = pjsize * 1000;
        // set up paint  
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);  
        paint.setColor(Color.GRAY);  
        paint.setStrokeWidth(1);  
        paint.setStyle(Style.STROKE);
        for(int i=0;i<= pjsize;i++)//将顶点的线变为红色的  警戒线
        {
//            if(i==pjsize){
//            	paint.setColor(Color.RED);
////            	continue;
//            }
//            pjvalue = 1;
            canvas.drawLine(blwidh,bheight-(bheight/pjsize)*i+margint,blwidh+xvalue*(xcount-1)*datasum,bheight-(bheight/pjsize)*i+margint, paint);//Y坐标
            drawline(pjvalue*i+ystr, blwidh/2, bheight-(bheight/pjsize)*i+margint, canvas);
        }
        ArrayList<Integer> xlist=new ArrayList<Integer>();//记录每个x的值
        //画直线（纵向）
        paint.setColor(Color.GRAY);
        if(dlk==null)
            return;
        canvas.drawLine(0,margint,0,bheight+margint, paint);
        Log.d("tt","dlk.size:"+dlk.size());
        for(int i=0;i<dlk.size();i++)
        {
//            xlist.add((blwidh+(width-blwidh)/dlk.size())*i);
            xlist.add(blwidh+xmargin*i*xtype);
//            if(isylineshow)
//            {
//            	if(i % 6 == 0){
//            		canvas.drawLine(blwidh+xvalue*i,margint,blwidh+xvalue*i,bheight+margint, paint);
//            	}
//                
//            }
////            if(i % 2 == 0){
//            	drawline(dlk.get(i) * 6+xstr, blwidh+(width-marginr-blwidh)/(xcount-1)*i, bheight+marginb/2, canvas);//X坐标参数
////            }
            
        }
        for(int i=0;i<= xcount-1;i++){
        	if(isylineshow){
        		
            		canvas.drawLine(blwidh+xvalue*i*datasum,margint,blwidh+xvalue*i*datasum,bheight+margint, paint);
            		
            }
        	drawline(xstr[i], blwidh+xvalue*i*datasum, bheight+marginb/2, canvas);//X坐标参数
        }
        //点的操作设置
        mPoints=getpoints(dlk, map, xlist, totalvalue, bheight);
        paint.setARGB(255, 231, 140, 48);
//        paint.setColor(Color.WHITE);  
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(3);
         
        if(mstyle==Mstyle.Curve)
            drawscrollline(mPoints, canvas, paint);
        else
            drawline(mPoints, canvas, paint);
         
        paint.setColor(Color.RED);  
        paint.setStyle(Style.FILL);  
        for (int i=0; i<mPoints.length; i++)
        {  
            canvas.drawRect(pointToRect(mPoints[i]),paint);  
        }  
    }  
 
    @Override 
    public boolean onTouchEvent(MotionEvent event) 
    {  
        switch (event.getAction()) 
        {  
        case MotionEvent.ACTION_DOWN:  
            for (int i=0; i<mPoints.length; i++)
            {  
                if (pointToRect(mPoints[i]).contains(event.getX(),event.getY()))
                {  
                    System.out.println("-yes-"+i);
                    mSelectedPoint = mPoints[i];  
                }  
            }  
            break;  
        case MotionEvent.ACTION_MOVE:  
            if ( null != mSelectedPoint)
            {   
//                mSelectedPoint.x = (int) event.getX();  
                mSelectedPoint.y = (int) event.getY();  
//                invalidate();  
            }  
            break;  
        case MotionEvent.ACTION_UP:  
            mSelectedPoint = null;  
            break;  
        default:  
            break;  
        }         
        return true;  
           
    }  
      
    
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
     
   
    private Point[] getpoints(ArrayList<Double> dlk,HashMap<Double, Double> map,ArrayList<Integer> xlist,int max,int h)
    {
        Point[] points=new Point[dlk.size()];
        for(int i=0;i<dlk.size();i++)
        {
        	double value = map.get(dlk.get(i));
            int ph=h-(int)(h*value/max);
            points[i]=new Point(xlist.get(i),ph+margint);
        }
        return points;
    }
     
    
    private void drawline(String text,int x,int y,Canvas canvas)
    {
        Paint p = new Paint();
        p.setColor(Color.GRAY) ;
        p.setAlpha(0x0000ff);   
        p.setTextSize(dip2px(context,7));  
//        String familyName = "宋体";   
//        Typeface font = Typeface.create(familyName,Typeface.ITALIC);   
//        p.setTypeface(font);   
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
            Log.d("tt","mapentry.getKey():"+mapentry.getKey());
            dlk.add((Double)mapentry.getKey());
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
 
     
     
     
    public void setbg(int c)
    {
        this.setBackgroundColor(c);
    }
     
    public HashMap<Double, Double> getMap() {
        return map;
    }
 
    public void setMap(HashMap<Double, Double> map) {
        this.map = map;
    }
 
    public int getTotalvalue() {
        return totalvalue;
    }
 
    public void setTotalvalue(int totalvalue) {
        this.totalvalue = totalvalue;
    }
 
    public int getPjvalue() {
        return pjvalue;
    }
 
    public void setPjvalue(int pjvalue) {
        this.pjvalue = pjvalue;
    }
 
 
    public String getYstr() {
        return ystr;
    }
 
    public void setYstr(String ystr) {
        this.ystr = ystr;
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