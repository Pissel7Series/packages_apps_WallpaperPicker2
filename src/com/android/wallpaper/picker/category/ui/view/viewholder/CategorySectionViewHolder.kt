/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.wallpaper.picker.category.ui.view.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.wallpaper.R
import com.android.wallpaper.categorypicker.viewmodel.SectionViewModel
import com.android.wallpaper.picker.category.ui.view.adapter.CategoryAdapter

/** This view holder caches reference to pertinent views in a [CategorySectionView] */
class CategorySectionViewHolder(itemView: View, val displayDensity: Float) :
    RecyclerView.ViewHolder(itemView) {

    // recycler view for the tiles
    private var sectionTiles: RecyclerView

    // title for the section
    private var sectionTitle: TextView
    init {
        sectionTiles = itemView.requireViewById(R.id.category_wallpaper_tiles)
        sectionTitle = itemView.requireViewById(R.id.section_title)
    }

    fun bind(item: SectionViewModel) {
        // TODO: this probably is not necessary but if in the case the sections get updated we
        //  should just update the adapter instead of instantiating a new instance
        sectionTiles.adapter = CategoryAdapter(item.items, displayDensity)
        sectionTiles.layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

        if (item.items.size > 1) {
            sectionTitle.text = "Section title" // TODO: update view model to include section title
            sectionTitle.visibility = View.VISIBLE
        } else {
            sectionTitle.visibility = View.GONE
        }
    }
}