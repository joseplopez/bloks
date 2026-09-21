package com.centelles.bloks.di

import com.centelles.bloks.engine.logic.GameEngine
import com.centelles.bloks.engine.logic.PieceGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePieceGenerator(): PieceGenerator = PieceGenerator()

    @Provides
    @Singleton
    fun provideGameEngine(pieceGenerator: PieceGenerator): GameEngine = GameEngine(pieceGenerator)
}
