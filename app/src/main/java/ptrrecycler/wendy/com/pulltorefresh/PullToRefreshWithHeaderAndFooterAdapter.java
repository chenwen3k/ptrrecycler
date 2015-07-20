package ptrrecycler.wendy.com.pulltorefresh;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mfw on 14-11-10.
 */
public abstract class PullToRefreshWithHeaderAndFooterAdapter extends PullToRefreshAdapter {
    private static final int TYPE_HEADER = 3;
    private static final int TYPE_FOOTER = 4;
    private View headerView;
    private View footerView;

    public PullToRefreshWithHeaderAndFooterAdapter(Context context) {
        super(context);
    }

    public abstract PullToRefreshViewHolder onCreatePRHFViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindPRHFViewHolder(PullToRefreshViewHolder pullToRefreshViewHolder, int position);

    public abstract int getContentRPHFItemViewType(int position);

    public abstract int getContentRPHFItemCount();

    @Override
    public PullToRefreshViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (TYPE_HEADER == viewType) {
            view = headerView;
        } else if (TYPE_FOOTER == viewType) {
            view = footerView;
        } else {
            return onCreatePRHFViewHolder(parent, viewType);
        }
        return new PullToRefreshViewHolder(view);
    }

    @Override
    public void onBindContentViewHolder(PullToRefreshViewHolder pullToRefreshViewHolder, int position) {
        if (TYPE_HEADER == pullToRefreshViewHolder.getItemViewType() || TYPE_FOOTER == pullToRefreshViewHolder.getItemViewType()) {
            return;
        }
        position = getRealPosition(position);
        onBindPRHFViewHolder(pullToRefreshViewHolder, position);
    }

    @Override
    public int getContentItemViewType(int position) {
        if (headerView != null && position == 0) {
            return TYPE_HEADER;
        }
        if (footerView != null && position == getContentItemCount() - 1) {
            return TYPE_FOOTER;
        }
        position = getRealPosition(position);
        return getContentRPHFItemViewType(position);
    }

    @Override
    public int getContentItemCount() {
        int contentItemCount = getContentRPHFItemCount();
        int itemCount = contentItemCount;
        if (headerView != null) {
            itemCount++;
        }
        if (contentItemCount == 0) {
            return itemCount;
        }
        if (footerView != null) {
            itemCount++;
        }
        return itemCount;
    }

    private int getRealPosition(int position) {
        if (headerView != null) {
            position--;
        }
        return position;
    }

    public void setHeader(View headerView) {
        this.headerView = headerView;
    }

    public void setFooter(View footerView) {
        this.footerView = footerView;
    }
}
