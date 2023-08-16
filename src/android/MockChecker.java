package id.co.aio.cordova;


import android.content.Intent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.provider.Settings.Secure;
import android.provider.Settings.Global;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

public class MockChecker extends CordovaPlugin {

    private JSONObject objGPS = new JSONObject();
    private final String TAG = "MOCKLOCATION";
    private id.co.aio.cordova.MockChecker mContext;

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {
        mContext = this;
        if (action.equals("check")) {
            objGPS = new JSONObject();
            objGPS.put("osVersion",android.os.Build.VERSION.SDK_INT);
            if (android.os.Build.VERSION.SDK_INT <= 29) {
                if (Secure.getString(this.cordova.getActivity().getContentResolver(), Secure.ALLOW_MOCK_LOCATION).equals("0")) {
                    objGPS.put("isMock", false);
                } else {
                    objGPS.put("isMock", true);
                    objGPS.put("title", "GPS spoofing detected");
                    objGPS.put("messages", "Please turn off Allow Mock locations option in developer options.");
                }

            } else {
                HashMap<String, Object> check = isMockPermissionGranted(mContext.cordova.getActivity());
                List<JSONArray> apps = (List)check.get("suspiciousApps");
                Boolean isDevModeEnabled = (Boolean)isDevModeEnabled(mContext.cordova.getActivity());
                objGPS.put("isMock", isDevModeEnabled && apps.size() > 0);
                objGPS.put("isDevMode", isDevModeEnabled);
                if (objGPS.getBoolean("isMock")) {
                    objGPS.put("title", "GPS spoofing detected");
                    objGPS.put("messages", "You have one or more GPS spoofing apps installed on your device.");
                    objGPS.put("suspiciousApps", new JSONArray(apps));
                }
            }
            // Log.i(TAG,"Location", "isMock: " + objGPS.get("isMock"));
            callbackContext.success(objGPS);
            return true;
        } else {
            return false;
        }

    }

    public static HashMap<String, Object> isMockPermissionGranted(Context context) {
        int count = 0;
        HashMap<String, Object> returnVal = new HashMap<>();
        List<JSONObject> apps = new LinkedList<>();
        PackageManager pm = context.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        // get list of all the apps installed
        List<ResolveInfo> ril = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                ApplicationInfo applicationInfo = ri.activityInfo.applicationInfo;
                PackageInfo packageInfo = null;
                // getting AppName
                String name = null;
                try {
                    Resources res = pm.getResourcesForApplication(applicationInfo);
                    packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
                    // if activity label res is found
                    if (ri.activityInfo.labelRes != 0) {
                        name = res.getString(ri.activityInfo.labelRes);
                    } else {
                        name = applicationInfo.loadLabel(pm).toString();
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    name = ri.activityInfo.applicationInfo.packageName;
                }
                if (packageInfo != null) {
                    // Get Permissions
                    String[] requestedPermissions = packageInfo.requestedPermissions;
                    if (requestedPermissions != null) {
                        for (int i = 0; i < requestedPermissions.length; i++) {
                            // Check for System App //
                            if (!((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)) {
                                if (requestedPermissions[i].equals("android.permission.ACCESS_MOCK_LOCATION") && !applicationInfo.packageName.equals(context.getPackageName())) {
                                    JSONObject app = new JSONObject();
                                    try {
                                        app.put("package", applicationInfo.packageName);
                                        app.put("appName", name);
                                    } catch(JSONException e) {}
                                    apps.add(app);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        }
        returnVal.put("suspiciousApps", apps);
        return returnVal;
    }

    public static boolean isDevModeEnabled(Context context) {
        if (Secure.getString(
                context.getContentResolver(),
                Global.DEVELOPMENT_SETTINGS_ENABLED)
                .equals("0")) {
            return false;
        } else {
            return true;
        }

    }

}
