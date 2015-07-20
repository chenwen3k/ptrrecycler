package ptrrecycler.wendy.com.pulltorefresh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * Created by 3k on 14-11-6.
 */
public class PullToRefreshRecycler extends RecyclerView {
    private PullToRefreshAdapter mAdapter;
    private PullToRefreshDecorator mDecorator;

    public PullToRefreshRecycler(Context context) {
        super(context, null);
    }

    public PullToRefreshRecycler(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDecorator = new SimplePullToRefreshDecorator(this, context, attrs);
        mDecorator.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mDecorator.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mDecorator.onScroll(recyclerView, dx, dy);
            }

        });
    }

    public PullToRefreshDecorator getDecorator() {
        return mDecorator;
    }

    public void setPullLoadEnable(boolean enable) {
        mDecorator.setPullLoadEnable(enable);
    }

    public void setPullRefreshEnable(boolean enable) {
        mDecorator.setPullRefreshEnable(enable);
    }

    public void setAdapter(PullToRefreshAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = adapter;
        mAdapter.setHeaderView(mDecorator.getHeaderView());
        mAdapter.setFooterView(mDecorator.getFooterView());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mDecorator.onTouchEvent(e);
        return super.onTouchEvent(e);
    }

    private class SimplePullToRefreshDecorator extends PullToRefreshDecorator {

        /**
         * Add a pull to refresh view to any view
         * Step1. set a refresh listener {@link PullToRefreshDecorator#setXListViewListener(PullToRefreshDecorator.IXListViewListener)}.
         * Step4. call {@link PullToRefreshDecorator#onScroll(android.view.ViewGroup, int, int, int)}.
         *
         * @param context
         * @param attrs
         */
        public SimplePullToRefreshDecorator(PullToRefreshRecycler recycler, Context context, AttributeSet attrs) {
            super(recycler, context, attrs);
        }

        @Override
        public void setMeasuredDimension(int width, int height) {
            this.setMeasuredDimension(width, height);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        mDecorator.computeScroll();
    }

    public void setXListViewListener(PullToRefreshDecorator.IXListViewListener listener) {
        mDecorator.setXListViewListener(listener);
    }

    public void stopRefresh() {
        mDecorator.stopRefresh();
    }

    public void stopLoadMore() {
        mDecorator.stopLoadMore();
    }

    @Override
    public void scrollBy(int x, int y) {
        try {
            super.scrollBy(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
