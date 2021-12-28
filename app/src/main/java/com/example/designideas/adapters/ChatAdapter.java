package com.example.designideas.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.designideas.databinding.ItemContainerMessageReceivedBinding;
import com.example.designideas.databinding.ItemContainerMessageSentBinding;
import com.example.designideas.entities.ChatMessage;
import com.example.designideas.listeners.ConversationListener;
import com.example.designideas.utilities.Constants;
import com.example.designideas.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
private final List<ChatMessage>messages;
private Bitmap receiverProfileImage;
private final Bitmap userProfileImage;
private final String senderId;
public static int VIEW_TYPE_SENT=1;
public static int VIEW_TYPE_RECEIVED=2;

    public ChatAdapter(List<ChatMessage> messages, Bitmap receiverProfileImage, Bitmap userProfileImage, String senderId) {
        this.messages = messages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.userProfileImage=userProfileImage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENT){
            return new SentMessageViewHolder(ItemContainerMessageSentBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,false
            ));
        }
        else
            return new ReceivedMessageViewHolder(ItemContainerMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,false
            ));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position)==VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(messages.get(position),userProfileImage);
        }
        else{
            ((ReceivedMessageViewHolder)holder).setData(messages.get(position),receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public int getItemViewType(int position) {
        if (messages.get(position).senderId.equals(senderId))
            return VIEW_TYPE_SENT;
        else
        return VIEW_TYPE_RECEIVED;
    }

    static  class SentMessageViewHolder extends RecyclerView.ViewHolder{
    private final ItemContainerMessageSentBinding binding;
     SentMessageViewHolder(ItemContainerMessageSentBinding itemContainerMessageSentBinding){
         super(itemContainerMessageSentBinding.getRoot());
         binding=itemContainerMessageSentBinding;
     }
    private void setData(ChatMessage message, Bitmap bitmap){
         binding.textMessageSent.setText(message.message);
         binding.textDateSent.setText(message.date);
         binding.imageProfileSent.setImageBitmap(bitmap);
     }
    }

    static class ReceivedMessageViewHolder extends  RecyclerView.ViewHolder{
        private final ItemContainerMessageReceivedBinding binding;
        ReceivedMessageViewHolder(ItemContainerMessageReceivedBinding itemContainerMessageReceivedBinding){
            super(itemContainerMessageReceivedBinding.getRoot());
            binding=itemContainerMessageReceivedBinding;
        }
     private void setData(ChatMessage message, Bitmap bitmap){
            binding.textMessageReceived.setText(message.message);
            binding.textDateRecieved.setText(message.date);
            if (bitmap!=null)
            {
                binding.imageProfileReceived.setImageBitmap(bitmap);
            }
        }
    }

    public static Bitmap getUserImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }
    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage=bitmap;
    }

}
