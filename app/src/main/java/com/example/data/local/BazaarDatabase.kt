package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ListingEntity::class,
        CategoryEntity::class,
        LocationEntity::class,
        FavoriteEntity::class,
        ChatMessageEntity::class,
        RechargeRequestEntity::class,
        UserEntity::class,
        AdminSettingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BazaarDatabase : RoomDatabase() {

    abstract fun listingDao(): ListingDao
    abstract fun categoryDao(): CategoryDao
    abstract fun locationDao(): LocationDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun chatDao(): ChatDao
    abstract fun rechargeDao(): RechargeDao
    abstract fun userDao(): UserDao
    abstract fun adminSettingDao(): AdminSettingDao

    companion object {
        @Volatile
        private var INSTANCE: BazaarDatabase? = null

        fun getDatabase(context: Context): BazaarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BazaarDatabase::class.java,
                    "meri_local_bazaar.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
