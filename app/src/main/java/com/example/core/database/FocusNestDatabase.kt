package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray

class FocusNestConverters {
  @TypeConverter
  fun fromStringList(value: List<String>?): String {
    val array = JSONArray()
    value?.forEach { array.put(it) }
    return array.toString()
  }

  @TypeConverter
  fun toStringList(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    return try {
      val array = JSONArray(raw)
      List(array.length()) { index -> array.optString(index, "") }.filter { it.isNotEmpty() }
    } catch (_: Exception) {
      emptyList()
    }
  }

  @TypeConverter
  fun fromIntList(value: List<Int>?): String {
    val array = JSONArray()
    value?.forEach { array.put(it) }
    return array.toString()
  }

  @TypeConverter
  fun toIntList(raw: String?): List<Int> {
    if (raw.isNullOrBlank()) return emptyList()
    return try {
      val array = JSONArray(raw)
      List(array.length()) { index -> array.optInt(index, 0) }
    } catch (_: Exception) {
      emptyList()
    }
  }
}

@Database(
  entities =
    [
      InstalledAppEntity::class,
      AppUsageEntity::class,
      AppLimitEntity::class,
      AppGroupEntity::class,
      BlockProfileEntity::class,
      BlockingScheduleEntity::class,
      FocusSessionEntity::class,
      FocusGoalEntity::class,
      UnlockRuleEntity::class,
      UnlockHistoryEntity::class,
      WebsiteRuleEntity::class,
      ReminderEntity::class,
      NotificationRecordEntity::class,
      DailyAnalyticsEntity::class,
      UserPreferenceEntity::class,
      NfcTagEntity::class,
      QrTokenEntity::class,
      ShortFormRuleEntity::class,
    ],
  version = 1,
  exportSchema = false,
)
@TypeConverters(FocusNestConverters::class)
abstract class FocusNestDatabase : RoomDatabase() {
  abstract fun installedAppDao(): InstalledAppDao
  abstract fun appUsageDao(): AppUsageDao
  abstract fun appLimitDao(): AppLimitDao
  abstract fun appGroupDao(): AppGroupDao
  abstract fun blockProfileDao(): BlockProfileDao
  abstract fun blockingScheduleDao(): BlockingScheduleDao
  abstract fun focusSessionDao(): FocusSessionDao
  abstract fun focusGoalDao(): FocusGoalDao
  abstract fun unlockDao(): UnlockDao
  abstract fun websiteRuleDao(): WebsiteRuleDao
  abstract fun reminderDao(): ReminderDao
  abstract fun notificationRecordDao(): NotificationRecordDao
  abstract fun dailyAnalyticsDao(): DailyAnalyticsDao
  abstract fun userPreferenceDao(): UserPreferenceDao
  abstract fun tokenVaultDao(): TokenVaultDao
  abstract fun shortFormRuleDao(): ShortFormRuleDao

  companion object {
    const val DATABASE_NAME = "focusnest_local.db"

    val MIGRATION_1_2 =
      object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
          // Schema evolution placeholder for future v2 columns without destructive fallback.
        }
      }

    @Volatile private var INSTANCE: FocusNestDatabase? = null

    fun getInstance(context: Context): FocusNestDatabase =
      INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                FocusNestDatabase::class.java,
                DATABASE_NAME,
              )
              .addMigrations(MIGRATION_1_2)
              .build()
              .also { INSTANCE = it }
        }
  }
}
