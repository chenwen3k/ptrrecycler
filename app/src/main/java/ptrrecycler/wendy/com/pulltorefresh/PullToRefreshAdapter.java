package ptrrecycler.wendy.com.pulltorefresh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mfw on 14-11-7.
 */
public abstract class PullToRefreshAdapter extends RecyclerView.Adapter<PullToRefreshAdapter.PullToRefreshViewHolder> {

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_FOOTER = 2;
    private static final int TYPE_CUSTOM = 3;
    private View mHeaderView;
    private View mFooterView;
    private Context mContext;

    /**
     * --------------------
     * |   Refresh header   | 0
     * --------------------
     * |      Container     | size
     * --------------------
     * |   Refresh footer   | size + 1
     * --------------------
     */
    public PullToRefreshAdapter(Context context) {
        mContext = context;
    }

    public abstract PullToRefreshViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindContentViewHolder(PullToRefreshViewHolder pullToRefreshViewHolder, int position);

    public abstract int getContentItemViewType(int position);

    public abstract int getContentItemCount();

    @Override
    final public PullToRefreshViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_HEADER == viewType) {
            return new PullToRefreshViewHolder(mHeaderView);
        } else if (TYPE_FOOTER == viewType) {
            return new PullToRefreshViewHolder(mFooterView);
        }
        return onCreateContentViewHolder(parent, viewType);
    }

    @Override
    final public void onBindViewHolder(PullToRefreshViewHolder pullToRefreshViewHolder, int position) {
        if (TYPE_HEADER == getItemViewType(position) || TYPE_FOOTER == getItemViewType(position)) {
            return;
        }
        position = getRealPosition(position);
        onBindContentViewHolder(pullToRefreshViewHolder, position);
    }

    @Override
    final public long getItemId(int i) {
        return i;
    }

    private int getRealPosition(int position) {
        if (mHeaderView != null) {
            position--;
        }
        return position;
    }

    public int getFirstContentPosition() {
        return mHeaderView == null ? 0 : 1;
    }

    public int getLastContentPosition() {
        // last position: no footer, size - 1, has footer, size - 2
        int lastPosition = mFooterView == null ? getItemCount() - 1 : getItemCount() - 2;
        return lastPosition;
    }

    void setHeaderView(View headerView) {
        mHeaderView = headerView;
    }

    void setFooterView(View footerView) {
        mFooterView = footerView;
    }

    @Override
    public int getItemCount() {
        int contentItemCount = getContentItemCount();
        int itemCount = contentItemCount;
        if (mHeaderView != null) {
            itemCount++;
        }

        if (contentItemCount == 0) {
            return itemCount;
        }
        if (mFooterView != null) {
            itemCount++;
        }
        return itemCount;
    }

    @Override
    final public int getItemViewType(int position) {
        if (mHeaderView != null && position == 0) {
            return TYPE_HEADER;
        }
        if (mFooterView != null && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_CUSTOM;
    }

    public static class PullToRefreshViewHolder extends RecyclerView.ViewHolder {

        public PullToRefreshViewHolder(View itemView) {
            super(itemView);
        }
    }
}
