package com.example.instagram;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class GridPostsAdapter extends RecyclerView.Adapter<GridPostsAdapter.ViewHolder> {
    private List<Post> posts;
    private List<Like> liked;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    GridPostsAdapter(Context context, List<Post> posts, List<Like> likes) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.posts = posts;
        this.liked = likes;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return posts.size();
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivPostPhoto;

        ViewHolder(View itemView) {
            super(itemView);
            ivPostPhoto = itemView.findViewById(R.id.ivPostPhoto);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        public void bind(Post post) {
            // Bind the post data to the view elements
            ParseFile image = post.getImage();
            if (image != null) {
                // load post image
                Glide.with(context).load(image.getUrl()).into(ivPostPhoto);

                // photo goes to detail page when clicked
                ivPostPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toDetails(post);
                    }
                });
            }
        }

        public void toDetails(Post post){
            String likeStatus = "empty";
            for(Like like : liked){
                if(like.getPost().getObjectId().equals(post.getObjectId())){
                   likeStatus = "filled";
                }
            }

            // open Details fragment
            FragmentTransaction fragmentTransaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, PostDetailsFragment.newInstance(post, likeStatus));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
