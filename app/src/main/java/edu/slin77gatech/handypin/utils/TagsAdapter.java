package edu.slin77gatech.handypin.utils;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;

import edu.slin77gatech.handypin.MapsActivity;
import edu.slin77gatech.handypin.R;

/**
 * Created by slin on 10/20/16.
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagViewHolder>{

    /**
     * Interface for handling tag click event.
     */

    public interface OnTagClickCallBack {
        public void onTagCheck(String tagString);
        public void onTagUnCheck(String tagString);

    }

    int itemNum;
    List<String> tags;
    WeakReference<OnTagClickCallBack> mActivity;

    public TagsAdapter(List<String> tagSet, OnTagClickCallBack activity) {
        tags = tagSet;
        mActivity = new WeakReference<OnTagClickCallBack>(activity);
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_card, parent, false);
        return new TagViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        holder.setTagBtnListener(mActivity);
    }

    public boolean addTag(String str) {
        tags.add(str);
        notifyDataSetChanged();
        return true;
    }

    public boolean removeTag(String str) {
        boolean ret = tags.remove(str);
        notifyDataSetChanged();
        return ret;
    }

    @Override
    public int getItemCount() {
        return tags.size() + 1;
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {

        EditText inputText;
        CheckBox tagBtn;

        TagViewHolder(View itemView) {
            super(itemView);
            tagBtn = (CheckBox) itemView.findViewById(R.id.add_tag_box);
            inputText = (EditText)itemView.findViewById(R.id.tag_input);
        }

        void setTagBtnListener(WeakReference<OnTagClickCallBack> callBackRef) {
            final OnTagClickCallBack callBack = callBackRef.get();
            if (callBack != null) {
                tagBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String text = inputText.getText().toString();
                        if (isChecked) {
                            callBack.onTagCheck(text);
                        } else {
                            callBack.onTagUnCheck(text);
                        }
                    }
                });
            }
        }
    }
}
