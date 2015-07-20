package ptrrecycler.wendy.com.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import ptrrecycler.wendy.com.pulltorefresh.PullToRefreshDecorator;
import ptrrecycler.wendy.com.pulltorefresh.PullToRefreshRecycler;

/**
 * Created by wenhao on 7/18/15.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.xrecyclerview)
    PullToRefreshRecycler refreshRecycler;
    private ItemsAdapter adapter;
    private ArrayList<String> itemsArray = new ArrayList<>();
    private int count = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        itemsArray.addAll(Arrays.asList(getResources().getStringArray(R.array.titles)));

        refreshRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemsAdapter(this, itemsArray);
        refreshRecycler.setAdapter(adapter);

        /**
         * 设置上拉加载更多
         */
        refreshRecycler.setPullLoadEnable(true);
        refreshRecycler.setPullRefreshEnable(true);
        refreshRecycler.setXListViewListener(new PullToRefreshDecorator.IXListViewListener() {
            @Override
            public void onRefresh() {
                addHeaderItem();
                refreshRecycler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        refreshRecycler.stopRefresh();
                    }
                }, 1000);
            }

            @Override
            public void onLoadMore() {
                addFooterItem();
                refreshRecycler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        refreshRecycler.stopLoadMore();
//                        xRecyclerView.disableLoadmore();
                    }
                }, 1000);
            }
        });
    }

    private void addHeaderItem() {
        count++;
        itemsArray.add(0, "Test" + count);
    }

    private void addFooterItem() {
        for(int i = 0; i < 5; i++ ){
            count++;
            itemsArray.add("Test" + count);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}