package com.example.instagram;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseUser;
import com.parse.ParseObject;

@ParseClassName("Comment")
public class Comment extends ParseObject{
    public static final String KEY_POST = "post";
    public static final String KEY_USER = "user";
    public static final String KEY_TEXT = "text";

    public Comment() {}

    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public Post getPost() { return (Post) getParseObject(KEY_POST); }

    public void setPost(Post post) { put(KEY_POST, post); }

    public String getText() { return getString(KEY_TEXT); }

    public void setText(String text) { put(KEY_TEXT, text); }
}
