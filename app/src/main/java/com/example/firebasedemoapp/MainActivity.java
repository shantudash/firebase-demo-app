package com.example.firebasedemoapp;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasedemoapp.util.SharedPrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public final static int SYSTEM_OVERLAY_PERMISSION_REQUEST_CODE = 101;
    /*
     * Resources
     * https://stackoverflow.com/questions/37711082/how-to-handle-notification-when-app-in-background-in-firebase
     *
     *
     * */

    SharedPrefManager sharedPrefManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefManager = new SharedPrefManager(MainActivity.this);
        TextView tvFirebaseToken= findViewById(R.id.tvFirebaseToken);
        String token="";

        if (TextUtils.isEmpty(sharedPrefManager.getUserFirebaseToken())) {
            storeFirebaseTokenIntoSharedPref();
        } else {
             token = sharedPrefManager.getUserFirebaseToken();
            Log.i(TAG, "firebase token :: " + token);
            tvFirebaseToken.setText(token);
        }
        String finalToken = token;
        tvFirebaseToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", finalToken);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "copied", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkDrawOverlayPermission();
        }
    }

    private void storeFirebaseTokenIntoSharedPref() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.i(TAG, "firebase token :: " + token);
                        sharedPrefManager.setUserFirebaseToken(token);
                        Log.i(TAG, "firebase token from shared pref :: " + sharedPrefManager.getUserFirebaseToken());
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        Log.v(TAG+ " App", "Package Name: " + getApplicationContext().getPackageName());

        // check if we already  have permission to draw over other apps
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            Log.v(TAG+" App", "Requesting Permission" + Settings.canDrawOverlays(MainActivity.this));
            showDialogForActivateSystemOverlay();
        } else {
            Log.v(TAG+" App", "We already have permission for it.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showDialogForActivateSystemOverlay() {
        new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setTitle("Display Over Other Apps")
                .setMessage("Please allow display over other apps to continue.")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Continue with operation
                    // if not construct intent to request permission
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getApplicationContext().getPackageName()));
                    someActivityResultLauncher.launch(intent);
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

//    @TargetApi(Build.VERSION_CODES.M)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.v("App", "OnActivity Result.");
//        //check if received result code
//        //  is equal our requested code for draw permission
//        if (requestCode == SYSTEM_OVERLAY_PERMISSION_REQUEST_CODE) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (Settings.canDrawOverlays(this)) {
//                    disablePullNotificationTouch();
//                }
//            }
//        }
//    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Log.i(TAG, "RESULT OK");
                }
            });
}