package com.shivam.invisiblewidgetspro.ui;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shivam.invisiblewidgetspro.R;
import com.shivam.invisiblewidgetspro.extras.AppsAdapter;
import com.shivam.invisiblewidgetspro.extras.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shivam on 11/02/17.
 * <p>
 * Some of the code is taken from
 * http://stacktips.com/tutorials/android/how-to-get-list-of-installed-apps-in-android
 */

public class AppSelectorDialogFragment extends DialogFragment {

    @BindView(R.id.recyclerview_installed_apps)
    RecyclerView recyclerView;

    private AppsAdapter adapter;
    private Context mContext;
    private ArrayList<ApplicationInfo> applist;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog, container);

        ButterKnife.bind(this, view);

        mContext = getActivity();

        //todo use recyclerview instead of list view

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.addOnItemTouchListener(new RecyclerViewClickListener(getActivity(), new
                RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                ((AppSelectedListener) getActivity()).getSelectedAppPackage(applist.get(position).packageName);

                dismiss();
            }
        }));

        new LoadApplications().execute();

        return view;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(mContext, null, mContext.getString(R.string
                    .loading_application));
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(mContext.getPackageManager().getInstalledApplications
                    (PackageManager.GET_META_DATA));
            //todo sort applist by app name

            Collections.sort(applist, new Comparator<ApplicationInfo>() {
                @Override
                public int compare(ApplicationInfo applicationInfo, ApplicationInfo t1) {
                    String name1 = applicationInfo.loadLabel(mContext.getPackageManager()).toString();
                    String name2 = t1.loadLabel(mContext.getPackageManager()).toString();

                    return name1.compareToIgnoreCase(name2);
                }
            });

            adapter = new AppsAdapter(mContext, applist);
            return null;
        }



        @Override
        protected void onPostExecute(Void result) {
            recyclerView.setAdapter(adapter);
            progress.dismiss();

            super.onPostExecute(result);
        }


        private ArrayList<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
            ArrayList<ApplicationInfo> applist = new ArrayList<>();
            for (ApplicationInfo info : list) {
                try {
                    //to load only those apps that can be launched
                    if (null != mContext.getPackageManager().getLaunchIntentForPackage(info.packageName)) {
                        applist.add(info);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return applist;
        }
    }

    public interface AppSelectedListener {
        public void getSelectedAppPackage(String packageName);
    }
}