package com.example.nick.gallery.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.nick.gallery.MainActivity;
import com.example.nick.gallery.R;
import com.example.nick.gallery.activity.PreviewImageActivity;
import com.example.nick.gallery.view.ViewHolder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.nick.gallery.fragment.AlbumFragment.GalleryViewModel.VIEW_ORIGINAL_SIZE;
import static com.example.nick.gallery.fragment.AlbumFragment.GalleryViewModel.VIEW_SQUARE_SIZE;

public class AlbumFragment extends Fragment {

    private static final String TAG = AlbumFragment.class.getSimpleName();
    private static final int REQUEST_CODE_WRITE_PERMISSION = 999;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 888;
    private static final int REQUEST_CODE_PREVIEW = 777;
    private static final int REQUEST_TAKE_PHOTO = 666;
    private static final int GRID_COLUMN = 3;
    private static final int POSTIVE_CAMERA_VIEW = 0;

    @BindView(R.id.recycle_view_images)
    RecyclerView mImageRecycleView;
    @BindView(R.id.image_view_camera)
    ImageView mCameraImageView;

    private String mCurrentPhotoPath;

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // init ui
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), GRID_COLUMN);
        mImageRecycleView.setHasFixedSize(true);
        mImageRecycleView.setLayoutManager(layoutManager);

        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_PERMISSION);
            Log.e(TAG, "WRITE_EXTERNAL_STORAGE permission isn't granted !!!");
            return;
        }
        setupImageGallery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_PERMISSION) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupImageGallery();
            } else {
               getActivity().finish();
            }
        }

        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (permissions[0].equals(Manifest.permission.CAMERA)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
               return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                if (resultCode == RESULT_OK) {
                    deliverSelectedImage(mCurrentPhotoPath);
                }
                // delete file if user pressed cancel
                else if (resultCode == RESULT_CANCELED) {
                    File f = new File(mCurrentPhotoPath);
                    if (f.exists()) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Uri> loadImageFromDevice() {
        ArrayList<Uri> imageUriList = new ArrayList<>();
        Uri imageUri = null;
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID + " DESC ";
        Cursor imageCursor = getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                orderBy);
        int count = imageCursor.getCount();
        for (int i = 0; i < count; i++) {
            imageCursor.moveToPosition(i);
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String filePath = imageCursor.getString(dataColumnIndex);
            imageUri = Uri.fromFile(new File(filePath));
            imageUriList.add(imageUri);
        }
        imageCursor.close();
        return imageUriList;
    }

    private void setupImageGallery() {
        final ArrayList<Uri> imageUriList = loadImageFromDevice();
        final ArrayList<GalleryViewModel> galleryViewModelList = new ArrayList<>();
        Log.d(TAG, "setupImageGallery) imageUriList size) "
                + imageUriList.size());

        boolean hasImage = imageUriList.size() > 0;

        mCameraImageView.setVisibility(hasImage ? GONE : VISIBLE);
        mImageRecycleView.setVisibility(hasImage ? VISIBLE : GONE);
        if (hasImage == false) {
            mCameraImageView.setVisibility(VISIBLE);
            mImageRecycleView.setVisibility(GONE);
            mCameraImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchCamera();
                }
            });
            Toast.makeText(getContext(), "No images available", Toast.LENGTH_SHORT).show();
            return;
        }

        // setup view model list
        for (Uri uri : imageUriList) {
            galleryViewModelList.add(new GalleryViewModel(VIEW_SQUARE_SIZE, uri.toString()));
        }

        // setup adapter
        GalleryAdapter galleryAdapter = new GalleryAdapter(galleryViewModelList,
                new ViewHolder.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(ViewHolder holder, int position) {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }

                // launch camera
                if (position == POSTIVE_CAMERA_VIEW) {
                    launchCamera();
                    return;
                }

                // preview image
                Intent intent = PreviewImageActivity.createLaunchIntent(getActivity(),
                        imageUriList, position);
                startActivityForResult(intent, REQUEST_CODE_PREVIEW);
            }

            @Override
            public void onClick(View v) {
                // do nothing
            }
        });
        mImageRecycleView.setAdapter(galleryAdapter);
    }

    private void launchCamera() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA_PERMISSION);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.nick.gallery",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
               "Nick/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
               ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void deliverSelectedImage(String selectedImagePath) {
        Intent intent = MainActivity.createMainActivityIntent(getActivity(), selectedImagePath);
        startActivity(intent);
    }

    public static class GalleryViewModel {

        // -----------ViewType Annotation---------------
        public static final int VIEW_SQUARE_SIZE = 0;
        public static final int VIEW_ORIGINAL_SIZE = 1;
        @IntDef({VIEW_SQUARE_SIZE, VIEW_ORIGINAL_SIZE })
        public @interface ViewType {}
        // -------------------------------------------

        private @ViewType int viewType;
        private String imageUri;

        public GalleryViewModel(int viewType, String imageUri) {
            this.viewType = viewType;
            this.imageUri = imageUri;
        }

        public int getViewType() {
            return viewType;
        }

        public String getImageUri() {
            return imageUri;
        }
    }


    private class GalleryAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<GalleryViewModel> mImageList;
        private ViewHolder.OnRecyclerViewItemClickListener mOnClickListener;

        public GalleryAdapter(@NonNull List<GalleryViewModel> imageList,
                              ViewHolder.OnRecyclerViewItemClickListener onClickListener) {
            mImageList = imageList;
            mOnClickListener = onClickListener;

            setCameraIcon();
        }

        public void setCameraIcon() {
            // setup camera option
            Uri photoImageUri = Uri.parse("android.resource://"
                    + getContext().getPackageName() + "/" + R.drawable.ic_photo_camera_white_48dp);
            mImageList.add(0,
                    new GalleryViewModel(VIEW_ORIGINAL_SIZE, photoImageUri.toString()));
        }

        @Override
        public int getItemViewType(int position) {
            return mImageList.get(position).getViewType();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder holder = null;
            if (viewType == VIEW_SQUARE_SIZE) {
                holder = new ViewHolder(R.layout.item_square_image, parent, mOnClickListener);
            } else if (viewType == VIEW_ORIGINAL_SIZE){
                holder = new ViewHolder(R.layout.item_original_image, parent, mOnClickListener);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            GalleryViewModel model = mImageList.get(position);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop();
            Glide.with(getContext())
                    .load(model.getImageUri())
                    .apply(requestOptions)
                    .into((ImageView) holder.getView(R.id.image_view));
        }

        @Override
        public int getItemCount() {
            return mImageList == null ? 0 : mImageList.size();
        }
    }
}
