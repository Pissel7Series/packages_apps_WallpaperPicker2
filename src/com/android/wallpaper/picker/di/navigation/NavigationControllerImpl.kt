/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.wallpaper.picker.di.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.android.wallpaper.model.WallpaperInfo
import com.android.wallpaper.module.InjectorProvider
import com.android.wallpaper.picker.PreviewFragment
import javax.inject.Inject

class NavigationControllerImpl @Inject constructor() : NavigationController {
    override fun navigateToPreview(
        activity: FragmentActivity,
        wallpaperInfo: WallpaperInfo,
        @PreviewFragment.PreviewMode mode: Int,
        viewAsHome: Boolean,
        viewFullScreen: Boolean,
        testingModeEnabled: Boolean,
        @IdRes viewId: Int,
        transition: Transition
    ) {
        // TODO: @abdullairum replace this preview fragment with the new preview fragment
        val previewFragment =
            InjectorProvider.getInjector()
                .getPreviewFragment(
                    activity,
                    wallpaperInfo,
                    mode,
                    viewAsHome,
                    viewFullScreen,
                    testingModeEnabled
                )

        when (transition) {
            Transition.ADD ->
                activity.supportFragmentManager
                    .beginTransaction()
                    .add(viewId, previewFragment)
                    .commit()
            Transition.REPLACE ->
                activity.supportFragmentManager
                    .beginTransaction()
                    .replace(viewId, previewFragment)
                    .commit()
        }
    }
}