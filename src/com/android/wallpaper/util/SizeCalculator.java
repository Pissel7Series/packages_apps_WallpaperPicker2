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
package com.android.wallpaper.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.android.systemui.shared.system.QuickStepContract;
import com.android.wallpaper.R;

/**
 * Simple utility class that calculates various sizes relative to the display or current
 * configuration.
 */
public class SizeCalculator {

    /**
     * This parameter is used for comparing the threshold DP of the screen on whether we want a
     * "fewer columns" configuration or a "more columns" configuration.
     */
    private static final int COLUMN_COUNT_THRESHOLD_DP = 820;

    /**
     * The number of columns for a "fewer columns" configuration of the category tiles grid.
     */
    private static final int CATEGORY_FEWER_COLUMNS = 3;

    /**
     * The number of columns for a "more columns" configuration of the category tiles grid.
     */
    private static final int CATEGORY_MORE_COLUMNS = 3;

    /**
     * The number of columns for a "fewer columns" configuration of the featured category tiles
     * grid.
     */
    private static final int FEATURED_CATEGORY_FEWER_COLUMNS = 2;

    /**
     * The number of columns for a "more columns" configuration of the featured category tiles grid.
     */
    private static final int FEATURED_CATEGORY_MORE_COLUMNS = 2;

    /**
     * The number of columns for a "fewer columns" configuration of the individual wallpaper tiles
     * grid.
     */
    private static final int INDIVIDUAL_FEWER_COLUMNS = 3;

    /**
     * The number of columns for a "more columns" configuration of the individual wallpaper tiles
     * grid.
     */
    private static final int INDIVIDUAL_MORE_COLUMNS = 4;

    /**
     * The number of columns for a "fewer columns" configuration of the featured individual
     * wallpaper tiles grid.
     */
    private static final int FEATURED_INDIVIDUAL_FEWER_COLUMNS = 2;

    /**
     * The number of columns for a "more columns" configuration of the featured individual wallpaper
     * tiles grid.
     */
    private static final int FEATURED_INDIVIDUAL_MORE_COLUMNS = 2;

    // Suppress default constructor for noninstantiability.
    private SizeCalculator() {
        throw new AssertionError("Can't initialize a SizeCalculator.");
    }

    /**
     * Returns the number of columns for a grid of category tiles. Selects from fewer and more
     * columns based on the width of the activity.
     */
    public static int getNumCategoryColumns(@NonNull Activity activity) {
        int windowWidthPx = getActivityWindowWidthPx(activity);
        return getNumCategoryColumns(activity, windowWidthPx);
    }

    /**
     * Returns the number of columns for a grid of individual tiles. Selects from fewer and more
     * columns based on the width of the activity.
     */
    public static int getNumIndividualColumns(@NonNull Activity activity) {
        int windowWidthPx = getActivityWindowWidthPx(activity);
        return getNumIndividualColumns(activity, windowWidthPx);
    }

    /**
     * Returns the number of columns for a grid of featured individual tiles. Selects from fewer and
     * more columns based on the width of the activity.
     */
    public static int getNumFeaturedIndividualColumns(@NonNull Activity activity) {
        int windowWidthPx = getActivityWindowWidthPx(activity);
        return getNumFeaturedIndividualColumns(activity, windowWidthPx);
    }

    private static int getNumCategoryColumns(Activity activity, int windowWidthPx) {
        return getNumColumns(activity, windowWidthPx, CATEGORY_FEWER_COLUMNS,
                CATEGORY_MORE_COLUMNS);
    }

    private static int getNumCategoryColumns(Context context, int windowWidthPx) {
        return getNumColumns(context, windowWidthPx, CATEGORY_FEWER_COLUMNS,
                CATEGORY_MORE_COLUMNS);
    }

    private static int getNumFeaturedCategoryColumns(Activity activity, int windowWidthPx) {
        return getNumColumns(activity, windowWidthPx, FEATURED_CATEGORY_FEWER_COLUMNS,
                FEATURED_CATEGORY_MORE_COLUMNS);
    }

    private static int getNumFeaturedCategoryColumns(Context context, int windowWidthPx) {
        return getNumColumns(context, windowWidthPx, FEATURED_CATEGORY_FEWER_COLUMNS,
                FEATURED_CATEGORY_MORE_COLUMNS);
    }

    private static int getNumIndividualColumns(Activity activity, int windowWidthPx) {
        return getNumColumns(
                activity, windowWidthPx, INDIVIDUAL_FEWER_COLUMNS, INDIVIDUAL_MORE_COLUMNS);
    }

    private static int getNumFeaturedIndividualColumns(Activity activity, int windowWidthPx) {
        return getNumColumns(activity, windowWidthPx, FEATURED_INDIVIDUAL_FEWER_COLUMNS,
                FEATURED_INDIVIDUAL_MORE_COLUMNS);
    }

    private static int getNumColumns(
            Context context, int windowWidthPx, int fewerCount, int moreCount) {
        WindowManager windowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        DisplayMetrics metrics = DisplayMetricsRetriever.getInstance()
                .getDisplayMetrics(context.getResources(), display);

        // Columns should be based on the size of the window, not the size of the display.
        int windowWidthDp = (int) (windowWidthPx / metrics.density);
        if (windowWidthDp < COLUMN_COUNT_THRESHOLD_DP) {
            return fewerCount;
        } else {
            return moreCount;
        }
    }

    /**
     * Returns the size of a category grid tile in px.
     */
    public static Point getCategoryTileSize(@NonNull Activity activity) {
        Resources res = activity.getResources();
        int windowWidthPx = getActivityWindowWidthPx(activity);

        int columnCount = getNumCategoryColumns(activity, windowWidthPx);
        return getSquareTileSize(columnCount, windowWidthPx,
                res.getDimensionPixelSize(R.dimen.grid_item_category_padding_horizontal),
                res.getDimensionPixelSize(R.dimen.category_grid_edge_space));
    }

    /**
     * Returns the size of a category grid tile in px.
     */
    public static Point getCategoryTileSize(Context context, int windowWidthPx) {
        Resources res = context.getResources();

        int columnCount = getNumCategoryColumns(context, windowWidthPx);
        return getSquareTileSize(columnCount, windowWidthPx,
                res.getDimensionPixelSize(R.dimen.grid_item_category_padding_horizontal),
                res.getDimensionPixelSize(R.dimen.category_grid_edge_space));
    }

    /**
     * Returns the size of a featured category grid tile in px.
     */
    public static Point getFeaturedCategoryTileSize(@NonNull Activity activity) {
        Resources res = activity.getResources();
        int windowWidthPx = getActivityWindowWidthPx(activity);

        int columnCount = getNumFeaturedCategoryColumns(activity, windowWidthPx);
        return getSquareTileSize(columnCount, windowWidthPx,
                res.getDimensionPixelSize(R.dimen.grid_item_category_padding_horizontal),
                res.getDimensionPixelSize(R.dimen.category_grid_edge_space));
    }

    /**
     * Returns the size of a featured category grid tile in px.
     */
    public static Point getFeaturedCategoryTileSize(Context context, int windowWidthPx) {
        Resources res = context.getResources();

        int columnCount = getNumFeaturedCategoryColumns(context, windowWidthPx);
        return getSquareTileSize(columnCount, windowWidthPx,
                res.getDimensionPixelSize(R.dimen.grid_item_category_padding_horizontal),
                res.getDimensionPixelSize(R.dimen.category_grid_edge_space));
    }

    /**
     * Returns the size of an individual grid tile for the given activity in px.
     */
    public static Point getIndividualTileSize(@NonNull Activity activity) {
        Resources res = activity.getResources();
        int windowWidthPx = getActivityWindowWidthPx(activity);

        int columnCount = getNumIndividualColumns(activity, windowWidthPx);
        return getSquareTileSize(columnCount, windowWidthPx,
                res.getDimensionPixelSize(R.dimen.grid_item_individual_padding_horizontal),
                res.getDimensionPixelSize(R.dimen.wallpaper_grid_edge_space));
    }

    /**
     * Returns the size of a featured individual grid tile for the given activity in px.
     */
    public static Point getFeaturedIndividualTileSize(@NonNull Activity activity) {
        Resources res = activity.getResources();
        int windowWidthPx = getActivityWindowWidthPx(activity);

        int columnCount = getNumFeaturedIndividualColumns(activity, windowWidthPx);
        return getSquareTileSize(columnCount, windowWidthPx,
                res.getDimensionPixelSize(R.dimen.grid_item_featured_individual_padding_horizontal),
                res.getDimensionPixelSize(R.dimen.featured_wallpaper_grid_edge_space));
    }

    /**
     * Returns a suggested thumbnail tile size for images that may be presented either as a
     * category or individual tile on any-sized activity on the device. This size matches the
     * individual tile size when an activity takes up the entire screen's width.
     */
    public static Point getSuggestedThumbnailSize(@NonNull Context appContext) {
        // Category tiles are larger than individual tiles, so get the number of columns for
        // categories and then calculate a tile size for when the app window takes up the entire
        // display.
        int windowWidthPx = getDeviceDisplayWidthPx(appContext);
        int columnCount = getNumColumns(
                appContext, windowWidthPx, INDIVIDUAL_FEWER_COLUMNS, INDIVIDUAL_MORE_COLUMNS);
        return getTileSize(appContext, columnCount, windowWidthPx);
    }

    /**
     * Returns the corner radius to use in a wallpaper preview view so that it's proportional
     * to the screen's corner radius
     */
    public static float getPreviewCornerRadius(@NonNull Activity activity, int previewWidth) {
        Point screenSize = ScreenSizeCalculator.getInstance().getScreenSize(
                activity.getWindowManager().getDefaultDisplay());

        return QuickStepContract.getWindowCornerRadius(activity)
                / ((float) screenSize.x / previewWidth);
    }

    /**
     * Returns the size of a grid tile with the given "fewer" count and "more" count, on the given
     * display. The size is determined by these counts and by the aspect ratio of the display and is
     * in units of px.
     */
    private static Point getTileSize(Context context, int columnCount, int windowWidthPx) {
        WindowManager windowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point screenSizePx = ScreenSizeCalculator.getInstance().getScreenSize(display);

        Resources res = context.getResources();
        int gridPaddingPx = res.getDimensionPixelSize(R.dimen.grid_padding);

        // Note: don't need to multiply by density because getting the dimension from resources
        // already does that.
        int guttersWidthPx = (columnCount + 1) * gridPaddingPx;
        int availableWidthPx = windowWidthPx - guttersWidthPx;

        int widthPx = Math.round((float) availableWidthPx / columnCount);
        int heightPx = Math.round(((float) availableWidthPx / columnCount)
                //* screenSizePx.y / screenSizePx.x);
                * res.getDimensionPixelSize(R.dimen.grid_tile_aspect_height)
                / res.getDimensionPixelSize(R.dimen.grid_tile_aspect_width));
        return new Point(widthPx, heightPx);
    }

    /**
     * Returns the size of a grid tile with the given "fewer" count and "more" count, on the given
     * display. The size is determined by these counts with the aspect ratio of 1:1 and is in units
     * of px.
     */
    private static Point getSquareTileSize(int columnCount, int windowWidthPx, int gridPaddingPx,
            int gridEdgeSpacePx) {
        int availableWidthPx = windowWidthPx
                - gridPaddingPx * 2 * columnCount // Item's left and right padding * column count
                - gridEdgeSpacePx * 2; // Grid view's left and right edge's space
        int widthPx = Math.round((float) availableWidthPx / columnCount);

        return new Point(widthPx, widthPx);
    }

    /**
     * Returns the available width of the activity window in pixels.
     */
    public static int getActivityWindowWidthPx(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point outPoint = new Point();
        display.getSize(outPoint);

        return outPoint.x;
    }

    /**
     * Returns the available width of the device's default display in pixels.
     */
    private static int getDeviceDisplayWidthPx(Context appContext) {
        WindowManager windowManager =
                (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();

        Point outPoint = new Point();
        defaultDisplay.getSize(outPoint);

        return outPoint.x;
    }

    /**
     * Adjusts the corner radius of the given view by doubling their current values
     *
     * @param view whose background is set to a GradientDrawable
     */
    public static void adjustBackgroundCornerRadius(View view) {
        GradientDrawable background = (GradientDrawable) view.getBackground();
        // Using try/catch because currently GradientDrawable has a bug where when the radii array
        // is null, instead of getCornerRadii returning null, it throws NPE.
        try {
            float[] radii = background.getCornerRadii();
            if (radii == null) {
                return;
            }
            for (int i = 0; i < radii.length; i++) {
                radii[i] *= 2f;
            }
            background = ((GradientDrawable) background.mutate());
            background.setCornerRadii(radii);
            view.setBackground(background);
        } catch (NullPointerException e) {
            //Ignore in this case, since it means the radius was 0.
        }
    }
}
