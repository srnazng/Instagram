package com.example.instagram;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseUser;
import com.parse.ParseObject;

@ParseClassName("Like")
public class Like extends ParseObject{
    public static final String KEY_POST = "post";
    public static final String KEY_USER = "user";

    public Like() {}

    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public Post getPost() { return (Post) getParseObject(KEY_POST); }

    public void setPost(Post post) { put(KEY_POST, post); }
}
