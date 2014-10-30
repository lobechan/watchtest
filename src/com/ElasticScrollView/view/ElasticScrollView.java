package com.ElasticScrollView.view;

import java.util.Date;

import com.fastfox.watchtest.R;
import com.fastfox.watchassistant.SleepActivity;
import com.fastfox.watchassistant.SportsActivity;



import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class ElasticScrollView extends ScrollView {
	private static final String TAG = "ElasticScrollView";
	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	// 实际的padding的距离与界面上偏移距离的比例
	private final static int RATIO = 3;

	private int headContentWidth;
	private int headContentHeight;

	private LinearLayout innerLayout;
	private LinearLayout headView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private OnRefreshListener refreshListener;
	private boolean isRefreshable;
	private int state;
	private boolean isBack;

	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	private boolean canReturn;
	private boolean isRecored;
	private int startY;
	
	private boolean fling;
	private Context context;
	private GestureDetector gesture;
	public ElasticScrollView(Context context) {
		super(context);
		this.context = context;
//		gesture = new GestureDetector(context,new GestureListener());
		init(context);
	}

	public ElasticScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
//		gesture = new GestureDetector(context,new GestureListener());
		init(context);
	}

	

	public void setContext(Context context) {
		this.context = context;
	}

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		innerLayout = new LinearLayout(context);
		innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		
		headView = (LinearLayout) inflater.inflate(R.layout.mylistview_head,
				null);
		
		arrowImageView = (ImageView) headView
				.findViewById(R.id.head_arrowImageView);
		progressBar = (ProgressBar) headView
				.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);
		measureView(headView);

		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();

		Log.i("size", "width:" + headContentWidth + " height:"
				+ headContentHeight);

		innerLayout.addView(headView);
		addView(innerLayout);

		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		state = DONE;
		isRefreshable = false;
		canReturn = false;
	}

	
	public void setGestureDetector(GestureDetector gestureDetector) {  
        this.gesture = gestureDetector;  
    }  
	/*@Override  
    public boolean onTouchEvent(MotionEvent event) {  
        super.onTouchEvent(event);  
        return gesture.onTouchEvent(event);  
    }  
   
    @Override  
    public boolean dispatchTouchEvent(MotionEvent ev){  
    	gesture.onTouchEvent(ev);  
        super.dispatchTouchEvent(ev);  
        return true;  
    }*/   
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
////		gesture.onTouchEvent(event);
//		/*if (false) {
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				if (getScrollY() == 0 && !isRecored) {
//					isRecored = true;
//					startY = (int) event.getY();
//					Log.i(TAG, "---down---");
//				}
//				break;
//			case MotionEvent.ACTION_UP:
////				if(!fling){
////	                scrollToScreen(event);
////					
////	            }
////	            fling = false;
//				if (state != REFRESHING && state != LOADING) {
//					if (state == DONE) {
//						// 什么都不做
//					}
//					if (state == PULL_To_REFRESH) {
//						state = DONE;
//						changeHeaderViewByState();
//						Log.i(TAG, "---pull to done---");
//					}
//					if (state == RELEASE_To_REFRESH) {
//						state = REFRESHING;
//						changeHeaderViewByState();
//						onRefresh();
//						Log.i(TAG, "---up to done---");
//					}
//				}
//				isRecored = false;
//				isBack = false;
//
//				break;
//			case MotionEvent.ACTION_MOVE:
//				int tempY = (int) event.getY();
//				if (!isRecored && getScrollY() == 0) {
//					Log.i(TAG, "---move---");
//					isRecored = true;
//					startY = tempY;
//				}
//
//				if (state != REFRESHING && isRecored && state != LOADING) {
//					// 可以松手去刷新了
//					if (state == RELEASE_To_REFRESH) {
//						canReturn = true;
//
//						if (((tempY - startY) / RATIO < headContentHeight)
//								&& (tempY - startY) > 0) {
//							state = PULL_To_REFRESH;
//							changeHeaderViewByState();
//							Log.i(TAG, "---up to pull---");
//						}
//						// 一下子推到顶了
//						else if (tempY - startY <= 0) {
//							state = DONE;
//							changeHeaderViewByState();
//							Log.i(TAG, "---up to done---");
//						} else {
//							// 不用进行特别的操作，只用更新paddingTop的值就行了
//						}
//					}
//					// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
//					if (state == PULL_To_REFRESH) {
//						canReturn = true;
//
//						// 下拉到可以进入RELEASE_TO_REFRESH的状态
//						if ((tempY - startY) / RATIO >= headContentHeight) {
//							state = RELEASE_To_REFRESH;
//							isBack = true;
//							changeHeaderViewByState();
//							Log.i(TAG, "---done or pull to up---");
//						}
//						// 上推到顶了
//						else if (tempY - startY <= 0) {
//							state = DONE;
//							changeHeaderViewByState();
//							Log.i(TAG, "---done or pull to done---");
//						}
//					}
//
//					// done状态下
//					if (state == DONE) {
//						if (tempY - startY > 0) {
//							state = PULL_To_REFRESH;
//							changeHeaderViewByState();
//						}
//					}
//
//					// 更新headView的size
//					if (state == PULL_To_REFRESH) {
//						headView.setPadding(0, -1 * headContentHeight
//								+ (tempY - startY) / RATIO, 0, 0);
//
//					}
//
//					// 更新headView的paddingTop
//					if (state == RELEASE_To_REFRESH) {
//						headView.setPadding(0, (tempY - startY) / RATIO
//								- headContentHeight, 0, 0);
//					}
//					if (canReturn) {
//						canReturn = false;
//						return true;
//					}
//				}
//				break;
//			}
//		}*/
//		
//		return false;
//	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);

			tipsTextview.setText(R.string.up_refresh);

			Log.i(TAG, "status，up_refresh");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText(R.string.pull_refresh);
			} else {
				tipsTextview.setText(R.string.pull_refresh);
			}
			Log.i(TAG, "status，pull_refresh");
			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText(R.string.refresh+"...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.i(TAG, "status,refresh...");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);

			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.goicon);
			tipsTextview.setText("下拉刷新");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.i(TAG, "当前状态，done");
			break;
		}
	}
	

	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void onRefreshComplete() {
		state = DONE;
		lastUpdatedTextView.setText("最近更新:" + new Date().toLocaleString());
		changeHeaderViewByState();
		invalidate();
		scrollTo(0, 0);
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}

	public void addChild(View child) {
		innerLayout.addView(child);
	}

	public void addChild(View child, int position) {
		innerLayout.addView(child, position);
	}
	
	/**
     * 用来计算拖动一段距离后，要显示哪个界面
     */
    private void scrollToScreen(MotionEvent event){
    	int leftWidth = (int) event.getX();
        int tabs = leftWidth/getWidth();
        int len = leftWidth - tabs*getWidth();
        if(len<getWidth()/2){
//          scrollTo(tabs*getWidth(),0);
//            scroller.startScroll(leftWidth, 0, -len, 0, len*2);
        	Log.d("tt3","---len<getWidth()/2---"+leftWidth);
        	Intent intent = new Intent();
        	intent.setClass(context, SleepActivity.class);
        	context.startActivity(intent);
        	
        }else{
//          scrollTo((tabs+1)*getWidth(),0);
//            scroller.startScroll(leftWidth, 0, getWidth()-len, 0, len*2);
        	Log.d("tt3","---len>=getWidth()/2---"+leftWidth);
            tabs = tabs + 1;
        }
        invalidate();
    }
    class GestureListener extends SimpleOnGestureListener{  
    	   
        @Override 
        public boolean onDoubleTap(MotionEvent e){  
            return super.onDoubleTap(e);  
        }  
   
        @Override 
        public boolean onDown(MotionEvent e){  
            return super.onDown(e);  
        }  
   
        @Override 
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY){  
            if(Math.abs(velocityX)>ViewConfiguration.get(context).getScaledMinimumFlingVelocity()){
                scrollToScreen(e1);
                fling = true;
            }
            return true;
        }  
         
        @Override
        public void onShowPress(MotionEvent e){
            super.onShowPress(e);
        }
         
        @Override 
        public void onLongPress(MotionEvent e){  
            super.onLongPress(e);  
        }  
   
        @Override 
        public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY){  
            if(distanceX>0&&getScrollX()<(getChildCount()-1)*getWidth()||
                    distanceX<0&&getScrollX()>0){
                scrollBy((int)distanceX,0);
            }
            return true;
        }  
   
        @Override 
        public boolean onSingleTapUp(MotionEvent e){  
            return super.onSingleTapUp(e);  
        }
    }
    
}
