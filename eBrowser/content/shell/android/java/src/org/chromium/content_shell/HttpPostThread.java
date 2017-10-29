package org.chromium.content_shell;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by shuai on 2017/7/19.
 */

public class HttpPostThread extends Thread {
    private Handler handler;
    private Context mContext;
    private String url;
    //private static final String TAG = "CLASSHttpPostThread";

    public HttpPostThread(String url, Context mContext, Handler handler){
        this.url = url;
        this.handler = handler;
        this.mContext = mContext;
    }

    @Override
    public void run() {
        try {
            URL httpUrl = new URL(url);
            try {
                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setReadTimeout(10 * 1000);
                conn.setRequestMethod("GET");

                conn.setDoInput(true);

                String result = null;
                if(conn.getResponseCode()==200){
                    InputStream in = conn.getInputStream();
                    byte[] data = new byte[2048];
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    int count = -1;
                    while((count = in.read(data,0,2048)) != -1)
                        outStream.write(data, 0, count);
                    result = new String(outStream.toByteArray(),"ISO-8859-1");
                }
		String temp = "失败";
		if(!result.isEmpty()){temp = "成功";}
		final String msg = temp;
		
		
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
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
