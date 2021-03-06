package com.example.gamebrowser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.Utils;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * Created by WorldSkills2020 on 10/23/2019.
 */

public class FrmBrowser extends StandOutWindow {

    public static boolean isRunning = false;


    public HashMap<String,String> clipboardFinder = new HashMap<>();

    public static String baseUrl = SettingActivity.defaultPage;
    WebView mWebView;

    @Override
    public String getAppName() {
        return Utils.getSP(this).getString("windowtext","使用说明")+" - 电竞浏览器";
    }

    @Override
    public int getAppIcon() {
        return R.drawable.ic;
    }


    private void getSelectedData() {
        String js = "(function getSelectedText() {" +
                "var txt;" +
                "if (window.getSelection) {" +
                "txt = window.getSelection().toString();" +
                "} else if (window.document.getSelection) {" +
                "txt = window.document.getSelection().toString();" +
                "} else if (window.document.selection) {" +
                "txt = window.document.selection.createRange().text;" +
                "}else{txt=\"\";}" +
                "CopyInterfaceCallback.onSelectionCallback(txt);" +
                "})()";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript("javascript:" + js, null);
        } else {
            mWebView.loadUrl("javascript:" + js);
        }
    }


    class SelectionCallback{
        @JavascriptInterface
        public void onSelectionCallback(final String text){
            if(text.length()>0){
                new Utils.EditDialog(FrmBrowser.this, "复制文本", text) {
                    @Override
                    public void onConfirmText(String text) {
                        setClipboard(text);
                        Toast.makeText(FrmBrowser.this, "文本已复制到剪切板", Toast.LENGTH_SHORT).show();
                    }
                }.show();
            }
            else{
                Toast.makeText(FrmBrowser.this, "没有选中文本", Toast.LENGTH_SHORT).show();
            }
        }

    }


    private int thisID = -1;
    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        baseUrl = Utils.getSP(this).getString("url",SettingActivity.defaultPage);
        thisID = id;
        isRunning = true;

        this.mWebView = new WebView(this);

        mWebView.addJavascriptInterface(new SelectionCallback(),"CopyInterfaceCallback");

        frame.addView(mWebView);
        renderW = Utils.getSP(this).getInt("rw",1280);
        renderH = Utils.getSP(this).getInt("rh",720);

        WebGameBoostEngine.boost(this, mWebView, baseUrl, new WebGameBoostEngine.OnTitleChangedListener() {
            @Override
            public void onTitleChanged(String title) {
                setTitle(thisID,title+" - 电竞浏览器");
            }

            @Override
            public void onIconChanged(Bitmap icon) {
                setIcon(thisID,icon);
            }
        });



        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mWebView.getLayoutParams();
        frame.setClickable(true);
        lp.width=renderW;
        lp.height = renderH;
        mWebView.setLayoutParams(lp);
        lp.gravity = Gravity.CENTER;
        rootView = frame;
        scaleView();

        mWebView.loadUrl(baseUrl);


    }

    int renderW=854,renderH=480;


    public void setClipboard(String text){
        ClipboardManager manager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText(null, text));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    FrameLayout rootView;
    void scaleView(){
        float pw = rootView.getWidth();
        float ph = rootView.getHeight();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mWebView.getLayoutParams();
        if(pw==0 || ph==0){
            Log.e("SCALE","View not initialized");
            return;
        }
        if(pw>=ph){
            lp.width=renderW;
            lp.height = renderH;
            if(pw / renderW * renderH < ph){
                //屏幕更高的场合
                mWebView.setScaleX(pw/renderW);
                mWebView.setScaleY(pw/renderW);
            }
            else{
                //屏幕更窄的场合
                mWebView.setScaleX(ph/renderH);
                mWebView.setScaleY(ph/renderH);
            }
            mWebView.setRotation(0);
        }
        else{
            lp.width=renderW;
            lp.height = renderH;

            float temp = pw;
            pw=ph;ph=temp;

            if(pw / renderW * renderH < ph){
                //屏幕更高的场合
                mWebView.setScaleX(pw/renderW);
                mWebView.setScaleY(pw/renderW);
            }
            else{
                //屏幕更窄的场合
                mWebView.setScaleX(ph/renderH);
                mWebView.setScaleY(ph/renderH);
            }

            mWebView.setRotation(reserveGraphic ? -90 : 90);
        }


        mWebView.setLayoutParams(lp);
    }

    @Override
    public void onResize(int id, Window window, View view, MotionEvent event) {
        super.onResize(id, window, view, event);
        if(null!=rootView){
            scaleView();
        }
    }

    public static String paste(Context context) {
        try {
            ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if(cmb.getPrimaryClip().getItemCount() > 0)
            return cmb.getPrimaryClip().getItemAt(0).getText().toString().trim();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    Handler hWnd = new Handler();

    @Override
    public Animation getCloseAnimation(int id) {
        AnimationSet anims = new AnimationSet(false);
        StandOutLayoutParams orignal = getWindow(id).getLayoutParams();
        ScaleAnimation scale = new ScaleAnimation(1,0.75f,1f,0.75f,orignal.width/2,orignal.height/2);
        scale.setDuration(400);
        scale.setFillAfter(true);
        scale.setInterpolator(this,android.R.anim.accelerate_interpolator);
        anims.addAnimation(scale);
        AlphaAnimation alpha = new AlphaAnimation(1,0);
        alpha.setDuration(300);
        alpha.setFillAfter(true);
        anims.addAnimation(alpha);
        isAnimShow = false;
        return anims;
    }

    @Override
    public Animation getShowAnimation(int id) {
        if(isAnimShow) {
            StandOutLayoutParams orignal = getWindow(id).getLayoutParams();
            int sourceX = orignal.x + orignal.width /2;
            int sourceY = orignal.y + orignal.height /2;
            int destX = Utils.getSP(this).getInt("bx", 0) + dip2px(this,18);
            int destY = Utils.getSP(this).getInt("by", 0) + dip2px(this,18);

            float xscale = (float)dip2px(this,36) / (float)orignal.width;
            float yscale = (float)dip2px(this,36) / (float)orignal.height;

            AnimationSet anims = new AnimationSet(false);

            TranslateAnimation translate = new TranslateAnimation(destX-sourceX,0,destY-sourceY,0);
            translate.setDuration(400);
            translate.setFillAfter(true);
            translate.setInterpolator(this,android.R.anim.accelerate_interpolator);
            ScaleAnimation scale = new ScaleAnimation(xscale,1f,yscale,1f,orignal.width/2,orignal.height/2);
            scale.setDuration(400);
            scale.setFillAfter(true);
            scale.setInterpolator(this,android.R.anim.accelerate_interpolator);
            anims.addAnimation(scale);
            anims.addAnimation(translate);
            AlphaAnimation alpha = new AlphaAnimation(0,1);
            alpha.setDuration(300);
            alpha.setFillAfter(true);
            alpha.setStartOffset(100);
            anims.addAnimation(alpha);
            isAnimShow = false;
            return anims;
        }
        else{

            AnimationSet anims = new AnimationSet(false);
            ScaleAnimation scale = new ScaleAnimation(0.75f,1f,0.75f,1f,ScaleAnimation.RELATIVE_TO_SELF,0.5f,ScaleAnimation.RELATIVE_TO_SELF,0.5f);
            scale.setDuration(400);
            scale.setFillAfter(true);
            scale.setInterpolator(this,android.R.anim.decelerate_interpolator);
            anims.addAnimation(scale);
            AlphaAnimation alpha = new AlphaAnimation(0,1);
            alpha.setDuration(300);
            alpha.setFillAfter(true);
            alpha.setStartOffset(100);
            anims.addAnimation(alpha);
            isAnimShow = false;
            return anims;
        }

    }

    @Override
    public Animation getHideAnimation(int id) {
        if(isAnimHide) {
            StandOutLayoutParams orignal = getWindow(id).getLayoutParams();
            int sourceX = orignal.x + orignal.width /2;
            int sourceY = orignal.y + orignal.height /2;
            int destX = Utils.getSP(this).getInt("bx", 0) + dip2px(this,18);
            int destY = Utils.getSP(this).getInt("by", 0) + dip2px(this,18);

            float xscale = (float)dip2px(this,36) / (float)orignal.width;
            float yscale = (float)dip2px(this,36) / (float)orignal.height;

            AnimationSet anims = new AnimationSet(false);

            TranslateAnimation translate = new TranslateAnimation(0,destX-sourceX,0,destY-sourceY);
            translate.setDuration(400);
            translate.setFillAfter(true);
            translate.setInterpolator(this,android.R.anim.accelerate_interpolator);
            ScaleAnimation scale = new ScaleAnimation(1f,xscale,1f,yscale,orignal.width/2,orignal.height/2);
            scale.setDuration(400);
            scale.setFillAfter(true);
            scale.setInterpolator(this,android.R.anim.accelerate_interpolator);
            anims.addAnimation(scale);
            anims.addAnimation(translate);
            AlphaAnimation alpha = new AlphaAnimation(1,0);
            alpha.setDuration(300);
            alpha.setFillAfter(true);
            anims.addAnimation(alpha);
            isAnimHide = false;
            return anims;
        }
        else{
            AnimationSet anims = new AnimationSet(false);
            StandOutLayoutParams orignal = getWindow(id).getLayoutParams();
            ScaleAnimation scale = new ScaleAnimation(1,0.75f,1f,0.75f,orignal.width/2,orignal.height/2);
            scale.setDuration(400);
            scale.setFillAfter(true);
            scale.setInterpolator(this,android.R.anim.accelerate_interpolator);
            anims.addAnimation(scale);
            AlphaAnimation alpha = new AlphaAnimation(1,0);
            alpha.setDuration(300);
            alpha.setFillAfter(true);
            anims.addAnimation(alpha);
            isAnimShow = false;
            return anims;
        }
    }

    public static boolean isAnimShow = false;
    @Override
    public boolean onShow(int id, Window window) {

        StandOutLayoutParams lp = window.getLayoutParams();

        lp.x = Utils.getSP(this).getInt("wx",100);
        lp.y = Utils.getSP(this).getInt("wy",100);
        lp.width = Utils.getSP(this).getInt("ww",dip2px(this,240));
        lp.height = Utils.getSP(this).getInt("wh",dip2px(this,200));


        window.setLayoutParams(lp);

        hWnd.postDelayed(resizer,300);

        return super.onShow(id, window);

    }

    public static boolean isAnimHide = false;
    @Override
    public boolean onHide(int id, Window window) {
        startService(new Intent(getApplicationContext(),WindowService.class));
        Utils.getSP(this).edit().putInt("wx",window.getLayoutParams().x)
                .putInt("wy",window.getLayoutParams().y)
                .putInt("ww",window.getLayoutParams().width)
                .putInt("wh",window.getLayoutParams().height).commit();
        isAnimHide = true;
        return super.onHide(id, window);
    }
    @Override
    public boolean onClose(int id, Window window) {
        Utils.getSP(this).edit().putInt("wx",window.getLayoutParams().x)
                .putInt("wy",window.getLayoutParams().y)
                .putInt("ww",window.getLayoutParams().width)
                .putInt("wh",window.getLayoutParams().height).commit();
        mWebView.destroy();
        isRunning = false;
        return super.onClose(id, window);
    }
    @Override
    public List<DropDownListItem> getDropDownItems(int id) {
        List<DropDownListItem> list = new ArrayList<>();
        list.add(new DropDownListItem(android.R.drawable.ic_menu_rotate,"刷新",new Runnable(){
            @Override
            public void run() {
                Utils.Confirm(getApplicationContext(), "是否重新载入？", new Runnable() {
                    @Override
                    public void run() {
                        mWebView.reload();
                    }
                });
            }
        }));
        list.add(new DropDownListItem(android.R.drawable.ic_menu_view, "隐藏", new Runnable() {
            @Override
            public void run() {
                hide(StandOutWindow.DEFAULT_ID);
            }
        }));


        list.add(new DropDownListItem(android.R.drawable.ic_menu_close_clear_cancel, "退出", new Runnable() {
            @Override
            public void run() {
                Utils.Confirm(getApplicationContext(), "是否退出？", new Runnable() {
                    @Override
                    public void run() {
                        close(StandOutWindow.DEFAULT_ID);
                    }
                });
            }
        }));

        list.add(new DropDownListItem(android.R.drawable.ic_menu_edit, "复制选中内容", new Runnable() {
            @Override
            public void run() {
                getSelectedData();
            }
        }));

        list.add(new DropDownListItem(android.R.drawable.ic_menu_crop, "翻转画面", new Runnable() {
            @Override
            public void run() {
               reserveGraphic=!reserveGraphic;
                hWnd.post(resizer);
            }
        }));
        return list;
    }

    private boolean reserveGraphic = false;

    @SuppressWarnings("WrongConstant")
    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        StandOutLayoutParams slp = new StandOutLayoutParams(id, dip2px(this,240), dip2px(this,200),
                StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER,dip2px(this,200), dip2px(this,150));
        slp.type = Utils.getFlagCompat();
        return slp;
    }

    public int getFlags(int id) {
        int flat = super.getFlags(id)
                |StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
                |StandOutFlags.FLAG_BODY_MOVE_ENABLE
                |StandOutFlags.FLAG_DECORATION_SYSTEM;

        return flat;
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return getResources().getString(R.string.app_name)+" 正在使用窗口";
    }

    Runnable resizer = new Runnable() {
        @Override
        public void run() {
            if(null!=rootView){
                if(rootView.getWidth()>0) {
                    scaleView();
                    return;
                }
            }
            hWnd.postDelayed(this,300);
        }
    };

    @Override
    public void onWindowStateChanged(int id, Window window, View view) {
        super.onWindowStateChanged(id, window, view);
        hWnd.postDelayed(resizer,300);
    }

    @Override
    public Notification getPersistentNotification(int id) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("Windows","窗口服务驻留通知", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        int icon = R.drawable.ic_notification_icon;
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String contentTitle = getPersistentNotificationTitle(id);
        String contentText = getPersistentNotificationMessage(id);
        String tickerText = String.format("%s: %s", contentTitle, contentText);

        // getPersistentNotification() is called for every new window
        // so we replace the old notification with a new one that has
        // a bigger id
        Intent notificationIntent = getPersistentNotificationIntent(id);

        PendingIntent contentIntent = null;

        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, 0,
                    notificationIntent,
                    // flag updates existing persistent notification
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification.Builder notification =null;

        if (Build.VERSION.SDK_INT >= 26) {
            notification = new Notification.Builder(c,"Windows");
        }
        else{
            notification= new Notification.Builder(c);
        }
        notification.setSmallIcon(icon);
        notification.setTicker(tickerText);
        notification.setWhen(when);

        notification.setContentText(contentTitle);
        notification.setContentText(contentText);
        notification.setContentIntent(contentIntent);

        //notification.setLatestEventInfo(c, contentTitle, contentText,contentIntent);

        Notification noti = notification.build();

        return noti;
    }

    @Override
    public boolean onFocusChange(int id, Window window, boolean focus) {
        if(focus){
            window.setAlpha(1.0f);
        }
        else{
            window.setAlpha(0.7f);
        }

        return super.onFocusChange(id, window, focus);
    }

    @Override
    public void onCustomButton1Click(final int id) {
        super.onCustomButton1Click(id);
    }

    @Override
    public Notification getHiddenNotification(int id) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rootView.removeAllViews();
        try{
            mWebView.destroy();
        }catch (Exception ex){}
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
