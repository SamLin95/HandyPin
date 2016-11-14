package edu.slin77gatech.handypin.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.slin77gatech.handypin.R;

/**
 * Created by slin on 10/23/16.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    PinComment[] mCommentsData;

    public CommentsAdapter(PinComment[] mCommentsData) {
        this.mCommentsData = mCommentsData;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_card, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        PinComment comment = mCommentsData[position];
        holder.mCommentUsername.setText(comment.getUsername());
        holder.mCommentText.setText(comment.getContent());
        holder.mComentTime.setText(comment.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return mCommentsData.length;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView mUserImg;
        TextView mCommentUsername;
        TextView mCommentText;
        TextView mComentTime;
        public CommentViewHolder(View itemView) {
            super(itemView);
            mUserImg = (ImageView)itemView.findViewById(R.id.comment_user_img);
            mCommentUsername = (TextView) itemView.findViewById(R.id.comment_user_name);
            mCommentText = (TextView) itemView.findViewById(R.id.comment_text);
            mComentTime = (TextView) itemView.findViewById(R.id.comment_time);
        }
    }
}
