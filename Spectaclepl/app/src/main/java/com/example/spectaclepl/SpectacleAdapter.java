package com.example.spectaclepl;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SpectacleAdapter extends RecyclerView.Adapter<SpectacleAdapter.ViewHolder> {
    private List<Spectacle> spectacleList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Spectacle spectacle);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public SpectacleAdapter(Context context, List<Spectacle> spectacleList) {
        this.context = context;
        this.spectacleList = spectacleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spectacle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spectacle spectacle = spectacleList.get(position);
        String imageUrl = "http://192.168.100.12:3000/public/images/" + spectacle.getimageResId();

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.image);

        holder.titre.setText(spectacle.getTitre());
        holder.artiste.setText(spectacle.getNomArtiste());

        String fullDate = spectacle.getDate();
        String dateOnly = fullDate.split("T")[0];
        holder.date.setText(dateOnly);

        holder.localisation.setText(spectacle.getlocalisation());
    }

    @Override
    public int getItemCount() {
        return spectacleList.size();
    }

    public void updateList(List<Spectacle> newList) {
        this.spectacleList = newList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titre, date, artiste, localisation;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_image);
            titre = itemView.findViewById(R.id.tv_titre);
            artiste = itemView.findViewById(R.id.tv_artiste);
            date = itemView.findViewById(R.id.spectacle_date);
            localisation = itemView.findViewById(R.id.spectacle_localisation);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Spectacle clickedSpectacle = spectacleList.get(position);

                    // Utilisez le listener si défini, sinon utilisez l'approche par défaut
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(clickedSpectacle);
                    } else {
                        // Approche par défaut (comme dans votre code original)
                        Intent intent = new Intent(context, DetailSpectacleActivity.class);
                        intent.putExtra("titre", clickedSpectacle.getTitre());
                        intent.putExtra("date", clickedSpectacle.getDate());
                        intent.putExtra("heureDebut", clickedSpectacle.getHeureDebut());
                        intent.putExtra("prix", clickedSpectacle.getPrixTicket());
                        intent.putExtra("image", "http://192.168.100.17:3000/public/images/" + clickedSpectacle.getimageResId());
                        intent.putExtra("description", clickedSpectacle.getDescription());
                        intent.putExtra("lieu", clickedSpectacle.getLieu());
                        intent.putExtra("nomArtiste", clickedSpectacle.getNomArtiste());
                        intent.putExtra("localisation", clickedSpectacle.getlocalisation());
                        intent.putExtra("place_dispo", clickedSpectacle.getplacedispo());
                        intent.putExtra("matricule", clickedSpectacle.getMatricule());
                        intent.putExtra("categorie", clickedSpectacle.getCategorie());

                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}
