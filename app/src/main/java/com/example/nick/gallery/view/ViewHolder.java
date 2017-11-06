package com.example.nick.gallery.view;

import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nick.gallery.R;
import java.lang.ref.WeakReference;

public class ViewHolder extends RecyclerView.ViewHolder {

  private SparseArray<View> mViews;
  private int mLayoutId;
  private Object mTag;
  public ViewHolder(View itemView, ViewGroup parent, OnRecyclerViewItemClickListener listener) {
    super(itemView);
    mViews = new SparseArray<View>();
    itemView.setTag(R.id.tag_viewholder, this);

    if (listener != null) {
      itemView.setOnClickListener(new OnViewHolderItemClickListener(listener));
    }
  }

  public ViewHolder(@LayoutRes int itemLayoutId, @NonNull ViewGroup parent,
                      OnRecyclerViewItemClickListener listener) {
    this(LayoutInflater.from(parent.getContext()).inflate(itemLayoutId, parent, false), parent,
            listener);
    mLayoutId = itemLayoutId;
  }

  public ViewHolder(@LayoutRes int itemLayoutId, @NonNull ViewGroup parent) {
    this(itemLayoutId, parent, null);
  }

  public <T extends View> T getView(@IdRes int viewId) {
    View view = mViews.get(viewId);
    if (view == null) {
      view = itemView.findViewById(viewId);
      mViews.put(viewId, view);
    }
    return (T) view;
  }


  public ViewHolder setText(@IdRes int viewId, CharSequence text) {
    TextView tv = getView(viewId);
    tv.setText(text);
    return this;
  }


  public ViewHolder setImageResource(@IdRes int viewId, @DrawableRes int resId) {
    ImageView view = getView(viewId);
    view.setImageResource(resId);
    return this;
  }

  public ViewHolder setImageUri(@IdRes int viewId, Uri uri) {
    ImageView view = getView(viewId);
    view.setImageURI(uri);
    return this;
  }

  public ViewHolder setVisibility(@IdRes int viewId, int visibility) {
    View view = getView(viewId);
    view.setVisibility(visibility);
    return this;
  }

  public View getConvertView() {
    return itemView;
  }

  public interface OnRecyclerViewItemClickListener extends View.OnClickListener {

    void onItemClick(ViewHolder holder, int position);

    @Override
    void onClick(View v);
  }

  private static class OnViewHolderItemClickListener implements View.OnClickListener {

    private WeakReference<OnRecyclerViewItemClickListener> mListener;

    public OnViewHolderItemClickListener(OnRecyclerViewItemClickListener listener) {
      mListener = new WeakReference<>(listener);
    }

    @Override
    public void onClick(View v) {
      OnRecyclerViewItemClickListener listener = mListener.get();

      ViewHolder holder = (ViewHolder) v.getTag(R.id.tag_viewholder);
      if (holder == null || listener == null) {
        return;
      }

      final int position = holder.getAdapterPosition();
      if (position == RecyclerView.NO_POSITION) {
        return;
      }

      if (listener != null) {
        listener.onItemClick(holder, position);
      }
    }
  }

}