package com.example.designideas.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.designideas.databinding.ItemContainerRecentConversationBinding;
import com.example.designideas.entities.ChatMessage;
import com.example.designideas.entities.User;
import com.example.designideas.listeners.ConversationListener;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder> {
    private List<ChatMessage> messages;
    private final ConversationListener conversationListener;
    public RecentConversationAdapter(List<ChatMessage> messages, ConversationListener conversationListener) {
        this.messages = messages;
        this.conversationListener=conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{
    ItemContainerRecentConversationBinding binding;
     public ConversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding ) {
       super(itemContainerRecentConversationBinding.getRoot());
        binding= itemContainerRecentConversationBinding;
     }
     void setData(ChatMessage message){
         binding.imageProfile.setImageBitmap(getConversationImage(message.conversationImage));
         binding.recentMessage.setText(message.message);
         System.out.println("NAMES: "+message.conversationName);
         binding.textFullnames.setText(message.conversationName);
         binding.getRoot().setOnClickListener(v->{
             User user=new User();
             user.id=message.conversationId;
             user.image=message.conversationImage;
             user.name=message.conversationName;
             conversationListener.onConversationClicked(user);
         });
     }

 }

    public static Bitmap getConversationImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }
}
