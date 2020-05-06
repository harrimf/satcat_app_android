package com.satcat.kalys;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.satcat.kalys.Managers.SocketIOManager;
import com.satcat.kalys.Managers.UserManager;
import com.satcat.kalys.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.realm.Realm;

public class KalysApplication extends Application implements LifecycleObserver {

    public static boolean IS_APP_IN_FOREGROUND = false;

    private static KalysApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        Realm.init(this);
        FirebaseApp.initializeApp(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("token", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Obtain a Realm instance
                        Realm realm = Realm.getDefaultInstance();

                        if(UserManager.getShared().isOneLocalUser()) {

                            User mainUser = UserManager.getShared().getUser();

                            if (!mainUser.getDeviceToken().equals(token)) {
                                realm.beginTransaction();
                                mainUser.setDeviceToken(token);
                                realm.commitTransaction();

                                SocketIOManager.getShared().adjustToken(token);

                            }
                        }

                        Log.d("successToken", token);
                    }
                });
    }


    public static KalysApplication getContext() {
        return mContext;
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            IS_APP_IN_FOREGROUND = false;
            Log.d("test1", "BACKGROUND");
            SocketIOManager.getShared().closeConnection();


        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appInForeground(){
        IS_APP_IN_FOREGROUND = true;
        Log.d("test", "FOREGROUND");
        SocketIOManager.getShared().establishConnection();



    }

}