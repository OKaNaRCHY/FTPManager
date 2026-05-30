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
            String size = s < 1024 ? s + " B" :
                    s < 1048576 ? String.format(Locale.getDefault(), "%.1f KB", s/1024.0) :
                    s < 1073741824 ? String.format(Locale.getDefault(), "%.1f MB", s/1048576.0) :
                    String.format(Locale.getDefault(), "%.1f GB", s/1073741824.0);
            h.info.setText(size);
        }
        h.itemView.setOnClickListener(v -> onClick.onClick(f));
        h.itemView.setOnLongClickListener(v -> { onLongClick.onClick(f); return true; });
    }

    private String getIcon(String name) {
        String e = name.contains(".") ?
                name.substring(name.lastIndexOf('.')+1).toLowerCase() : "";
        switch (e) {
            // Resim
            case "jpg": case "jpeg": case "png": case "gif":
            case "webp": case "bmp": case "svg": return "🖼️";
            // Video
            case "mp4": case "mkv": case "avi": case "mov":
            case "wmv": case "flv": case "3gp": return "🎬";
            // Müzik
            case "mp3": case "wav": case "flac": case "aac":
            case "ogg": case "m4a": case "wma": return "🎵";
            // PDF
            case "pdf": return "📕";
            // Word
            case "doc": case "docx": case "odt": return "📘";
            // Excel
            case "xls": case "xlsx": case "ods": case "csv": return "📗";
            // PowerPoint
            case "ppt": case "pptx": case "odp": return "📙";
            // Arşiv
            case "zip": case "rar": case "7z": case "tar":
            case "gz": case "bz2": return "📦";
            // APK
            case "apk": return "📱";
            // Kod
            case "java": case "kt": case "py": case "js":
            case "ts": case "cpp": case "c": case "h": return "💻";
            // Web
            case "html": case "htm": case "css": return "🌐";
            // Metin
            case "txt": case "log": case "md": return "📝";
            // JSON/XML/Config
            case "json": case "xml": case "yaml": case "yml":
            case "ini": case "cfg": case "gradle": return "⚙️";
            // Font
            case "ttf": case "otf": case "woff": return "🔤";
            // APK yüklü
            case "xapk": return "📲";
            // Veritabanı
            case "db": case "sqlite": case "sql": return "🗄️";
            // ISO/IMG
            case "iso": case "img": return "💿";
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
