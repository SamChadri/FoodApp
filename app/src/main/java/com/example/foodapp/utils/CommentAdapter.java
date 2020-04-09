package com.example.foodapp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.fooddata.Post.Comment;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentAdapterHolder> {

    private ArrayList<Comment> _comments;



    public CommentAdapter(ArrayList<Comment> comments){
        _comments = comments;
    }

    public static class CommentAdapterHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public TextView comment;
        public TextView loadMoreComments;

        public CommentAdapterHolder(View view){
            super(view);
            username = view.findViewById(R.id.commentUsername);
            comment = view.findViewById(R.id.commentText);
            loadMoreComments = view.findViewById(R.id.showRepliesText);
        }
    }

    @Override
    public CommentAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);

        CommentAdapterHolder holder = new CommentAdapterHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(CommentAdapterHolder viewHolder, int position){
        viewHolder.username.setText(_comments.get(position).username);
        viewHolder.comment.setText(_comments.get(position).comment);

    }

    @Override
    public int getItemCount(){
        return _comments.size();
    }
}
