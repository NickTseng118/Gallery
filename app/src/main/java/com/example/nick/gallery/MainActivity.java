package com.example.nick.gallery;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import com.example.nick.gallery.fragment.HomeFragment;


import butterknife.ButterKnife;

import static com.example.nick.gallery.activity.PreviewImageActivity.Args.SELECTED_IMAGE;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static Intent createMainActivityIntent(Context packageContext,
                                           @NonNull String imageUri) {
        Intent intent = new Intent(packageContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SELECTED_IMAGE, imageUri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String imageUri = null;
        if (intent != null) {
            imageUri = intent.getStringExtra(SELECTED_IMAGE);
        }

        // launch HomeFragment
        pushFragment(HomeFragment.newInstance(imageUri));
    }

    public void pushFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_layout_content, fragment,
                fragment.getClass().getName()).commit();
    }

    @Override
    public void onBackPressed() {
        popFragment();
    }

    public void popFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.d(TAG, "popFragment) getBackStackEntryCount) "
                + fragmentManager.getBackStackEntryCount());
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        // FIXME: find a better way to handle backPressed
        backStackEntryCount --;
        if (backStackEntryCount > 0) {
            fragmentManager.popBackStack();
            return;
        } else {
            finish();
        }
    }
}