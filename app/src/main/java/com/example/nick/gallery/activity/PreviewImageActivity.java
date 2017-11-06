package com.example.nick.gallery.activity;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.nick.gallery.MainActivity;
import com.example.nick.gallery.R;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewImageActivity extends AppCompatActivity {

    private static final String TAG = PreviewImageActivity.class.getSimpleName();

    public interface Args {
        String IMAGE_LIST = "IMAGE_LIST";
        String START_INDEX = "START_INDEX";
        String SELECTED_IMAGE = "SELECTED_IMAGE";
    }

    public interface OnViewClickListener {
        void onImageSelectedListener(View v);
    }

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    private ImagePagerAdapter mImagePagerAdapter;

    public static Intent createLaunchIntent(Context packageContext,
                                            @NonNull ArrayList<Uri> imageList, int startIndex) {
        Intent intent = new Intent(packageContext, PreviewImageActivity.class);
        if (imageList != null && imageList.size() > 0) {
            intent.putExtra(Args.IMAGE_LIST, imageList);
        }
        intent.putExtra(Args.START_INDEX, startIndex);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        int startIndex = intent.getIntExtra(Args.START_INDEX, 0);
        final ArrayList<Uri> imageList =
                (ArrayList<Uri>) intent.getExtras().getSerializable(Args.IMAGE_LIST);
        if (imageList != null && imageList.size() > 0) {
            mImagePagerAdapter = new ImagePagerAdapter(this, imageList);
            mViewPager.setAdapter(mImagePagerAdapter);
            mViewPager.setCurrentItem(startIndex);
            mImagePagerAdapter.setOnViewClickListener(new OnViewClickListener() {
                @Override
                public void onImageSelectedListener(View v) {
                    Uri imageUri = imageList.get((Integer) v.getTag());
                    Log.d(TAG, "onImageSelectedListener) imageUri) " + imageUri);

                    // go to mainActivity
                    deliverSelectedImage(imageUri.toString());
                }
            });
        }
    }

    private void deliverSelectedImage(String imageUri) {
        Intent intent = MainActivity.createMainActivityIntent(this, imageUri);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (mImagePagerAdapter != null) {
            mImagePagerAdapter.setOnViewClickListener(null);
            mImagePagerAdapter = null;
        }
        super.onDestroy();
    }

    private static class ImagePagerAdapter extends PagerAdapter {

        private List<ViewHolder> mViewHolderList = new ArrayList<>();
        private LayoutInflater mInflater;
        private ArrayList<Uri> mImageList;
        private OnViewClickListener mOnViewClickListener;


        public ImagePagerAdapter(Context context, ArrayList<Uri> imageList) {
            mImageList = imageList;
            mInflater = LayoutInflater.from(context);
        }

        public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
            mOnViewClickListener = onViewClickListener;
        }

        public ArrayList<Uri> getImageList() {
            return mImageList;
        }

        @Override
        public int getCount() {
            return mImageList == null ? 0 : mImageList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final ViewHolder viewHolder;
            if (mViewHolderList != null && mViewHolderList.size() > 0) {
                viewHolder = mViewHolderList.get(0);
                mViewHolderList.remove(0);
            } else {
                final View item = mInflater.inflate(R.layout.item_image_view, container, false);
                viewHolder = new ViewHolder(item, mOnViewClickListener);
            }
            viewHolder.selectTextView.setTag(position);
            Uri imageUri = mImageList.get(position);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop();
            Glide.with(container.getContext())
                    .load(imageUri)
                    .apply(requestOptions)
                    .into(viewHolder.imageView);

            container.addView(viewHolder.itemView);
            return viewHolder;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ViewHolder viewHolder = (ViewHolder) object;
            (container).removeView(viewHolder.itemView);
            mViewHolderList.add(viewHolder);
            container.removeView(viewHolder.itemView);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((ViewHolder) object).itemView;
        }

        // This is called when notifyDataSetChanged() is called
        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        public void removeItem(int position) {

        }
    }

    public static class ViewHolder {
        public View itemView;
        @BindView(R.id.iv_image)
        ImageView imageView;
        @BindView(R.id.tv_select)
        TextView selectTextView;

        public ViewHolder(View itemView, OnViewClickListener onViewClickListener) {
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
            setOnViewClickListener(onViewClickListener);
        }

        private void setOnViewClickListener(@NonNull final OnViewClickListener onViewClickListener) {
            selectTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewClickListener != null) {
                        onViewClickListener.onImageSelectedListener(v);
                    }
                }
            });
        }
    }

}
