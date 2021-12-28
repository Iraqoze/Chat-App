package com.example.designideas.activities;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.designideas.R;
import com.example.designideas.databinding.ActivityLoginBinding;
import com.example.designideas.utilities.Constants;
import com.example.designideas.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    Animation leftrightAnim,rightAnim;
    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager= new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
    }

    private void initViews() {
        leftrightAnim = AnimationUtils.loadAnimation(LoginActivity.this,R.anim.leftright_animation);
        rightAnim=AnimationUtils.loadAnimation(LoginActivity.this,R.anim.rightleft_animation);
        binding.welcomeTxt.setAnimation(leftrightAnim);

       binding.loginBtn.setOnClickListener(v->{
        if (isValidLoginData()){
            login();
        }


        });

        binding.textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        isLoading(true);
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
    firestore.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL,binding.email.getText().toString())
            .whereEqualTo(Constants.KEY_PASSWORD,binding.password.getText().toString())
            .get()
            .addOnCompleteListener(task->{
                isLoading(false);
                if (task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                    DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                    preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                    preferenceManager.putString(Constants.KEY_EMAIL,documentSnapshot.getString(Constants.KEY_EMAIL));
                    preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    isLoading(false);
                    Toast.makeText(getApplicationContext(), "Incorrect Username or Password", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void isLoading(boolean bool){
        if (bool){
            binding.loginBtn.setVisibility(View.GONE);
            binding.progressCircular1.setVisibility(View.VISIBLE);
        }
        else{

            binding.progressCircular1.setVisibility(View.GONE);
            binding.loginBtn.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValidLoginData(){
        if(binding.email.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()){
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(binding.password.getText().toString().isEmpty()){
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return  true;
    }
}