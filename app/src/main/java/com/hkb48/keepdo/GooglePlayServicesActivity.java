package com.hkb48.keepdo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GooglePlayServicesActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GoogleDriveActivity";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Reference to the folder name on google drive.
     */
    private static final String DRIVE_FOLDER_NAME = "hkb_keepdo";

    /**
     * Reference to the file name on google drive.
     */
    private static final String DRIVE_FILE_NAME = "keepdo.db";

    /**
     * Preference to save the DriveId of existing client data file.
     */
    private static final String DRIVE_ID_PREFERENCE = "hkb_data_file_id";

    /**
     * DriveId reference of an existing folder of client data file.
     */
    private static final String DRIVE_ID_PREFERENCE_FOLDER_KEY = "hkb_data_folder_id_key";

    /**
     * DriveId reference of an existing client data file.
     */
    private static final String DRIVE_ID_PREFERENCE_FILE_KEY = "hkb_data_file_id_key";

    private SharedPreferences mSharedPreferences;
    private DriveId mClientDataFolderDriveId;
    private DriveId mClientDataFileDriveId;

    private DatabaseAdapter mDBAdapter = null;

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDBAdapter = DatabaseAdapter.getInstance(this);

        if (savedInstanceState != null) {
            Log.d(TAG, "saveInstanceStat is not null");
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }

        mSharedPreferences = this.getPreferences(getApplicationContext().MODE_PRIVATE);
        if (mSharedPreferences.contains(DRIVE_ID_PREFERENCE_FILE_KEY)) {
            mClientDataFolderDriveId = DriveId.decodeFromString(mSharedPreferences.getString(DRIVE_ID_PREFERENCE_FOLDER_KEY, null));
            mClientDataFileDriveId = DriveId.decodeFromString(mSharedPreferences.getString(DRIVE_ID_PREFERENCE_FILE_KEY, null));
        } else {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.clear();
            editor.commit();
        }
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    // Optionally, add additional APIs and scopes if required.
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected()");

        storeDataToGoogleDrive();
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "onConnectionSuspended()");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed(): " + result.toString());

        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }

        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }

        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }

    private final void storeDataToGoogleDrive() {
        final ResultCallback<DriveApi.DriveIdResult> fileCheckedCallback = new ResultCallback<DriveApi.DriveIdResult>() {
            @Override
            public void onResult(DriveApi.DriveIdResult result) {
                if (!result.getStatus().isSuccess() || mClientDataFileDriveId == null) {
                    showMessage("Cannot find DriveId. Are you authorized to view this file?");
                    return;
                }

                // Overwrite the data to google drive
                DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), result.getDriveId());
                new UpdateClientDataAsyncTask(GooglePlayServicesActivity.this).execute(file);
            }
        };

        // Check if the client data file has exist
        if (mClientDataFileDriveId == null) {
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(DRIVE_FOLDER_NAME).build();
            Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(
            //Drive.DriveApi.getAppFolder(getGoogleApiClient()).createFolder(
                    getGoogleApiClient(), changeSet).setResultCallback(folderCreatedCallback);
        } else {
            Drive.DriveApi.fetchDriveId(getGoogleApiClient(), mClientDataFileDriveId.toString())
                    .setResultCallback(fileCheckedCallback);
        }
    }

    public class UpdateClientDataAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {
        public UpdateClientDataAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            Log.i(TAG, "doInBackgroundConnected()");

            DriveFile file = args[0];
            try {
                Log.i(TAG, "doInBackgroundConnected() " + file.toString());

                DriveApi.DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.i(TAG, "doInBackgroundConnected() file.open() failed...");
                    return false;
                }

                DriveContents driveContents = driveContentsResult.getDriveContents();
                OutputStream outputStream = driveContents.getOutputStream();
                BufferedInputStream in = new BufferedInputStream(mDBAdapter.readDatabaseStream());
                byte[] buffer = new byte[1024];
                while (in.read(buffer) >= 0) {
                    Log.i(TAG, "doInBackgroundConnected() writing...");
                    outputStream.write(buffer);
                }
                com.google.android.gms.common.api.Status status =
                        driveContents.commit(getGoogleApiClient(), null).await();

                in.close();

                return status.getStatus().isSuccess();
            } catch (IOException e) {
                Log.e(TAG, "IOException while appending to the output stream", e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                showMessage("Error while editing contents");
                finishActivity();
                return;
            }

            showMessage("Successfully edited contents");
            finishActivity();
        }
    }

    final private ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(DriveFolder.DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the folder");
                return;
            }

            mClientDataFolderDriveId = result.getDriveFolder().getDriveId();
            showMessage(this.getClass().getName() + "Created a folder: " + mClientDataFolderDriveId);

            // create new contents resource
            Drive.DriveApi.newDriveContents(getGoogleApiClient())
                    .setResultCallback(driveContentsCallback);
        }
    };

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            Log.i(TAG, this.getClass().getName() + "onResult()");

            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new file contents");
                return;
            }

            DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), mClientDataFolderDriveId);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(DRIVE_FILE_NAME)
                    .setMimeType("application/octet-stream")
                    .setStarred(true).build();
            folder.createFile(getGoogleApiClient(), changeSet, result.getDriveContents())
                    .setResultCallback(fileCreatedCallback);
        }
    };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCreatedCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }

            showMessage(this.getClass().getName() + "Created a file: " + result.getDriveFile().getDriveId());

            try {
                if (mSharedPreferences == null)
                    mSharedPreferences = getPreferences(getApplication().MODE_PRIVATE);
                //SharedPreferences.Editor editor = mSharedPreferences.edit();
                //editor.putString(DRIVE_ID_PREFERENCE_FOLDER_KEY, mClientDataFolderDriveId.toString());
                //editor.putString(DRIVE_ID_PREFERENCE_FILE_KEY, mClientDataFileDriveId.toString());
                //editor.commit();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }

            // Fed content to the empty file have created
            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), result.getDriveFile().getDriveId());
            new UpdateClientDataAsyncTask(GooglePlayServicesActivity.this).execute(file);
        }
    };

    /**
     * Terminate the Google Drive activity
     */
    private void finishActivity() {
        GooglePlayServicesActivity.this.finish();
    }

    /**
     * Shows a toast message.
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    private GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
