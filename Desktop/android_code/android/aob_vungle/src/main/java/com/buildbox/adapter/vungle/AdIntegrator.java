package com.buildbox.adapter.vungle;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.vungle.ads.AdConfig;
import com.vungle.ads.BannerAd;
import com.vungle.ads.BannerAdListener;
import com.vungle.ads.BannerAdSize;
import com.vungle.ads.BaseAd;
import com.vungle.ads.InitializationListener;
import com.vungle.ads.InterstitialAd;
import com.vungle.ads.InterstitialAdListener;
import com.vungle.ads.RewardedAd;
import com.vungle.ads.RewardedAdListener;
import com.vungle.ads.VungleAds;
import com.vungle.ads.VungleError;
import com.vungle.ads.VunglePrivacySettings;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Vungle SDK v7 AdIntegrator
 * Implements AdIntegratorInterface for Banner, Interstitial and Rewarded ads.
 */
public class AdIntegrator implements com.buildbox.AdIntegratorInterface {

    private static final String TAG = "VungleAdIntegrator";
    private static final String SDK_ID = "adbox-vungle";

    private WeakReference<Activity> activityRef;
    private com.buildbox.AdIntegratorManagerInterface managerInterface;

    private String appId = "";
    private String bannerPlacementId = "";
    private String interstitialPlacementId = "";
    private String rewardedPlacementId = "";

    private boolean sdkInitialized = false;
    private boolean userConsentGiven = false;
    private boolean targetsChildren = false;

    // Banner
    private BannerAd bannerAd;
    private boolean bannerLoaded = false;
    private boolean bannerVisible = false;
    private FrameLayout bannerContainer;

    // Interstitial
    private InterstitialAd interstitialAd;
    private boolean interstitialLoaded = false;

    // Rewarded
    private RewardedAd rewardedAd;
    private boolean rewardedLoaded = false;

    // Load state constants (match existing adapter contract)
    private static final int LOAD_STATE_IDLE = 0;
    private static final int LOAD_STATE_LOADING = 1;
    private static final int LOAD_STATE_LOADED = 2;
    private static final int LOAD_STATE_ERROR = 3;

    private int bannerLoadState = LOAD_STATE_IDLE;
    private int interstitialLoadState = LOAD_STATE_IDLE;
    private int rewardedLoadState = LOAD_STATE_IDLE;

    // ─────────────────────────────────────────────────────────────────────────
    // AdIntegratorInterface: lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initAds(HashMap<String, String> initValues,
                        WeakReference<Activity> activity,
                        com.buildbox.AdIntegratorManagerInterface managerInterface) {
        this.activityRef = activity;
        this.managerInterface = managerInterface;

        appId = getOrDefault(initValues, "Vungle App ID", "");
        bannerPlacementId = getOrDefault(initValues, "Vungle Banner Placement ID", "");
        interstitialPlacementId = getOrDefault(initValues, "Vungle Interstitial Placement ID", "");
        rewardedPlacementId = getOrDefault(initValues, "Vungle Rewarded Video Placement ID", "");

        if (appId.isEmpty()) {
            Log.e(TAG, "Vungle App ID not set – init aborted");
            managerInterface.sdkFailed(SDK_ID);
            return;
        }

        // Apply consent before init
        applyPrivacySettings();

        VungleAds.init(activity.get(), appId, new InitializationListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Vungle SDK initialized successfully");
                sdkInitialized = true;
                managerInterface.sdkLoaded(SDK_ID);
            }

            @Override
            public void onError(VungleError vungleError) {
                Log.e(TAG, "Vungle SDK init failed: " + vungleError.getLocalizedMessage());
                managerInterface.sdkFailed(SDK_ID);
            }
        });
    }

    @Override
    public void cleanup() {
        destroyBanner();
        interstitialAd = null;
        rewardedAd = null;
    }

    @Override
    public boolean sdkNeedsInit() {
        return !VungleAds.isInitialized();
    }

    @Override
    public boolean sdkIsReady() {
        return sdkInitialized;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Privacy / Consent
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void setUserConsent(boolean consentGiven) {
        userConsentGiven = consentGiven;
        applyPrivacySettings();
    }

    @Override
    public void setTargetsChildren(boolean targetsChildren) {
        this.targetsChildren = targetsChildren;
        applyPrivacySettings();
    }

    private void applyPrivacySettings() {
        VunglePrivacySettings.setGDPRStatus(userConsentGiven,
                userConsentGiven ? "1.0" : "");
        VunglePrivacySettings.setCOPPAStatus(targetsChildren);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Banner
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initBanner() {
        if (!sdkInitialized || bannerPlacementId.isEmpty()) return;
        bannerLoadState = LOAD_STATE_LOADING;
        destroyBanner();

        bannerAd = new BannerAd(activityRef.get(), bannerPlacementId, BannerAdSize.BANNER);
        bannerAd.setAdListener(new BannerAdListener() {
            @Override
            public void onAdLoaded(BaseAd ad) {
                Log.d(TAG, "Banner loaded");
                bannerLoaded = true;
                bannerLoadState = LOAD_STATE_LOADED;
                managerInterface.bannerLoaded(SDK_ID);
            }

            @Override
            public void onAdFailedToLoad(BaseAd ad, VungleError error) {
                Log.e(TAG, "Banner failed to load: " + error.getLocalizedMessage());
                bannerLoaded = false;
                bannerLoadState = LOAD_STATE_ERROR;
                managerInterface.bannerFailed(SDK_ID);
            }

            @Override
            public void onAdStart(BaseAd ad) {}

            @Override
            public void onAdImpression(BaseAd ad) {
                managerInterface.bannerImpression(SDK_ID);
            }

            @Override
            public void onAdClicked(BaseAd ad) {}

            @Override
            public void onAdFailedToPlay(BaseAd ad, VungleError error) {}

            @Override
            public void onAdEnd(BaseAd ad) {}

            @Override
            public void onAdLeftApplication(BaseAd ad) {}
        });

        bannerAd.load(null);
    }

    @Override
    public boolean showBanner() {
        Activity activity = activityRef != null ? activityRef.get() : null;
        if (activity == null || !bannerLoaded || bannerAd == null) return false;
        if (!bannerAd.canPlayAd()) return false;

        activity.runOnUiThread(() -> {
            if (bannerContainer == null) {
                bannerContainer = new FrameLayout(activity);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
                );
                ViewGroup root = activity.getWindow().getDecorView().findViewById(android.R.id.content);
                root.addView(bannerContainer, params);
            }
            bannerContainer.setVisibility(View.VISIBLE);
            bannerAd.getBannerView(activity);
            if (bannerAd.getBannerView(activity).getParent() == null) {
                bannerContainer.addView(bannerAd.getBannerView(activity));
            }
            bannerVisible = true;
        });
        return true;
    }

    @Override
    public void hideBanner() {
        Activity activity = activityRef != null ? activityRef.get() : null;
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (bannerContainer != null) {
                bannerContainer.setVisibility(View.GONE);
            }
            bannerVisible = false;
        });
    }

    @Override
    public boolean isBannerVisible() {
        return bannerVisible;
    }

    @Override
    public long bannerLoadState() {
        return bannerLoadState;
    }

    @Override
    public void clearBannerLoadStateErrors() {
        if (bannerLoadState == LOAD_STATE_ERROR) {
            bannerLoadState = LOAD_STATE_IDLE;
        }
    }

    private void destroyBanner() {
        if (bannerAd != null) {
            bannerAd.finishAd();
            bannerAd = null;
        }
        if (bannerContainer != null) {
            bannerContainer.removeAllViews();
        }
        bannerLoaded = false;
        bannerVisible = false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Interstitial
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initInterstitial() {
        if (!sdkInitialized || interstitialPlacementId.isEmpty()) return;
        interstitialLoadState = LOAD_STATE_LOADING;
        interstitialAd = new InterstitialAd(activityRef.get(), interstitialPlacementId, new AdConfig());
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onAdLoaded(BaseAd ad) {
                Log.d(TAG, "Interstitial loaded");
                interstitialLoaded = true;
                interstitialLoadState = LOAD_STATE_LOADED;
                managerInterface.interstitialLoaded(SDK_ID);
            }

            @Override
            public void onAdFailedToLoad(BaseAd ad, VungleError error) {
                Log.e(TAG, "Interstitial failed to load: " + error.getLocalizedMessage());
                interstitialLoaded = false;
                interstitialLoadState = LOAD_STATE_ERROR;
                managerInterface.interstitialFailed(SDK_ID);
            }

            @Override
            public void onAdStart(BaseAd ad) {}

            @Override
            public void onAdImpression(BaseAd ad) {
                managerInterface.interstitialImpression(SDK_ID);
            }

            @Override
            public void onAdClicked(BaseAd ad) {}

            @Override
            public void onAdFailedToPlay(BaseAd ad, VungleError error) {}

            @Override
            public void onAdEnd(BaseAd ad) {
                interstitialLoaded = false;
                managerInterface.interstitialClosed(SDK_ID);
            }

            @Override
            public void onAdLeftApplication(BaseAd ad) {}
        });

        interstitialAd.load(null);
    }

    @Override
    public boolean showInterstitial() {
        Activity activity = activityRef != null ? activityRef.get() : null;
        if (activity == null || !interstitialLoaded || interstitialAd == null) return false;
        if (!interstitialAd.canPlayAd()) return false;

        activity.runOnUiThread(() -> interstitialAd.play(activity));
        return true;
    }

    @Override
    public long interstitialLoadState() {
        return interstitialLoadState;
    }

    @Override
    public void clearInterstitialLoadStateErrors() {
        if (interstitialLoadState == LOAD_STATE_ERROR) {
            interstitialLoadState = LOAD_STATE_IDLE;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rewarded Video
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initRewardedVideo() {
        if (!sdkInitialized || rewardedPlacementId.isEmpty()) return;
        rewardedLoadState = LOAD_STATE_LOADING;
        rewardedAd = new RewardedAd(activityRef.get(), rewardedPlacementId, new AdConfig());
        rewardedAd.setAdListener(new RewardedAdListener() {
            @Override
            public void onAdLoaded(BaseAd ad) {
                Log.d(TAG, "Rewarded ad loaded");
                rewardedLoaded = true;
                rewardedLoadState = LOAD_STATE_LOADED;
                managerInterface.rewardedVideoLoaded(SDK_ID);
            }

            @Override
            public void onAdFailedToLoad(BaseAd ad, VungleError error) {
                Log.e(TAG, "Rewarded ad failed to load: " + error.getLocalizedMessage());
                rewardedLoaded = false;
                rewardedLoadState = LOAD_STATE_ERROR;
                managerInterface.rewardedVideoFailed(SDK_ID);
            }

            @Override
            public void onAdStart(BaseAd ad) {}

            @Override
            public void onAdImpression(BaseAd ad) {
                managerInterface.rewardedVideoImpression(SDK_ID);
            }

            @Override
            public void onAdClicked(BaseAd ad) {}

            @Override
            public void onAdFailedToPlay(BaseAd ad, VungleError error) {}

            @Override
            public void onAdRewarded(BaseAd ad) {
                managerInterface.rewardedVideoDidReward(SDK_ID, true);
            }

            @Override
            public void onAdEnd(BaseAd ad) {
                rewardedLoaded = false;
                managerInterface.rewardedVideoDidEnd(SDK_ID, true);
            }

            @Override
            public void onAdLeftApplication(BaseAd ad) {}
        });

        rewardedAd.load(null);
    }

    @Override
    public boolean showRewardedVideo() {
        Activity activity = activityRef != null ? activityRef.get() : null;
        if (activity == null || !rewardedLoaded || rewardedAd == null) return false;
        if (!rewardedAd.canPlayAd()) return false;

        activity.runOnUiThread(() -> rewardedAd.play(activity));
        return true;
    }

    @Override
    public boolean isRewardedVideoAvailable() {
        return rewardedLoaded && rewardedAd != null && rewardedAd.canPlayAd();
    }

    @Override
    public long rewardedVideoLoadState() {
        return rewardedLoadState;
    }

    @Override
    public void clearRewardedVideoLoadStateErrors() {
        if (rewardedLoadState == LOAD_STATE_ERROR) {
            rewardedLoadState = LOAD_STATE_IDLE;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Activity lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onActivityCreated(Activity activity) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        cleanup();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String getOrDefault(HashMap<String, String> map, String key, String defaultValue) {
        String value = map.get(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
