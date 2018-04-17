package com.fusion.kim.medmanager;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class AddMedsActivity extends AppCompatActivity {

    private EditText mMedNameInput, mDescriptionInput, mIntervalsInput;
    private TextInputLayout mMednameLayout, mDescLayout, mIntervalsLayout;

    private Button mSubmitBtn;
    private CalendarView mStartDateView, mEndDateView;

    private String mStartDate = null, mEndDate = null;

    private DatabaseReference mMedsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meds);

        mMedsRef = FirebaseDatabase.getInstance().getReference().child("Medicine");

        mMedNameInput = findViewById(R.id.input_med_name);
        mDescriptionInput = findViewById(R.id.input_description);
        mIntervalsInput = findViewById(R.id.input_intervals);

        mMednameLayout = findViewById(R.id.input_layout_med_name);
        mDescLayout = findViewById(R.id.input_layout_description);
        mIntervalsLayout = findViewById(R.id.input_layout_frequency);

        mStartDateView = findViewById(R.id.start_date_view);
        mEndDateView = findViewById(R.id.end_date_view);

        mStartDateView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();

                mStartDate = convertDate(clickedDayCalendar.getTimeInMillis(), "dd/MM/yyyy");
            }
        });

        mEndDateView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();

                mEndDate = convertDate(clickedDayCalendar.getTimeInMillis(), "dd/MM/yyyy");
            }
        });

        mSubmitBtn = findViewById(R.id.btn_submit);

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Adding Medicine...");
        pDialog.setCancelable(false);

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateMedName())
                    return;
                if (!validateDescription())
                    return;
                if (!validateIntervals())
                    return;

                pDialog.show();

                String name = mMedNameInput.getText().toString();
                double interval = Double.parseDouble(mIntervalsInput.getText().toString());
                String desc = mDescriptionInput.getText().toString();

                HashMap<String, Object> medsMap = new HashMap<>();

                medsMap.put("name", name);
                medsMap.put("description", desc);
                medsMap.put("interval", interval);
                medsMap.put("startDate", mStartDate);
                medsMap.put("endDate", mEndDate);


                mMedsRef.push().setValue(medsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            pDialog.dismiss();
                            Toast.makeText(AddMedsActivity.this, "Medicine added Successfully", Toast.LENGTH_LONG).show();

                        }
                        else {
                            pDialog.dismiss();
                            Toast.makeText(AddMedsActivity.this, "Failed to add Medicine, please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }

    public static String convertDate(Long dateInMilliseconds,String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(String.valueOf(dateInMilliseconds))).toString();
    }

    private boolean validateMedName() {
        if (mMedNameInput.getText().toString().trim().isEmpty()) {
            mMednameLayout.setError(("Enter Medicine Name"));
            return false;
        } else {
            mMednameLayout.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateDescription() {
        if (mDescriptionInput.getText().toString().trim().isEmpty()) {
            mDescLayout.setError(("Enter Description"));
            return false;
        } else {
            mDescLayout.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateIntervals() {
        if (mIntervalsInput.getText().toString().trim().isEmpty()) {
            mIntervalsLayout.setError(("Enter Interval"));
            return false;
        } else {
            mIntervalsLayout.setErrorEnabled(false);
        }

        return true;
    }
}
