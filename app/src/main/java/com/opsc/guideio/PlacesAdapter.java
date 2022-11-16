package com.opsc.guideio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
        // Creating an instance of the Book Model with the current position of the Book Arraylist
        PlacesModel model = placesArrayList.get(position);

        // Getting the Title from the book model at the current position
        //String title = model.getTitle();
        // Getting the Description from the book model at the current position
        String xCoordinates = model.getX();
        String yCoordinates = model.getY();
        // Getting the Timestamp from the book model at the current position
        String timestamp = model.getTimestamp();
        // Getting the IMage URL from the book model at the current position
        String imageUrl = model.getUrl();
        // Getting the Category ID from the book model at the current position

        // Setting the Title in the row_book layout
        //holder.titleTv.setText(title);
        // Setting the Description in the row_book layout
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
        holder.moreBtn.setOnClickListener(view -> moreOptionsDialog(model, holder));
    }

    private void moreOptionsDialog(PlacesModel model, PlacesHolder holder) {
        // Options to present to user
        String[] options = {"Edit", "Delete"};

        // Getting the BookID from the book model at the current position
        String bookId = model.getId();
        // Getting the BookURL from the book model at the current position
        String bookUrl = model.getUrl();
        // Getting the Book Title from the book model at the current position
        //String bookTitle = model.getTitle();

        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, (dialogInterface, i) -> {
                    if (i == 0) {
                        /*// Handle onClick for Edit Option
                        Intent intent = new Intent(context, UpdateActivity.class);
                        intent.putExtra("bookId", bookId);
                        context.startActivity(intent);*/
                    } else if (i == 1) {
                        // Handle onClick for Delete Option
                        // Calling the deleteBook method from MyApplication class and passing respective values
                        MyApplication.deleteBook(
                                context,
                                "" + bookId,
                                "" + bookUrl
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
        TextView titleTv, coordinatesTv;
        ImageButton moreBtn;

        public PlacesHolder(@NonNull View itemView) {
            super(itemView);

            // Initialising Views
            placeImage = itemView.findViewById(R.id.placeImage);
            titleTv = itemView.findViewById(R.id.titleTv);
            coordinatesTv = itemView.findViewById(R.id.coordinatesTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
        }
    }
}
