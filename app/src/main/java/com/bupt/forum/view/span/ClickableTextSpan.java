package com.bupt.forum.view.span;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import com.bupt.forum.activity.BoardArticleListActivity;
import com.bupt.forum.activity.ReadArticleActivity;
import com.bupt.forum.eventtype.Event;
import com.bupt.forum.sdkutil.BYR_BBS_API;

import de.greenrobot.event.EventBus;

/**
 * Created by Lue on 2016/5/17.
 */
public class ClickableTextSpan extends ClickableSpan
{
    private Context context;
    private final String text;
    private final String url;

    public ClickableTextSpan(Context context, String text, String url)
    {
        super();
        this.context = context;
        this.text = text;
        this.url = url;
    }
    

    @Override
    public void updateDrawState(TextPaint ds)
    {
        ds.setColor(Color.BLUE); //设置链接的文本颜色
//        ds.setUnderlineText(false); //去掉下划线
    }

    @Override
    public void onClick(View widget)
    {
        if(url.contains("bbs.byr.cn/"))
        {
            if (url.contains("article"))
            {
                int index1 = url.lastIndexOf("/");
                String id = url.substring(index1 + 1);
                int article_id;
                try
                {
                    article_id = Integer.parseInt(id);
                } catch (NumberFormatException e)
                {
                    Toast.makeText(context, "Oops, 网址格式有错误！", Toast.LENGTH_SHORT).show();
                    return;
                }
                //通知原Activity，现在将要开启一个新的Activity，原来的需要注销EventBus
                EventBus.getDefault().post(new Event.Start_New());

                String text1 = url.substring(0, index1);
                int index2 = text1.lastIndexOf("/");
                String board_name = text1.substring(index2 + 1);

                Intent intent = new Intent(context, ReadArticleActivity.class);
                intent.putExtra("board_name", board_name);
                intent.putExtra("article_id", article_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            else if (url.contains("board"))
            {
                int index = url.lastIndexOf("/");
                String board_name = url.substring(index + 1);

                //本地 SharedPreferences
                SharedPreferences My_SharedPreferences;

                My_SharedPreferences = context.getSharedPreferences("My_SharePreference", Context.MODE_PRIVATE);

                String board_description = My_SharedPreferences.getString(board_name, "null");

                if (!board_description.equals("null"))
                {
                    //通知原Activity，现在将要开启一个新的Activity，原来的需要注销EventBus
                    EventBus.getDefault().post(new Event.Start_New());

                    Intent intent = new Intent(context, BoardArticleListActivity.class);
                    intent.putExtra("Board_Description", board_description);
                    boolean is_favorite = !(BYR_BBS_API.Favorite_Boards.get(board_description) == null);
                    intent.putExtra("Is_Favorite", is_favorite);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else
                {
                    Toast.makeText(context, "Oops, 网址格式有错误！", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            Intent intent = null;
            if (url.contains("http") || url.contains("https") || url.contains("www."))
            {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }

            else if (url.contains("@") && url.contains(".com"))
            {
                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + url));
            }

            else
            {
                intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+url));
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }
}
