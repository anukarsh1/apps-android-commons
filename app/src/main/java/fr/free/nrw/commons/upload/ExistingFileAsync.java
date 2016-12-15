package fr.free.nrw.commons.upload;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;

import java.io.IOException;
import java.util.ArrayList;

import android.support.v7.app.AlertDialog;

import fr.free.nrw.commons.CommonsApplication;
import fr.free.nrw.commons.contributions.ContributionsActivity;

/**
 * Sends asynchronous queries to the Commons MediaWiki API to check that file doesn't already exist
 * Returns true if file exists, false if it doesn't
 */
public class ExistingFileAsync extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = fr.free.nrw.commons.upload.ExistingFileAsync.class.getName();

    private String fileSHA1;
    private Context context;

    public ExistingFileAsync(String fileSHA1, Context context) {
        super();
        this.fileSHA1 = fileSHA1;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        MWApi api = CommonsApplication.createMWApi();
        ApiResult result;

        // https://commons.wikimedia.org/w/api.php?action=query&list=allimages&format=xml&aisha1=801957214aba50cb63bb6eb1b0effa50188900ba
        try {
            result = api.action("query")
                    .param("format", "xml")
                    .param("list", "allimages")
                    .param("aisha1", fileSHA1)
                    .get();
            Log.d(TAG, "Searching Commons API for existing file: " + result.toString());
        } catch (IOException e) {
            Log.e(TAG, "IO Exception: ", e);
            return false;
        }

        ArrayList<ApiResult> resultNodes = result.getNodes("/api/query/allimages/img");
        Log.d(TAG, "Result nodes: " + resultNodes);

        boolean fileExists;
        if (!resultNodes.isEmpty()) {
            fileExists = true;
        } else {
            fileExists = false;
        }

        Log.d(TAG, "File already exists in Commons:" + fileExists);
        return fileExists;
    }

    @Override
    protected void onPostExecute(Boolean fileExists) {
        super.onPostExecute(fileExists);
        //TODO: Add Dialog here to tell user file exists, do you want to continue? Yes/No

        if (fileExists) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setMessage("This file already exists in Commons. Are you sure you want to proceed?")
                    .setTitle("Warning");
            builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Go back to ContributionsActivity
                    Intent intent = new Intent(context, ContributionsActivity.class);
                    context.startActivity(intent);
                }

            });
            builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //No need to do anything, user remains on upload screen
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}