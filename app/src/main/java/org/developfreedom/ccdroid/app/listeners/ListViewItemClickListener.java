/*
 * ListViewItemClickListener.java
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

package org.developfreedom.ccdroid.app.listeners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import org.developfreedom.ccdroid.app.R;

import java.util.Map;

public class ListViewItemClickListener implements AdapterView.OnItemClickListener {
    static String TAG = ListViewItemClickListener.class.getSimpleName();
    ListView projectsListView;
    Context context;

    public ListViewItemClickListener(ListView projectsListView, Context applicationContext) {
        this.projectsListView = projectsListView;
        this.context = applicationContext;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        Log.v(TAG, "Listview item clicked");
        ListAdapter adapter = projectsListView.getAdapter();
        final Map<String, String> clickedItem = (Map<String, String>) adapter.getItem(position);
        final String url = clickedItem.get("url");

        showAlertDialog(url, getDetails(clickedItem));
    }

    private void showAlertDialog(final String url, Spanned details) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Details");
        alert.setMessage(details);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG, "Opening the web url");
                openUrl(url);
            }
        });
        alert.show();
    }

    private void openUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
        );
        context.startActivity(browserIntent);
    }

    private Spanned getDetails(Map<String, String> clickedItem) {
        String details = "";
        for (String key : clickedItem.keySet()) {
            details += "<b>";
            details += key.substring(0, 1).toUpperCase() + key.substring(1);
            details += ": ";
            details += "</b>";
            details += clickedItem.get(key);
            details += "<br/>";
        }
        Log.v(TAG, "Details: \n" + Html.fromHtml(details));
        return Html.fromHtml(details);
    }
}
