package kr.or.hanium.chungbukhansung.escapepresbyopia.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;
import kr.or.hanium.chungbukhansung.escapepresbyopia.model.MainMenuItem;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.ViewHolder> {

    private final List<MainMenuItem> menuItems;

    public MainMenuAdapter(List<MainMenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_cardview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainMenuItem item = menuItems.get(position);
        holder.menuView.setCardBackgroundColor(item.bg);
        holder.imageView.setImageResource(item.image);
        holder.textView.setText(item.label);
        holder.itemView.setElevation(11);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final CardView menuView;
        final ImageView imageView;
        final TextView textView;

        private ViewHolder(View itemView) {
            super(itemView);
            menuView = itemView.findViewById(R.id.menuView);
            imageView = itemView.findViewById(R.id.menuIcon);
            textView = itemView.findViewById(R.id.menuLabel);
        }
    }
}
