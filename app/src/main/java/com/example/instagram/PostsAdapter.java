package com.example.instagram;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private Context context;
    private List<Post> posts;
    private List<Like> liked;

    public static final String TAG = "Adapter";

    public PostsAdapter(Context context, List<Post> posts, List<Like> liked) {
        this.context = context;
        this.posts = posts;
        this.liked = liked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername;
        private ImageView ivProfile;
        private ImageView ivImage;
        private TextView tvDescription;
        private ImageView ivHeart;
        private ImageView ivSpeechBubble;
        private TextView tvLikes;
        private TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivSpeechBubble = itemView.findViewById(R.id.ivSpeechBubble);
            ivHeart = itemView.findViewById(R.id.ivHeart);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Post post) {
            // Bind the post data to the view elements
            tvDescription.setText(Html.fromHtml("<b>" + post.getUser().getUsername() + "</b> " +  post.getDescription()));
            tvDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toDetails(post, ivHeart.getContentDescription().toString());
                }
            });
            tvUsername.setText(post.getUser().getUsername());
            tvTime.setText(getRelativeTimeAgo(post.getCreatedAt().toString()));

            // go to post creator's profile
            tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toProfile(post.getUser());
                }
            });
            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toProfile(post.getUser());
                }
            });

            // show heart icon based on liked status
            ivHeart.setContentDescription("empty");
            ivHeart.setImageResource(R.drawable.ufi_heart);

            // TODO: better way to search?
            for(Like like : liked){
                if(like.getPost().getObjectId().equals(post.getObjectId())){
                    ivHeart.setContentDescription("filled");
                    ivHeart.setImageResource(R.drawable.ufi_heart_active);
                }
            }

            // show number of likes
            if(post.getInt("numLikes") == 1){
                tvLikes.setText(post.getInt("numLikes") + " like");
            }
            else{
                tvLikes.setText(post.getInt("numLikes") + " likes");
            }

            // like or unlike post
            ivHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ivHeart.getContentDescription().toString().equals("filled")){
                        ivHeart.setImageResource(R.drawable.ufi_heart);
                        unlike(post);
                        if(post.getInt("numLikes") == 1){
                            tvLikes.setText((post.getInt("numLikes")) + " like");
                        }
                        else{
                            tvLikes.setText((post.getInt("numLikes")) + " likes");
                        }
                        ivHeart.setContentDescription("empty");
                    }
                    else{
                        ivHeart.setImageResource(R.drawable.ufi_heart_active);
                        like(post);
                        if(post.getInt("numLikes") == 1){
                            tvLikes.setText((post.getInt("numLikes")) + " like");
                        }
                        else{
                            tvLikes.setText((post.getInt("numLikes")) + " likes");
                        }
                        ivHeart.setContentDescription("filled");
                    }
                }
            });

            // go to comments fragment for post
            ivSpeechBubble.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toComments(post);
                }
            });

            // show image
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);

                ivImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toDetails(post, ivHeart.getContentDescription().toString());
                    }
                });
            }

            // profile image
            ParseFile profile =  post.getUser().getParseFile("profileImage");
            if (profile != null) {
                Glide.with(context).load(profile.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile);
            }
        }

        // go to details fragment of post
        public void toDetails(Post post, String likeStatus){
            FragmentTransaction fragmentTransaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, PostDetailsFragment.newInstance(post, likeStatus));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        // go to profile fragment of post's creator
        public void toProfile(ParseUser user){
            FragmentTransaction fragmentTransaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, ProfileFragment.newInstance(user));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        // go to profile page of post creator
        public void toComments(Post post) {
            FragmentTransaction fragmentTransaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, CommentsFragment.newInstance(post));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        public void like(Post post){
            Like entity = new Like();

            entity.put("user", ParseUser.getCurrentUser());
            entity.put("post", post);

            // Saves the new object.
            // Notice that the SaveCallback is totally optional!
            entity.saveInBackground(e -> {
                if (e==null){
                    //Save was done
                }else{
                    //Something went wrong
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            post.put("numLikes", post.getInt("numLikes") + 1);

            post.saveInBackground(e -> {
                if (e==null){
                    //Save was done
                }else{
                    //Something went wrong
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void unlike(Post post){
            // specify what type of data we want to query - Like.class
            ParseQuery<Like> query = ParseQuery.getQuery(Like.class).whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser()).whereEqualTo(Like.KEY_POST, post);
            // include data referred by user key
            query.include(Like.KEY_USER);

            // start an asynchronous call for posts
            query.findInBackground(new FindCallback<Like>() {
                @Override
                public void done(List<Like> likes, ParseException e) {
                    // check for errors
                    if (e != null) {
                        Log.e(TAG, "Issue with getting posts", e);
                        return;
                    }
                    for(Like like : likes){
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Like");

                        // Retrieve the object by id
                        query.getInBackground(like.getObjectId(), (object, e2) -> {
                            if (e2 == null) {
                                //Object was fetched
                                //Deletes the fetched ParseObject from the database
                                object.deleteInBackground(e3 -> {
                                    if(e3==null){
                                        Log.i(TAG, "delete like successful");
                                    }else{
                                        //Something went wrong while deleting the Object
                                        Log.e(TAG, "delete like failure");
                                    }
                                });
                            }else{
                                //Something went wrong
                                Log.e(TAG, "get likes failure");
                            }
                        });

                    }
                }
            });


            post.put("numLikes", post.getInt("numLikes") - 1);

            post.saveInBackground(e -> {
                if (e==null){
                    //Save was done

                }else{
                    //Something went wrong
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minutes ago";
            } else if (diff < 90 * MINUTE_MILLIS) {
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hours ago";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " days ago";
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return "";
    }
}

