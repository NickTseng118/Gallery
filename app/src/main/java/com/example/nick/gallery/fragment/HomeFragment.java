package com.example.nick.gallery.fragment;


import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.nick.gallery.MainActivity;
import com.example.nick.gallery.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.nick.gallery.activity.PreviewImageActivity.Args.SELECTED_IMAGE;

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    @BindView(R.id.image_view_picture)
    AppCompatImageView mPictureImageView;
    @BindView(R.id.image_view_placeholder)
    AppCompatImageView mPlaceHolderImageView;
    @BindView(R.id.btn_album)
    AppCompatButton mAlbumButton;

    private AnimationDrawable mRefreshAnimationDrawable;

    public static HomeFragment newInstance(String imageUri) {
        HomeFragment homeFragment = new HomeFragment();
        if (TextUtils.isEmpty(imageUri)) {
            return homeFragment;
        }
        Bundle args = new Bundle();
        args.putString(SELECTED_IMAGE, imageUri);
        homeFragment.setArguments(args);
        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        Log.d(TAG, "onActivityCreated) getArguments) " + args);
        if (args != null && args.containsKey(SELECTED_IMAGE)) {
            // set image
            String imageUri = (String) args.getString(SELECTED_IMAGE);
            Log.d(TAG, "onActivityCreated) imageUri) " + imageUri);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop();
            Glide.with(getContext())
                    .load(imageUri)
                    .apply(requestOptions)
                    .into(mPictureImageView);
        } else {
            // animation
            mPlaceHolderImageView.setVisibility(View.VISIBLE);
            mPictureImageView.setVisibility(View.GONE);
            mPlaceHolderImageView.setBackgroundResource(R.drawable.ig_refresh);
            mRefreshAnimationDrawable = (AnimationDrawable) mPlaceHolderImageView.getBackground();
            mRefreshAnimationDrawable.start();
        }

        // go to album page
        mAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).pushFragment(AlbumFragment.newInstance());
                }
            }
        });
    }
}
