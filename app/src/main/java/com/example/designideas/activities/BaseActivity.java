package com.example.designideas.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.designideas.utilities.Constants;
import com.example.designideas.utilities.PreferenceManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documenetReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager= new PreferenceManager(getApplicationContext());
        FirebaseFirestore firestore= FirebaseFirestore.getInstance();
        documenetReference=firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documenetReference.update(Constants.KEY_AVAILABILITY_STATUS,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documenetReference.update(Constants.KEY_AVAILABILITY_STATUS,1);
    }
}
