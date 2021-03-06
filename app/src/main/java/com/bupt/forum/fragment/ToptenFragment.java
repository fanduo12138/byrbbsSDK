package com.bupt.forum.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.bupt.forum.R;
import com.bupt.forum.activity.ReadArticleActivity;
import com.bupt.forum.adapter.ToptenArticleListAdapter;
import com.bupt.forum.eventtype.Event;
import com.bupt.forum.global.ContextApplication;
import com.bupt.forum.helper.MyDataBaseHelper;
import com.bupt.forum.helper.WidgetHelper;
import com.bupt.forum.metadata.Article;
import com.bupt.forum.sdkutil.BYR_BBS_API;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import de.greenrobot.event.EventBus;


public class ToptenFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate, AdapterView.OnItemClickListener
{
    private View view;
    private BGARefreshLayout mBGARefreshLayout;
    private ListView listview_topten;
    private List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
    private List<Article> articleList = new ArrayList<>();
    private ToptenArticleListAdapter adapter ;

    private LinearLayout containter;
    //private ProgressBar progressBar;
    //private TextView loading_textview;

    private MyDataBaseHelper dataBaseHelper;

    //为了避免Fragment之间切换时每次都会调用onCreateView方法，导致每次Fragment的布局都重绘，因此设置一个变量保存状态
    private boolean loaded_flag = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //创建或者填充Fragment的UI，并且返回它。如果这个Fragment没有UI， 返回null
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_topten, null);

        mBGARefreshLayout = (BGARefreshLayout)view.findViewById(R.id.layout_topten_article_list);
        listview_topten = (ListView)view.findViewById(R.id.topten_list);
        containter = (LinearLayout)view.findViewById(R.id.linear_container);

        // 为BGARefreshLayout设置代理
        mBGARefreshLayout.setDelegate(this);
        // 设置下拉刷新和上拉加载更多的风格     参数1：应用程序上下文，参数2：是否具有上拉加载更多功能
        BGANormalRefreshViewHolder holder = new BGANormalRefreshViewHolder(ContextApplication.getAppContext(), false);
        mBGARefreshLayout.setRefreshViewHolder(holder);

        listview_topten.setOnItemClickListener(this);

        //注册EventBus
        EventBus.getDefault().registerSticky(this);

        mBGARefreshLayout.beginRefreshing();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return view;
    }

    /**
     * 获取当天的十大热门话题
     */
    public void getTopten()
    {
        WidgetHelper widgetHelper = new WidgetHelper();

        widgetHelper.getTopten();
    }

    /**
     * 响应 WidgetHelper 发布的当天十大热门话题
     * @param topten_article_list
     */
    public void onEventMainThread(Event.Topten_ArticleList topten_article_list)
    {
        listItems.clear();
        articleList.clear();

        for(Article article : topten_article_list.getTopten_list())
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("board", article.getBoard_name());
            map.put("title", article.getTitle());

            listItems.add(map);
            articleList.add(article);
        }

        if(adapter == null)
        {
            adapter = new ToptenArticleListAdapter(ContextApplication.getAppContext(), listItems);
            listview_topten.setAdapter(adapter);
        }
        else
            adapter.notifyDataSetChanged();

        //判断传入的数据是从本地读取的还是从网络获取的
        boolean is_Local = topten_article_list.isLocal();
        if(!is_Local)
        {
            //将本次获取到的十大内容存在数据库中，以便下次打开的时候先从本地读取
            Gson gson = new Gson();
            String json = gson.toJson(articleList);
            MyDataBaseHelper.getInstance().SaveTopTen(json);

            if (mBGARefreshLayout.getCurrentRefreshStatus() == BGARefreshLayout.RefreshStatus.REFRESHING)
                mBGARefreshLayout.endRefreshing();
        }
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout)
    {
        if(BYR_BBS_API.isNetWorkAvailable())
        {
            // 如果网络可用，则加载网络数据
            getTopten();
        }
        else
        {
            // 网络不可用，结束下拉刷新
            Toast.makeText(ContextApplication.getAppContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            mBGARefreshLayout.endRefreshing();
        }
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout)
    {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Article article = articleList.get(position);

        Intent intent = new Intent(getActivity(), ReadArticleActivity.class);
        intent.putExtra("board_name", article.getBoard_name());
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        //注销EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ((ViewGroup)view.getParent()).removeView(view);
    }
}
