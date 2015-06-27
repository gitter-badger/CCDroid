/*
 * MainActivity.java
 *
 * Copyright (c) 2015 Shubham Chaudhary <me@shubhamchaudhary.in>
 *
 * CCDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CCDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CCDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.developfreedom.ccdroid.app;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.developfreedom.ccdroid.app.controllers.ListViewController;
import org.developfreedom.ccdroid.app.controllers.ProjectStorageController;
import org.developfreedom.ccdroid.app.listeners.ListViewItemClickListener;
import org.developfreedom.ccdroid.app.storage.ProjectContract;
import org.developfreedom.ccdroid.app.storage.ProviderController;
import org.developfreedom.ccdroid.app.sync.GenericAccountService;
import org.developfreedom.ccdroid.app.sync.SyncUtils;
import org.developfreedom.ccdroid.app.utils.LogUtils;
import org.developfreedom.ccdroid.app.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.developfreedom.ccdroid.app.utils.LogUtils.*;


public class MainActivity
        extends ActionBarActivity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        ListViewController {

    private static String TAG = LogUtils.makeLogTag(MainActivity.class);
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private ListView projectsListView;
    private Config config;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProjectStorageController mProjectStorageController;
    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        config = new Config(this);
        mProjectStorageController = new ProviderController(getContentResolver());
        SyncUtils.CreateSyncAccount(getApplicationContext());
    }



    @Override
    protected void onStart() {
        super.onStart();
        updateListView();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.main_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = GenericAccountService.GetAccount();
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, ProjectContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, ProjectContract.CONTENT_AUTHORITY);
                    swipeRefreshLayout.setRefreshing(syncActive || syncPending);
                }
            });
        }
    };

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_drawer_item1);
                break;
            case 2:
                mTitle = getString(R.string.title_drawer_item2);
                break;
            case 3:
                mTitle = getString(R.string.title_drawer_item0);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (item.getItemId() == R.id.action_refresh) {
            refresh();
            return true;
        }

        if (item.getItemId() == R.id.action_add_url) {
            show_add_url_dialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        LOGD(TAG, "Refreshing");
        if (Utils.isOnline(this)) {
            // fetch data
            SyncUtils.TriggerRefresh();
        } else {
            LOGI(TAG, "refresh: No Network");
            Toast.makeText(this, getString(R.string.toast_network_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    private void show_add_url_dialog() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_message_add_url)
                .setTitle(R.string.dialog_title_add_url);

        final EditText input = new EditText(this);
        input.setText(config.getUrl());
        builder.setView(input);

        // Add the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                //TODO: Check input text to be a url
                config.setUrl(input.getText().toString());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void updateListView() {
        updateListView(mProjectStorageController.get());
    }

    @Override
    public void updateListView(List<Project> projects) {
        if (projects == null) {
            Toast.makeText(this, getString(R.string.toast_unable_to_fetch_project_list), Toast.LENGTH_SHORT).show();
            LOGE(TAG, "Error: project list came empty");
            return;
        }
        LOGD(TAG, "Starting listview update");
        SimpleAdapter adapter = getAdapterFor(projects);

        projectsListView = (ListView) findViewById(R.id.fragment_listview_projects);

        projectsListView.setAdapter(adapter);
        LOGD(TAG, "Adapter set to projects listview has " + adapter.getCount() + " items");

        projectsListView.setOnItemClickListener(
                new ListViewItemClickListener(
                        projectsListView,
                        this
                )
        );
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private SimpleAdapter getAdapterFor(List<Project> projects) {
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        for (Project project : projects) {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            int drawableId = getDrawableId(
                    project.getLastBuildStatus(),
                    project.getActivity()
            );

            hashMap.put("flag", Integer.toString(drawableId));
            hashMap.put("name", project.getName());
            hashMap.put("activity", project.getActivity());
            hashMap.put("time", project.getLastBuildTime());
            hashMap.put("label", project.getLastBuildLabel());
            hashMap.put("url", project.getWebUrl());

            dataList.add(hashMap);
        }

        String[] keysInDataHashmap = {
                "flag",
                "name",
        };
        int[] valuesIdInListviewLayout = {
                R.id.lw_status_flag,
                R.id.lw_project_name,
        };

        SimpleAdapter adapter = new SimpleAdapter(
                getBaseContext(),
                dataList,
                R.layout.list_row_layout_project, //this layout defines the layout of each item
                keysInDataHashmap,
                valuesIdInListviewLayout
        );
        return adapter;
    }

    private int getDrawableId(String lastBuildStatus, String activity) {
        int drawableId;
        switch (lastBuildStatus) {
            case "Success":
                drawableId = R.drawable.button_green;
                break;
            case "Failure":
                drawableId = R.drawable.button_red;
                break;
            case "Unknown":
                if (activity.equals("Building")) {
                    drawableId = R.drawable.button_refresh;
                } else {
                    drawableId = R.drawable.button_yellow;
                }
                break;
            default:
                drawableId = R.drawable.button_grey;
                break;
        }
        return drawableId;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
