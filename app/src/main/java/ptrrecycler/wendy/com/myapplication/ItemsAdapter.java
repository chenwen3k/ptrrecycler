package ptrrecycler.wendy.com.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import ptrrecycler.wendy.com.pulltorefresh.PullToRefreshAdapter;

/**
 * Created by wenhao on 7/18/15.
 */
public class ItemsAdapter extends PullToRefreshAdapter {

    private static final String TAG = ItemsAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<String> itemsArray;

    public ItemsAdapter(Context context, ArrayList<String> items) {
        super(context);
        this.context = context;
        this.itemsArray = items;
    }

    @Override
    public ItemsViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        return new ItemsViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindContentViewHolder(PullToRefreshViewHolder pullToRefreshViewHolder, int position) {
        ItemsViewHolder holder = (ItemsViewHolder) pullToRefreshViewHolder;
        holder.textView.setText(itemsArray.get(position));
    }

    @Override
    public int getContentItemViewType(int position) {
        return 0;
    }

    @Override
    public int getContentItemCount() {
        return itemsArray == null ? 0 : itemsArray.size();
    }

    public class ItemsViewHolder extends PullToRefreshViewHolder {
        @Bind(R.id.text_view) TextView textView;

        public ItemsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}