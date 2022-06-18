package com.example.instagram;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentsFragment extends Fragment {
    private Post post;
    private List<Comment> allComments;
    private EditText etComment;
    private Button btnSend;

    private RecyclerView rvComments;
    protected CommentsAdapter adapter;

    public static final String TAG = "CommentsFragment";

    public CommentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentsFragment newInstance(Post post) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable("post", post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post = getArguments().getParcelable("post");
        }
        allComments = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        // set the adapter on the recycler view
        adapter = new CommentsAdapter(getContext(), allComments);
        rvComments = view.findViewById(R.id.rvComments);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));

        etComment = view.findViewById(R.id.etComment);
        btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Comment entity = new Comment();

                if(post == null){
                    Log.e(TAG, "post nuil");
                }
                entity.put("post", post);
                entity.put("user", ParseUser.getCurrentUser());
                entity.put("text", etComment.getText().toString());

                // Saves the new object.
                // Notice that the SaveCallback is totally optional!
                entity.saveInBackground(e -> {
                    if (e==null){
                        //Save was done
                        getComments();
                        etComment.setText("");
                        Log.i(TAG, "comment sent success");
                    }else{
                        //Something went wrong
                        Log.i(TAG, "comment sent fail " + e);
                    }
                });

            }
        });

        getComments();
        return view;
    }

    // get liked photos
    private void getComments () {
        // specify what type of data we want to query - Like.class
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class).whereEqualTo(Comment.KEY_POST, post);
        // include data referred by user key
        query.include(Comment.KEY_USER);

        // start an asynchronous call for comments
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> comments, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments", e);
                    return;
                }
                // save received posts to list and notify adapter of new data
                allComments.clear();
                allComments.addAll(comments);

                for(Comment comment : comments){
                    Log.i(TAG, comment.getText());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

}