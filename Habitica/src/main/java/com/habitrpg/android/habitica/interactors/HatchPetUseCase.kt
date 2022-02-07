package com.habitrpg.android.habitica.interactors

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class HatchPetUseCase @Inject
constructor(private val inventoryRepository: InventoryRepository, postExecutionThread: PostExecutionThread) : UseCase<HatchPetUseCase.RequestValues, Items>(postExecutionThread) {
    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<Items> {
        return inventoryRepository.hatchPet(requestValues.egg, requestValues.potion) {
            val petWrapper = View.inflate(requestValues.context, R.layout.pet_imageview, null) as? FrameLayout
            val petImageView = petWrapper?.findViewById(R.id.pet_imageview) as? ImageView

            DataBindingUtils.loadImage(petImageView, "stable_Pet-" + requestValues.egg.key + "-" + requestValues.potion.key)
            val potionName = requestValues.potion.text
            val eggName = requestValues.egg.text
            val dialog = HabiticaAlertDialog(requestValues.context)
            dialog.setTitle(requestValues.context.getString(R.string.hatched_pet_title, potionName, eggName))
            dialog.setAdditionalContentView(petWrapper)
            dialog.addButton(R.string.equip, true) { _, _ ->
                inventoryRepository.equip("pet", requestValues.egg.key + "-" + requestValues.potion.key)
                    .subscribe({}, RxErrorHandler.handleEmptyError())
            }
            dialog.addButton(R.string.share, false) { hatchingDialog, _ ->
                val message = requestValues.context.getString(R.string.share_hatched, potionName, eggName)
                val petImageSideLength = 140
                val sharedImage = Bitmap.createBitmap(petImageSideLength, petImageSideLength, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(sharedImage)
                petImageView?.drawable?.setBounds(0, 0, petImageSideLength, petImageSideLength)
                petImageView?.drawable?.draw(canvas)
                requestValues.context.shareContent("hatchedPet", message, sharedImage)
                hatchingDialog.dismiss()
            }
            dialog.setExtraCloseButtonVisibility(View.VISIBLE)
            dialog.enqueue()
        }
    }

    class RequestValues(val potion: HatchingPotion, val egg: Egg, val context: BaseActivity) : UseCase.RequestValues
}