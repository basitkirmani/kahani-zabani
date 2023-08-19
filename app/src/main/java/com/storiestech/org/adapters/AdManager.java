package com.storiestech.org.adapters;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.stories.storyappn.R;

import org.jetbrains.annotations.NotNull;

public class AdManager {

    @NotNull
    private static final String TEST_DEVICE = "9A8444263475079460D79BA5F195FA3C";
    private static final boolean ENBALE_TEST = false;
    @NotNull
    private static InterstitialAd ad;
    private static int adCount;
    private static final AdManager INSTANCE = null;

    public static int getAdCount() {
        return adCount;
    }

    public static void setAdCount(int var1) {
        adCount = var1;
    }

    public static void setUpBanner(@NotNull Activity activity) {
        final AdView adView = activity.findViewById(R.id.adView);
        adView.setVisibility(View.GONE);

        if (activity.getString(R.string.bannervisible).equals("yes")) {
            if (ENBALE_TEST) {
                adView.loadAd((new AdRequest.Builder()).addTestDevice(TEST_DEVICE).build());
            } else {
                adView.loadAd((new AdRequest.Builder()).build());
            }
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    adView.setVisibility(View.VISIBLE);
                }
            });
        }
        else {
            adView.setVisibility(View.GONE);
        }
    }

    public static void setUpInterstitialAd(@NotNull Activity activity) {

        if (activity.getString(R.string.insertialvisible).equals("yes")) {

            if (adCount >= activity.getResources().getInteger(R.integer.adCount)) {
                ad = new InterstitialAd(activity);
                ad.setAdUnitId(activity.getString(R.string.AdmobFullScreenAdsID));
                loadAd();
                ad.setAdListener((new AdListener() {
                    public void onAdClosed() {
                        super.onAdClosed();
                        if (AdManager.INSTANCE != null) {
                            loadAd();
                        }
                    }
                }));
            }
        }
    }

    private static void loadAd() {

        if (ENBALE_TEST) {
            ad.loadAd((new AdRequest.Builder()).addTestDevice(TEST_DEVICE).build());
        } else {
            ad.loadAd((new AdRequest.Builder()).build());
        }
    }

    public static void onBackPress(Activity activity) {
        if (activity.getString(R.string.insertialvisible).equals("yes")) {

            if (adCount > activity.getResources().getInteger(R.integer.adCount)) {
                ad.show();
                adCount = 0;
            }
            adCount++;
        }
    }

}
