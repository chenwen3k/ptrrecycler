package ptrrecycler.wendy.com.pulltorefresh;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import ptrrecycler.wendy.com.myapplication.R;


/**
 * w
 * Created by 3k on 14-11-6.
 */
public abstract class PullToRefreshDecorator {

    private boolean hasMeasure = false;
    private boolean handleMeasure = false;
    private float mLastY = -1; // save event y
    private Scroller mScroller; // used for scroll back
    private Object mScrollListener; // user's scroll listener

    // the interface to trigger refresh and load more.
    private IXListViewListener mListViewListener;

    // -- header view
    private XListViewHeader mHeaderView;
    private LinearLayout mHeaderLayout;
    // header view content, use it to calculate the Header's height. And hide it
    // when disable pull refresh.
    private RelativeLayout mHeaderViewContent;
    private TextView mHeaderTimeView;
    private int mHeaderViewHeight; // header view's height
    private boolean mEnablePullRefresh = true;
    private boolean mPullRefreshing = false; // is refreashing.

    // -- footer view
    private XListViewFooter mFooterView;
    private LinearLayout mFooterLayout;
    private boolean mEnablePullLoad;
    private boolean mPullLoading;
    private boolean mIsFooterReady = false;

    // total list items, used to detect is at the bottom of listview.
    private int mTotalItemCount;

    // for mScroller, scroll back from header or footer.
    private int mScrollBack;
    private final static int SCROLLBACK_HEADER = 0;
    private final static int SCROLLBACK_FOOTER = 1;

    private final static int SCROLL_DURATION = 400; // scroll back duration
    private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px
    // at bottom, trigger
    // load more.
    private final static float OFFSET_RADIO = 1.8f; // support iOS like pull feature.

    private Context mContext;
    private ViewGroup mView;


    /**
     * Add a pull to refresh view to any view
     * set a refresh listener
     *
     * @param context
     * @param attrs
     */
    public PullToRefreshDecorator(ViewGroup view, Context context, AttributeSet attrs) {
        mView = view;
        mContext = context;
        initWithContext(context);
    }

    private void initWithContext(Context context) {
        mScroller = new Scroller(context, new DecelerateInterpolator());
        // XListView need the scroll event, and it will dispatch the event to
        // user's listener (as a proxy).


        // init header view
        mHeaderView = new XListViewHeader(context);
        mHeaderLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mHeaderLayout.setLayoutParams(params);
        mHeaderLayout.setGravity(Gravity.CENTER);
        mHeaderLayout.addView(mHeaderView);

        mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.xlistview_header_content);
        mHeaderTimeView = (TextView) mHeaderView.findViewById(R.id.xlistview_header_time);

        // init footer view
        mFooterView = new XListViewFooter(context);
        mFooterLayout = new LinearLayout(context);
        mFooterLayout.setLayoutParams(params);
        mFooterLayout.setGravity(Gravity.CENTER);
        mFooterLayout.addView(mFooterView);

        // init header height
        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mHeaderViewHeight = mHeaderViewContent.getHeight();
                        mView.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                    }
                });

        // make sure XListViewFooter is the last footer view, and only add once.
        if (!mIsFooterReady) {
            mIsFooterReady = true;
        }
    }

    public void setOnScrollListener(Object l) {
        mScrollListener = l;
        if (mView instanceof ListView) {
            ListView listView = (ListView) mView;
            listView.setOnScrollListener((AbsListView.OnScrollListener) l);
        } else if (mView instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) mView;
            recyclerView.addOnScrollListener((RecyclerView.OnScrollListener) l);
        }
    }

    /**
     * enable or disable pull down refresh feature.
     *
     * @param enable
     */
    public void setPullRefreshEnable(boolean enable) {
        mEnablePullRefresh = enable;
        if (!mEnablePullRefresh) { // disable, hide the content
            mHeaderViewContent.setVisibility(View.INVISIBLE);
        } else {
            mHeaderViewContent.setVisibility(View.VISIBLE);
        }
    }

    /**
     * enable or disable pull up load more feature.
     *
     * @param enable
     */
    public void setPullLoadEnable(boolean enable) {
        mEnablePullLoad = enable;
        if (!mEnablePullLoad) {
            mFooterView.hide();
            mFooterView.setOnClickListener(null);
        } else {
            mPullLoading = false;
            mFooterView.show();
            mFooterView.setState(XListViewFooter.STATE_NORMAL);
            // both "pull up" and "click" will invoke load more.
            mFooterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoadMore();
                }
            });
        }
    }

    /**
     * stop refresh, reset header view.
     */
    public void stopRefresh() {
        if (mPullRefreshing) {
            mPullRefreshing = false;
            resetHeaderHeight();
        }
        setRefreshTime(System.currentTimeMillis());
    }

    /**
     * stop load more, reset footer view.
     */
    public void stopLoadMore() {
        if (mPullLoading) {
            mPullLoading = false;
            mFooterView.setState(XListViewFooter.STATE_NORMAL);
        }
    }

    public void setRefreshTime(long time) {
        mHeaderView.setRefreshTime(time);
    }

    /**
     * set last refresh time
     *
     * @param time
     */
    public void setRefreshTime(String time) {
        mHeaderTimeView.setText(time);
    }

//    private void invokeOnScrolling() {
//        if (mScrollListener != null) {
//            if(mView instanceof ListView) {
//                ListView listView = (ListView) mView;
//                AbsListView.OnScrollListener listener = (AbsListView.OnScrollListener) mScrollListener;
////                listener.onScroll(m);
//            } else if(mView instanceof RecyclerView) {
//                RecyclerView recyclerView = (RecyclerView) mView;
//                RecyclerView.OnScrollListener listener = (RecyclerView.OnScrollListener) mScrollListener;
////                listener.onScrollStateChanged(recyclerView, scrollState);
//            }
//        }
//    }

    private void updateHeaderHeight(float delta) {
        mHeaderView.setVisiableHeight((int) delta
                + mHeaderView.getVisiableHeight());
        if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头
            if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
                mHeaderView.setState(XListViewHeader.STATE_READY);
            } else {
                mHeaderView.setState(XListViewHeader.STATE_NORMAL);
            }
        }
        if (mView instanceof ListView) {
            ((ListView) mView).setSelection(0); // scroll to top each time
        } else if (mView instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) mView;
            recyclerView.getLayoutManager().scrollToPosition(0);
        }
    }

    /**
     * reset header view's height.
     */
    private void resetHeaderHeight() {
        int height = mHeaderView.getVisiableHeight();
        if (height == 0) // not visible.
            return;
        // refreshing and header isn't shown fully. do nothing.
        if (mPullRefreshing && height <= mHeaderViewHeight) {
            return;
        }
        int finalHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mPullRefreshing && height > mHeaderViewHeight) {
            finalHeight = mHeaderViewHeight;
        }
        mScrollBack = SCROLLBACK_HEADER;
        mScroller.startScroll(0, height, 0, finalHeight - height,
                SCROLL_DURATION);
        // trigger computeScroll
        mView.invalidate();
    }

    private void updateFooterHeight(float delta) {
        int height = (int) (mFooterView.getBottomMargin() + delta);
        if (mEnablePullLoad && !mPullLoading) {
            if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
                // more.
                mFooterView.setState(XListViewFooter.STATE_READY);
            } else {
                mFooterView.setState(XListViewFooter.STATE_NORMAL);
            }
        }
        mFooterView.setBottomMargin(height);

//		setSelection(mTotalItemCount - 1); // scroll to bottom
    }

    private void resetFooterHeight() {
        int bottomMargin = mFooterView.getBottomMargin();
        if (bottomMargin > 0) {
            mScrollBack = SCROLLBACK_FOOTER;
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
                    SCROLL_DURATION);
            mView.invalidate();
        }
    }

    private void startLoadMore() {
        mPullLoading = true;
        mFooterView.setState(XListViewFooter.STATE_LOADING);
        if (mListViewListener != null) {
            mListViewListener.onLoadMore();
        }
    }

    private int getFirstVisiblePosition() {
        int position = 0;
        if (mView instanceof ListView) {
            ListView listView = (ListView) mView;
            position = listView.getFirstVisiblePosition();
        } else if (mView instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) mView;
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            position = layoutManager.findFirstVisibleItemPosition() - (mHeaderView == null || mHeaderView.getVisibility() != View.VISIBLE ? 0 : 1);
            if (position < 0) {
                position = 0;
            }
        }
        return position;
    }

    private int getLastVisiblePosition() {
        int position = 0;
        if (mView instanceof ListView) {
            ListView listView = (ListView) mView;
            position = listView.getLastVisiblePosition();
        } else if (mView instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) mView;
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            position = layoutManager.findLastVisibleItemPosition();
        }
        return position;
    }

    public void onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (getFirstVisiblePosition() == 0
                        && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0) && mEnablePullRefresh) {
                    // the first item is showing, header has shown or pull down.
                    updateHeaderHeight(deltaY / OFFSET_RADIO);
//                    invokeOnScrolling();
                } else if (getLastVisiblePosition() == mTotalItemCount - 1
                        && (mFooterView.getBottomMargin() > 0 || deltaY < 0) && mEnablePullLoad) {
                    // last item, already pulled up or want to pull up.
                    updateFooterHeight(-deltaY / OFFSET_RADIO);
                }
                break;
            default:
                mLastY = -1; // reset
                if (getFirstVisiblePosition() == 0) {
                    // invoke refresh
                    if (mEnablePullRefresh
                            && mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
                        mPullRefreshing = true;
                        mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
                        if (mListViewListener != null) {
                            mListViewListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();
                } else if (getLastVisiblePosition() == mTotalItemCount - 1) {
                    // invoke load more.
                    if (mEnablePullLoad
                            && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
                        startLoadMore();
                    }
                    resetFooterHeight();
                }
                break;
        }
        return;
    }

    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScrollBack == SCROLLBACK_HEADER) {
                mHeaderView.setVisiableHeight(mScroller.getCurrY());
            } else {
                mFooterView.setBottomMargin(mScroller.getCurrY());
            }
            mView.postInvalidate();
//            invokeOnScrolling();
        }
        return;
    }

    public void onScrollStateChanged(ViewGroup view, int scrollState) {
        if (mScrollListener != null) {
            if (view instanceof ListView) {
                ListView listView = (ListView) view;
                AbsListView.OnScrollListener listener = (AbsListView.OnScrollListener) mScrollListener;
            } else if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                RecyclerView.OnScrollListener listener = (RecyclerView.OnScrollListener) mScrollListener;
            }
        }
    }

    public void onScroll(ViewGroup view, Integer... scrollParams) {
        if (mScrollListener != null) {
            if (view instanceof ListView) {
                AbsListView.OnScrollListener listener = (AbsListView.OnScrollListener) mScrollListener;
                ListView listView = (ListView) view;
//                listener.onScroll(listView, scrollParams[0], scrollParams[1], scrollParams[2]);
                // send to user's listener
                mTotalItemCount = scrollParams[2];
            } else if (view instanceof RecyclerView) {
                RecyclerView.OnScrollListener listener = (RecyclerView.OnScrollListener) mScrollListener;
                RecyclerView recyclerView = (RecyclerView) view;
//                listener.onScrolled(recyclerView, scrollParams[0], scrollParams[1]);
                mTotalItemCount = recyclerView.getLayoutManager().getItemCount();
            }
        }
    }

    public void setHandleMeasure(boolean handle) {
        handleMeasure = handle;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!hasMeasure || !handleMeasure) {
            hasMeasure = true;
        } else {
            setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
        }
    }

    public abstract void setMeasuredDimension(int width, int height);

    public void setXListViewListener(IXListViewListener l) {
        mListViewListener = l;
    }

    /**
     * you can listen ListView.OnScrollListener or this one. it will invoke
     * onXScrolling when header/footer scroll back.
     */
    public interface OnXScrollListener extends AbsListView.OnScrollListener {
        void onXScrolling(View view);
    }

    /**
     * implements this interface to get refresh/load more event.
     */
    public interface IXListViewListener {
        void onRefresh();

        void onLoadMore();
    }

    public View getHeaderView() {
        return mHeaderLayout;
    }

    public View getFooterView() {
        return mFooterLayout;
    }
}
