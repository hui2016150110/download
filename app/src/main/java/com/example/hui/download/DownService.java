package com.example.hui.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

/**
 * Created by hui on 2019/3/23.
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class DownService extends Service {
    final String CHANNEL_ID = "com.hui.downloadTask";
    final String CHANNEL_NAME = "com.hui.downloadTask.name";
    NotificationChannel mNotificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
    private DownloadTask mDownloadTask;
    private String downloadUrl;
    private DownloadListener mListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().createNotificationChannel(mNotificationChannel);
            getNotificationManager().notify(1,getNotofication("正在下载...",progress));
        }

        @Override
        public void onSuccess() {
            mDownloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotofication("下载成功",-1));
            Toast.makeText(DownService.this,"Success",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            mDownloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotofication("下载失败",-1));
            Toast.makeText(DownService.this,"Failed",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            mDownloadTask = null;
            Toast.makeText(DownService.this,"Paused",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            mDownloadTask = null;
            Toast.makeText(DownService.this,"Canceled",Toast.LENGTH_SHORT).show();
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder{
        public void startDownload(String url){
            if(mDownloadTask == null){
                downloadUrl = url;
                mDownloadTask = new DownloadTask(mListener);
                mDownloadTask.execute(downloadUrl);
                startForeground(1,getNotofication("正在下载...",0));
                Toast.makeText(DownService.this,"正在下载",Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload(){
            if(mDownloadTask!=null){
                mDownloadTask.pauseDownload();
            }
        }
        public void cancelDownload(){
            if(mDownloadTask!=null){
                mDownloadTask.cancelDownload();
            }else{
                if(downloadUrl!=null){
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory+fileName);
                    if(file.exists())
                        file.delete();
                    getNotificationManager().cancel(1);
                    stopForeground(true);

                }
            }
        }

    }



    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotofication(String title,int progress){
        Intent intent = new Intent(this, DownloadAcivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
        builder.setSmallIcon(R.drawable.a1);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.a1));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        if(progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }
}
