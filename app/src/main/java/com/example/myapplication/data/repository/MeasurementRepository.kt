package com.example.myapplication.data.repository

import com.example.myapplication.data.local.MeasurementDao
import com.example.myapplication.data.local.endOfDayMillis
import com.example.myapplication.data.local.startOfDayMillis
import com.example.myapplication.data.local.toDomain
import com.example.myapplication.data.local.toEntity
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasurementRepository(
    private val measurementDao: MeasurementDao
) {

    fun observeMeasurementsForRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Measurement>> {
        val start = startDate.startOfDayMillis()
        val end = endDate.endOfDayMillis()
        return measurementDao.getMeasurementsForRange(start, end).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeMeasurementsForDay(date: LocalDate): Flow<List<Measurement>> {
        val start = date.startOfDayMillis()
        val end = date.endOfDayMillis()
        return measurementDao.getMeasurementsForRange(start, end).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeMeasurementsForTypeAndRange(
        type: MeasurementType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Measurement>> {
        val start = startDate.startOfDayMillis()
        val end = endDate.endOfDayMillis()
        return measurementDao.getMeasurementsForTypeAndRange(type.name, start, end).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeLatestMeasurements(limit: Int): Flow<List<Measurement>> {
        return measurementDao.getLatestMeasurements(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getMeasurementById(id: Long): Measurement? {
        return measurementDao.getMeasurementById(id)?.toDomain()
    }

    suspend fun saveMeasurement(measurement: Measurement): Long {
        val entity = measurement.toEntity()
        if (entity.id == 0L) {
            return measurementDao.insertMeasurement(entity)
        }
        measurementDao.updateMeasurement(entity)
        return entity.id
    }

    suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.deleteMeasurement(measurement.toEntity())
    }
}

