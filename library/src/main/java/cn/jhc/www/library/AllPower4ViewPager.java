package cn.jhc.www.library;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


/**
 * (^_^) (0_0)
 * Created by hbw on 2019/8/7.
 *
 * There is two kind of mode to use OPower4ViewPagerBuilder:
 * 1. "Fragment_based_mode"   it will use the fragment to show the user defined layout
 * 2. "ImageView_based_mode"  it will show the some images based on imageview directly
 */
public class AllPower4ViewPager extends RelativeLayout {

    private static final String TAG = "HBW";

    private LastPageStateListener mLastPageStateListener;

    private final int DEFAULT_HEIGHT = 30;
    private final int DEFAULT_WIDTH = 30;
    private final int DEFAULT_LEFT_MARGIN = 50;
    private final int INIT_DOT_INDEX = 0;

    private int mPageCount;

    private List<Fragment4ViewPager> mFragments = new ArrayList<>();
    private List<ImageView> mImages = new ArrayList<>();
    private int[] mImageIds;
    private String[] mImageUrls;
    private List<ImageView> mDots = new ArrayList<>();

    private Context mContext;
    private ViewPager mViewPager;
    private ViewGroup mDotGroup;
    private RadioGroup mRadioGroup = null;
    private int[] mRadioGroupItems;
    private View mSelf;

    //for radiobutton to switch the viewpager's current item
    public void setCurrentItem(int index)
    {
        mViewPager.setCurrentItem(index);
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {

            if (mDots.size()>0)
                setDotImage(i);

            if (mRadioGroup != null)
                mRadioGroup.check(mRadioGroupItems[i]);

            if (i == mPageCount-1)
                mLastPageStateListener.lastPageSelected(i);
            else
                mLastPageStateListener.lastPageUnSelected(i);

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    private void setDotImage(int i) {
        for (int j = 0; j < mPageCount ; j++) {
            ImageView dot = mDots.get(j);
            dot.setImageResource(j==i? R.drawable.dot_white:R.drawable.dot_black);
        }
    }

    private PagerAdapter mImageAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = mImages.get(position);
            container.addView(imageView);
            return imageView;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mImages.get(position));
        }
    };

    public AllPower4ViewPager show()
    {
        this.addView(mSelf);
        return this;
    }

    public AllPower4ViewPager needIndicateDots(Boolean b, @Nullable int...w_h_leftMargin)
    {
        if (b)
        {
            //init the dots
            for (int i = 0; i <mPageCount ; i++) {
                ImageView dot = new ImageView(mContext);
                if (i == INIT_DOT_INDEX)
                    dot.setImageResource(R.drawable.dot_white);
                else
                    dot.setImageResource(R.drawable.dot_black);
                mDots.add(dot);

                LinearLayout.LayoutParams param;
                if (w_h_leftMargin != null && w_h_leftMargin.length>0){
                    param = new LinearLayout.LayoutParams(w_h_leftMargin[0],w_h_leftMargin[1]);
                    param.leftMargin = w_h_leftMargin[2];
                }else {
                    param = new LinearLayout.LayoutParams(DEFAULT_WIDTH,DEFAULT_HEIGHT);
                    param.leftMargin = DEFAULT_LEFT_MARGIN;
                }

                dot.setLayoutParams(param);
                mDotGroup.addView(dot);
            }

            //binding the dots and the viewpager
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        }else{
            //do not init dots, set dots gone
            mDotGroup.setVisibility(GONE);
        }

        return this;
    }

    /**
     * if you want to binding the viewpager with a bottom navigation bar, then you should
     * input  "true" -> "b",
     * and input "the RadioGroup instance" ->  "radioGroup",
     * and input "the RadioButton instance" -> "item_ids"
     *
     * radioGroup is bottom navigation bar
     * item_ids is the RadioButton's resource id (for example: R.id.xx)
     *
     * @param b
     * @param radioGroup
     * @param item_ids
     * @return
     */
    public AllPower4ViewPager needBottomNavibar(Boolean b, @Nullable RadioGroup radioGroup, @Nullable int[] item_ids)
    {
        if (b && radioGroup!= null && item_ids != null)
        {
            mRadioGroup = radioGroup;
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
            mRadioGroupItems = item_ids;
            mRadioGroup.check(item_ids[INIT_DOT_INDEX]);
        }

        return this;
    }

    /**
     * obviously, this function is used to set the views to viewpager, it support three different kind of mode:
     * 1. you can pass the views when you need the "Fragment_based_mode"
     * 2. you can pass the image resources(for example: R.id.xx or URL) when you need "ImageView_based_mode"
     * @param context
     * @param layoutViews
     * @return
     */
    public AllPower4ViewPager setDataSrc(Context context, List<View> layoutViews, LastPageStateListener listener)
    {
        if (listener != null)
            mLastPageStateListener = listener;

        mContext = context;
        initView();
        mPageCount = layoutViews.size();
        for (int i = 0; i < mPageCount; i++) {
            mFragments.add(new Fragment4ViewPager().setLayout(layoutViews.get(i)));
            Log.d(TAG, "setDataSrc: "+i);
        }
        initFragmentViewPager();

        return this;
    }

    public AllPower4ViewPager setDataSrc(Context context, final int[] resIds, LastPageStateListener listener)
    {
        if (listener != null)
            mLastPageStateListener = listener;

        mContext = context;
        mPageCount = resIds.length;
        initView();

        setImages(resIds);

        return this;
    }

    public AllPower4ViewPager setDataSrc(Context context, String[] urls, LastPageStateListener listener)
    {
        if (listener != null)
            mLastPageStateListener = listener;

        mContext = context;
        mPageCount = urls.length;
        initView();

        setImages(urls);

        return this;
    }

    /**
     * this function is used to set the dots' image, the default image is a white dot and a black dot
     * when the viewpager's current item is changed this function will be invoked to reset the dots' image
     * @param resIds
     */
    private void setImages(int[] resIds) {

        mImageIds = resIds;
        for (int i = 0; i < mImageIds.length; i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(mImageIds[i]);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImages.add(imageView);
        }

        mViewPager.setAdapter(mImageAdapter);
    }

    private void setImages(String[] resIds) {

        mImageUrls = resIds;
        for (int i = 0; i < mImageUrls.length; i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(mContext).load(mImageUrls[i]).into(imageView);
            mImages.add(imageView);
        }

        mViewPager.setAdapter(mImageAdapter);
    }

    /**
     * this function is used to do the initial action.
     * "mSelf" represent the layout which OPower4ViewPagerBuilder instance is binding,
     * you need to invoke the method "show()" to add the mSelf to OPower4ViewPagerBuilder instance,
     * otherwise the screen will be blank.
     *
     * you can see the opower4viewpager.xml to help you understand the struct of the layout
     */
    private void initView() {

        mSelf = LayoutInflater.from(mContext).inflate(R.layout.opower4viewpager,null);

        mViewPager = mSelf.findViewById(R.id.viewpager);
        mDotGroup = (ViewGroup) mSelf.findViewById(R.id.dot_group);
    }

    /**
     * this function will be invoked by "setDataSrc(Context context, List<View> layoutViews)"
     * you do not need to invoke it manually
     */
    private void initFragmentViewPager() {
        mViewPager.setAdapter(new FragmentPagerAdapter(((AppCompatActivity)mContext).getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                Log.d(TAG, "getItem: "+i);
                return mFragments.get(i);
            }

            @Override
            public int getCount() {
                Log.d(TAG, "getCount: "+mFragments.size());
                return mFragments.size();
            }
        });
    }


    public static interface LastPageStateListener{

        public void lastPageSelected(int currentIndex);
        public void lastPageUnSelected(int currentIndex);
    }


    /**
     * there is no need to invoke the functions below manually
     *
     */
    public AllPower4ViewPager(Context context) {
        super(context);

    }

    public AllPower4ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AllPower4ViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AllPower4ViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
