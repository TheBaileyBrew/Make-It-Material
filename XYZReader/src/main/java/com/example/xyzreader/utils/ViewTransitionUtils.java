package com.example.xyzreader.utils;

import android.app.Activity;
import android.os.Build;
import android.transition.TransitionInflater;

import com.example.xyzreader.R;


public class ViewTransitionUtils {

    static public void setupEnterFadeAnimation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setEnterTransition(TransitionInflater.from(activity).inflateTransition(R.transition.activity_fade));
        }
    }

    static public void setupExitFadeAnimation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setExitTransition(TransitionInflater.from(activity).inflateTransition(R.transition.activity_fade));
        }
    }

    static public void setupEnterExplodeAnimation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setEnterTransition(TransitionInflater.from(activity).inflateTransition(R.transition.activity_explode));
        }
    }

    static public void setupExitExplodeAnimation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setEnterTransition(TransitionInflater.from(activity).inflateTransition(R.transition.activity_explode));
        }
    }

}
