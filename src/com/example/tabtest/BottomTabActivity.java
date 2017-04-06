package com.example.tabtest;

import com.example.tabtest.PagerSlidingTab.IconTabProvider;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BottomTabActivity extends Activity {
	
	private int[] mIcons = {R.drawable.newscenter,R.drawable.govaffairs,
			R.drawable.home,R.drawable.smartservice,R.drawable.setting};
	
	private int[] mPressIcons = {R.drawable.newscenter_press,R.drawable.govaffairs_press,
			R.drawable.home_press,R.drawable.smartservice_press,R.drawable.setting_press};
	
	private String[] mTitles = {"标签1","标签2","标签3","标签4","标签5"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bottom);		
		ViewPager pager = (ViewPager) findViewById(R.id.pager_bottom);
		PagerSlidingTab tab = (PagerSlidingTab) findViewById(R.id.tab_bottom);
		
		pager.setAdapter(new MyAdapter());
		tab.setBackgroundResource(R.drawable.bottom_tab_bg);
		tab.setTabTextColor(0xffffffff, 0xffff0000);
		tab.setViewPager(pager);
	}
	
	private class MyAdapter extends PagerAdapter implements IconTabProvider{
		
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
		public int getDefaultIcon(int position) {
			return mIcons[position];
		}

		@Override
		public int getSelectIcon(int position) {
			return mPressIcons[position];
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
