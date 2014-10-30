package com.fastfox.watchassistant;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fastfox.watchtest.R;

public class TitleBarView extends RelativeLayout implements OnClickListener {
	private TextView mTextTitle;
	private Button mBtnBack;
	private Context mContext;

	public TitleBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		LayoutInflater.from(mContext).inflate(R.layout.view_title_bar, this,
				true);
		mTextTitle = (TextView) findViewById(R.id.tv_title);
		mBtnBack = (Button) findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(this);
		TypedArray a = mContext.obtainStyledAttributes(attrs,
				R.styleable.titlebar);

		if (a != null) {
			int n = a.getIndexCount();
			for (int i = 0; i < n; i++) {
				int attr = a.getIndex(i);
				switch (attr) {
				case R.styleable.titlebar_bar_text:
					CharSequence title = a.getText(attr);
					mTextTitle.setText(title);
					break;
				case R.styleable.titlebar_bar_btn_back:
					int visibleLeft = a.getInt(
							R.styleable.titlebar_bar_btn_back, 1);
					switch (visibleLeft) {
					case 0:
						mBtnBack.setVisibility(View.INVISIBLE);
						break;
					case 1:
						mBtnBack.setVisibility(View.VISIBLE);
						break;
					case 2:
						mBtnBack.setVisibility(View.GONE);
						break;
					}
					break;
				}
			}
			a.recycle();
		}
//		setBackgroundResource(R.drawable.bg_titlebar);
		setBackgroundColor(mContext.getResources().getColor(R.color.titlebgcolor));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_back) {
			((Activity) mContext).finish();
		}
	}

	public final void setTitleText(CharSequence text) {
		this.mTextTitle.setText(text);
	}

	public final void setTitleText(int resid) {
		this.mTextTitle.setText(resid);
	}

	public final void setBackButtonVisibility(int visibility) {
		this.mBtnBack.setVisibility(visibility);
	}

}
