package com.example.designideas.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.designideas.databinding.ActivityRegisterBinding;
import com.example.designideas.utilities.Constants;
import com.example.designideas.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private PreferenceManager preferenceManager;
    private String encodeImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager= new PreferenceManager(getApplicationContext());
        initViews();

    }
    private void initViews() {

        binding.textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRegDataValid()){
                    register();
                }
            }
        });
        binding.frameLytImage.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

    }
    private void register(){
isLoading(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        HashMap<String, Object> user= new HashMap<>();
        user.put(Constants.KEY_NAME, binding.fullnames.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.email.getText().toString().trim());
        user.put(Constants.KEY_PASSWORD,binding.password.getText().toString());
        user.put(Constants.KEY_IMAGE,encodeImage);
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .add(user).addOnSuccessListener(documentReference -> {
        isLoading(false);
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
        preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
        preferenceManager.putString(Constants.KEY_NAME,binding.fullnames.getText().toString());
        preferenceManager.putString(Constants.KEY_EMAIL,binding.email.getText().toString().trim());
        preferenceManager.putString(Constants.KEY_IMAGE,encodeImage);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        }).addOnFailureListener(exception->{
            isLoading(false);
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();

        });


    }
    private void isLoading(boolean bool){
        if (bool){
            binding.registerBtn.setVisibility(View.GONE);
            binding.progressCircular2.setVisibility(View.VISIBLE);
        }
        else{

            binding.progressCircular2.setVisibility(View.GONE);
            binding.registerBtn.setVisibility(View.VISIBLE);
        }
    }
    private boolean isRegDataValid(){
        if (encodeImage==null){
            Toast.makeText(this, "Select Profile Image", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(binding.fullnames.getText().toString().isEmpty()){
            Toast.makeText(this, "Enter Names", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(binding.email.getText().toString().trim().isEmpty()){
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
        else if(binding.confirmPassword.getText().toString().isEmpty()){
            Toast.makeText(this, "Confirm password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!binding.confirmPassword.getText().toString().equalsIgnoreCase(binding.password.getText().toString())){
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private  String encodeImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
       byte[]bytes=byteArrayOutputStream.toByteArray();
       return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImage=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result->{
                if(result.getResultCode()==RESULT_OK){
                    if(result.getData()!=null){
                        Uri imageUri=result.getData().getData();
                        try {
                            InputStream inputStream=getContentResolver().openInputStream(imageUri);
                          Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                          binding.profileImage.setImageBitmap(bitmap);
                          binding.textPickImage.setVisibility(View.GONE);
                          encodeImage=encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
}

