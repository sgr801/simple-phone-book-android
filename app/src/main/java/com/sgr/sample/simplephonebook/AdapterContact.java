package com.sgr.sample.simplephonebook;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AdapterContact extends ArrayAdapter<ContactItem> implements Filterable {

    private Context context;
    private List<ContactItem> contacts, filterList;
    private LayoutInflater inflater;
    private ContactFilter filter;
    private String tradieId, senderId;
    private boolean isShareTradie = false;

    public AdapterContact(Context context, List<ContactItem> contacts) {
        super(context, R.layout.adapter_contact, contacts);
        this.contacts = contacts;
        this.context = context;
        filterList = new ArrayList<>();
        this.filterList.addAll(contacts);
        isShareTradie = false;
    }


    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new ContactFilter();
        return filter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_contact, parent, false);
        } else {
            view = convertView;
        }
        viewHolder.name = (TextView) view.findViewById(R.id.name);
        viewHolder.photo = (ImageView) view.findViewById(R.id.photo);
        viewHolder.number = (TextView) view.findViewById(R.id.phone);
        viewHolder.name.setText(contacts.get(position).getName());
        viewHolder.number.setText(contacts.get(position).getPhone());

        final ContactItem currentContact = contacts.get(position);

        if (currentContact.getContactImage() != null) {
            Bitmap contactImage = getContactImage(currentContact.getContactImage());
            viewHolder.photo.setImageBitmap(contactImage);
        }else {
            viewHolder.photo.setImageResource(R.drawable.default_contact_photo);
        }

        return view;
    }

    private Bitmap getContactImage(byte[] photo) {
        int targetW = 50, targetH = 50;
        BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapFactory.decodeByteArray(photo, 0, photo.length, options);
        options.inJustDecodeBounds = true;
        int imageW = options.outWidth;
        int imageH = options.outHeight;
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(imageW / targetW, imageH / targetH);
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeByteArray(photo, 0, photo.length, options);
    }


    public class ViewHolder {
        ImageView photo;
        TextView name, number;
    }

    private class ContactFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String data = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            if (data.length() > 0) {
                List<ContactItem> filteredList = new ArrayList<>(filterList);
                List<ContactItem> nList = new ArrayList<>();
                int count = filteredList.size();
                for (int i = 0; i < count; i++) {
                    ContactItem item = filteredList.get(i);
                    String name = item.getName().toLowerCase();
                    String phone = item.getPhone().toLowerCase();
                    if (name.startsWith(data) || phone.startsWith(data))
                        nList.add(item);
                }
                results.count = nList.size();
                results.values = nList;
            } else {
                List<ContactItem> list = new ArrayList<>(filterList);
                results.count = list.size();
                results.values = list;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contacts = (ArrayList<ContactItem>) results.values;
            clear();
            for (int i = 0; i < contacts.size(); i++) {
                ContactItem item = (ContactItem) contacts.get(i);
                add(item);
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


}

