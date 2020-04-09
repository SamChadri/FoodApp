package com.example.foodapp.RegisterUI;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import com.example.foodapp.R;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class DateOfBirth extends AppCompatActivity {

    //TODO: Convert this to fragments

    private static HashMap<Object, Object> buildUser;
    private final String TAG = "DateOfBirth";


    public static int datePickerID;
    public static int pickerButtonID;
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);

            datePickerID = dialog.getDatePicker().getId();


            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    pickerButtonID = ((DatePickerDialog)dialog).getButton(DatePickerDialog.BUTTON_POSITIVE).getId();

                }
            });

            return dialog;
        }



        public void onDateSet(DatePicker view, int year, int month, int day){
            Intent intent = new Intent(getActivity(), CreatePassword.class);
            Date newDate = new GregorianCalendar(year, month -1, day).getTime();
            Timestamp dob = new Timestamp(newDate);
            buildUser.put("year", year);
            buildUser.put("month", month);
            buildUser.put("day", day);
            intent.putExtra(GetStarted.ARGKEY, buildUser);
            startActivity(intent);

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_of_birth);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if(getIntent().getSerializableExtra(GetStarted.ARGKEY) != null){
            Log.d(TAG, "Getting Intent Extra");
            buildUser = (HashMap)getIntent().getSerializableExtra(GetStarted.ARGKEY);
        }else{
            Log.d(TAG, "Getting buildUser from sharedPreferences");
            buildUser = (HashMap)getPreferences(Context.MODE_PRIVATE).getAll();
        }




    }

    @Override
    protected void onStop(){
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();

        for(HashMap.Entry<Object, Object> item: buildUser.entrySet()){
            if(item.getValue() instanceof Integer){
                editor.putInt((String)item.getKey(), (int)item.getValue());
            }else{
                editor.putString((String)item.getKey(), (String)item.getValue());
            }
        }

        editor.commit();
        Log.d(TAG, "Put values into sharedPreferences");

        super.onStop();

    }

    public void showTimePickerDialog(View v){
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}
