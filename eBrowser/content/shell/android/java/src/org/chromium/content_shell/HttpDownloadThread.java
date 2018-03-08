package org.chromium.content_shell;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;
import org.chromium.base.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by shuai on 2017/6/3.
 */

public class HttpDownloadThread extends Thread{
    private Handler handler;
    private Context mContext;
    private String url;
    private static final String TAG = "eBrowser.HttpDownloadThread";
    //private static final String TAG = "CLASSHttpDownloadThread";
    public HttpDownloadThread(String url, Context mContext, Handler handler) {
        this.url = url;
        this.handler = handler;
        this.mContext = mContext;
    }
    @Override
    public void run() {
        try {
            URL httpUrl = new URL(url);
            File downloadFile = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setReadTimeout(10 * 60 * 1000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                InputStream in = conn.getInputStream();
                FileOutputStream out = null;
                String fileName = url.substring(url.lastIndexOf("=")+1);

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String parent = Environment.getExternalStorageDirectory().getAbsolutePath() + "/libsvm/";
                    downloadFile = new File(parent, fileName);
                    out = new FileOutputStream(downloadFile);
                }
                byte[] b = new byte[2 * 1024];
                int len;
                if (out != null) {
                    while ((len = in.read(b)) != -1) {
                        out.write(b, 0, len);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                     Log.w(TAG, "end-time: %s ", System.currentTimeMillis());
                     Toast.makeText(mContext,"model downloading success",Toast.LENGTH_SHORT).show();
                 }
             });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        super.run();
    }
}
