package com.bupt.forum.network;

import android.content.Context;
import android.graphics.Bitmap;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.bupt.forum.global.ContextApplication;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.IOException;

import okhttp3.OkHttpClient;


public class PicassoHelper
{
    private volatile static PicassoHelper m_picassoHelper;
    private Picasso picasso;

    private PicassoHelper()
    {
        Context context = ContextApplication.getAppContext();
        OkHttpClient client = OkHttpHelper.getM_OkHttpHelper().getOkHttpClient();
        OkHttp3Downloader downloader = new OkHttp3Downloader(client);
        picasso = new Picasso.Builder(context).downloader(downloader).build();
    }

    /**
     * 使用单例模式
     *
     * @return
     */
    public static PicassoHelper getPicassoHelper()
    {
        if (m_picassoHelper == null)
        {
            synchronized (PicassoHelper.class)
            {
                if (m_picassoHelper == null)
                {
                    m_picassoHelper = new PicassoHelper();
                }
            }
        }
        return m_picassoHelper;
    }

    /**
     * 获取图片
     * @param url  图片链接
     * @param zoom 缩小的比例
     * @return 图片
     */
    public Bitmap getBitmap(String url, final int zoom)
    {
        if(url == null)
        {
            return null;
        }

        Bitmap bitmap_large = null;

        if(zoom == 1)
        {
            try
            {
                bitmap_large = picasso.load(url).get();
                return bitmap_large;
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }


        Transformation transformation = new Transformation()
        {
            @Override
            public Bitmap transform(Bitmap source)
            {
                double z = Math.log(zoom+1)/Math.log(2);
                int targetwidth = (int)(source.getWidth() / z);
                int targetheight = (int)(source.getHeight() / z);

                Bitmap result = Bitmap.createScaledBitmap(source, targetwidth, targetheight, false);
                if(result != source)
                    source.recycle();
                return result;
            }

            @Override
            public String key()
            {
                return "transformation" + " desiredWidth";
            }
        };

        try
        {
            bitmap_large = picasso.load(url).transform(transformation).get();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return bitmap_large;
    }

    /**
     * 当可以直接使用 load(url).into(ImageView)时，调用此方法
     * @param url 图片链接
     * @param zoom 缩小的比例
     * @return
     */
    public RequestCreator loadImage(String url, final int zoom)
    {
        Transformation transformation = new Transformation()
        {
            @Override
            public Bitmap transform(Bitmap source)
            {
                double z = Math.log(zoom+1)/Math.log(2);
                int targetwidth = (int)(source.getWidth() / z);
                int targetheight = (int)(source.getHeight() / z);

                Bitmap result = Bitmap.createScaledBitmap(source, targetwidth, targetheight, false);
                if(result != source)
                    source.recycle();
                return result;
            }

            @Override
            public String key()
            {
                return "transformation" + " desiredWidth";
            }
        };

        return picasso.load(url).transform(transformation);
    }
}
