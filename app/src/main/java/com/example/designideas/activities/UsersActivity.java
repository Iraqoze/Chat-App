package com.example.designideas.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.designideas.R;
import com.example.designideas.adapters.UsersAdapter;
import com.example.designideas.databinding.ActivityUsersBinding;
import com.example.designideas.entities.User;
import com.example.designideas.listeners.UserListener;
import com.example.designideas.utilities.Constants;
import com.example.designideas.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {
private ActivityUsersBinding binding;
private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        getUsers();
        initViews();
    }

    private void getUsers(){
        isLoading(true);
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
       firestore.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task->{
                    isLoading(false);
                    String currentUserId=preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult()!=null){
                        List<User> users= new ArrayList<>();
                        for (QueryDocumentSnapshot query: task.getResult()) {
                            if (currentUserId.equals(query.getId()))
                                continue;
                            User user= new User();
                            user.name=query.getString(Constants.KEY_NAME);
                            user.image=query.getString(Constants.KEY_IMAGE);
                            user.email=query.getString(Constants.KEY_EMAIL);
                            user.token=query.getString(Constants.KEY_FCM_TOKEN);
                            user.id=query.getId();
                            users.add(user);
                        }
                        if (users.size()>0){
                            UsersAdapter adapter= new UsersAdapter(users,this);
                            binding.usersRecyclerView.setAdapter(adapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);

                        }
                        else{
                            showError();
                        }
                    }
                    else{
                        showError();
                    }
                });
    }
    private void showError(){
        binding.errorMessage.setText(String.format("%s"," No Users Available"));
        binding.errorMessage.setVisibility(View.VISIBLE);
    }
    private void isLoading(boolean bool){
        if (bool){
            binding.progressCircular3.setVisibility(View.VISIBLE);
        }
        else{

            binding.progressCircular3.setVisibility(View.GONE);
        }
    }
    private void initViews(){
        binding.icBack.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}