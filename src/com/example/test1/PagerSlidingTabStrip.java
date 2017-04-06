
package com.example.test1;

import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PagerSlidingTabStrip extends HorizontalScrollView {

	/**
	 * 当想要设置带顶部图标的TextView，需要实现该接口
	 */
	public interface IconTabProvider {
		public int getPageIconResId(int position);
	}

	private static final int[] ATTRS = new int[] { android.R.attr.textSize, android.R.attr.textColor };
	
	private LinearLayout.LayoutParams defaultTabLayoutParams; //当需要滚动时，使用此布局方式
	private LinearLayout.LayoutParams expandedTabLayoutParams; //当不需要滚动（自动充满屏幕）时，使用此布局方式

	private final PageListener pageListener = new PageListener();
	public OnPageChangeListener delegatePageListener;

	private LinearLayout tabsContainer;
	private ViewPager pager;

	private int tabCount;

	private int currentPosition = 0;
	private int selectedPosition = 0;
	private float currentPositionOffset = 0f;

	private Paint rectPaint;
	private Paint dividerPaint;
	
	private int indicatorColor = 0xFF666666; //滚动线颜色
	private int underlineColor = 0x1A000000; //下划线颜色
	private int dividerColor = 0x1A000000; //分割线颜色

	private boolean shouldExpand = false; //是否滚动，也即是否自动充满屏幕，false：不需要充满，true：需要充满
	private boolean textAllCaps = true; //全部字母大写

	private int scrollOffset = 52;
	private int indicatorHeight = 8; //滚动线高度
	private int underlineHeight = 2; //下划线高度
	private int dividerPadding = 12; //分割线内边距
	private int tabPadding = 12; //每个标题的左右内边距
	private int dividerWidth = 1; //分割线宽度

	private int tabTextSize = 12; //标题文字的大小
	private int tabTextColor = 0xFF666666; //标题文字的颜色
	private int selectedTabTextColor = 0xFF666666; //被选中时的，标题文字的颜色
	private Typeface tabTypeface = null; //字体设置
	private int tabTypefaceStyle = Typeface.NORMAL; //字体设置

	private int lastScrollX = 0;

	private int tabBackgroundResId = R.drawable.background_tab;

	private Locale locale; //语言相关类

	public PagerSlidingTabStrip(Context context) {
		this(context, null);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setFillViewport(true);//ScrollView中方法，当设置为true时，ScrollView中的子控件会填满其内部空间
		setWillNotDraw(false);//当需要调用Draw方法绘制自己的画面时，需要设置该方法的参数为false;

		//设置一个横向的线性布局，用于放置标签
		tabsContainer = new LinearLayout(context);
		tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
		tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(tabsContainer);

		//获取手机的分辨率
		DisplayMetrics dm = getResources().getDisplayMetrics();
		//将dp和sp转换为px
		scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
		indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
		underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
		dividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
		tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
		dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
		tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

		//获取属性值：textSize and textColor
		TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
		tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
		tabTextColor = a.getColor(1, tabTextColor);
		a.recycle();

		// 获取自定义的属性值
		a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);
		indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, indicatorColor);
		underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, underlineColor);
		dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, dividerColor);
		indicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight,
				indicatorHeight);
		underlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight,
				underlineHeight);
		dividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding,
				dividerPadding);
		tabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight,
				tabPadding);
		tabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground,
				tabBackgroundResId);
		shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, shouldExpand);
		scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset,
				scrollOffset);
		textAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, textAllCaps);
		a.recycle();
		
		rectPaint = new Paint();
		rectPaint.setAntiAlias(true);//设置抗锯齿
		rectPaint.setStyle(Style.FILL);//设置画笔类型为填充

		dividerPaint = new Paint();
		dividerPaint.setAntiAlias(true);//设置抗锯齿
		dividerPaint.setStrokeWidth(dividerWidth);//设置画笔宽度

		defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT);
		expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

		//获取语言类型
		if (locale == null) {
			locale = getResources().getConfiguration().locale;
		}
	}

	//为标签设置相关联的ViewPager，在这之前需要保证此ViewPager设置了数据适配器
	public void setViewPager(ViewPager pager) {
		this.pager = pager;

		if (pager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}

		pager.setOnPageChangeListener(pageListener);

		notifyDataSetChanged();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.delegatePageListener = listener;
	}

	public void notifyDataSetChanged() {

		tabsContainer.removeAllViews();

		tabCount = pager.getAdapter().getCount();

		for (int i = 0; i < tabCount; i++) {

			if (pager.getAdapter() instanceof IconTabProvider) {
				this.shouldExpand = true;
				addIconTab(i, pager.getAdapter().getPageTitle(i).toString(), ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));
			} else {
				addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
			}

		}

		updateTabStyles();

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeGlobalOnLayoutListener(this);
				currentPosition = pager.getCurrentItem();
				scrollToChild(currentPosition, 0);
			}
		});

	}

	/**
	 * 添加纯文本的TextView
	 * @param position 插入的位置
	 * @param title 图标的文字
	 */
	private void addTextTab(final int position, String title) {

		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();
		addTab(position, tab);
	}

	/**
	 * 添加带有图标的TextView
	 * @param position 插入的位置
	 * @param title 图标的文字
	 * @param resId 图标的图片
	 */
	private void addIconTab(final int position, String title, int resId){ 	
		//添加图标的文字
		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();
		
		//添加图标的图片
		Drawable drawable = getResources().getDrawable(resId);    
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());      
		tab.setCompoundDrawables(null, drawable, null, null);
		
		addTab(position, tab);
	}  

	private void addTab(final int position, View tab) {
		tab.setFocusable(true);
		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.setCurrentItem(position);
			}
		});

		tab.setPadding(tabPadding, 0, tabPadding, 0);
		tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
	}

	private void updateTabStyles() {

		for (int i = 0; i < tabCount; i++) {

			View v = tabsContainer.getChildAt(i);

			v.setBackgroundResource(tabBackgroundResId);

			if (v instanceof TextView) {

				TextView tab = (TextView) v;
				tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
				tab.setTypeface(tabTypeface, tabTypefaceStyle);
				tab.setTextColor(tabTextColor);

				// setAllCaps() is only available from API 14, so the upper case
				// is made manually if we are on a
				// pre-ICS-build
				if (textAllCaps) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
						tab.setAllCaps(true);
					} else {
						tab.setText(tab.getText().toString().toUpperCase(locale));
					}
				}
				if (i == selectedPosition) {
					tab.setTextColor(selectedTabTextColor);
				}
			}
		}

	}

	private void scrollToChild(int position, int offset) {

		if (tabCount == 0) {
			return;
		}

		int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

		if (position > 0 || offset > 0) {
			newScrollX -= scrollOffset;
		}

		if (newScrollX != lastScrollX) {
			lastScrollX = newScrollX;
			scrollTo(newScrollX, 0);
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		//对isInEditMode()的判断，可以避免可视化编辑器无法识别自定义控件的错误
		if (isInEditMode() || tabCount == 0) {
			return;
		}

		//获取当前控件的高度
		final int height = getHeight();

		//画控件的下划线，实际是画一个矩形
		rectPaint.setColor(underlineColor);
		//四个参数依次为，距左侧距离，距上侧距离，距右侧距离，距下侧距离
		canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);

		//画滚动线
		rectPaint.setColor(indicatorColor);
		//获取当前Tab的左右边距
		View currentTab = tabsContainer.getChildAt(currentPosition);
		float lineLeft = currentTab.getLeft();
		float lineRight = currentTab.getRight();

		// if there is an offset, start interpolating left and right coordinates
		// between current and next tab
		//获取滚动线的左右边距
		if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {

			View nextTab = tabsContainer.getChildAt(currentPosition + 1);
			final float nextTabLeft = nextTab.getLeft();
			final float nextTabRight = nextTab.getRight();

			lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
			lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
		}

		canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);

		//画分割线
		dividerPaint.setColor(dividerColor);
		for (int i = 0; i < tabCount - 1; i++) {
			View tab = tabsContainer.getChildAt(i);
			canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding,
					dividerPaint);
		}
	}

	//监听ViewPager页面的变化
	private class PageListener implements OnPageChangeListener {

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			currentPosition = position;//当前页面
			currentPositionOffset = positionOffset;//当前页面移动的百分比
			
			//标签跟随页面的变化移动
			scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));
			
			//重绘布局
			invalidate();
			
			//接口回调
			if (delegatePageListener != null) {
				delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				//标签跟随页面的变化移动
				scrollToChild(pager.getCurrentItem(), 0);
			}

			//接口回调
			if (delegatePageListener != null) {
				delegatePageListener.onPageScrollStateChanged(state);
			}
		}

		@Override
		public void onPageSelected(int position) {
			//当前选中的页面
			selectedPosition = position;
			
			updateTabStyles();
			//接口回调
			if (delegatePageListener != null) {
				delegatePageListener.onPageSelected(position);
			}
		}

	}

	/**
	 * 设置滚动线的颜色
	 * @param indicatorColor 颜色值
	 */
	public void setIndicatorColor(int indicatorColor) {
		this.indicatorColor = indicatorColor;
		invalidate();
	}

	/**
	 * 设置滚动线的颜色
	 * @param resId 颜色的Id值
	 */
	public void setIndicatorColorResource(int resId) {
		this.indicatorColor = getResources().getColor(resId);
		invalidate();
	}

	/**
	 * 设置滚动线的高度
	 * @param indicatorLineHeightPx 像素值
	 */
	public void setIndicatorHeight(int indicatorLineHeightPx) {
		this.indicatorHeight = indicatorLineHeightPx;
		invalidate();
	}

	/**
	 * 设置下划线的颜色
	 * @param underlineColor 颜色值
	 */
	public void setUnderlineColor(int underlineColor) {
		this.underlineColor = underlineColor;
		invalidate();
	}

	/**
	 * 设置下划线的颜色
	 * @param resId 颜色的Id值
	 */
	public void setUnderlineColorResource(int resId) {
		this.underlineColor = getResources().getColor(resId);
		invalidate();
	}
	
	/**
	 * 设置下划线的高度
	 * @param underlineHeightPx 像素值
	 */
	public void setUnderlineHeight(int underlineHeightPx) {
		this.underlineHeight = underlineHeightPx;
		invalidate();
	}

	/**
	 * 设置分割线的颜色
	 * @param dividerColor 颜色值
	 */
	public void setDividerColor(int dividerColor) {
		this.dividerColor = dividerColor;
		invalidate();
	}

	/**
	 * 设置分割线的颜色
	 * @param resId 颜色的Id值
	 */
	public void setDividerColorResource(int resId) {
		this.dividerColor = getResources().getColor(resId);
		invalidate();
	}
	
	/**
	 * 设置分割线的宽度
	 * @param dividerWidthPx
	 */
	public void setDividerWidth(int dividerWidthPx){
		this.dividerWidth = dividerWidthPx;
		invalidate();
	}

	/**
	 * 设置分割线上下的边距
	 * @param dividerPaddingPx 像素值
	 */
	public void setDividerPadding(int dividerPaddingPx) {
		this.dividerPadding = dividerPaddingPx;
		invalidate();
	}

	/**
	 * 设置文字的大小
	 * @param textSizePx 像素值
	 */
	public void setTextSize(int textSizePx) {
		this.tabTextSize = textSizePx;
		updateTabStyles();
	}
	
	/**
	 * 设置文字颜色
	 * @param resId 颜色的Id值
	 */
	public void setTextColorResource(int resId) {
		this.tabTextColor = getResources().getColor(resId);
		updateTabStyles();
	}
	
	/**
	 * 设置文字颜色
	 * @param textColor 颜色值
	 */
	public void setTextColor(int textColor) {
		this.tabTextColor = textColor;
		updateTabStyles();
	}
	
	/**
	 * 设置文字的颜色
	 * @param resId 未选中时的颜色的Id值
	 * @param selectedResId 选中时的颜色的Id值
	 */
	public void setTextColorResource(int resId, int selectedResId) {
		this.tabTextColor = getResources().getColor(resId);
		this.selectedTabTextColor = getResources().getColor(selectedResId);
		updateTabStyles();
	}

	/**
	 * 设置文字的颜色
	 * @param textColor 未选中时颜色值
	 * @param selectedTextColor 选中时的颜色值
	 */
	public void setTextColor(int textColor, int selectedTextColor) {
		this.tabTextColor = textColor;
		this.selectedTabTextColor = selectedTextColor;
		updateTabStyles();
	}

	/**
	 * 设置标题的背景，一般为一个状态选择器
	 * @param resId
	 */
	public void setTabBackground(int resId) {
		this.tabBackgroundResId = resId;
		updateTabStyles();
	}

	/**
	 * 设置标题的左右边距
	 * @param paddingPx
	 */
	public void setTabPaddingLeftRight(int paddingPx) {
		this.tabPadding = paddingPx;
		updateTabStyles();
	}
	
	public boolean isTextAllCaps() {
		return textAllCaps;
	}

	public void setAllCaps(boolean textAllCaps) {
		this.textAllCaps = textAllCaps;
	}
	
	public void setScrollOffset(int scrollOffsetPx) {
		this.scrollOffset = scrollOffsetPx;
		invalidate();
	}
	
	public void setTypeface(Typeface typeface, int style) {
		this.tabTypeface = typeface;
		this.tabTypefaceStyle = style;
		updateTabStyles();
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		currentPosition = savedState.currentPosition;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPosition = currentPosition;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPosition;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPosition = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPosition);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}
