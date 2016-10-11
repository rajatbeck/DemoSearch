package ngvl.android.demosearch.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ngvl.android.demosearch.R;

/**
 * Created by Rajat on 10/11/2016.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder> {

    List<String> scriptList;

    public SearchResultAdapter(List<String> scriptList) {
        this.scriptList = scriptList;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_row_layout, parent, false);
        SearchViewHolder searchViewHolder = new SearchViewHolder(view);
        return searchViewHolder;
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        holder.scriptName.setText(scriptList.get(position));
    }

    @Override
    public int getItemCount() {
        return scriptList.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        private TextView scriptName;

        public SearchViewHolder(View itemView) {
            super(itemView);
            scriptName = (TextView) itemView.findViewById(R.id.script_name);
        }
    }
}
