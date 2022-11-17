package com.opsc.guideio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesHolder> {

    Context context;
    public ArrayList<PlacesModel> placesArrayList;

    public PlacesAdapter(Context context, ArrayList<PlacesModel> placesArrayList) {
        this.context = context;
        this.placesArrayList = placesArrayList;
    }

    @NonNull
    @Override
    public PlacesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_row, parent, false);
        return new PlacesAdapter.PlacesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesHolder holder, int position) {
        // Creating an instance of the Places Model with the current position of the Place Arraylist
        PlacesModel model = placesArrayList.get(position);

        // Getting the x coordinate from the places model at the current position
        String xCoordinates = model.getX();
        // Getting the y coordinate from the places model at the current position
        String yCoordinates = model.getY();
        // Getting the Timestamp from the places model at the current position
        String timestamp = model.getTimestamp();
        // Getting the Image URL from the places model at the current position
        String imageUrl = model.getUrl();

        // Setting the x and y coordinates in the images_row layout
        holder.coordinatesTv.setText(new StringBuilder()
                .append("X: ")
                .append(xCoordinates)
                .append(", Y: ")
                .append(yCoordinates));

        // Calling the loadImageFromURL method from MyApplication class and passing respective values
        MyApplication.loadImageFromURL(
                "" + imageUrl,
                holder.placeImage);

        // Show dialog for more button
        // Elliott, T. (2022) Adding onclicklistener to Recyclerview in Android, DEV Community. DEV Community. Available at: https://dev.to/theplebdev/adding-onclicklistener-to-recyclerview-in-android-3amb (Accessed: November 17, 2022).
        holder.moreBtn.setOnClickListener(view -> moreOptionsDialog(model, holder));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("x", placesArrayList.get(position).getX());
            intent.putExtra("y", placesArrayList.get(position).getY());
            // kenju (1958) How do I get extra data from intent on Android?, Stack Overflow. Available at: https://stackoverflow.com/questions/4233873/how-do-i-get-extra-data-from-intent-on-android (Accessed: November 17, 2022).
            // Rehman, S. (no date) Intent tutorial in Android with example and types, Abhi Android. Available at: https://abhiandroid.com/programming/intent-in-android (Accessed: November 17, 2022).
            context.startActivity(intent);
        });
    }

    private void moreOptionsDialog(PlacesModel model, PlacesHolder holder) {
        // Options to present to user
        String[] options = {"Delete"};

        // Getting the id from the places model at the current position
        String id = model.getId();
        // Getting the url from the places model at the current position
        String url = model.getUrl();

        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, (dialogInterface, i) -> {
                    if (i == 0) {
                        // Handle onClick for Delete Option
                        // Calling the deletePlace method from MyApplication class and passing respective values
                        MyApplication.deletePlace(
                                context,
                                "" + id,
                                "" + url
                        );
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return placesArrayList.size();
    }

    class PlacesHolder extends RecyclerView.ViewHolder {

        ImageView placeImage;
        TextView coordinatesTv;
        ImageButton moreBtn;

        public PlacesHolder(@NonNull View itemView) {
            super(itemView);

            // Initialising Views
            placeImage = itemView.findViewById(R.id.placeImage);
            coordinatesTv = itemView.findViewById(R.id.coordinatesTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
        }

    }
}
