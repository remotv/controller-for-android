/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.remo.android.settingsutil.preferences;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.IntDef;
import androidx.preference.PreferenceViewHolder;
import tv.remo.android.settingsutil.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Helper for all of the different TwoTargetPreference classes to share
 *
 * Taken from the original TwoTargetPreference.java. That is why the license text is up above
 */
class TwoTargetPreferenceHelper {

    @IntDef({ICON_SIZE_DEFAULT, ICON_SIZE_MEDIUM, ICON_SIZE_SMALL})
    @Retention(RetentionPolicy.SOURCE)
    @interface IconSize {
    }

    static final int ICON_SIZE_DEFAULT = 0;
    static final int ICON_SIZE_MEDIUM = 1;
    static final int ICON_SIZE_SMALL = 2;

    static void handleIconSize(Context context, ImageView icon, @IconSize int iconSize){
        switch (iconSize) {
            case ICON_SIZE_SMALL:
                int mSmallIconSize = context.getResources().getDimensionPixelSize(
                        R.dimen.two_target_pref_small_icon_size);
                icon.setLayoutParams(new LinearLayout.LayoutParams(mSmallIconSize, mSmallIconSize));
                break;
            case ICON_SIZE_MEDIUM:
                int mMediumIconSize = context.getResources().getDimensionPixelSize(
                        R.dimen.two_target_pref_medium_icon_size);
                icon.setLayoutParams(
                        new LinearLayout.LayoutParams(mMediumIconSize, mMediumIconSize));
                break;
            case ICON_SIZE_DEFAULT:
                break;
        }
    }

    static void setupSecondTarget(PreferenceViewHolder holder, boolean shouldHideSecondTarget) {
        final View divider = holder.findViewById(R.id.two_target_divider);
        final View widgetFrame = holder.findViewById(android.R.id.widget_frame);
        if (divider != null) {
            divider.setVisibility(shouldHideSecondTarget ? View.GONE : View.VISIBLE);
        }
        if (widgetFrame != null) {
            widgetFrame.setVisibility(shouldHideSecondTarget ? View.GONE : View.VISIBLE);
        }
    }
}
