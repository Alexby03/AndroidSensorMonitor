package com.example.sensors.data

import com.example.sensors.core.MeasuredResult
import org.apache.commons.csv.CSVFormat
import java.io.FileReader

fun readResultsCSV(path: String): List<MeasuredResult> {
    val reader = FileReader(path)

    val csvRecords = CSVFormat.DEFAULT
        .withFirstRecordAsHeader()
        .parse(reader)

    return csvRecords.map { record ->
        MeasuredResult(
            timestamp = record.get("timestamp").toLong(),
            algo1Angle = record.get("algo1Angle").toFloat(),
            algo2Angle = record.get("algo2Angle").toFloat()
        )
    }
}