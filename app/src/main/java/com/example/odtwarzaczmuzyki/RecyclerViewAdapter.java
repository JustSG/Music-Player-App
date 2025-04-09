package com.example.odtwarzaczmuzyki;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    public static final String UTWORY = "utwory";
    private Context context;
    private ArrayList<Utwor> utwory;
    private UtworDB utworDB;
    private List<Integer> odtworzenia;

    public RecyclerViewAdapter(Context context, ArrayList<Utwor> utwory, List<Integer> odtworzenia, UtworDB utworDB) {
        this.context = context;
        this.utwory = utwory;
        this.odtworzenia = odtworzenia;
        this.utworDB = utworDB;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.recycler_view_row, parent, false);
        return new RecyclerViewAdapter.ViewHolder(view);
    } //wypełnianie wierszy (ała)

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.textViewNazwa.setText(utwory.get(position).getNazwaUtworu());
        holder.textViewCzas.setText(OdtwarzaczActivity.miliNaSek(utwory.get(position).getCzasTrwania()));
        holder.textViewWykonawca.setText(utwory.get(position).getWykonawca());
        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + utwory.get(position).getIdAlbumu());
        Glide.with(holder.imageViewIkona.getContext()).load(albumArtUri).placeholder(R.drawable.icon_pusty).error(R.drawable.icon_pusty).into(holder.imageViewIkona);

        if (position < odtworzenia.size()) {
            holder.textViewIloscOdtworzen.setText(String.valueOf(utwory.get(position).getOdtworzenia()));
        } else {
            holder.textViewIloscOdtworzen.setText("0");
        }

        holder.imageViewOpcje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_opcje, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.wiecejInfo) {
                            Odtwarzacz.getInstance().reset();
                            Odtwarzacz.index = holder.getAdapterPosition();
                            Intent intent = new Intent(view.getContext(), InfoActivity.class);
                            intent.putExtra(UTWORY, utwory);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utworDB.zwrocDao().aktualizujOdtworzenia(utwory.get(holder.getAdapterPosition()).getSciezkaDoPliku());
                odtworzenia.set(holder.getAdapterPosition(), odtworzenia.get(holder.getAdapterPosition()) + 1);
                Utwor tenUtwor = utwory.get(holder.getAdapterPosition());
                tenUtwor.zwiekszLiczbeOdtworzen();
                notifyItemChanged(holder.getAdapterPosition());

                Odtwarzacz.getInstance().reset();
                Odtwarzacz.index = holder.getAdapterPosition();
                Intent intent = new Intent(view.getContext(), OdtwarzaczActivity.class);
                intent.putExtra(UTWORY, utwory);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });

    } //przypisujemy wartości do recycler_view_row.xml

    @Override
    public int getItemCount() {
        return utwory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageViewIkona;
        public TextView textViewNazwa;
        public TextView textViewCzas;
        public TextView textViewIloscOdtworzen;
        public TextView textViewWykonawca;
        public ImageView imageViewOpcje;


        public ViewHolder(View itemView) {
            super(itemView);

            imageViewIkona = itemView.findViewById(R.id.imageViewIkona);
            textViewNazwa = itemView.findViewById(R.id.textViewNazwa);
            textViewCzas = itemView.findViewById(R.id.textViewCzas);
            textViewIloscOdtworzen = itemView.findViewById(R.id.textViewIloscOdtworzen);
            textViewWykonawca = itemView.findViewById(R.id.textViewWykonawca);
            imageViewOpcje = itemView.findViewById(R.id.imageViewOpcje);

        }
    } //pobiera imageView, textView itp. z recycler_view_row.xml
}
