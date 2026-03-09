package com.example.myapplication

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.repository.MeasurementRepository
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseAndRepositoryInstrumentedTest {

    @Test
    fun insertAndReadMeasurementForDay() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val repository = MeasurementRepository(db.measurementDao())

        val now = LocalDateTime.now()
        val measurement = Measurement(
            id = 0L,
            timestamp = now,
            type = MeasurementType.TEMPERATURE,
            temperatureCelsius = 37.0
        )

        repository.saveMeasurement(measurement)

        val today = LocalDate.from(now)
        val results = repository.observeMeasurementsForDay(today).first()

        assertEquals(1, results.size)
        assertEquals(37.0, results.first().temperatureCelsius, 0.01)

        db.close()
    }
}

