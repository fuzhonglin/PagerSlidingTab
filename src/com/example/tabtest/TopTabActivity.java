package com.example.tabtest;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TopTabActivity extends Activity {
	
	private String[] mTitles=new String[]{"tab1","tab2","tab3","tab4","tab5","tab6","tab7","tab8","tab9","tab10"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_top);
		ViewPager pager = (ViewPager) findViewById(R.id.pager_top);
		PagerSlidingTab tab = (PagerSlidingTab) findViewById(R.id.tab_top);
		
		pager.setAdapter(new MyAdapter());
		
		tab.setBackgroundColor(0xffcccccc);
		tab.setTabTextSize(15, 15);
		tab.setTabTextColor(0xff000000, 0xff00ff00);
		tab.setTabPadding(10, 5);
		
		tab.setViewPager(pager);
		
		tab.setIndicator(8, 0xffff0000);
		tab.setUnderline(1, 0xff00ff00);
		tab.setDivider(1, 0xff00ff00);
		tab.setDividerPadding(5);
		
	}
	
	private class MyAdapter extends PagerAdapter{
		
		@Override
		public int getCount() {
			return mTitles.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return mTitles[position];
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = ViewPager.inflate(getApplicationContext(), R.layout.tab_pager, null);
			TextView text = (TextView) view.findViewById(R.id.text);
			text.setText(mTitles[position]);
			container.addView(view);
			return view;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}
	}

}
