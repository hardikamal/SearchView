package com.lapism.searchview;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ResultViewHolder> implements Filterable {

    private final SearchHistoryTable mHistoryDatabase;
    private String key = " ";
    private List<SearchItem> mResultList = new ArrayList<>();
    private List<SearchItem> mSuggestionsList = new ArrayList<>();

    private OnItemClickListener mItemClickListener;

    public SearchAdapter(Context context, List<SearchItem> suggestionsList) {
        mSuggestionsList = suggestionsList;
        mHistoryDatabase = new SearchHistoryTable(context);
        mResultList = mHistoryDatabase.getAllItems();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (!TextUtils.isEmpty(constraint)) {
                    List<SearchItem> results = new ArrayList<>();
                    List<SearchItem> history = new ArrayList<>();
                    history.addAll(mHistoryDatabase.getAllItems());
                    history.addAll(mSuggestionsList);

                    key = constraint.toString().toLowerCase(Locale.getDefault());

                    for (SearchItem str : history) {
                        String string = str.get_text().toString().toLowerCase(Locale.getDefault());
                        if (string.contains(key)) {
                            results.add(str);
                        }
                    }

                    filterResults.values = results;
                    filterResults.count = results.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    mResultList.clear();
                    List<?> result = (List<?>) results.values;
                    for (Object object : result) {
                        if (object instanceof SearchItem) {
                            mResultList.add((SearchItem) object);
                        }
                    }
                } else {
                    mResultList.clear();
                    mResultList = mHistoryDatabase.getAllItems();
                }

                notifyDataSetChanged();
            }
        };
    }

    @Override
    public ResultViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.search_item, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder viewHolder, int position) {
        SearchItem item = mResultList.get(position);

        viewHolder.icon_left.setImageResource(item.get_icon());
        viewHolder.icon_left.setColorFilter(SearchView.getIconColor(), PorterDuff.Mode.SRC_IN);
        viewHolder.text.setTextColor(SearchView.getTextColor());
        viewHolder.text.setText(item.get_text(), TextView.BufferType.SPANNABLE);

        String string = viewHolder.text.getText().toString();
        SpannableString s = new SpannableString(viewHolder.text.getText());

        //if (string.toLowerCase(Locale.getDefault()).contains(key)) {
        if (string.contains(key)) {
            s.setSpan(new ForegroundColorSpan(SearchView.getTextHighlightColor()), string.indexOf(key), string.indexOf(key) + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.text.setText(s, TextView.BufferType.SPANNABLE);
            key = " ";
        } else {
            s.removeSpan(new ForegroundColorSpan(SearchView.getTextColor()));
            viewHolder.text.setText(s, TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView icon_left;
        public final TextView text;

        public ResultViewHolder(View view) {
            super(view);
            icon_left = (ImageView) view.findViewById(R.id.imageView_item_icon_left);
            text = (TextView) view.findViewById(R.id.textView_item_text);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }

}