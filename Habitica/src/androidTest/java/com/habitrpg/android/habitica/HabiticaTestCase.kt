package com.habitrpg.android.habitica

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Before
import java.io.InputStreamReader
import java.lang.reflect.Type
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaField

open class HabiticaTestCase: TestCase() {
    val gson = GSonFactoryCreator.createGson()

    val apiClient: ApiClient = mockk(relaxed = true)
    val userRepository: UserRepository = mockk(relaxed = true)
    val taskRepository: TaskRepository = mockk(relaxed = true)
    val inventoryRepository: InventoryRepository = mockk(relaxed = true)
    val socialRepository: SocialRepository = mockk(relaxed = true)
    val tutorialRepository: TutorialRepository = mockk(relaxed = true)
    val appConfigManager: AppConfigManager = mockk(relaxed = true)
    val contentRepository: ContentRepository = mockk(relaxed = true)
    val userViewModel: MainUserViewModel = mockk(relaxed = true)
    val sharedPreferences: SharedPreferences = mockk(relaxed = true)
    val soundManager: SoundManager = mockk(relaxed = true)
    val notificationsManager: NotificationsManager = mockk(relaxed = true)
    val hostConfig: HostConfig = mockk(relaxed = true)
    val analyticsManager: AnalyticsManager = mockk(relaxed = true)
    val maintenanceService: MaintenanceApiService = mockk(relaxed = true)
    val taskFilterHelper: TaskFilterHelper = mockk(relaxed = true)
    val tagRepository: TagRepository = mockk(relaxed = true)

    val userSubject = PublishSubject.create<User>()
    val userEvents: Flowable<User> = userSubject.toFlowable(BackpressureStrategy.DROP)
    var user = User()
    lateinit var content: ContentResult

    val errorSlot = slot<Throwable>()
    val unmanagedSlot = slot<BaseObject>()

    @Before
    fun setUp() {
        clearAllMocks()

        mockkObject(MainNavigationController)

        user = loadJsonFile("user", User::class.java)
        user.stats?.lvl = 20
        user.stats?.points = 30
        every { userRepository.getUser() } returns userEvents
        every { userViewModel.user } returns MutableLiveData<User?>(user)
        mockkObject(RxErrorHandler)
        every { RxErrorHandler.reportError(capture(errorSlot)) } answers {
            throw errorSlot.captured
        }
        every { socialRepository.getUnmanagedCopy(capture(unmanagedSlot)) } answers { unmanagedSlot.captured }
        content = loadJsonFile("content", ContentResult::class.java)
        every { inventoryRepository.getPets() } returns Flowable.just(content.pets)
        every { inventoryRepository.getMounts() } returns Flowable.just(content.mounts)
        every { inventoryRepository.getItems(Food::class.java) } returns Flowable.just(content.food)
        every { inventoryRepository.getItems(Egg::class.java) } returns Flowable.just(content.eggs)
        every { inventoryRepository.getItems(HatchingPotion::class.java) } returns Flowable.just(content.hatchingPotions)
        every { inventoryRepository.getItems(QuestContent::class.java) } returns Flowable.just(content.quests)

        every { inventoryRepository.getItems(Food::class.java, any()) } returns Flowable.just(content.food)
        every { inventoryRepository.getItems(Egg::class.java, any()) } answers {
            Flowable.just(content.eggs)
        }
        every { inventoryRepository.getItems(HatchingPotion::class.java, any()) } returns Flowable.just(content.hatchingPotions)
        every { inventoryRepository.getItems(QuestContent::class.java, any()) } returns Flowable.just(content.quests)
    }

    internal fun <T> loadJsonFile(s: String, type: Type): T {
        val userStream = javaClass.classLoader?.getResourceAsStream("${s}.json")
        return gson.fromJson(gson.newJsonReader(InputStreamReader(userStream)), type)
    }

    internal fun <C> initializeInjects(obj: C) {
        obj!!::class.java.kotlin.members.forEach {
            if (it.returnType == UserRepository::class.starProjectedType) assign(it, obj, userRepository)
            if (it.returnType == TaskRepository::class.starProjectedType) assign(it, obj, taskRepository)
            if (it.returnType == InventoryRepository::class.starProjectedType) assign(it, obj, inventoryRepository)
            if (it.returnType == SocialRepository::class.starProjectedType) assign(it, obj, socialRepository)
            if (it.returnType == TutorialRepository::class.starProjectedType) assign(it, obj, tutorialRepository)
            if (it.returnType == ContentRepository::class.starProjectedType) assign(it, obj, contentRepository)
            if (it.returnType == AppConfigManager::class.starProjectedType) assign(it, obj, appConfigManager)
            if (it.returnType == MainUserViewModel::class.starProjectedType) assign(it, obj, userViewModel)
            if (it.returnType == ApiClient::class.starProjectedType) assign(it, obj, apiClient)
            if (it.returnType == SoundManager::class.starProjectedType) assign(it, obj, soundManager)
            if (it.returnType == SharedPreferences::class.starProjectedType) assign(it, obj, sharedPreferences)
            if (it.returnType == NotificationsManager::class.starProjectedType) assign(it, obj, notificationsManager)
            if (it.returnType == HostConfig::class.starProjectedType) assign(it, obj, hostConfig)
            if (it.returnType == AnalyticsManager::class.starProjectedType) assign(it, obj, analyticsManager)
            if (it.returnType == MaintenanceApiService::class.starProjectedType) assign(it, obj, maintenanceService)
            if (it.returnType == TaskFilterHelper::class.starProjectedType) assign(it, obj, taskFilterHelper)
            if (it.returnType == TagRepository::class.starProjectedType) assign(it, obj, tagRepository)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <P, C> assign(it: KCallable<*>, obj: C, value: P) {
        if ((it as KMutableProperty1<C, P>).javaField!!.get(obj) == null) {
            it.set(obj, value)
        }
    }
}
