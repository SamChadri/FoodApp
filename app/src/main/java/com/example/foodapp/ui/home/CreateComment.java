package com.example.foodapp.ui.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.Home;
import com.example.foodapp.R;
import com.example.foodapp.ui.create.Create;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link CreateComment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateComment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private EditText comment;
    private ImageButton profilePic;
    private Button addButton;

    private int objectID;
    private boolean isPost;
    private User foodUser;

    private HomeViewModel homeViewModel;

    private View root;
    private LayoutInflater inflater;
    private ViewGroup container;

    private FragManagerViewModel fragManagerViewModel;

    public CreateComment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateComment.
     */
    public static CreateComment newInstance(String param1, String param2) {
        CreateComment fragment = new CreateComment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        objectID = CreateCommentArgs.fromBundle(getArguments()).getID();
        isPost = CreateCommentArgs.fromBundle(getArguments()).getIsPost();
    }

    //TODO: Implement delete comment somewhere.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_create_comment, container, false);

        comment = root.findViewById(R.id.newComment);
        profilePic = root.findViewById(R.id.profileIcon);
        addButton = root.findViewById(R.id.newCommentButton);
        addButton.setEnabled(false);

        this.inflater = inflater;
        this.container = container;


        FoodLibrary.getUser(AuthLib.getAcitveUser().getUid())
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        foodUser = task.getResult().toObject(User.class);
                        addButton.setEnabled(true);
                    }
                });



        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedBundleInstance){
        super.onActivityCreated(savedBundleInstance);

        homeViewModel =
                ViewModelProviders.of(getActivity()).get(HomeViewModel.class);

        fragManagerViewModel =
                ViewModelProviders.of(getActivity()).get(FragManagerViewModel.class);

        fragManagerViewModel.setFragStatus(R.id.createComment);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comment.getText().toString() != null && !comment.getText().toString().isEmpty()){
                    //TODO Make Comment its own class
                    //TODO: Get rid of keyboard after Comment creation.
                    Post.Comment newComment = new Post.Comment(foodUser.username, comment.getText().toString());
                    homeViewModel.setTag("newComment", newComment);
                    if(isPost){
                        homeViewModel.createPostComment(objectID, newComment);
                        homeViewModel.getLibStatus().observe(getViewLifecycleOwner(), new Observer<HashMap>() {
                            @Override
                            public void onChanged(HashMap hashMap) {
                                if(hashMap.containsKey("postUpdated") && hashMap.containsKey("userUpdated") &&
                                        hashMap.containsKey("postCommentCreated")){
                                    if((boolean)hashMap.get("postUpdated") && (boolean)hashMap.get("userUpdated") &&
                                            (boolean)hashMap.get("postCommentCreated")){
                                        ((Home)getActivity()).onSupportNavigateUp();
                                    }else{
                                        Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }else{
                        homeViewModel.createRecipeComment(objectID, newComment);
                        homeViewModel.getLibStatus().observe(getViewLifecycleOwner(), new Observer<HashMap>() {
                            @Override
                            public void onChanged(HashMap hashMap) {
                                if(hashMap.containsKey("recipeUpdated") && hashMap.containsKey("userUpdated") &&
                                        hashMap.containsKey("recipeCommentCreated")){
                                    if((boolean)hashMap.get("recipeUpdated") && (boolean)hashMap.get("userUpdated") &&
                                            (boolean)hashMap.get("recipeCommentCreated")){
                                        ((Home)getActivity()).onSupportNavigateUp();
                                    }else{
                                        Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }

                }else{
                    Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();

                }
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        homeViewModel.clearUpdates();

    }


}
