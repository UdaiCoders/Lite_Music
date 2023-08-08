package app.UDC.music.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import java.util.ArrayList;
import java.util.List;

import app.UDC.music.ActivityMain;
import app.UDC.music.R;
import app.UDC.music.model.MusicItem;


public class MusicFileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private Context ctx;
    private ArrayList<MusicItem> items = new ArrayList<>();
    private List<MusicItem> original_items = new ArrayList<>();

    private OnItemClickListener onItemClickListener;
    private ItemFilter mFilter = new ItemFilter();

    public interface OnItemClickListener {
        void onItemClick(View view, MusicItem obj, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public TextView artist;
        public ImageView icon;
        public MaterialRippleLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            artist = (TextView) v.findViewById(R.id.artist);
            icon = (ImageView) v.findViewById(R.id.icon);
            lyt_parent = (MaterialRippleLayout) v.findViewById(R.id.lyt_parent);
        }
    }

    public MusicFileListAdapter(Context ctx, ArrayList<MusicItem> items) {
        this.ctx = ctx;
        this.items = items;
        this.original_items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_music, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder vItem = (ViewHolder) holder;
            final MusicItem m = items.get(position);

            vItem.title.setText(m.getTitle());
            vItem.artist.setText(m.getArtist());
            vItem.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, m, position);
                    }
                }
            });
            if (m.isFav()) {
                vItem.icon.setImageResource(R.drawable.fav_icon);
            } else {
                vItem.icon.setImageResource(R.drawable.music_icon);
            }
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<MusicItem> getItem() {
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(ArrayList<MusicItem> items) {
        this.items = items;
        this.original_items = items;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    // define class filter
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<MusicItem> list = original_items;
            final ArrayList<MusicItem> nList = new ArrayList<>(list.size());
            for (MusicItem obj : list) {
                String artist = obj.getArtist().toLowerCase();
                String title = obj.getTitle().toLowerCase();
                String album = obj.getAlbum().toLowerCase();
                if (artist.contains(filterString) || title.contains(filterString) || album.contains(filterString)) {
                    nList.add(obj);
                }
            }
            results.values = nList;
            results.count = nList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items = (ArrayList<MusicItem>) results.values;
            notifyDataSetChanged();
            ActivityMain.getInstance().noMusicChecker();
        }

    }

}