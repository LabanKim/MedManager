package com.fusion.kim.medmanager;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.support.v7.widget.SearchView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Filter;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mMedsRef;

    private RecyclerView mMedRecycler;

    private GoogleSignInClient mClient;

    private List<Medicines> mMedsList;
    private List<Medicines> mFilteredList;

    private TextView mMainHeaderTv;

    //Pending intent instance
    private static PendingIntent pendingIntent;

    private static final int ALARM_REQUEST_CODE = 133;

    private MedicineAdapter mAdapter;

    private String mKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /* Retrieve a PendingIntent that will perform a broadcast */
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, ALARM_REQUEST_CODE, alarmIntent, 0);

        mMedsList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        mMedsRef = FirebaseDatabase.getInstance().getReference().child("Medicine");

        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    finish();
                }

            }
        };

        mMedRecycler = findViewById(R.id.meds_recyclerview);
        mMedRecycler.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMainHeaderTv = findViewById(R.id.tv_header);
                mMainHeaderTv.setText("Welcome "+dataSnapshot.child("displayName").getValue(String.class) + " " +
                        "Here are your Meds");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mClient = GoogleSignIn.getClient(this, gso);

        mMedsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap<String, Object> medsMap = ( HashMap<String, Object>) dataSnapshot.getValue();

                String key = null, name = null, desc = null, startDate = null, endDate = null;
                int interval = 0;

                key = dataSnapshot.getKey().toString();

                Set set = medsMap.entrySet();
                Iterator iterator = set.iterator();
                while(iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry)iterator.next();

                    if (mentry.getKey().equals("name"))
                        name = mentry.getValue().toString();

                    if (mentry.getKey().equals("interval"))
                        interval = Integer.parseInt(mentry.getValue().toString());

                    if (mentry.getKey().equals("description"))
                        desc = mentry.getValue().toString();

                    if (mentry.getKey().equals("startDate"))
                        startDate = mentry.getValue().toString();

                    if (mentry.getKey().equals("endDate"))
                        endDate = mentry.getValue().toString();

                }


                mMedsList.add(new Medicines(key, name, desc, startDate, endDate, interval));
                mAdapter = new MedicineAdapter(MainActivity.this, mMedsList);
                mMedRecycler.setAdapter(mAdapter);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


                HashMap<String, Object> medsMap = ( HashMap<String, Object>) dataSnapshot.getValue();

                String key = null, name = null, desc = null, startDate = null, endDate = null;
                int interval = 0;

                key = dataSnapshot.getKey().toString();

                Set set = medsMap.entrySet();
                Iterator iterator = set.iterator();

                while(iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry)iterator.next();


                    if (mentry.getKey().equals("name"))
                        name = mentry.getValue().toString();

                    if (mentry.getKey().equals("interval"))
                        interval = Integer.parseInt(mentry.getValue().toString());

                    if (mentry.getKey().equals("description"))
                        desc = mentry.getValue().toString();

                    if (mentry.getKey().equals("startDate"))
                        startDate = mentry.getValue().toString();

                    if (mentry.getKey().equals("endDate"))
                        endDate = mentry.getValue().toString();


                }


                mMedsList.add(new Medicines(key, name, desc, startDate, endDate, interval));
                mAdapter.notifyDataSetChanged();
                mAdapter = new MedicineAdapter(MainActivity.this, mMedsList);
                mMedRecycler.setAdapter(mAdapter);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddMedsActivity.class));

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        search(searchView);
        return true;
    }

    private void search(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();

        if (id == R.id.action_logout) {

            mClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                mAuth.signOut();

                            }

                        }
                    });


            return true;
        }

        if (id == R.id.action_stop_alarm){

            stopAlarmManager();

            return true;

        }

        if (id == R.id.action_profile){

            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            profileIntent.putExtra("key", mKey);
            startActivity(profileIntent);

            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public class MedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mNameTv, mDescTv;
        private ImageView mDeleteIv;

        public List<Medicines> medicinesList;

        public MedViewHolder(View itemView, List<Medicines> medicinesList) {
            super(itemView);

            this.medicinesList = medicinesList;

            itemView.setOnClickListener(this);

            mNameTv = itemView.findViewById(R.id.tv_med_name);
            mDeleteIv = itemView.findViewById(R.id.iv_delete);
            mDescTv = itemView.findViewById(R.id.tv_desc);

        }

        public void setName(String name){
            mNameTv.setText(name);
        }

        public void setDesc(String desc){
            mDescTv.setText(desc);
        }

        @Override
        public void onClick(View v) {
            final int selectedItemPosition = mMedRecycler.getChildPosition(v);
            Intent detailsIntent = new Intent(MainActivity.this, MedDetailsActivity.class);
            detailsIntent.putExtra("name", medicinesList.get(selectedItemPosition).getName());
            detailsIntent.putExtra("desc", medicinesList.get(selectedItemPosition).getDescription());
            detailsIntent.putExtra("startDate", medicinesList.get(selectedItemPosition).getStartDate());
            detailsIntent.putExtra("endDate", medicinesList.get(selectedItemPosition).getEndDate());
            detailsIntent.putExtra("intervals", medicinesList.get(selectedItemPosition).getInterval());
            startActivity(detailsIntent);

        }
    }

    public class MedicineAdapter extends RecyclerView.Adapter<MedViewHolder> {

        List<Medicines> dataList;
        Context context;

        public MedicineAdapter(Context context, List<Medicines> dataList) {

            this.context = context;
            this.dataList = dataList;
            mMedsList = dataList;


        }

        @NonNull
        @Override
        public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MedViewHolder viewHolder = null;

            View layoutView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.med_item, parent, false);
            viewHolder = new MedViewHolder(layoutView, mMedsList);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final MedViewHolder holder, final int position) {

            holder.setName(dataList.get(position).getName());
            holder.setDesc(dataList.get(position).getDescription());
            triggerAlarmManager(getTimeInterval(dataList.get(position).getInterval()));

            holder.mDeleteIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mMedsRef.child(dataList.get(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mAdapter.notifyDataSetChanged();
                                mMedRecycler.setAdapter(mAdapter);
                                Toast.makeText(context, "Medicine Removed" , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            });

        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }


        public Filter getFilter() {

            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {

                    String charString = charSequence.toString();

                    if (charString.isEmpty()) {

                        mFilteredList = mMedsList;
                    } else {

                        ArrayList<Medicines> filteredList = new ArrayList<>();

                        for (Medicines medicines : mMedsList) {

                            if (medicines.getName().toLowerCase().contains(charString)) {

                                filteredList.add(medicines);
                            }
                        }

                        mFilteredList = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mFilteredList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mFilteredList = (ArrayList<Medicines>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }


    }

    //get time interval to trigger alarm manager
    private int getTimeInterval(int getInterval) {

        int interval = getInterval;

        return interval * 60 * 60;//convert hours into seconds;
    }

    //Trigger alarm manager with entered time interval
    public void triggerAlarmManager(int alarmTriggerTime) {
        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add alarmTriggerTime seconds to the calendar object
        cal.add(Calendar.SECOND, alarmTriggerTime);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//get instance of alarm manager
        manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);//set alarm manager with entered timer by converting into milliseconds

    }

    //Stop/Cancel alarm manager
    public void stopAlarmManager() {

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);//cancel the alarm manager of the pending intent


        //Stop the Media Player Service to stop sound
        stopService(new Intent(MainActivity.this, AlarmSoundService.class));

        //remove the notification from notification tray
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(AlarmNotificationService.NOTIFICATION_ID);

    }
}
