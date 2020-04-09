package com.example.foodapp.ui.create;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.example.foodapp.Capture;
import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.Home;
import com.example.foodapp.R;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.fooddata.Recipe;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateRecipe extends Fragment {

    private View root;
    private String dirLabel = "newDir";
    private String ingrLabel = "newIngr";

    private static String TAG = "CreateRecipe";

    private CreateRecipeViewModel mViewModel;
    private EditText recipeTitle;
    private EditText recipeDescription;
    private EditText servings;
    private EditText totalTimeText;


    private EditText direction1;
    private EditText direction2;
    private EditText direction3;
    private Button moreDirButton;
    private int moreDirCount = 0;

    private EditText ingredient1;
    private EditText ingredient2;
    private EditText ingredient3;
    private Button moreIngrButton;
    private int moreIngrCount = 100;

    private LinearLayout ingrLayout;
    private LinearLayout dirLayout;

    private FragManagerViewModel fragManagerViewModel;
    private LayoutInflater inflater;
    private ViewGroup container;

    private Button addRecipeButton;
    private ImageButton addRecipeImageButton;

    public CreateRecipeViewModel getmViewModel(){
        return mViewModel;
    }

    public static CreateRecipe newInstance() {
        return new CreateRecipe();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root =  inflater.inflate(R.layout.create_recipe_fragment, container, false);



        this.inflater = inflater;
        this.container = container;

        recipeTitle = root.findViewById(R.id.recipeTitleText);
        recipeDescription = root.findViewById(R.id.recipeDescriptionText);
        servings = root.findViewById(R.id.ServingsText);
        totalTimeText = root.findViewById(R.id.totalTimeText);

        ingrLayout = root.findViewById(R.id.ingredientsLinearLayout);
        dirLayout = root.findViewById(R.id.directionsLinearLayout);

        direction1 = root.findViewById(R.id.direction1Text);
        direction2 = root.findViewById(R.id.direction2Text);
        direction3 = root.findViewById(R.id.direction3Text);
        moreDirButton = root.findViewById(R.id.moreDirectionsButton);

        ingredient1 = root.findViewById(R.id.ingredient1Text);
        ingredient2 = root.findViewById(R.id.ingredient2Text);
        ingredient3 = root.findViewById(R.id.ingredient3Text);
        moreIngrButton = root.findViewById(R.id.moreIngredientsButton);

        addRecipeButton = root.findViewById(R.id.addRecipeButton);
        addRecipeImageButton = root.findViewById(R.id.addRecipeImageButton);


        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(CreateRecipeViewModel.class);

        fragManagerViewModel =
                ViewModelProviders.of(getActivity()).get(FragManagerViewModel.class);

        fragManagerViewModel.setFragStatus(this.getId());
        fragManagerViewModel.currFragment = R.id.createRecipe;

        File image = new File(getActivity().getCacheDir(), Capture.FILENAME);
        if(image.exists()){
            try{
                FileInputStream fileStream = new FileInputStream(image);
                Bitmap bitmapImage = BitmapFactory.decodeStream(fileStream);
                int newHeight = (int)DeviceDimensionsHelper.convertDpToPixel(250, root.getContext());
                int newWidth = (int)DeviceDimensionsHelper.convertDpToPixel(250, root.getContext());
                Bitmap scaledImage = Bitmap.createScaledBitmap(bitmapImage, newWidth, newHeight, false);
                addRecipeImageButton.setImageBitmap(scaledImage);
                //addRecipeImageButton.setRotation(90);

            }catch(IOException e ){

                Log.d(TAG, "Error occurred: " + e);

            }
        }

        mViewModel.getFoodLibStatus().observe(getViewLifecycleOwner(), new Observer<HashMap>() {
            @Override
            public void onChanged(HashMap hashMap) {
                if(hashMap.containsKey("userRetrieved") && !hashMap.containsKey("recipeImageUploaded")){
                    if((boolean)hashMap.get("userRetrieved")){
                        mViewModel.newRecipe.username = mViewModel.getFoodUser().username;
                    }else{
                        Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });


        moreDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText newDirection = new EditText(root.getContext());
                newDirection.setTextSize(12);
                newDirection.setId(moreDirCount++);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                newDirection.setLayoutParams(layoutParams);
                dirLayout.addView(newDirection);
            }
        });

        moreIngrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText newIngredient = new EditText(root.getContext());
                newIngredient.setTextSize(12);
                newIngredient.setId(moreIngrCount++);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                newIngredient.setLayoutParams(layoutParams);

                ingrLayout.addView(newIngredient);
            }
        });



        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRecipe(mViewModel.newRecipe);
                mViewModel.addRecipePicture(image);
                mViewModel.getFoodLibStatus().observe(getViewLifecycleOwner() , new Observer<HashMap>() {
                    @Override
                    public void onChanged(HashMap hashMap) {
                        if(hashMap.containsKey("recipeImageUploaded") && hashMap.containsKey("imageUrlRetrieved") && !hashMap.containsKey("recipeAdded")){
                            if((boolean)hashMap.get("recipeImageUploaded") && (boolean)hashMap.get("imageUrlRetrieved")){
                                mViewModel.addRecipe();
                                Intent intent = new Intent(getActivity(), Home.class);
                                startActivity(intent);

                            }else{
                                Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();

                            }
                        }
                    }
                });

            }
        });
    }

    private ArrayList<String> getExtraEntries( ArrayList<String> entries ,boolean isDirection){
        int upperLimit = isDirection ?  moreDirCount : moreIngrCount;
        for(int i = isDirection ? 0 : 100; i < upperLimit; i++){
            EditText entry = root.findViewById(i);
            entries.add(entry.getText().toString());
        }
        return entries;
    }

    private void generateRecipe(Recipe retval){
        retval.servings = Integer.parseInt(servings.getText().toString());
        retval.totalTime = Integer.parseInt(totalTimeText.getText().toString());
        retval.title = recipeTitle.getText().toString();
        retval.description = recipeDescription.getText().toString();

        ArrayList<String> directions = new ArrayList<>();
        ArrayList<String> ingredients = new ArrayList<>();

        directions.add(direction1.getText().toString());
        directions.add(direction2.getText().toString());
        directions.add(direction3.getText().toString());

        ingredients.add(ingredient1.getText().toString());
        ingredients.add(ingredient2.getText().toString());
        ingredients.add(ingredient3.getText().toString());

        directions = getExtraEntries(directions, true);
        ingredients = getExtraEntries(ingredients, false);

        retval.ingredients = ingredients;
        retval.directions = directions;

        retval.setID();

        retval.rating = new Recipe.Rating(0, 0);
        if(retval.difficulty == null || retval.difficulty.isEmpty()){
            retval.difficulty = "Beginner";

        }



    }


    public void onRadioButtonClicked(View view){

        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()){
            case(R.id.beginnerRadio):{
                if(checked){
                    mViewModel.newRecipe.difficulty = "Beginner";
                }
            }
            case(R.id.intermediateRadio):{
                if(checked){
                    mViewModel.newRecipe.difficulty = "Intermediate";
                }
            }
            case(R.id.advancedRadio):{
                if(checked){
                    mViewModel.newRecipe.difficulty = "Advanced";
                }
            }
        }
    }

    private Bitmap scaleImage(Bitmap bm, int width, int height){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        float widthRatio =  bm.getWidth() / (float)  width;
        float heightRatio = bm.getHeight() /(float)  height ;
        return Bitmap.createScaledBitmap(bm, (int)(bm.getWidth() *widthRatio), (int)(bm.getHeight() * heightRatio), true);
    }



}
