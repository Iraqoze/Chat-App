package com.example.designideas.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.designideas.adapters.RecentConversationAdapter;
import com.example.designideas.databinding.ActivityMainBinding;
import com.example.designideas.entities.ChatMessage;
import com.example.designideas.entities.User;
import com.example.designideas.listeners.ConversationListener;
import com.example.designideas.utilities.Constants;
import com.example.designideas.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversationListener {
private ActivityMainBinding binding;
private PreferenceManager preferenceManager;
private List<ChatMessage> conversations;
private RecentConversationAdapter recentConversationAdapter;
private FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        init();
        listenConversation();
        loadUserInfo();
        getToken();
        initViews();
    }

    private void loadUserInfo(){
        binding.username.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes= Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.userImage.setImageBitmap(bitmap);

    }
    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
      DocumentReference documentReference= firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
       documentReference.update(Constants.KEY_FCM_TOKEN,token)
               .addOnFailureListener(exception->{
                  toast("Failed to update Token");
               });

    }
    private void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::updateToken);
    }
    private void logout(){
        toast("logging out...");
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        DocumentReference documentReference= firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String,Object> updates=new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }).addOnFailureListener(exception->{
            toast("Unable to logout");
        });
    }

    private  void initViews(){
        binding.icLogout.setOnClickListener(v->{
            logout();
        });
        binding.fabNewChat.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(intent);
        });
    }
    private void init(){
        conversations= new ArrayList<>();
        recentConversationAdapter= new RecentConversationAdapter(conversations,this);
        binding.recyclerConversations.setAdapter(recentConversationAdapter);
        firestore=FirebaseFirestore.getInstance();
    }
    private  final EventListener<QuerySnapshot> eventListener=(value, error)-> {
        if (error != null) {
            return;
        }
        if(value!=null){
            for (DocumentChange documentChange:value.getDocumentChanges()){
                if(documentChange.getType()==DocumentChange.Type.ADDED){
                    String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                    ChatMessage message= new ChatMessage();
                    message.senderId=senderId;
                    message.receiverId=receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        message.conversationImage=documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        message.conversationName=documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        message.conversationId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }
                    else{
                        message.conversationImage=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        message.conversationName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        message.conversationId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    message.message=documentChange.getDocument().getString(Constants.KEY_RECENT_MESSAGE);
                    message.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    conversations.add(message);

                }
                else if(documentChange.getType()==DocumentChange.Type.MODIFIED){
                    for (int i=0; i<conversations.size();i++){
                    String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
              conversations.get(i).message=documentChange.getDocument().getString(Constants.KEY_RECENT_MESSAGE);
              conversations.get(i).dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
              break;
            }
                    }
                }
            }
            Collections.sort(conversations,(obj1, obj2)->obj2.dateObject.compareTo(obj1.dateObject));
            recentConversationAdapter.notifyDataSetChanged();
            binding.recyclerConversations.smoothScrollToPosition(0);
            binding.recyclerConversations.setVisibility(View.VISIBLE);
            binding.progressCircularMain.setVisibility(View.GONE);
        }
    };
    private void listenConversation(){
        firestore.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        firestore.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @Override
    public void onConversationClicked(User user) {
       Intent intent= new Intent(getApplicationContext(),ChatActivity.class);
       intent.putExtra(Constants.KEY_USER,user);
       startActivity(intent);
    }
}