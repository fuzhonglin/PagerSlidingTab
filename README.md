# PagerSlidingTab
该控件可当作标签控件使用，并且有可滚动以及不可滚动两种模式。并且此控件中的标签可以是纯文本，也可以是带有图标。当需要使得标签带有图标时，需要与该空间关联的ViewPager的Adapter实现该控件中的如下接口：interface IconTabProvider。

**该控件具有以下常用方法：**

* public void setViewPager(ViewPager pager)；为标签设置相关联的ViewPager，在这之前需要保证此ViewPager设置了数据适配器。

* public void setIndicator(int indicatorLineHeight, int indicatorColor)；设置滚动线的样式，indicatorLineHeight：滚动线的高度，单位为dp，indicatorColor：滚动线的颜色值。

* public void setUnderline(int underlineHeight, int underlineColor)；设置下划线的样式，underlineHeight：下划线的高度，单位为dp，underlineColor：下划线的颜色值。

* public void setDivider(int dividerWidth, int dividerColor)；设置分割线的样式，dividerWidth：分割线的宽度，单位为dp，dividerColor：分割线的颜色值。

* public void setDividerPadding(int dividerPadding)；设置分割线上下的边距，dividerPadding：上下边距值，单位为dp。

* public void setTabTextSize(int textSize, int selectedTextSize)；该方法用于设置标题文字的大小，单位为sp，textSize：未选中时文字的大小，selectedTextSize：选中时文字的大小。

* public void setTabTextColorResId(int textColor, int selectedTextColor)；该方法用于设置文字的颜色，textColor，未选中时颜色值的id值，selectedTextColor：选中时的颜色值的id值。

* public void setTabTextColor(int textColor, int selectedTextColor)；该方法用于设置文字的颜色，textColor：未选中时颜色值，selectedTextColor：选中时的颜色值。

* public void setTabPadding(int horizontalPadding, int verticalPadding)；该方法用于设置标题的内边距，单位为dp，horizontalPadding：标题的左右边距，verticalPadding：标题的上下边距。
	
