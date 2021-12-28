package com.example.designideas.entities;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
   public String senderId, receiverId,message, date, conversationName,conversationId,conversationImage;
   public Date dateObject;
}
