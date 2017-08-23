package org.connectbot.util;

import android.app.PendingIntent;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class that is used to communicate with the the current active activity,
 * to start other activities by a PendingIntent and get the result back from the activity.
 */
public class ActivityResultDispatcher {
    private static ActivityResultDispatcher instance;

    private List<OnActivityResultListener> onActivityResultListeners;
    private OnStartForResultListener onStartForResultListener;

    private ActivityResultDispatcher() {
        onActivityResultListeners = new ArrayList<>();
    }

    public static ActivityResultDispatcher getInstance() {
        if (instance == null) {
            instance = new ActivityResultDispatcher();
        }
        return instance;
    }

    /**
     * Registers a listener that gets notified when an Activity result is dispatched
     * @param listener the listener that watches for an activity result.
     */
    public void registerOnActivityResultListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    /**
     * Unregisters a registered OnActivityResultListener
     * @param listener the listener that should be unregistered
     */
    public void unregisterOnActivityResultListener(OnActivityResultListener listener) {
        for (int i = 0; i < onActivityResultListeners.size(); i++) {
            if (onActivityResultListeners.get(i) == listener) {
                onActivityResultListeners.remove(i);
            }
        }
    }

    /**
     * Registers a listener that gets notified when somebody else requests a startForResult
     * @param listener the listner that should be registered
     */
    public void registerOnStartForResultListener(OnStartForResultListener listener) {
        onStartForResultListener = listener;
    }

    /**
     * Unregisters the listener
     */
    public void unregisterOnStartForResultListener() {
        onStartForResultListener = null;
    }

    /**
     * Requests a startForResult from an activity.
     * @param intent Pending Intent that should be send for a result
     * @param requestCode the request Code for sending the PendingIntent
     * @return true if there is an activity registered, false otherwise
     */
    public boolean requestStartForResult(PendingIntent intent, int requestCode) {
        if (onStartForResultListener != null) {
            onStartForResultListener.onStartForResult(intent, requestCode);
            return true;
        }
        return false;
    }

    /**
     * Notifies all registered OnActivityResultListener that there is a result from an activity
     * @param requestCode the requestCode from OnActivityResult
     * @param resultCode the resultCode from OnActivityResult
     * @param data the data from OnActivityResult
     */
    public void dispatchActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnActivityResultListener listener : onActivityResultListeners) {
            if (listener != null) {
                listener.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    interface OnActivityResultListener {
        public void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public interface OnStartForResultListener {
        void onStartForResult(PendingIntent pendingIntent, int requestCode);
    }
}
