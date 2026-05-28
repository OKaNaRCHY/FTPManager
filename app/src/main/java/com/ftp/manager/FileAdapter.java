package com.ftp.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {

    interface OnClick { void onClick(File f); }

    private List<File> files = new ArrayList<>();
    private final OnClick onClick, onLongClick;

    public FileAdapter(OnClick onClick, OnClick onLongClick) {
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    public void setFiles(List<File> list) {
        files = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        File f = files.get(pos);
        h.icon.setText(f.isDirectory() ? "📁" : getIcon(f.getName()));
        h.name.setText(f.getName());
        if (f.isDirectory()) {
            h.info.setText("Klasör");
        } else {
            long s = f.length();
            String size = s < 1024 ? s + "B" : s < 1048576 ?
                    String.format(Locale.getDefault(), "%.1fKB", s/1024.0) :
                    String.format(Locale.getDefault(), "%.1fMB", s/1048576.0);
            h.info.setText(size);
        }
        h.itemView.setOnClickListener(v -> onClick.onClick(f));
        h.itemView.setOnLongClickListener(v -> { onLongClick.onClick(f); return true; });
    }

    private String getIcon(String name) {
        String e = name.contains(".") ? name.substring(name.lastIndexOf('.')+1).toLowerCase() : "";
        switch (e) {
            case "jpg": case "jpeg": case "png": case "gif": return "🖼";
            case "mp4": case "mkv": case "avi": return "🎬";
            case "mp3": case "wav": case "flac": return "🎵";
            case "pdf": return "📄";
            case "zip": case "rar": return "📦";
            case "apk": return "📱";
            case "txt": return "📝";
            default: return "📄";
        }
    }

    @Override
    public int getItemCount() { return files.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView icon, name, info;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.tv_icon);
            name = v.findViewById(R.id.tv_name);
            info = v.findViewById(R.id.tv_info);
        }
    }
}
