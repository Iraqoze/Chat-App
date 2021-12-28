package com.example.designideas.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.designideas.adapters.ChatAdapter;
import com.example.designideas.databinding.ActivityChatBinding;
import com.example.designideas.entities.ChatMessage;
import com.example.designideas.entities.User;
import com.example.designideas.network.ApiClient;
import com.example.designideas.network.ApiService;
import com.example.designideas.utilities.Constants;
import com.example.designideas.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
private ActivityChatBinding binding;
private User receivedUser;
private List<ChatMessage> messages;
private ChatAdapter chatAdapter;
private PreferenceManager preferenceManager;
private FirebaseFirestore firestore;
private String conversationId=null;
private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceivedUserInfo();
        initViews();
        init();
        listenMessages();
    }
    private void loadReceivedUserInfo(){
        receivedUser=(User)getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.recepientName.setText(receivedUser.name);
    }
    private Bitmap getBitmapFromEncodedString(String encodedImage){
            if (encodedImage!=null){
                byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
                Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                return bitmap;
            }else{
                return null;
            }
    }
    private void init(){
        preferenceManager=new PreferenceManager(getApplicationContext());
        messages=new ArrayList<>();
        chatAdapter=new ChatAdapter(messages,getBitmapFromEncodedString(receivedUser.image),
               getBitmapFromEncodedString(preferenceManager.getString(Constants.KEY_IMAGE)),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatsRecyclerView.setAdapter(chatAdapter);
        firestore=FirebaseFirestore.getInstance();
    }
    private void sendMessage(){
        HashMap<String,Object> message=new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receivedUser.id);
        message.put(Constants.KEY_MESSAGE,binding.messageToSend.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        firestore.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId!=null){
            updateConversation(binding.messageToSend.getText().toString());
        }
        else {
            HashMap<String,Object> conversion=new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_RECEIVER_NAME,receivedUser.name);
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receivedUser.id);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receivedUser.image);
            conversion.put(Constants.KEY_RECENT_MESSAGE,binding.messageToSend.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());

            addConversation(conversion);
        }
        if (!isReceiverAvailable){
            try {
                JSONArray tokens= new JSONArray();
                tokens.put(receivedUser.token);
                JSONObject data= new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.messageToSend.getText().toString());

                JSONObject body= new JSONObject();
                body.put(Constants.KEY_REMOTE_MSG_CONTENT,data);
                body.put(Constants.KEY_REMOTE_MSG_REGISTRATION_IDS,tokens);
                sendNotification(body.toString());
            }catch (Exception e){
                showToast(e.getMessage());
            }
        }
        binding.messageToSend.setText(null);
    }
    private void initViews(){
        binding.icChatback.setOnClickListener(v->{
            Intent intent=new Intent(getApplicationContext(),UsersActivity.class);
            startActivity(intent);
            finish();
        });
        binding.layoutSend.setOnClickListener(v->{
            if (binding.messageToSend.getText().toString().isEmpty())
                return;
            else
                sendMessage();
        });
    }
    private void updateConversation(String message){
        DocumentReference documentReference=
                firestore.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .document(conversationId);
        documentReference.update(Constants.KEY_RECENT_MESSAGE,message,
                Constants.KEY_TIMESTAMP, new Date());
    }

    private void addConversation(HashMap<String,Object> conversation){
        firestore.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId=documentReference.getId());
    }
    private String getDateFormat(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private  final EventListener<QuerySnapshot> eventListener=(value,error)->{
        if(error!=null){
            return;
        }
        if(value!=null){
            int count=messages.size();
            for (DocumentChange documentChange:value.getDocumentChanges()){
                if (documentChange.getType()== DocumentChange.Type.ADDED){
                    ChatMessage message=new ChatMessage();
                    message.senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    message.receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    message.message=documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    message.date=getDateFormat(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    message.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    messages.add(message);
                }
                Collections.sort(messages,(obj1,obj2)->obj1.dateObject.compareTo(obj2.dateObject));
                if (count==0){
                    chatAdapter.notifyDataSetChanged();
                }
                else {
                    chatAdapter.notifyItemRangeInserted(messages.size(),messages.size());
                    binding.chatsRecyclerView.smoothScrollToPosition(messages.size()-1);
                }
                binding.chatsRecyclerView.setVisibility(View.VISIBLE);
            }
            binding.progressCircular4.setVisibility(View.GONE);
            if (conversationId==null){
                checkForConversation();
            }
        }
    };
    //Listening to incoming messages
    private void listenMessages(){
        firestore.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receivedUser.id)
                .addSnapshotListener(eventListener);

        firestore.collection((Constants.KEY_COLLECTION_CHAT))
                .whereEqualTo(Constants.KEY_SENDER_ID,receivedUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    //Checking for conversation remotely
    private void checkForConversationRemotely(String senderId, String receiverId){
        firestore.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener= task->{
        if (task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot  =task.getResult().getDocuments().get(0);
            conversationId=documentSnapshot.getId();
        }
    };

    private void checkForConversation(){
        if(messages.size()!=0){
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receivedUser.id);
            checkForConversationRemotely(receivedUser.id, preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }
    private void listenAvailabilityOfReceiver(){
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(receivedUser.id)
                .addSnapshotListener(ChatActivity.this,(value,error)->{
            if(error!=null){
                return;
            }
            if (value!=null){
                if (value.getLong(Constants.KEY_AVAILABILITY_STATUS)!=null){
                    int availability= Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY_STATUS)
                    ).intValue();
                    isReceiverAvailable =availability==1;
                }
                receivedUser.token=value.getString(Constants.KEY_FCM_TOKEN);
                if (receivedUser.image!=null){
                    receivedUser.image=value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receivedUser.image));
                    chatAdapter.notifyItemRangeChanged(0,messages.size());
                }
            }
            if (isReceiverAvailable){
                binding.availabilityStatus.setVisibility(View.VISIBLE);
            }
            else{
                binding.availabilityStatus.setVisibility(View.GONE);
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //Sending Notifications
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class)
                .sendMessage(Constants.getRemoteMessageHeaders(),messageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if(response.isSuccessful()){
                                try {
                                    if (response.body()!=null){
                                        JSONObject responseJson= new JSONObject(response.body());
                                        JSONArray results= responseJson.getJSONArray("results");
                                        if (responseJson.getInt("failure")==1){
                                            JSONObject error=(JSONObject) results.get(0);
                                            showToast(error.getString("error"));
                                            return;
                                        }
                                    }
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                                showToast("Notification Sent Successfully");
                            }
                            else{
                               showToast("Error: "+response.code());
                            }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        showToast(t.getMessage());
                    }
                });
    }

}