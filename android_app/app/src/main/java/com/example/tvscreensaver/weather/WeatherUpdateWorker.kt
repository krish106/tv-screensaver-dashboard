package com.example.tvscreensaver.weather

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class WeatherUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "WeatherUpdateWorker"
        private const val WORK_NAME = "weather_update_work"
        const val UPDATE_INTERVAL_MINUTES = 15L
        
        fun scheduleWeatherUpdates(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                UPDATE_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            
            Log.d(TAG, "Weather updates scheduled every $UPDATE_INTERVAL_MINUTES minutes")
        }

        fun cancelWeatherUpdates(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Weather updates cancelled")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting weather update...")
        
        try {
            val locationProvider = LocationProvider(applicationContext)
            val weatherRepository = WeatherRepository(applicationContext, locationProvider)
            
            val success = weatherRepository.updateWeatherData()
            
            return if (success) {
                Log.d(TAG, "Weather update completed successfully")
                Result.success()
            } else {
                Log.w(TAG, "Weather update failed")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Weather update error", e)
            return Result.failure()
        }
    }
}
