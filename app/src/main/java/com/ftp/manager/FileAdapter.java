package com.ftp.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {

    interface OnClick { void onClick(File f); }
    interface OnSelectionChanged { void onSelectionChanged(int count); }

    private List<File> files = new ArrayList<>();
    private final OnClick onClick;
    private OnSelectionChanged onSelectionChanged;
    private Set<Integer> selectedPositions = new HashSet<>();
    private boolean multiSelectMode = false;

    public FileAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void setOnSelectionChanged(OnSelectionChanged listener) {
        this.onSelectionChanged = listener;
    }

    public void setFiles(List<File> list) {
        files = new ArrayList<>(list);
        clearSelection();
        notifyDataSetChanged();
    }

    public List<File> getSelectedFiles() {
        List<File> selected = new ArrayList<>();
        for (int pos : selectedPositions) {
            if (pos < files.size()) selected.add(files.get(pos));
        }
        return selected;
    }

    public void clearSelection() {
        selectedPositions.clear();
        multiSelectMode = false;
        notifyDataSetChanged();
        if (onSelectionChanged != null) onSelectionChanged.onSelectionChanged(0);
    }

    public void selectAll(List<File> allFiles) {
        selectedPositions.clear();
        multiSelectMode = true;
        for (int i = 0; i < files.size(); i++) {
            selectedPositions.add(i);
        }
        notifyDataSetChanged();
        if (onSelectionChanged != null) {
            onSelectionChanged.onSelectionChanged(selectedPositions.size());
        }
    }

    public void enterMultiSelectMode() {
        multiSelectMode = true;
        notifyDataSetChanged();
        if (onSelectionChanged != null) {
            onSelectionChanged.onSelectionChanged(-1);
        }
    }

    public boolean isMultiSelectMode() { return multiSelectMode; }
    public int getSelectedCount() { return selectedPositions.size(); }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        File f = files.get(pos);
        boolean selected = selectedPositions.contains(pos);

        h.icon.setText(f.isDirectory() ? "📁" : getIcon(f.getName()));
        h.icon.setTextSize(28);
        h.name.setText(f.getName());

        if (f.isDirectory()) {
            h.info.setText(R.string.folder);
        } else {
            long s = f.length();
            String size = s < 1024 ? s + " B" :
                    s < 1048576 ? String.format(Locale.getDefault(), "%.1f KB", s/1024.0) :
                    s < 1073741824 ? String.format(Locale.getDefault(), "%.1f MB", s/1048576.0) :
                    String.format(Locale.getDefault(), "%.1f GB", s/1073741824.0);
            h.info.setText(size);
        }

        if (multiSelectMode) {
            h.checkbox.setVisibility(View.VISIBLE);
            h.checkbox.setChecked(selected);
            h.itemView.setBackgroundColor(selected ? 0x331565C0 : 0x00000000);
        } else {
            h.checkbox.setVisibility(View.GONE);
            h.itemView.setBackgroundColor(0x00000000);
        }

        h.itemView.setOnClickListener(v -> {
            int p = h.getAdapterPosition();
            if (p == RecyclerView.NO_ID) return;
            if (multiSelectMode) {
                toggleSelection(p);
            } else {
                onClick.onClick(files.get(p));
            }
        });

        h.itemView.setOnLongClickListener(v -> {
            int p = h.getAdapterPosition();
            if (p == RecyclerView.NO_ID) return false;
            if (!multiSelectMode) {
                multiSelectMode = true;
                notifyDataSetChanged();
                if (onSelectionChanged != null) onSelectionChanged.onSelectionChanged(-1);
            }
            toggleSelection(p);
            return true;
        });
    }

    private void toggleSelection(int pos) {
        if (selectedPositions.contains(pos)) {
            selectedPositions.remove(pos);
        } else {
            selectedPositions.add(pos);
        }
        if (selectedPositions.isEmpty() && !multiSelectMode) multiSelectMode = false;
        notifyItemChanged(pos);
        if (onSelectionChanged != null) {
            onSelectionChanged.onSelectionChanged(selectedPositions.size());
        }
    }

    private String getIcon(String name) {
        String e = name.contains(".") ?
                name.substring(name.lastIndexOf('.')+1).toLowerCase() : "";
        switch (e) {
            case "jpg": case "jpeg": case "png": case "gif":
            case "webp": case "bmp": case "svg": return "🖼️";
            case "mp4": case "mkv": case "avi": case "mov":
            case "wmv": case "flv": case "3gp": return "🎬";
            case "mp3": case "wav": case "flac": case "aac":
            case "ogg": case "m4a": case "wma": return "🎵";
            case "pdf": return "🔴";
            case "doc": case "docx": case "odt": return "📘";
            case "xls": case "xlsx": case "ods": case "csv": return "📗";
            case "ppt": case "pptx": case "odp": return "📙";
            case "zip": case "rar": case "7z": case "tar":
            case "gz": case "bz2": return "📦";
            case "apk": return "📱";
            case "java": case "kt": case "py": case "js":
            case "ts": case "cpp": case "c": case "h": return "💻";
            case "html": case "htm": case "css": return "🌐";
            case "txt": case "log": case "md": return "📝";
            case "json": case "xml": case "yaml": case "yml":
            case "ini": case "cfg": case "gradle": return "⚙️";
            case "ttf": case "otf": case "woff": return "🔤";
            case "db": case "sqlite": case "sql": return "🗄️";
            case "iso": case "img": return "💿";
            default: return "📄";
        }
    }

    @Override
    public int getItemCount() { return files.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView icon, name, info;
        CheckBox checkbox;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.tv_icon);
            name = v.findViewById(R.id.tv_name);
            info = v.findViewById(R.id.tv_info);
            checkbox = v.findViewById(R.id.checkbox);
        }
    }
}
