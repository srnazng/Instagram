package com.example.instagram;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private TextView tvName;
    private TextView tvBio;
    private ImageView ivProfileImage;
    private ImageView ivEditProfileImage;
    private ImageView ivEdit;
    private EditText etName;
    private EditText etBio;
    private ImageView ivFinish;

    public static final int GET_FROM_GALLERY = 3;

    private List<Post> allPosts;
    private List<Like> allLikes;

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
    public static ProfileFragment newInstance(ParseUser user) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allPosts = new ArrayList<>();
        allLikes = new ArrayList<>();

        if (getArguments() != null) {
            user = getArguments().getParcelable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        getUserPosts();

        // bind with view
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        ParseFile image =  user.getParseFile("profileImage");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfileImage);
        }
        else{
            Glide.with(this).load(R.drawable.prof).apply(RequestOptions.circleCropTransform()).into(ivProfileImage);
        }
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileUsername.setText(user.getUsername());
        tvName = view.findViewById(R.id.tvName);
        if(user.get("name") != null){
            tvName.setText(user.get("name").toString());
        }
        tvBio = view.findViewById(R.id.tvBio);
        if(user.get("bio") != null){
            tvBio.setText(user.get("bio").toString());
        }

        RecyclerView recyclerView = view.findViewById(R.id.rvPostGrid);
        ivEditProfileImage = view.findViewById(R.id.ivEditProfileImage);
        ivFinish = view.findViewById(R.id.ivFinish);
        ivEdit = view.findViewById(R.id.ivEdit);
        etBio = view.findViewById(R.id.etBio);
        etName = view.findViewById(R.id.etName);

        // allow editing permissions if on current user's profile
        if(user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
            // edit profile image
            ivEditProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
                }
            });

            // edit bio or name
            ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvBio.setVisibility(view.INVISIBLE);
                    tvName.setVisibility(view.INVISIBLE);
                    ivEdit.setVisibility(view.GONE);
                    etBio.setText(tvBio.getText());
                    etName.setText(tvName.getText());
                    etBio.setVisibility(view.VISIBLE);
                    etName.setVisibility(view.VISIBLE);
                    ivFinish.setVisibility(view.VISIBLE);
                }
            });

            ivFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvBio.setVisibility(view.VISIBLE);
                    tvName.setVisibility(view.VISIBLE);
                    ivEdit.setVisibility(view.VISIBLE);

                    etBio.setVisibility(view.INVISIBLE);
                    etName.setVisibility(view.INVISIBLE);
                    ivFinish.setVisibility(view.GONE);

                    user.put("bio", etBio.getText().toString());
                    user.put("name", etName.getText().toString());
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null){
                                Log.e(TAG, "Error while saving", e);
                                Toast.makeText(getActivity(), "Error while saving", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Log.i(TAG, "Profile save was successful");
                            tvBio.setText(etBio.getText().toString());
                            tvName.setText(etName.getText().toString());
                        }
                    });
                }
            });
        }
        else{
            ivEditProfileImage.setVisibility(view.GONE);
            ivFinish.setVisibility(view.GONE);
            ivEdit.setVisibility(view.GONE);
            if(user.get("bio") == null){
                etBio.setVisibility(view.GONE);
                tvBio.setVisibility(view.GONE);
            }
        }

        // set up the RecyclerView
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        adapter = new GridPostsAdapter(getActivity(), allPosts, allLikes);
        recyclerView.setAdapter(adapter);

        // styling of columns in grid
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

    // convert bitmap image to ParseFile
    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    // after upload new profile image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                Glide.with(this).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(ivProfileImage);
                user.put("profileImage", conversionBitmapParseFile(bitmap));
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null){
                            Log.e(TAG, "Error while saving", e);
                            Toast.makeText(getActivity(), "Error while saving", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(TAG, "Profile save was successful");
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getUserPosts(){
        getLiked();
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class).whereEqualTo("user", user);
        Log.i(TAG, user.getObjectId());
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

    // get liked photos
    private void getLiked () {
        // specify what type of data we want to query - Like.class
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        // include data referred by user key
        query.include(Like.KEY_USER).whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());

        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Like>() {
            @Override
            public void done(List<Like> likes, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // save received posts to list and notify adapter of new data
                allLikes.clear();
                allLikes.addAll(likes);
            }
        });
    }

}