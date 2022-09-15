package com.lhl.imagecheck;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import java.util.HashMap;
import java.util.Map;

class ImageCheck implements Application.ActivityLifecycleCallbacks {
    private static float rate = 1.2f;
    private static int maxDeep = 5;

    ImageCheck(Context context) {
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
    }

    Map<Activity, FragmentManager.FragmentLifecycleCallbacks> callbacksMap = new HashMap<>();
    Map<Activity, androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks> androidxCallbacksMap = new HashMap<>();

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            FL fl = new FL();
            activity.getFragmentManager().registerFragmentLifecycleCallbacks(fl, true);
            callbacksMap.put(activity, fl);
        }
        if (activity instanceof FragmentActivity) {
            AFL afl = new AFL();
            androidxCallbacksMap.put(activity, afl);
            ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(afl, true);
        }

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        checkViewDeep(activity.getClass().getName(), ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0));
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        checkImage(activity);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        callbacksMap.remove(activity);
        androidxCallbacksMap.remove(activity);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static class FL extends FragmentManager.FragmentLifecycleCallbacks {

        @Override
        public void onFragmentDetached(FragmentManager fm, Fragment f) {
            checkImage(f);
        }

        @Override
        public void onFragmentResumed(FragmentManager fm, Fragment f) {
            super.onFragmentResumed(fm, f);
            checkViewDeep(f.getClass().getName(), f.getView());
        }
    }

    static class AFL extends androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks {
        @Override
        public void onFragmentDetached(@NonNull androidx.fragment.app.FragmentManager fm, @NonNull androidx.fragment.app.Fragment f) {
            checkImage(f);
        }

        @Override
        public void onFragmentResumed(@NonNull androidx.fragment.app.FragmentManager fm, @NonNull androidx.fragment.app.Fragment f) {
            super.onFragmentResumed(fm, f);
            checkViewDeep(f.getClass().getName(), f.getView());
        }
    }

    static void checkImage(Activity activity) {
        checkImage("", activity.getClass().getName(), ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0));
    }

    static void checkImage(androidx.fragment.app.Fragment fragment) {
        checkImage("", fragment.getClass().getName(), fragment.getView());
    }

    static void checkImage(Fragment fragment) {
        checkImage("", fragment.getClass().getName(), fragment.getView());
    }

    static void checkImage(String parent, String name, View c) {
        if (name == null || name.isEmpty() || c == null)
            return;
        Drawable drawable = c.getBackground();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            log(parent, name, 0, (BitmapDrawable) drawable, c);
        }

        if (c instanceof ImageView) {
            boolean hasBackground = drawable != null;
            drawable = ((ImageView) c).getDrawable();
            if (drawable != null) {
                if (hasBackground)
                    log(parent, name, 2, (BitmapDrawable) drawable, c);
                if (drawable instanceof BitmapDrawable)
                    log(parent, name, 1, (BitmapDrawable) drawable, c);
            }

        } else if (c instanceof ViewGroup) {
            int childCount = ((ViewGroup) c).getChildCount();
            for (int i = 0; i < childCount; i++)
                checkImage(String.format("%s->%s", parent, c.getClass().getSimpleName()), name, ((ViewGroup) c).getChildAt(i));
        }
    }

    static void checkViewDeep(String name, View c) {
        if (c == null)
            return;
        checkViewDeep(0, "", name, c);
    }

    static void checkViewDeep(int deep, String parent, String name, View c) {
        deep++;
        if (deep > maxDeep) {
            Log.e("ImageCheck", String.format("fragmentOrActivity:%s,path:%s,view deep > %s", name, String.format("%s->", parent, c.getClass().getSimpleName()), maxDeep));
            return;
        }
        if(deep==maxDeep){
            if(c instanceof ViewGroup && ((ViewGroup) c).getChildCount()>0){
                Log.e("ImageCheck", String.format("fragmentOrActivity:%s,path:%s,view deep > %s", name, String.format("%s->%s", parent, c.getClass().getSimpleName()), maxDeep));
            }
            return;
        }
        if(c instanceof ViewGroup){
            int childCount = ((ViewGroup) c).getChildCount();
            for (int i =0;i<childCount;i++)
                checkViewDeep(deep,String.format("%s->%s",parent,c.getClass().getSimpleName()),name,((ViewGroup) c).getChildAt(i));
        }
    }

    static void log(String parent, String name, int background, BitmapDrawable drawable, View c) {
        Bitmap bitmap = drawable.getBitmap();
        if (bitmap == null)
            return;
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        int vW = c.getWidth();
        int vH = c.getHeight();
        if (vW * rate > bw && vH * rate > bh)
            return;
        String view = c.getClass().getSimpleName();
        int index = parent.indexOf("FrameLayout->");
        if (index > 0)
            parent = parent.substring(index + "FrameLayout->".length());
        String id = c.getId() == View.NO_ID ? "" : c.getResources().getResourceName(c.getId());
        Log.e("ImageCheck", String.format("fragmentOrActivityName:%s,view:%s->%s,type:%s,id:%s,viewWidth:%s*%s,bitmapWidth:%s*%s",
                name, parent, view, background == 0 ? "background" : background == 1 ? "image" : "background and image", id, vW, vH, bw, bh));
    }

}
