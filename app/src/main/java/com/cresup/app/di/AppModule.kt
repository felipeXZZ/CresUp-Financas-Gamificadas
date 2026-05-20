package com.cresup.app.di

import android.content.Context
import androidx.room.Room
import com.cresup.app.data.local.AppDatabase
import com.cresup.app.data.local.dao.ChallengeDao
import com.cresup.app.data.local.dao.GoalDao
import com.cresup.app.data.local.dao.TransactionDao
import com.cresup.app.data.local.dao.UserDao
import com.cresup.app.data.remote.api.QuoteApi
import com.cresup.app.data.repository.ChallengeRepositoryImpl
import com.cresup.app.data.repository.GoalRepositoryImpl
import com.cresup.app.data.repository.QuoteRepositoryImpl
import com.cresup.app.data.repository.TransactionRepositoryImpl
import com.cresup.app.data.repository.UserRepositoryImpl
import com.cresup.app.domain.repository.ChallengeRepository
import com.cresup.app.domain.repository.GoalRepository
import com.cresup.app.domain.repository.QuoteRepository
import com.cresup.app.domain.repository.TransactionRepository
import com.cresup.app.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "cresup_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()
    @Provides fun provideChallengeDao(db: AppDatabase): ChallengeDao = db.challengeDao()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://zenquotes.io/api/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideQuoteApi(retrofit: Retrofit): QuoteApi =
        retrofit.create(QuoteApi::class.java)

    @Provides
    @Singleton
    fun provideUserRepository(impl: UserRepositoryImpl): UserRepository = impl

    @Provides
    @Singleton
    fun provideTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository = impl

    @Provides
    @Singleton
    fun provideGoalRepository(impl: GoalRepositoryImpl): GoalRepository = impl

    @Provides
    @Singleton
    fun provideChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository = impl

    @Provides
    @Singleton
    fun provideQuoteRepository(impl: QuoteRepositoryImpl): QuoteRepository = impl
}
