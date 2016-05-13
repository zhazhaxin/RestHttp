package cn.alien95.resthttplibrary;

import android.content.Context;
import android.view.ViewGroup;

import cn.alien95.resthttp.view.HttpImageView;
import cn.alien95.view.adapter.BaseViewHolder;
import cn.alien95.view.adapter.RecyclerAdapter;

/**
 * Created by linlongxin on 2016/5/13.
 */
public class ImageAdapter extends RecyclerAdapter<String> {

    public ImageAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder<String> onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(parent,R.layout.item_image);
    }


    class ImageViewHolder extends BaseViewHolder<String>{

        private HttpImageView imageView;

        public ImageViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
            imageView = (HttpImageView) itemView.findViewById(R.id.image_view);
        }

        @Override
        public void setData(String object) {
            super.setData(object);
            imageView.setImageUrl(object);
        }
    }
}
