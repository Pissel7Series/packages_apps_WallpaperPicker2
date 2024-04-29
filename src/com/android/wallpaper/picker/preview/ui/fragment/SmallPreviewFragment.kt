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
package com.android.wallpaper.picker.preview.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.Transition
import com.android.wallpaper.R
import com.android.wallpaper.module.logging.UserEventLogger
import com.android.wallpaper.picker.AppbarFragment
import com.android.wallpaper.picker.preview.ui.binder.DualPreviewSelectorBinder
import com.android.wallpaper.picker.preview.ui.binder.PreviewActionsBinder
import com.android.wallpaper.picker.preview.ui.binder.PreviewSelectorBinder
import com.android.wallpaper.picker.preview.ui.binder.SetWallpaperButtonBinder
import com.android.wallpaper.picker.preview.ui.binder.SetWallpaperProgressDialogBinder
import com.android.wallpaper.picker.preview.ui.fragment.smallpreview.DualPreviewViewPager
import com.android.wallpaper.picker.preview.ui.fragment.smallpreview.adapters.TabTextPagerAdapter
import com.android.wallpaper.picker.preview.ui.fragment.smallpreview.views.TabsPagerContainer
import com.android.wallpaper.picker.preview.ui.view.PreviewActionGroup
import com.android.wallpaper.picker.preview.ui.viewmodel.Action
import com.android.wallpaper.picker.preview.ui.viewmodel.WallpaperPreviewViewModel
import com.android.wallpaper.util.DisplayUtils
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * This fragment displays the preview of the selected wallpaper on all available workspaces and
 * device displays.
 */
@AndroidEntryPoint(AppbarFragment::class)
class SmallPreviewFragment : Hilt_SmallPreviewFragment() {

    @Inject @ApplicationContext lateinit var appContext: Context
    @Inject lateinit var displayUtils: DisplayUtils
    @Inject lateinit var logger: UserEventLogger

    private val wallpaperPreviewViewModel by activityViewModels<WallpaperPreviewViewModel>()
    private lateinit var setWallpaperProgressDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        postponeEnterTransition()
        val view =
            inflater.inflate(
                if (displayUtils.hasMultiInternalDisplays())
                    R.layout.fragment_small_preview_foldable
                else R.layout.fragment_small_preview_handheld,
                container,
                false,
            )
        setUpToolbar(view, true, true)
        bindScreenPreview(view)
        bindPreviewActions(view)

        SetWallpaperButtonBinder.bind(
            button = view.requireViewById(R.id.button_set_wallpaper),
            viewModel = wallpaperPreviewViewModel,
            lifecycleOwner = viewLifecycleOwner,
        ) {
            findNavController().navigate(R.id.setWallpaperDialog)
        }

        setWallpaperProgressDialog = getSetWallpaperProgressDialog(inflater)
        SetWallpaperProgressDialogBinder.bind(
            dialog = setWallpaperProgressDialog,
            viewModel = wallpaperPreviewViewModel,
            lifecycleOwner = viewLifecycleOwner,
        )

        view.doOnPreDraw {
            // FullPreviewConfigViewModel not being null indicates that we are navigated to small
            // preview from the full preview, and therefore should play the shared element re-enter
            // animation. Reset it after views are finished binding.
            wallpaperPreviewViewModel.resetFullPreviewConfigViewModel()
            startPostponedEnterTransition()
        }

        return view
    }

    override fun getDefaultTitle(): CharSequence {
        return getString(R.string.preview)
    }

    override fun getToolbarTextColor(): Int {
        return ContextCompat.getColor(requireContext(), R.color.system_on_surface)
    }

    private fun bindScreenPreview(view: View) {
        val currentNavDestId = checkNotNull(findNavController().currentDestination?.id)
        if (displayUtils.hasMultiInternalDisplays()) {
            val dualPreviewView: DualPreviewViewPager =
                view.requireViewById(R.id.dual_preview_pager)
            val viewPager =
                view.requireViewById<TabsPagerContainer>(R.id.pager_container).getViewPager()

            DualPreviewSelectorBinder.bind(
                viewPager,
                dualPreviewView,
                wallpaperPreviewViewModel,
                appContext,
                viewLifecycleOwner,
                currentNavDestId,
                (reenterTransition as Transition?),
                wallpaperPreviewViewModel.fullPreviewConfigViewModel.value,
            ) { sharedElement ->
                wallpaperPreviewViewModel.isViewAsHome =
                    (viewPager.adapter as TabTextPagerAdapter).getIsHome(viewPager.currentItem)
                val extras =
                    FragmentNavigatorExtras(sharedElement to FULL_PREVIEW_SHARED_ELEMENT_ID)
                // Set to false on small-to-full preview transition to remove surfaceView jank.
                (view as ViewGroup).isTransitionGroup = false
                findNavController()
                    .navigate(
                        resId = R.id.action_smallPreviewFragment_to_fullPreviewFragment,
                        args = null,
                        navOptions = null,
                        navigatorExtras = extras
                    )
            }
        } else {
            val viewPager =
                view.requireViewById<TabsPagerContainer>(R.id.pager_container).getViewPager()

            PreviewSelectorBinder.bind(
                viewPager,
                view.requireViewById(R.id.pager_previews),
                displayUtils.getRealSize(displayUtils.getWallpaperDisplay()),
                // TODO: pass correct view models for the view pager
                wallpaperPreviewViewModel,
                appContext,
                viewLifecycleOwner,
                currentNavDestId,
                (reenterTransition as Transition?),
                wallpaperPreviewViewModel.fullPreviewConfigViewModel.value,
            ) { sharedElement ->
                wallpaperPreviewViewModel.isViewAsHome =
                    (viewPager.adapter as TabTextPagerAdapter).getIsHome(viewPager.currentItem)
                val extras =
                    FragmentNavigatorExtras(sharedElement to FULL_PREVIEW_SHARED_ELEMENT_ID)
                // Set to false on small-to-full preview transition to remove surfaceView jank.
                (view as ViewGroup).isTransitionGroup = false
                findNavController()
                    .navigate(
                        resId = R.id.action_smallPreviewFragment_to_fullPreviewFragment,
                        args = null,
                        navOptions = null,
                        navigatorExtras = extras
                    )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Always reset isTransitionGroup value on start for the edge case that the
                // navigation is cancelled and the fragment resumes.
                (view as ViewGroup).isTransitionGroup = true
            }
        }
    }

    private fun bindPreviewActions(view: View) {
        val shareActivityResult =
            registerForActivityResult(
                object : ActivityResultContract<Intent, Int>() {
                    override fun createIntent(context: Context, input: Intent): Intent {
                        return input
                    }

                    override fun parseResult(resultCode: Int, intent: Intent?): Int {
                        return resultCode
                    }
                },
            ) {
                view
                    .findViewById<PreviewActionGroup>(R.id.action_button_group)
                    ?.setIsChecked(Action.SHARE, false)
            }
        PreviewActionsBinder.bind(
            actionGroup = view.requireViewById(R.id.action_button_group),
            floatingSheet = view.requireViewById(R.id.floating_sheet),
            previewViewModel = wallpaperPreviewViewModel,
            actionsViewModel = wallpaperPreviewViewModel.previewActionsViewModel,
            deviceDisplayType = displayUtils.getCurrentDisplayType(requireActivity()),
            lifecycleOwner = viewLifecycleOwner,
            logger = logger,
            onStartEditActivity = {
                findNavController()
                    .navigate(
                        resId = R.id.action_smallPreviewFragment_to_creativeEditPreviewFragment,
                        args = Bundle().apply { putParcelable(ARG_EDIT_INTENT, it) },
                        navOptions = null,
                        navigatorExtras = null,
                    )
            },
            onStartShareActivity = { shareActivityResult.launch(it) },
            onShowDeleteConfirmationDialog = { viewModel ->
                val context = context ?: return@bind
                AlertDialog.Builder(context)
                    .setMessage(R.string.delete_wallpaper_confirmation)
                    .setOnDismissListener { viewModel.onDismiss.invoke() }
                    .setPositiveButton(R.string.delete_live_wallpaper) { _, _ ->
                        if (viewModel.creativeWallpaperDeleteUri != null) {
                            appContext.contentResolver.delete(
                                viewModel.creativeWallpaperDeleteUri,
                                null,
                                null
                            )
                        } else if (viewModel.liveWallpaperDeleteIntent != null) {
                            appContext.startService(viewModel.liveWallpaperDeleteIntent)
                        }
                        activity?.finish()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            },
        )
    }

    private fun getSetWallpaperProgressDialog(
        inflater: LayoutInflater,
    ): AlertDialog {
        val dialogView = inflater.inflate(R.layout.set_wallpaper_progress_dialog_view, null)
        return AlertDialog.Builder(activity).setView(dialogView).create()
    }

    companion object {
        const val SMALL_PREVIEW_HOME_SHARED_ELEMENT_ID = "small_preview_home"
        const val SMALL_PREVIEW_LOCK_SHARED_ELEMENT_ID = "small_preview_lock"
        const val SMALL_PREVIEW_HOME_FOLDED_SHARED_ELEMENT_ID = "small_preview_home_folded"
        const val SMALL_PREVIEW_HOME_UNFOLDED_SHARED_ELEMENT_ID = "small_preview_home_unfolded"
        const val SMALL_PREVIEW_LOCK_FOLDED_SHARED_ELEMENT_ID = "small_preview_lock_folded"
        const val SMALL_PREVIEW_LOCK_UNFOLDED_SHARED_ELEMENT_ID = "small_preview_lock_unfolded"
        const val FULL_PREVIEW_SHARED_ELEMENT_ID = "full_preview"
        const val ARG_EDIT_INTENT = "arg_edit_intent"
    }
}
