package com.fusion.kim.medmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MedDetailsActivity extends AppCompatActivity {

    private TextView mNameTv, mDescTv, mStartDateTv, mEndDateTv, mIntervalsTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_details);

        mNameTv = findViewById(R.id.tv_det_med_name);
        mDescTv = findViewById(R.id.tv_det_med_description);
        mStartDateTv = findViewById(R.id.tv_med_start_date);
        mEndDateTv = findViewById(R.id.tv_med_end_date);
        mIntervalsTv = findViewById(R.id.tv_med_intervals);

        Bundle medData = getIntent().getExtras();

        String name = medData.getString("name");
        String desc = medData.getString("desc");
        String startDate = medData.getString("startDate");
        String endDate = medData.getString("endDate");
        int interval = medData.getInt("intervals");

        mNameTv.setText(name);
        mDescTv.setText(desc);
        mStartDateTv.setText(startDate);
        mEndDateTv.setText(endDate);
        mIntervalsTv.setText(""+interval);

    }
}
