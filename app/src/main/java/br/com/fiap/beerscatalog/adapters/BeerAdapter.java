package br.com.fiap.beerscatalog.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

import br.com.fiap.beerscatalog.R;
import br.com.fiap.beerscatalog.helpers.DateHelper;
import br.com.fiap.beerscatalog.models.Beer;


public class BeerAdapter extends RecyclerView.Adapter<BeerAdapter.BeerViewHolder> implements StickyRecyclerHeadersAdapter<BeerAdapter.HeaderViewHolder> {

    private static final int THUMBNAIL_SIZE = 100;
    private Context context;
    private List<Beer> beers;

    public BeerAdapter(Context context, List<Beer> beers) {
        this.context = context;
        this.beers = beers;
    }

    @Override
    public BeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.beer_entry, parent, false);
        return new BeerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BeerViewHolder holder, int position) {
        Beer beer = beers.get(position);

        holder.name.setText(beer.name);
        holder.brewery.setText(beer.brewery);
        holder.style.setText(beer.style);
        holder.abv.setText(String.valueOf(beer.abv) + "%");

        if(holder.date != null){
            holder.date.setText(DateHelper.Parse(beer.date).substring(0,5));
        }

        if (beer.imageURL != null) {
            Bitmap image = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(beer.imageURL), THUMBNAIL_SIZE, THUMBNAIL_SIZE);
            try {
                holder.image.setImageBitmap(image);
            }catch(Exception e){
                holder.image.setImageResource(R.mipmap.ic_launcher);
            }
        }else{
            holder.image.setImageResource(R.mipmap.ic_launcher);
        }
    }

    @Override
    public long getHeaderId(int position) {
        String date = DateHelper.Parse(beers.get(position).date);
        long id = date.subSequence(0, 1).charAt(0) + date.subSequence(1, 2).charAt(0) +
                date.subSequence(3, 4).charAt(0) + date.subSequence(4, 5).charAt(0);
        return id;
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.beer_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder holder, int position) {
        String date = DateHelper.Parse(beers.get(position).date);
        holder.title.setText(date.substring(0,5));
    }

    @Override
    public int getItemCount() {
        return beers.size();
    }

    public class BeerViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView brewery;
        protected TextView style;
        protected TextView abv;
        protected ImageView image;
        protected TextView date;

        public BeerViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            brewery = (TextView) itemView.findViewById(R.id.brewery);
            style = (TextView) itemView.findViewById(R.id.style);
            abv = (TextView) itemView.findViewById(R.id.abv);
            image = (ImageView) itemView.findViewById(R.id.image);
            date = (TextView) itemView.findViewById(R.id.date);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public void swap(List<Beer> data){
        this.beers.clear();
        this.beers.addAll(data);
        notifyDataSetChanged();
    }
}
