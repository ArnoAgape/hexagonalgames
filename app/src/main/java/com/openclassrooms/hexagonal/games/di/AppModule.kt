package com.openclassrooms.hexagonal.games.di

import com.openclassrooms.hexagonal.games.data.service.comment.CommentApi
import com.openclassrooms.hexagonal.games.data.service.comment.FirebaseCommentApi
import com.openclassrooms.hexagonal.games.data.service.post.FirebasePostApi
import com.openclassrooms.hexagonal.games.data.service.user.FirebaseUserApi
import com.openclassrooms.hexagonal.games.data.service.post.PostApi
import com.openclassrooms.hexagonal.games.data.service.user.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This class acts as a Dagger Hilt module, responsible for providing dependencies to other parts of the application.
 * It's installed in the SingletonComponent, ensuring that dependencies provided by this module are created only once
 * and remain available throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
  /**
   * Provides a Singleton instance of PostApi using a FirebasePostApi implementation for testing purposes.
   * This means that whenever a dependency on PostApi is requested, the same instance of PostFakeApi will be used
   * throughout the application, ensuring consistent data for testing scenarios.
   *
   * @return A Singleton instance of FirebasePostApi.
   */
  @Provides
  @Singleton
  fun providePostApi(): PostApi {
    return FirebasePostApi()
  }

  @Provides
  @Singleton
  fun provideUserApi(): UserApi {
    return FirebaseUserApi()
  }

  @Provides
  @Singleton
  fun provideCommentApi(): CommentApi {
    return FirebaseCommentApi()
  }

}
