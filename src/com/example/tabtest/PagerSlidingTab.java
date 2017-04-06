package com.example.tabtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PagerSlidingTab extends HorizontalScrollView {
	
	private LinearLayout mTabsContainer;//用于容纳页签的控件
	private ViewPager mPager;//和当前页签控件关联的ViewPager

	private int mIndicatorColor = 0x00000000; //页签滚动线颜色
	private int mUnderlineColor = 0xffff0000; //页签下划线颜色
	private int mDividerColor = 0x00000000; //页签分割线颜色
	private int mIndicatorHeight = 0; //页签滚动线高度
	private int mUnderlineHeight = 0; //页签下划线高度
	private int mDividerPadding = 0; //页签分割线内边距
	private int mDividerWidth = 0; //页签分割线宽度
	
	private int mScrollOffset = dip2px(60); //页签起始位置的偏移量
	private int mTabHorizontalPadding = dip2px(12); //每个标题的左右内边距
	private int mTabVerticalPadding = dip2px(5); //每个标题的上下内边距
	private int mCurrentPosition = 0;//当前页签位置
	private int mSelectPosition = 0;//选中的页签位置
	private int mTabCount;//页签中标题的数量

	private int mTabTextSize = 12; //标题文字的大小
	private int mSelectedTabTextSize = 15; //选中标题文字的大小
	private int mTabTextColor = 0xFF666666; //标题文字的颜色
	private int mSelectedTabTextColor = 0xFFFF6666; //被选中时的，标题文字的颜色
	
	private int mLastScrollX = 0; //页签滚动到的最终位置
	private float mCurrentPositionOffset;//当前页签滚动的百分比
	private boolean mScrollable = true;//当前页签是否可滚动
	
	private int[] mDefaultIconIds;//页签中标题未被选中时的图标id数组
	private int[] mSelectIconIds;//页签中标题被选中时的图标id数组
	private String[] mPagerTitles;//页签中的标题数组
	
	private Paint mRectPaint; //用于画矩形的画笔
	private Paint mDividerPaint; //用于画线条的画笔
	
	private OnPageChangeListener mPageListener;//页面变动监听类
	
	//当需要使用带图标的页签时，必须实现此类
	public interface IconTabProvider {
		
		/**此方法用于获取未被选中时的图标样式的id*/
		public int getDefaultIcon(int position);
		
		/**此方法用于获取被选中时的图标样式的id*/
		public int getSelectIcon(int position);
	}
	
	/**此方法用于设置页面变动的监听类*/
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mPageListener = listener;
	}

	public PagerSlidingTab(Context context) {
		this(context, null);
	}

	public PagerSlidingTab(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerSlidingTab(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setFillViewport(true);//ScrollView中方法，当设置为true时，ScrollView中的子控件会填满其内部空间
		setWillNotDraw(false);//当需要调用Draw方法绘制自己的画面时，需要设置该方法的参数为false
		
		initTabsContainer();
		initPaint();
	}

	/**设置一个横向的线性布局，用于放置标签*/
	private void initTabsContainer() {
		mTabsContainer = new LinearLayout(getContext());
		mTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
		mTabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(mTabsContainer);
	}
	
	/**初始化画笔工具*/
	private void initPaint() {
		
		//初始化矩形画笔
		if(mRectPaint== null){
			mRectPaint = new Paint();
		}
		mRectPaint.setAntiAlias(true);//设置抗锯齿
		mRectPaint.setStyle(Style.FILL);//设置画笔类型为填充

		//初始化线性画笔
		if(mDividerPaint == null){
			mDividerPaint = new Paint();
		}
		mDividerPaint.setAntiAlias(true);//设置抗锯齿
		mDividerPaint.setStrokeWidth(mDividerWidth);//设置画笔宽度
		
	}

	//为标签设置相关联的ViewPager，在这之前需要保证此ViewPager设置了数据适配器
	public void setViewPager(ViewPager pager) {
		
		mTabsContainer.removeAllViews();
		mPager = pager;
		
		//如果该ViewPager没有设置适配器，则抛异常
		if (mPager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}
		
		mTabCount = mPager.getAdapter().getCount();
		mDefaultIconIds = new int[mTabCount];
		mSelectIconIds = new int[mTabCount];
		mPagerTitles = new String[mTabCount];
		
		for (int i=0; i<mTabCount; i++) {
			//通过判断adapter是否实现了IconTabProvider，确定该页签是否带有图标
			if (mPager.getAdapter() instanceof IconTabProvider) {
				mScrollable = false;
				mDefaultIconIds[i] = ((IconTabProvider)mPager.getAdapter()).getDefaultIcon(i);
				mSelectIconIds[i] = ((IconTabProvider)mPager.getAdapter()).getSelectIcon(i);
				mPagerTitles[i] = mPager.getAdapter().getPageTitle(i).toString();
				//添加标题
				addTextTab(i, mPagerTitles[i]);
			} else {
				mScrollable = true;
				mPagerTitles[i] = mPager.getAdapter().getPageTitle(i).toString();
				//添加标题
				addTextTab(i, mPagerTitles[i]);
			}
		}
		
		//为相关联的ViewPager设置页面变化监听类
		mPager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				mCurrentPositionOffset = positionOffset;
				mCurrentPosition = position;
				//当为可滚动的页签时，需要根据页面的滑动而滚动
				if(mScrollable){
					scrollToChild(position, (int)(positionOffset * mTabsContainer.getChildAt(position).getWidth()));			
					invalidate();
				}
				
				//接口回调
				if(mPageListener!=null){
					mPageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				//当停止滑动且为可滚动的页签时，需根据当前页面滚动
				if(state == ViewPager.SCROLL_STATE_IDLE && mScrollable){
					scrollToChild(mPager.getCurrentItem(), 0);
					invalidate();
				}
				
				//接口回调
				if(mPageListener!=null){
					mPageListener.onPageScrollStateChanged(state);
				}
			}
			
			@Override
			public void onPageSelected(int position) {
				mSelectPosition = position;
				//当为可滚动的页签时，需要根据当前页面的位置而滚动
				if(mScrollable) {
					scrollToChild(position, 0);
				}
				//根据选定页面的位置，调整标题的文字和图标样式
				updateTabStyle();
				
				//接口回调
				if(mPageListener!=null){
					mPageListener.onPageSelected(position);
				}
			}
		});
	}

	//该方法用于对标题中的文字和图标的样式进行更新
	protected void updateTabStyle() {
		for(int i=0; i<mTabCount; i++){
			TextView child = (TextView) mTabsContainer.getChildAt(i);
			if(child == null){
				continue;
			}
			if(i == mSelectPosition){
				//当标题被选中时设置如下样式
				child.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSelectedTabTextSize);
				child.setTextColor(mSelectedTabTextColor);
				if(!mScrollable){
					//如果设置了标题的图标，则按下类方式设置标题的图标
					Drawable selectIcon = getResources().getDrawable(mSelectIconIds[i]);
					selectIcon.setBounds(0, 0, selectIcon.getMinimumWidth(), selectIcon.getMinimumHeight());
					child.setCompoundDrawables(null, selectIcon, null, null);
				}
			}else{
				//当标题未被选中时，按下列方式设置标题的样式
				child.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTabTextSize);
				child.setTextColor(mTabTextColor);
				if(!mScrollable){
					//当设置了标题的图标时，按下列方式设置标题的图标
					Drawable defaultIcon = getResources().getDrawable(mDefaultIconIds[i]);
					defaultIcon.setBounds(0, 0, defaultIcon.getMinimumWidth(), defaultIcon.getMinimumHeight());
					child.setCompoundDrawables(null, defaultIcon, null, null);
				}
			}
		}
	}

	//该方法用于生成标题对象并添加到页签中
	private void addTextTab(int position, String title) {
		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();
		
		//当标题被选中时设置如下样式
		if(position == mCurrentPosition){
			tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSelectedTabTextSize);
			tab.setTextColor(mSelectedTabTextColor);
			if(!mScrollable){
				//如果设置了标题的图标，则按下类方式设置标题的图标
				Drawable selectIcon = getResources().getDrawable(mSelectIconIds[position]);
				if(selectIcon!=null){
					selectIcon.setBounds(0, 0, selectIcon.getMinimumWidth(), selectIcon.getMinimumHeight());
					tab.setCompoundDrawables(null, selectIcon, null, null);
				}
			}
		}else{
			//当标题未被选中时设置如下样式
			tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTabTextSize);
			tab.setTextColor(mTabTextColor);
			if(!mScrollable){
				//如果设置了标题的图标，则按下类方式设置标题的图标
				Drawable defaultIcon = getResources().getDrawable(mDefaultIconIds[position]);
				if(defaultIcon!=null){
					defaultIcon.setBounds(0, 0, defaultIcon.getMinimumWidth(), defaultIcon.getMinimumHeight());
					tab.setCompoundDrawables(null, defaultIcon, null, null);
				}
			}
		}
		addTab(position, tab);
	}
	
	//根据当前页签是否可以滚动，向页签中添加标题
	private void addTab(final int position, View tab) {
		//可滚动的Params
		LinearLayout.LayoutParams defaultTabLayoutParams = new LinearLayout.LayoutParams(
									  LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
		//充满父控件，即不可以滚动
		LinearLayout.LayoutParams expandedTabLayoutParams = new LinearLayout.LayoutParams(
													   0, LayoutParams.MATCH_PARENT, 1.0f);
		//设置为可获取焦点
		tab.setFocusable(true);
		//设置标题的点击事件
		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//点击后ViewPager跳转到对应页面
				mPager.setCurrentItem(position, false);
			}
		});
		
		//设置标题的内边距
		tab.setPadding(mTabHorizontalPadding, mTabVerticalPadding, mTabHorizontalPadding, mTabVerticalPadding);
		mTabsContainer.addView(tab, position, mScrollable?defaultTabLayoutParams:expandedTabLayoutParams);
	}

	//实现控件的滚动事件
	private void scrollToChild(int position, int offset) {
		if (mTabCount == 0) {
			return;
		}

		int newScrollX = mTabsContainer.getChildAt(position).getLeft() + offset;

		if (position > 0 || offset > 0) {
			newScrollX -= mScrollOffset;
		}

		if (newScrollX != mLastScrollX) {
			mLastScrollX = newScrollX;
			scrollTo(newScrollX, 0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		if(mScrollable){
			//对isInEditMode()的判断，可以避免可视化编辑器无法识别自定义控件的错误
			if (isInEditMode() || mTabCount == 0) {
				return;
			}
			//获取当前控件的高度
			final int height = (getHeight()==0?getMeasuredHeight():getHeight());

			//画控件的下划线，实际是画一个矩形
			mRectPaint.setColor(mUnderlineColor);
			//四个参数依次为，距左侧距离，距上侧距离，距右侧距离，距下侧距离
			canvas.drawRect(0, height - mUnderlineHeight, mTabsContainer.getWidth(), height, mRectPaint);

			//画滚动线
			mRectPaint.setColor(mIndicatorColor);
			//获取当前Tab的左右边距
			View currentTab = mTabsContainer.getChildAt(mCurrentPosition);
			float lineLeft = currentTab.getLeft();
			float lineRight = currentTab.getRight();

			//获取滚动线的左右边距
			if (mCurrentPositionOffset>0f && mCurrentPosition<mTabCount-1) {

				View nextTab = mTabsContainer.getChildAt(mCurrentPosition + 1);
				final float nextTabLeft = nextTab.getLeft();
				final float nextTabRight = nextTab.getRight();

				lineLeft = (mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset) * lineLeft);
				lineRight = (mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset) * lineRight);
			}

			canvas.drawRect(lineLeft, height - mIndicatorHeight, lineRight, height, mRectPaint);

			//画分割线
			mDividerPaint.setColor(mDividerColor);
			for (int i = 0; i < mTabCount - 1; i++) {
				View tab = mTabsContainer.getChildAt(i);
				if(tab!=null){
					canvas.drawLine(tab.getRight(), mDividerPadding, tab.getRight(), height - mDividerPadding,
							mDividerPaint);
				}
			}
		}
	}
	
	/**该方法用于将dip值转换为px值*/
	private int dip2px(float dip) {
		float density = getContext().getResources().getDisplayMetrics().density;
		return (int) (dip * density + 0.5f);
	}
	
	/**该方法用于改变标题样式*/
	private void changeTabStyles() {
		for (int i = 0; i < mTabCount; i++) {
			View view = mTabsContainer.getChildAt(i);
			if(view==null) continue;
			if (view instanceof TextView) {
				TextView tab = (TextView) view;
				if (i == mSelectPosition) {
					tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSelectedTabTextSize);
					tab.setTextColor(mSelectedTabTextColor);
				}else{
					tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTabTextSize);
					tab.setTextColor(mTabTextColor);
				}
			}
		}
	}

	/**
	 * 设置滚动线的样式
	 * @param indicatorLineHeight
	 * 		滚动线的高度，单位为dp
	 * @param indicatorColor
	 * 		滚动线的颜色值（16进制）
	 */
	public void setIndicator(int indicatorLineHeight, int indicatorColor) {
		mIndicatorHeight = dip2px(indicatorLineHeight);
		mIndicatorColor = indicatorColor;
		invalidate();
	}
	
	/**
	 * 设置下划线的样式
	 * @param underlineHeight
	 * 		下划线的高度，单位为dp
	 * @param underlineColor
	 * 		下划线的颜色值（16进制）
	 */
	public void setUnderline(int underlineHeight, int underlineColor) {
		mUnderlineHeight = dip2px(underlineHeight);
		mUnderlineColor = underlineColor;
		invalidate();
	}
	
	/**
	 * 设置分割线的样式
	 * @param dividerWidth
	 * 		分割线的宽度，单位为dp
	 * @param dividerColor
	 * 		分割线的颜色值（16进制）
	 */
	public void setDivider(int dividerWidth, int dividerColor){
		mDividerWidth = dip2px(dividerWidth);
		mDividerColor = dividerColor;
		invalidate();
	}

	/**
	 * 设置分割线上下的边距
	 * @param dividerPadding
	 * 		上下边距值，单位为dp
	 */
	public void setDividerPadding(int dividerPadding) {
		mDividerPadding = dip2px(dividerPadding);
		invalidate();
	}

	/**
	 * 该方法用于设置标题文字的大小，单位为sp
	 * @param textSize
	 * 		未选中时文字的大小
	 * @param selectedTextSize
	 * 		选中时文字的大小
	 */
	public void setTabTextSize(int textSize, int selectedTextSize) {
		mTabTextSize = textSize;
		mSelectedTabTextSize = selectedTextSize;
		changeTabStyles();
	}
	
	/**
	 * 该方法用于设置文字的颜色
	 * @param textColor 
	 * 		未选中时颜色值的id值
	 * @param selectedTextColor 
	 * 		选中时的颜色值的id值
	 */
	public void setTabTextColorResId(int textColor, int selectedTextColor) {
		mTabTextColor = getResources().getColor(textColor);
		mSelectedTabTextColor = getResources().getColor(selectedTextColor);
		changeTabStyles();
	}

	/**
	 * 该方法用于设置文字的颜色
	 * @param textColor 
	 * 		未选中时颜色值
	 * @param selectedTextColor 
	 * 		选中时的颜色值
	 */
	public void setTabTextColor(int textColor, int selectedTextColor) {
		mTabTextColor = textColor;
		mSelectedTabTextColor = selectedTextColor;
		changeTabStyles();
	}

	/**
	 * 该方法用于设置标题的内边距，单位为dp
	 * @param leftAndRightPadding
	 * 		标题的左右边距
	 * @param topAndBottomPadding
	 * 		标题的上下边距
	 */
	public void setTabPadding(int horizontalPadding, int verticalPadding) {
		mTabHorizontalPadding = dip2px(horizontalPadding);
		mTabVerticalPadding = dip2px(verticalPadding);
		changeTabStyles();
	}
	
}
