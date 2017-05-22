package cn.lemon.restdemo.main;

import android.content.Context;
import android.view.ViewGroup;

import cn.alien95.restdemo.R;
import cn.lemon.resthttp.view.HttpImageView;
import cn.lemon.view.adapter.BaseViewHolder;
import cn.lemon.view.adapter.RecyclerAdapter;

/**
 * Created by linlongxin on 2016/5/13.
 */
public class ImageAdapter extends RecyclerAdapter<String> {

    public ImageAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder<String> onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(parent);
    }

    private class ImageViewHolder extends BaseViewHolder<String>{

        private HttpImageView imageView;

        public ImageViewHolder(ViewGroup parent) {
            super(parent, R.layout.item_image);
        }

        @Override
        public void onInitializeView() {
            super.onInitializeView();
            imageView = (HttpImageView) itemView.findViewById(R.id.image_view);
        }

        @Override
        public void setData(String object) {
            super.setData(object);
            imageView.setImageUrl(object);
        }
    }
}
