package com.example.tylerwalker.buyyouadrink.module

import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.home.HomeViewModel
import com.example.tylerwalker.buyyouadrink.activity.login.LoginActivity
import com.example.tylerwalker.buyyouadrink.activity.login.LoginViewModel
import com.example.tylerwalker.buyyouadrink.activity.login.SignUpActivity
import com.example.tylerwalker.buyyouadrink.activity.login.SignUpViewModel
import com.example.tylerwalker.buyyouadrink.activity.map.MapActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.ProfileActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.SetupProfileActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.SetupProfileViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(loginActivity: LoginActivity)
    fun inject(loginViewModel: LoginViewModel)
    fun inject(signUpActivity: SignUpActivity)
    fun inject(signUpViewModel: SignUpViewModel)
    fun inject(homeActivity: HomeScreen)
    fun inject(homeViewModel: HomeViewModel)
    fun inject(homeActivity: ProfileActivity)
    fun inject(setupProfileActivity: SetupProfileActivity)
    fun inject(setupProfileViewModel: SetupProfileViewModel)
    fun inject(mapActivity: MapActivity)
}