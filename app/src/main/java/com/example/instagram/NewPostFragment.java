package com.example.instagram;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewPostFragment extends Fragment {
    private EditText etDescription;
    private Button btnPost;
    private ImageView btnTakePhoto;
    private ImageView ivPhoto;
    private ImageView ivGallery;

    private File photoFile;
    private ParseFile photoParseFile;
    private View view;
    public String photoFileName = "photo.jpg";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final int GET_FROM_GALLERY = 3;

    public static final String TAG = "NewPostFragment";

    public NewPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewPostFragment.
     */
    public static NewPostFragment newInstance() {
        NewPostFragment fragment = new NewPostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void savePost(String description, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(photoParseFile);
        post.setUser(currentUser);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getActivity(), "Error while saving", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Post save was successful");
                etDescription.setText("");
                ivPhoto.setImageResource(0);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       view = inflater.inflate(R.layout.fragment_new_post, container, false);
       etDescription = view.findViewById(R.id.etDescription);
       btnPost = view.findViewById(R.id.btnPost);
       btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
       ivPhoto = view.findViewById(R.id.ivPhoto);
       ivGallery = view.findViewById(R.id.ivGallery);

       // button to open camera
       btnTakePhoto.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onLaunchCamera(v);
           }
       });

       // button to send post
       btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                if(description.isEmpty()){
                    Toast.makeText(getActivity(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(photoParseFile == null || ivPhoto.getDrawable() == null){
                    Toast.makeText(getActivity(), "No image", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, currentUser, photoFile);
            }
       });

       // button to choose image from gallery
       ivGallery.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
           }
       });

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

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);
        photoParseFile = new ParseFile(photoFile);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            Log.i(TAG, "onLaunchCamera attempt success");
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
        else{
            Log.i(TAG, "onLaunchCamera attempt fail");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivPhoto.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                ivPhoto.setImageBitmap(bitmap);
                photoParseFile = conversionBitmapParseFile(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

}