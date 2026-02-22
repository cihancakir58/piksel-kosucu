package com.secrethq.store;

import java.lang.ref.WeakReference;

import org.cocos2dx.lib.Cocos2dxActivity;

import android.app.ProgressDialog;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.secrethq.store.util.*;

import kotlinx.coroutines.GlobalScope;


public class PTStoreBridge {
    private static boolean readyToPurchase = false;

    private static Cocos2dxActivity activity;
    private static WeakReference<Cocos2dxActivity> s_activity;
    private static final String TAG = "PTStoreBridge";

    private static native String licenseKey();

    public static native void purchaseDidComplete(String productId);

    public static native void purchaseDidCompleteRestoring(String productId);

    public static native boolean isProductConsumible(String productId);

    // private static BillingDataSource billingDataSource;  // Disabled - not available
    private static boolean inProgress = false;
    static public void initBridge(Cocos2dxActivity _activity) {
        activity = _activity;

        s_activity = new WeakReference<Cocos2dxActivity>(activity);
        // billingDataSource = BillingDataSource.initialize(activity.getApplication(), GlobalScope.INSTANCE);
        // Billing disabled for production build - using Google Play Billing Client v8.3.0

    }

    public static void acknowledgePendingPurchases() {
        // s_activity.get().runOnUiThread(() -> {
        //     billingDataSource.acknowledgePendingPurchases(activity, (resultCode, message) -> {
        //         if (resultCode == billingDataSource.getBILLING_RESPONSE_RESULT_OK()) {
        //             purchaseDidCompleteRestoring(message);
        //         } else if (resultCode == billingDataSource.getBILLING_RESPONSE_RESULT_RESTORE_COMPLETED()) {
        //             s_activity.get().runOnUiThread(() -> {
        //                 Toast.makeText(activity, "All pending purchases have been acknowledged.", Toast.LENGTH_SHORT).show();
        //             });
        //         }
        //         return null;
        //     });
        // });
    }

    static public void purchase(final String storeId, boolean isConsumable) {
        // Billing disabled for production build
        if (inProgress) {
            return;
        }
        inProgress = true;
        s_activity.get().runOnUiThread(() -> {
            Toast.makeText(activity, "In-app purchases are disabled.", Toast.LENGTH_SHORT).show();
            inProgress = false;
        });
    }

    static public void restorePurchases() {
        // Billing disabled for production build
        s_activity.get().runOnUiThread(() -> {
            Toast.makeText(activity, "Purchase restoration is disabled.", Toast.LENGTH_SHORT).show();
        });
    }
}