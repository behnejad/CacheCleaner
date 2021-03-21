package com.example.copcleaner;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private List<AppItem> installedApps;
    private RecyclerView appRecycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appRecycle = findViewById(R.id.installed_app_list);

        try {
            installedApps = getInstalledApps();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        appRecycle.setAdapter(new AppAdapter(installedApps));
        appRecycle.setLayoutManager(new LinearLayoutManager(this));

        TextView countApps = findViewById(R.id.countApps);
        countApps.setText("Total Installed Apps: " + installedApps.size());
    }

    private List<AppItem> getInstalledApps() throws PackageManager.NameNotFoundException {
        List<AppItem> apps = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packs = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        for (PackageInfo p : packs) {
            String appName = p.applicationInfo.loadLabel(packageManager).toString();
            Drawable icon = p.applicationInfo.loadIcon(packageManager);
            String packages = p.applicationInfo.packageName;

            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packages, PackageManager.GET_SHARED_LIBRARY_FILES);
            String publicSourceDir = applicationInfo.dataDir;
            File file = new File(publicSourceDir);
            Log.d("", String.format(Locale.US, "%s - %d", publicSourceDir, file.length()));

            apps.add(new AppItem(appName, icon, packages, file.length()));
        }

        Collections.sort(apps, (o1, o2) -> Long.compare(o2.size, o1.size));
        return apps;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public class AppAdapter extends RecyclerView.Adapter<AppViewHolder> {
        private final List<AppItem> items;

        public AppAdapter(List<AppItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AppViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.installed_app_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            holder.setData(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textInListView;
        private final ImageView imageInListView;
        private final TextView packageInListView;
        private final TextView packageSize;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textInListView = itemView.findViewById(R.id.app_name);
            imageInListView = itemView.findViewById(R.id.app_icon);
            packageInListView = itemView.findViewById(R.id.app_package);
            packageSize = itemView.findViewById(R.id.app_size);
        }

        public void setData(AppItem item) {
            textInListView.setText(item.name);
            imageInListView.setImageDrawable(item.icon);
            packageInListView.setText(item.packages);
            packageSize.setText(FormatFileSize(item.size));
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageInListView.getText().toString()));
            startActivity(intent);
        }
    }

    public class AppItem {
        public String name;
        public Drawable icon;
        public String packages;
        public long size;

        public AppItem(String name, Drawable icon, String packages, long size) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
            this.size = size;
        }
    }

    private String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + " B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + " KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + " MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + " GB";
        }
        return fileSizeString;
    }
}