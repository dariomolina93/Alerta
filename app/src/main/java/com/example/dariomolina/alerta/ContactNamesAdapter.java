package com.example.dariomolina.alerta;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContactNamesAdapter extends RecyclerView.Adapter<ContactNamesAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView CardView;

        public ViewHolder(CardView l) {
            super(l);
            CardView = l;
        }
    }

    private String[] names;

    public ContactNamesAdapter(String[] names) {
        this.names = names;
    }

    // Attaching the layout to the adapter
    @Override
    public ContactNamesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView ll = (CardView) LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_contact_names, parent, false);

        return new ViewHolder(ll);
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    // Binds the view with data
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CardView CardView = holder.CardView;
        TextView textView = CardView.findViewById(R.id.name_text);
        textView.setText(names[position]);
    }

}
