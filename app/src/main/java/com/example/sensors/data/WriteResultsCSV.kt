package com.example.sensors.data

import com.example.sensors.core.MeasuredResult
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter

fun writeResultsCSV(
    results: List<MeasuredResult>,
    outputPath: String
) {
    FileWriter(outputPath).use { writer ->
        CSVPrinter(
            writer,
            CSVFormat.DEFAULT.withHeader("timestamp", "algo1Angle", "algo2Angle")
        ).use { csv ->
            results.forEach { result ->
                csv.printRecord(result.timestamp, result.algo1Angle, result.algo2Angle)
            }
        }
    }
}