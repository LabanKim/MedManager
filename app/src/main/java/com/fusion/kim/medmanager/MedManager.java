package com.fusion.kim.medmanager;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MedManager extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
