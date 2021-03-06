package io.tylerwalker.buyyouadrink.activity.profile

import android.app.Application
import android.arch.lifecycle.*
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.tylerwalker.buyyouadrink.model.*
import javax.inject.Inject
import io.tylerwalker.buyyouadrink.R

class SetupProfileViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    val name = MutableLiveData<String>()
    val location = MutableLiveData<Coordinates>()
    val email = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val bio = MutableLiveData<String>()
    val coverImage = MutableLiveData<String>()
    val profileImage = MutableLiveData<String>()
    val drinkSelections = MutableLiveData<MutableList<Drink>>()
    val favoriteDrink = MutableLiveData<Drink>()

    @Inject
    lateinit var profileEventsProcessor: PublishProcessor<ProfileEvent>
    @Inject
    lateinit var profileEventsFlowable: Flowable<ProfileEvent>
    @Inject
    lateinit var navigationEventsProcessor: PublishProcessor<NavigationEvent>

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var userRepository: UserRepository

    var trash = CompositeDisposable()

    private val logTag = "SetupProfileViewModel"

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        trash.add(saveProfileObservable())

        drinkSelections.value?.forEach {
            it.apply {
                isSelected = true
                publishProfileEvent(ProfileEvent.ToggleDrink(this))
            }
        }
    }

    private fun saveProfileObservable(): Disposable = profileEventsFlowable
            .filter { it === ProfileEvent.SaveProfile }
            .doOnNext { Log.d(logTag, "ProfileEvent: SaveProfile") }
            .map { validateInput() }
            .flatMap { saveUser(it) }
            .subscribe({
                if (it.status) {
                    Log.d(logTag, "Save Profile Success: $it")
                    Toast.makeText(getApplication(), "Profile Updated.", Toast.LENGTH_LONG).show()
                    with (localStorage) {
                        putCurrentUser(it.user)
                        setFirstRun(false)
                        navigationEventsProcessor.onNext(NavigationEvent.Home)
                    }
                } else {
                    Log.d(logTag, "Save Profile Service Error ${it.error}")
                    if (it.error != null) {
                        Toast.makeText(getApplication(), "${it.error}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(getApplication(), "Something went wrong...", Toast.LENGTH_LONG).show()
                    }
                }
                }, {
                Log.d(logTag, " Save Profile Error ${it.message}")
                Toast.makeText(getApplication(), "${it.message}", Toast.LENGTH_LONG).show()
            })

    private fun validateInput(): User {
        val user = localStorage.getCurrentUser() ?: throw Exception("No user.")
        Log.d(logTag, "validateInput(): current user ${R.drawable.user}")


        name.value?.let {
            if (it.length > 30) throw Exception("Display name must be less than 30 characters.")
            if (!it.matches(Regex("^[a-zA-Z0-9_]*\$"))) throw Exception("It should only contain alphanumeric characters or underscores.")

            user.display_name = it
        } ?: throw Exception("Display name is required.")

        location.value?.let {
            user.location = it
        }

        email.value?.let {
            if (!Patterns.EMAIL_ADDRESS.matcher(it).matches()) throw Exception("Must be a valid email address.")

            user.email = it
        } ?: throw Exception("Email is required.")

        phone.value?.let {
            if (!(it.length == 10 && it.all { it.isDigit() })) throw Exception("Must be a valid phone number.")

            user.phone = it
        }

        bio.value?.let {
            if (it.length < 15) throw Exception("Bio must be at least 15 characters.")

            user.bio = it
        }

        profileImage.value?.let { user.profile_image = it }
        coverImage.value?.let { user.cover_image = it }

        drinkSelections.value?.map {
            it.name
        }?.let {
            user.drinks = it.joinToString(",")
        }

        favoriteDrink.value?.let {
            user.favoriteDrink = it.name
        }

        Log.d(logTag, "validateInput() success, user: $user")
        return user
    }

    private fun saveUser(user: User): Flowable<UserResponse> = userRepository.updateUser(user).toFlowable()

    fun toggleJuice(view: View) { toggleDrink(Drink.Juice) }
    fun toggleCoffee(view: View) { toggleDrink(Drink.Coffee) }
    fun toggleBubbleTea(view: View) { toggleDrink(Drink.BubbleTea) }
    fun toggleBeer(view: View) { toggleDrink(Drink.Beer) }

    private fun toggleDrink(drink: Drink) {
        if (isDrinkSelected(drink)) {
            removeDrinkFromSelections(drink)
            drink.isSelected = false
        } else {
            addDrinkToSelections(drink)
            drink.isSelected = true
        }

        publishProfileEvent(ProfileEvent.ToggleDrink(drink))
    }

    fun addDrinkToSelections(drink: Drink) =
            drinkSelections.value?.run {
                if (!contains(drink)) {
                    add(drink)
                }
            }

    private fun removeDrinkFromSelections(drink: Drink) =
            drinkSelections.value?.run {
                if (contains(drink)) {
                    remove(drink)
                }
            }

    private fun isDrinkSelected(drink: Drink) = drinkSelections.value?.contains(drink) ?: false

    fun selectJuice(view: View) { selectFavoriteDrink(Drink.Juice) }
    fun selectCoffee(view: View) { selectFavoriteDrink(Drink.Coffee) }
    fun selectBubbleTea(view: View) { selectFavoriteDrink(Drink.BubbleTea) }
    fun selectBeer(view: View) { selectFavoriteDrink(Drink.Beer) }

    fun selectFavoriteDrink(drink: Drink) {
        favoriteDrink.value = drink
    }

    fun done(view: View) {
        when (favoriteDrink.value) {
            null -> { Toast.makeText(getApplication(), "Selection is required.", Toast.LENGTH_SHORT).show() }
            else -> { profileEventsProcessor.onNext(ProfileEvent.DismissFavoriteDrinkDialog) }
        }
    }

    fun showFavoriteDrinkDialog(view: View) {
        profileEventsProcessor.onNext(ProfileEvent.ShowFavoriteDrinkDialog)
    }

    inner class GooglePlaceSelectionListener: PlaceSelectionListener {
        override fun onPlaceSelected(place: Place?) {
            place?.latLng?.let {
                location.value = Coordinates(it.latitude.toFloat(), it.longitude.toFloat())
            }
        }

        override fun onError(p0: Status?) {
            Toast.makeText(getApplication(), "We couldn't use that location.", Toast.LENGTH_LONG).show()
            Log.e(logTag, "error: ${p0?.status}")
        }
    }

    fun save(view: View) {
        publishProfileEvent(ProfileEvent.SaveProfile)
    }

    fun chooseProfileImage(view: View) {
        publishProfileEvent(ProfileEvent.ChooseProfileImage)
    }

    fun chooseCoverImage(view: View) {
        publishProfileEvent(ProfileEvent.ChooseCoverImage)
    }

    fun publishProfileEvent(event: ProfileEvent) {
        profileEventsProcessor.onNext(event)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun newTrash() {
        trash = CompositeDisposable()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun emptyTrash() {
        trash.clear()
        trash.dispose()
    }
}