package com.example.instagram;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    public static final String TAG = "ProfileFragment";

    private ParseUser user;
    private TextView tvProfileUsername;
    private RecyclerView rvPostGrid;

    private List<Post> allPosts;

    private GridPostsAdapter adapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allPosts = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        getUserPosts();

        // set user
        user = ParseUser.getCurrentUser(); // this will now be null
        if(user == null){
            Intent i = new Intent(getActivity(), LoginActivity.class);
            startActivity(i);
        }

        // bind with view
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileUsername.setText(user.getUsername());
        RecyclerView recyclerView = view.findViewById(R.id.rvPostGrid);

        // set up the RecyclerView
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        adapter = new GridPostsAdapter(getActivity(), allPosts);
        recyclerView.setAdapter(adapter);

        final int gridWidth = recyclerView.getWidth();
        final int cellWidthDP = 50;
        final int cellHeightDP = 80;

        final Resources r = getResources();
        final double cellWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cellWidthDP, r.getDisplayMetrics());
        final double cellHeightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cellHeightDP, r.getDisplayMetrics());

        final int itemsPerRow = (int) Math.floor((double) gridWidth / cellWidthPx);
        final int rowCount = (int) Math.ceil((double) allPosts.size() / itemsPerRow);

        final ViewGroup.LayoutParams prm = recyclerView.getLayoutParams();
        prm.height = (int) Math.ceil((double) rowCount * cellHeightPx);

        recyclerView.setLayoutParams(prm);

        return view;
    }

    public void getUserPosts(){
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class).whereEqualTo("user", ParseUser.getCurrentUser());
        Log.i(TAG, ParseUser.getCurrentUser().getObjectId());
        // include data referred by user key
        query.include(Post.KEY_USER);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }

                // save received posts to list and notify adapter of new data
                allPosts.clear();
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

}