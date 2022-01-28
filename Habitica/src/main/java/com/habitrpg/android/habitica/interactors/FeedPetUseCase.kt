package com.habitrpg.android.habitica.interactors

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.responses.FeedResponse
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class FeedPetUseCase @Inject
constructor(
    private val inventoryRepository: InventoryRepository,
    postExecutionThread: PostExecutionThread
) : UseCase<FeedPetUseCase.RequestValues, FeedResponse>(postExecutionThread) {
    override fun buildUseCaseObservable(requestValues: FeedPetUseCase.RequestValues): Flowable<FeedResponse> {
        return inventoryRepository.feedPet(requestValues.pet, requestValues.food)
            .doOnNext { feedResponse ->
                (requestValues.context as? SnackbarActivity)?.showSnackbar(content = feedResponse.message)
                if (feedResponse.value == -1) {
                    val mountWrapper =
                        View.inflate(
                            requestValues.context,
                            R.layout.pet_imageview,
                            null
                        ) as? FrameLayout
                    val mountImageView =
                        mountWrapper?.findViewById(R.id.pet_imageview) as? ImageView

                    DataBindingUtils.loadImage(
                        mountImageView,
                        "Mount_Icon_" + requestValues.pet.key
                    )
                    val dialog = HabiticaAlertDialog(requestValues.context)
                    dialog.setTitle(
                        requestValues.context.getString(
                            R.string.evolved_pet_title,
                            requestValues.pet.text
                        )
                    )
                    dialog.setAdditionalContentView(mountWrapper)
                    dialog.addButton(R.string.onwards, true)
                    dialog.addButton(R.string.share, false) { hatchingDialog, _ ->
                        val message =
                            requestValues.context.getString(
                                R.string.share_raised,
                                requestValues.pet.text
                            )
                        val mountImageSideLength = 99
                        val sharedImage = Bitmap.createBitmap(
                            mountImageSideLength,
                            mountImageSideLength,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(sharedImage)
                        mountImageView?.drawable?.setBounds(
                            0,
                            0,
                            mountImageSideLength,
                            mountImageSideLength
                        )
                        mountImageView?.drawable?.draw(canvas)
                        requestValues.context.shareContent("raisedPet", message, sharedImage)
                        hatchingDialog.dismiss()
                    }
                    dialog.enqueue()
                }
            }
    }

    class RequestValues(val pet: Pet, val food: Food, val context: BaseActivity) :
        UseCase.RequestValues
}