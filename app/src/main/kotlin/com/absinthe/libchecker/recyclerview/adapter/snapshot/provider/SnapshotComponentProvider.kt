package com.absinthe.libchecker.recyclerview.adapter.snapshot.provider

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.LifecycleCoroutineScope
import coil.load
import com.absinthe.libchecker.R
import com.absinthe.libchecker.bean.ADDED
import com.absinthe.libchecker.bean.CHANGED
import com.absinthe.libchecker.bean.MOVED
import com.absinthe.libchecker.bean.REMOVED
import com.absinthe.libchecker.constant.GlobalValues
import com.absinthe.libchecker.constant.librarymap.IconResMap
import com.absinthe.libchecker.extensions.valueUnsafe
import com.absinthe.libchecker.recyclerview.adapter.snapshot.node.SnapshotComponentNode
import com.absinthe.libchecker.utils.LCAppUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val SNAPSHOT_COMPONENT_PROVIDER = 3

class SnapshotComponentProvider(val lifecycleScope: LifecycleCoroutineScope) : BaseNodeProvider() {

    override val itemViewType: Int = SNAPSHOT_COMPONENT_PROVIDER

    override val layoutId: Int = R.layout.item_snapshot_detail_component

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val snapshotItem = (item as SnapshotComponentNode).item

        helper.setText(R.id.tv_name, snapshotItem.title)

        val colorRes = when (snapshotItem.diffType) {
            ADDED -> R.color.material_green_300
            REMOVED -> R.color.material_red_300
            CHANGED -> R.color.material_yellow_300
            MOVED -> R.color.material_blue_300
            else -> Color.TRANSPARENT
        }

        helper.getView<ImageView>(R.id.iv_type_icon).load(
            when (snapshotItem.diffType) {
                ADDED -> R.drawable.ic_add
                REMOVED -> R.drawable.ic_remove
                CHANGED -> R.drawable.ic_changed
                MOVED -> R.drawable.ic_move
                else -> Color.TRANSPARENT
            }
        )

        helper.itemView.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))

        val chip = helper.getView<Chip>(R.id.chip)

        lifecycleScope.launch(Dispatchers.IO) {
            val rule = LCAppUtils.getRuleWithRegex(snapshotItem.name, snapshotItem.itemType)

            withContext(Dispatchers.Main) {
                rule?.let {
                    chip.apply {
                        setChipIconResource(IconResMap.getIconRes(it.iconIndex))
                        text = it.label
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
                        visibility = View.VISIBLE

                        if (!GlobalValues.isColorfulIcon.valueUnsafe) {
                            val icon = chipIcon
                            icon?.let {
                                it.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
                                chipIcon = it
                            }
                        } else if (IconResMap.isSingleColorIcon(it.iconIndex)) {
                            chipIconTint = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.black))
                        }
                    }
                } ?: let { chip.isGone = true }
            }
        }
    }
}