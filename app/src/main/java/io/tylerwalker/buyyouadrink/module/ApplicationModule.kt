package io.tylerwalker.buyyouadrink.module

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.tylerwalker.buyyouadrink.activity.messages.MessageEvent
import io.tylerwalker.buyyouadrink.model.*
import io.tylerwalker.buyyouadrink.service.AuthService
import io.tylerwalker.buyyouadrink.service.ConversationService
import io.tylerwalker.buyyouadrink.service.InvitationService
import io.tylerwalker.buyyouadrink.service.LocationService
import javax.inject.Singleton

@Module
class ApplicationModule(val context: Context) {
    @Provides
    @Singleton
    fun provideAuthService(): AuthService {
        return AuthService()
    }

    @Provides
    @Singleton
    fun provideLocationService(): LocationService {
        return LocationService()
    }

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return UserRepository()
    }

    @Provides
    @Singleton
    fun provideInvitationService(): InvitationService {
        return InvitationService()
    }

    @Provides
    @Singleton
    fun provideConversationService(): ConversationService {
        return ConversationService()
    }

    @Provides
    fun provideContext(): App = App()

    @Provides
    @Singleton
    fun provideLocalStorage(): LocalStorage {
        return LocalStorage(context)
    }

    @Provides
    @Singleton
    fun provideCurrentUser(localStorage: LocalStorage): User = localStorage.getCurrentUser() ?: User()

    @Provides
    @Singleton
    fun provideAuthEventsProcessor(): PublishProcessor<AuthEvent> = PublishProcessor.create()

    @Provides
    @Singleton
    fun provideAuthEventsFlowable(processor: PublishProcessor<AuthEvent>): Flowable<AuthEvent> = processor

    @Provides
    @Singleton
    fun provideNavigationEventsProcessor(): PublishProcessor<NavigationEvent> = PublishProcessor.create()

    @Provides
    @Singleton
    fun provideNavigationEventsFlowable(processor: PublishProcessor<NavigationEvent>): Flowable<NavigationEvent> = processor

    @Provides
    @Singleton
    fun provideProfileEventsProcessor(): PublishProcessor<ProfileEvent> = PublishProcessor.create()

    @Provides
    @Singleton
    fun provideProfileEventsFlowable(processor: PublishProcessor<ProfileEvent>): Flowable<ProfileEvent> = processor

    @Provides
    @Singleton
    fun provideInvitationEventsProcessor(): PublishProcessor<InvitationEvent> = PublishProcessor.create()

    @Provides
    @Singleton
    fun provideInvitationEventsFlowable(processor: PublishProcessor<InvitationEvent>): Flowable<InvitationEvent> = processor

    @Provides
    @Singleton
    fun provideMessageEventsProcessor(): PublishProcessor<MessageEvent> = PublishProcessor.create()

    @Provides
    @Singleton
    fun provideMessageEventsFlowable(processor: PublishProcessor<MessageEvent>): Flowable<MessageEvent> = processor
}