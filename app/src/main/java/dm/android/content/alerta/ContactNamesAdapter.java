package dm.android.content.alerta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import dm.android.content.alerta.R;

import java.util.ArrayList;

public class ContactNamesAdapter extends RecyclerView.Adapter<ContactNamesAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView CardView;

        public ViewHolder(CardView l) {
            super(l);
            CardView = l;
        }
    }

    public interface Listener {
        void onClick(int position);
        void onClickCheckBox(Contact contact, boolean isChecked);
    }

    private ArrayList<Contact> contacts;
    private Listener listener;

    public ContactNamesAdapter(ArrayList<Contact> contacts) {
        this.contacts = contacts;
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
        return contacts.size();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    // Binds the view with data
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CardView cardView = holder.CardView;
        final Contact contact = contacts.get(position);
        TextView textView = cardView.findViewById(R.id.name_text);
        textView.setText(contact.getName());
        CheckBox checkBox = cardView.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(contact.getIsSelected());

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(listener != null) {
                    contact.setIsSelected(isChecked);
                    listener.onClickCheckBox(contact, isChecked);
                }
            }
        });
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onClick(position);
                }
            }
        });
    }
}
