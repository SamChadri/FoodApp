package com.example.foodapp.ui.home;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;


import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.R;
import com.example.foodapp.utils.CommentAdapter;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Recipe;
import com.example.fooddata.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Observable;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecipeDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecipeDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeDetailFragment extends Fragment {
    // TODO: Clean this shit up.
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static int expandedAppBarHeight;
    private static int statusBarHeight;

    private String mParam1;
    private String mParam2;

    private static final String TAG = "RecipeDetailFragment";

    private Toolbar toolbar;
    private AppBarLayout barLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView barImage;
    private AppBarLayout.LayoutParams ogParams;
    private TabLayout tabLayout;
    private int currTab;

    private int recipe_id;
    private int origin_id;



    private RecyclerView ingredientsRecyclerView;
    private RecyclerView.Adapter ingredientsAdapter;
    private RecyclerView.LayoutManager ingredientsLayout;

    private RecyclerView directionsRecyclerView;
    private RecyclerView.Adapter directionsAdapter;
    private RecyclerView.LayoutManager directionsLayout;

    private RecyclerView commentsRecyclerView;
    private RecyclerView.Adapter commentsAdapter;
    private RecyclerView.LayoutManager commentsLayout;
    private View root;

    private ImageButton likeButton;
    private ImageButton commentButton;

    private RatingBar ratingBar;

    private boolean createComment;

    private OnFragmentInteractionListener mListener;
    private LayoutInflater instanceInflater;
    private ViewGroup instanceContainer;

    private TextView recipeTitle;
    private TextView description;
    private TextView difficulty;
    private TextView totalTime;
    private TextView caloriesView;
    private TextView fatsView;
    private TextView carbsView;
    private TextView proteinView;
    private TextView author;

    private LinearLayout nutritionLayout;

    private boolean recipeRetrieved = true;
    private FragManagerViewModel fragManagerViewModel;

    private HomeViewModel homeViewModel;

    public RecipeDetailFragment() {
        // Required empty public constructor
    }
    //TODO: Fix comments
    //TODO: Add change onCompleteListeners to be aware of lifecycle events;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecipeDetailFragment.
     */
    public static RecipeDetailFragment newInstance(String param1, String param2) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
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



        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            statusBarHeight = (int)getResources().getDimension(resourceId);
            Log.d(TAG, "StatusBarHeight: " + statusBarHeight);

        }
        //expandedAppBarHeight = barLayout.getHeight();
        //Log.d(TAG, "AppBarHeight: " + barLayout.getHeight());

        recipe_id = RecipeDetailFragmentArgs.fromBundle(getArguments()).getRecipeId();
        origin_id = RecipeDetailFragmentArgs.fromBundle(getArguments()).getOriginId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root =  inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        recipeTitle = root.findViewById(R.id.recipeTitle);
        description = root.findViewById(R.id.recipeDescription);
        difficulty = root.findViewById(R.id.recipeDifficulty);
        totalTime = root.findViewById(R.id.recipeTotalTime);

        author = root.findViewById(R.id.recipeAuthorText);
        nutritionLayout = root.findViewById(R.id.nutritionLayout);
        caloriesView = root.findViewById(R.id.nutritionCalories);
        fatsView = root.findViewById(R.id.nutritionFats);
        carbsView = root.findViewById(R.id.nutritionCarbs);
        proteinView = root.findViewById(R.id.nutritionProtein);


        ingredientsRecyclerView = root.findViewById(R.id.ingredientsRecyclerView);
        directionsRecyclerView = root.findViewById(R.id.directionsRecyclerView);
        commentsRecyclerView = root.findViewById(R.id.recipeCommentsRecyclerView);
        likeButton = root.findViewById(R.id.recipeLikeButton);
        commentButton = root.findViewById(R.id.recipeCommentButton);
        ratingBar = root.findViewById(R.id.recipeRating);

        instanceContainer = container;
        instanceInflater = inflater;

        createComment = false;
        nutritionLayout.setVisibility(View.GONE);





        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        tabLayout = activity.findViewById(R.id.homeTabLayout);
        FloatingActionButton fab  = activity.findViewById(R.id.fab);
        barLayout = activity.findViewById(R.id.appBar);
        toolbar = activity.findViewById(R.id.toolBar);
        barImage = activity.findViewById(R.id.imageHeader);

        collapsingToolbarLayout = getActivity().findViewById(R.id.collapseToolBar);
        ogParams =  (AppBarLayout.LayoutParams)collapsingToolbarLayout.getLayoutParams();
        fragManagerViewModel =
                ViewModelProviders.of(getActivity()).get(FragManagerViewModel.class);

        homeViewModel =
                ViewModelProviders.of(getActivity()).get(HomeViewModel.class);

        fragManagerViewModel.setFragStatus(R.id.recipeDetailFragment);
        fragManagerViewModel.currFragment = R.id.recipeDetailFragment;
        fragManagerViewModel.getArgs().putInt("recipe_id", recipe_id);
        fragManagerViewModel.getArgs().putInt("origin_id", origin_id);

        FoodLibrary.getRecipe(recipe_id).addOnCompleteListener(getActivity(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult().toObject(Recipe.class) != null && recipeRetrieved){
                    if(getContext() == null){
                        return;
                    }
                    AppBarLayout.LayoutParams newParams = new AppBarLayout.LayoutParams(ogParams.width, (int)DeviceDimensionsHelper.convertDpToPixel(250, getContext()));
                    collapsingToolbarLayout.setLayoutParams(newParams);


                    Recipe recipe = task.getResult().toObject(Recipe.class);
                    recipeTitle.setText(recipe.title);
                    description.setText(recipe.description);
                    ratingBar.setRating(recipe.rating.rating);
                    difficulty.setText(recipe.difficulty);
                    totalTime.setText(Integer.toString(recipe.totalTime));

                    if(recipe.author != null && !recipe.author.isEmpty()){
                        author.setText(recipe.author);
                    }
                    if(recipe.nutrition != null){
                        nutritionLayout.setVisibility(View.VISIBLE);
                        caloriesView.setText(Integer.toString(recipe.nutrition.calories));
                        fatsView.setText(Integer.toString(recipe.nutrition.fat));
                        carbsView.setText(Integer.toString(recipe.nutrition.carbohydrates));
                        proteinView.setText(Integer.toString(recipe.nutrition.protein));
                    }



                    likeButton.setTag(recipe);
                    commentButton.setTag(recipe);

                    if(recipe.images.get(0) != null){
                        new DownloadImageTask(barImage).execute(recipe.images.get(0));
                    }

                    boolean userCommented = false;
                    for(User.UserComment userComment : homeViewModel.getFoodUser().userComments){
                        if(userComment.recipe_id == recipe_id){
                            userCommented = true;
                        }
                    }

                    if(userCommented){
                        commentButton.setSelected(true);
                    }else{
                        commentButton.setSelected(false);
                    }

                    ingredientsAdapter = new ItemAdapter(recipe.ingredients, false);
                    directionsAdapter = new ItemAdapter(recipe.directions, true);

                    ingredientsLayout = new LinearLayoutManager(root.getContext());
                    directionsLayout = new LinearLayoutManager(root.getContext());

                    ingredientsRecyclerView.setLayoutManager(ingredientsLayout);
                    directionsRecyclerView.setLayoutManager(directionsLayout);

                    ingredientsRecyclerView.setAdapter(ingredientsAdapter);
                    directionsRecyclerView.setAdapter(directionsAdapter);

                    if(recipe.comments != null && !recipe.comments.isEmpty()){
                        commentsAdapter = new CommentAdapter(recipe.comments);
                        commentsLayout = new LinearLayoutManager(root.getContext());

                        commentsRecyclerView.setLayoutManager(commentsLayout);
                        commentsRecyclerView.setAdapter(commentsAdapter);
                    }

                    likeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    commentButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            collapsingToolbarLayout.setLayoutParams(ogParams);
                            barImage.setImageResource(android.R.color.transparent);
                            NavDirections action = RecipeDetailFragmentDirections.actionRecipeDetailFragmentToCreateComment(recipe.id, false);
                            Navigation.findNavController(v).navigate(action);
                            createComment = true;
                        }
                    });

                    ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                            if(fromUser){
                                recipe.rating.updateRating(rating);

                                FoodLibrary.updateRecipeRating(recipe.id, recipe.rating.rating);
                                User.UserRating userRating = new User.UserRating(homeViewModel.getFoodUser().username, recipe_id, rating);
                                FoodLibrary.addUserRating(homeViewModel.getFoodUser(), userRating);
                            }
                        }
                    });


                }else{
                    Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();                   //TODO: Implement error page.



                    //TODO: Implement error page.
                }
            }
        });





        tabLayout.setVisibility(View.GONE);


        currTab = getResources().getInteger(R.integer.recipeTabPositon);
        toolbar.setTitle(" ");



    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();

        collapsingToolbarLayout.setLayoutParams(ogParams);
        barImage.setImageResource(android.R.color.transparent);

        if(origin_id == R.string.home_origin_id && !createComment){
            tabLayout.getTabAt(currTab).select();
            //TODO: maybe put this in the ondestinationchanged listner
        }

    }

    @Override
    public void onPause(){
        super.onPause();

        homeViewModel.clearUpdates();

    }




    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public ImageView view;
        public Toolbar bar;
        public DownloadImageTask(ImageView view){
            this.view = view;
        }

        @Override
        protected Bitmap doInBackground(String... args){
            String url = args[0];
            Bitmap image = null;
            try{
                InputStream stream = new URL(url).openStream();
                image = BitmapFactory.decodeStream(stream);

            }catch (IOException e){
                Log.d(TAG,"DownloadImageTask() - Error occurred: " + e);
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            int displayWidth = DeviceDimensionsHelper.getDisplayWidth(view.getContext());
            int imageHeight = (int)DeviceDimensionsHelper.convertDpToPixel(250, view.getContext());
            Bitmap scaledResult = scaleImage(result, displayWidth, imageHeight);
            //Bitmap scaledResult = Bitmap.createScaledBitmap(result,displayWidth, imageHeight, true);
            this.view.setImageBitmap(scaledResult);
        }

        private Bitmap scaleImage(Bitmap bm, int width, int height){
            float widthRatio = width / (float) bm.getWidth();
            float heightRatio = height / (float) bm.getHeight();
            return Bitmap.createScaledBitmap(bm, (int)(bm.getWidth() *widthRatio), (int)(bm.getHeight() * heightRatio), true);
        }
    }





    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

