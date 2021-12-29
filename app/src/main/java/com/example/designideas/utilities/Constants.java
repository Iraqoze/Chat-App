package com.example.designideas.utilities;

import com.example.designideas.BuildConfig;

import java.util.HashMap;
import java.util.Properties;

public class Constants {
    public  static  final String KEY_COLLECTION_USERS="users";
    public static final String KEY_NAME="name";
    public  static final String KEY_EMAIL="email";
    public static final String KEY_PASSWORD="password";
    public static final String KEY_PREFERENCE_NAME="chatAppPreference";
    public static  final String KEY_IS_SIGNED_IN="isSignedIn";
    public static final String KEY_USER_ID="userId";
    public static final String KEY_MAUTH_ID="mAuthId";
    public  static final String KEY_IMAGE="image";
    public  static final String KEY_FCM_TOKEN="fcmToken";
    public static final String KEY_USER="user";
    public static final String KEY_COLLECTION_CHAT="chat";
    public static final String KEY_SENDER_ID="senderId";
    public static final String KEY_RECEIVER_ID="receiverId";
    public static final String KEY_MESSAGE="message";
    public static final String KEY_TIMESTAMP="timeStamp";

    public static final String KEY_COLLECTION_CONVERSATION="conversation";
    public static final String KEY_SENDER_NAME="senderName";
    public static final String KEY_RECEIVER_NAME="receiverName";
    public static final String KEY_SENDER_IMAGE="senderImage";
    public static final String KEY_RECEIVER_IMAGE="receiverImage";
    public static final String KEY_RECENT_MESSAGE="recentMessage";
    public static final String KEY_AVAILABILITY_STATUS="availabilityStatus";

    public static final String KEY_REMOTE_MSG_AUTHORIZATION="Authorization";
    public static final String KEY_REMOTE_MSG_CONTENT_TYPE="Content-Type";
    public static final String KEY_REMOTE_MSG_CONTENT="data";
    public static final String KEY_REMOTE_MSG_REGISTRATION_IDS="registration_ids";

    public static HashMap<String, String> remoteMessageHeaders=null;
    public static HashMap<String, String> getRemoteMessageHeaders(){
        if (remoteMessageHeaders==null){
            remoteMessageHeaders= new HashMap<>();
            remoteMessageHeaders.put(KEY_REMOTE_MSG_AUTHORIZATION,BuildConfig.API_KEY_AUTH);
            remoteMessageHeaders.put(KEY_REMOTE_MSG_CONTENT_TYPE,"application/json");
        }

        return remoteMessageHeaders;
    }

}
