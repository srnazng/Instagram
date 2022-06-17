package com.example.instagram;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostDetailsFragment extends Fragment {
    private TextView tvTimestamp;
    private TextView tvDescription;
    private TextView tvUsername;
    private ImageView ivImage;
    private ImageView ivProfilePhoto;
    private ImageView ivLike;
    private TextView tvLikeNum;

    private Post post;
    private String likeStatus;

    public static final String TAG = "PostDetailsFragment";

    public PostDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostDetailsFragment newInstance(Post post, String likeStatus) {
        PostDetailsFragment fragment = new PostDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("post", post);
        args.putString("likeStatus", likeStatus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_details, container, false);

        Bundle bundle = this.getArguments();
        post = bundle.getParcelable("post");
        likeStatus = bundle.getString("likeStatus");

        ivLike = view.findViewById(R.id.ivLike);
        if (likeStatus.equals("filled")) {
            ivLike.setImageResource(R.drawable.ufi_heart_active);
        } else {
            ivLike.setImageResource(R.drawable.ufi_heart);
        }

        tvLikeNum = view.findViewById(R.id.tvLikeNum);
        // show number of likes
        if (post.getInt("numLikes") == 1) {
            tvLikeNum.setText(post.getInt("numLikes") + " like");
        } else {
            tvLikeNum.setText(post.getInt("numLikes") + " likes");
        }
        tvTimestamp = view.findViewById(R.id.tvTimestamp);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvUsername = view.findViewById(R.id.tvUsername);
        ivImage = view.findViewById(R.id.ivImage);
        if (post.getImage() != null) {
            Glide.with(this).load(post.getImage().getUrl()).into(ivImage);
        }
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        ParseFile profile = post.getUser().getParseFile("profileImage");
        if (profile != null) {
            Glide.with(getActivity()).load(profile.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfilePhoto);
        }
        tvUsername.setText(post.getUser().getUsername());
        tvDescription.setText(Html.fromHtml("<b>" + post.getUser().getUsername() + "</b> " + post.getDescription()));
        tvTimestamp.setText(getRelativeTimeAgo(post.getCreatedAt().toString()));

        // go to post creator's profile
        tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toProfile(post.getUser());
            }
        });
        ivProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toProfile(post.getUser());
            }
        });

        // like or unlike post
        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(likeStatus.equals("filled")){
                    ivLike.setImageResource(R.drawable.ufi_heart);
                    unlike(post);
                    if(post.getInt("numLikes") == 1){
                        tvLikeNum.setText((post.getInt("numLikes")) + " like");
                    }
                    else{
                        tvLikeNum.setText((post.getInt("numLikes")) + " likes");
                    }
                    likeStatus = "empty";
                }
                else{
                    ivLike.setImageResource(R.drawable.ufi_heart_active);
                    like(post);
                    if(post.getInt("numLikes") == 1){
                        tvLikeNum.setText((post.getInt("numLikes")) + " like");
                    }
                    else{
                        tvLikeNum.setText((post.getInt("numLikes")) + " likes");
                    }
                    likeStatus = "filled";
                }
            }
        });

        return view;
    }

    // format time
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
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    // go to profile page of post creator
    public void toProfile(ParseUser user) {
        FragmentTransaction fragmentTransaction = (getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, ProfileFragment.newInstance(user));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void like(Post post) {
        Like entity = new Like();

        entity.put("user", ParseUser.getCurrentUser());
        entity.put("post", post);

        // Saves the new object.
        // Notice that the SaveCallback is totally optional!
        entity.saveInBackground(e -> {
            if (e == null) {
                //Save was done
            } else {
                //Something went wrong
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        post.put("numLikes", post.getInt("numLikes") + 1);

        post.saveInBackground(e -> {
            if (e == null) {
                //Save was done
            } else {
                //Something went wrong
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unlike(Post post) {
        // specify what type of data we want to query - Like.class
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class).whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser()).whereEqualTo(Like.KEY_POST, post);
        // include data referred by user key
        query.include(Like.KEY_USER).whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());

        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Like>() {
            @Override
            public void done(List<Like> likes, com.parse.ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Like like : likes) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Like");

                    // Retrieve the object by id
                    query.getInBackground(like.getObjectId(), (object, e2) -> {
                        if (e2 == null) {
                            //Object was fetched
                            //Deletes the fetched ParseObject from the database
                            object.deleteInBackground(e3 -> {
                                if (e3 == null) {
                                    Toast.makeText(getActivity(), "Delete Successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    //Something went wrong while deleting the Object
                                    Toast.makeText(getActivity(), "Error: " + e3.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            //Something went wrong
                            Toast.makeText(getActivity(), e2.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}