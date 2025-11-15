package com.speedtest.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.speedtest.app.data.local.entity.SpeedTestResult
import kotlin.math.max

/**
 * Composable for displaying speed test history chart
 */
@Composable
fun SpeedHistoryChart(
    results: List<SpeedTestResult>,
    modifier: Modifier = Modifier
) {
    if (results.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data to display",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(modifier = modifier) {
        // Chart title
        Text(
            text = "Speed History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Chart
        SpeedLineChart(
            results = results,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ChartLegendItem(
                color = MaterialTheme.colorScheme.primary,
                label = "Download"
            )
            ChartLegendItem(
                color = MaterialTheme.colorScheme.secondary,
                label = "Upload"
            )
        }
    }
}

@Composable
private fun SpeedLineChart(
    results: List<SpeedTestResult>,
    modifier: Modifier = Modifier
) {
    val downloadColor = MaterialTheme.colorScheme.primary
    val uploadColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Canvas(modifier = modifier.padding(16.dp)) {
        val width = size.width
        val height = size.height

        if (results.isEmpty()) return@Canvas

        // Find max speed for scaling
        val maxSpeed = max(
            results.maxOf { it.downloadSpeed },
            results.maxOf { it.uploadSpeed }
        ).let { max(it, 1.0) } // Avoid division by zero

        val pointSpacing = width / (results.size - 1).coerceAtLeast(1)

        // Draw grid lines
        for (i in 0..4) {
            val y = height * i / 4
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw download speed line
        val downloadPath = Path()
        results.forEachIndexed { index, result ->
            val x = index * pointSpacing
            val y = height - (result.downloadSpeed / maxSpeed * height).toFloat()

            if (index == 0) {
                downloadPath.moveTo(x, y)
            } else {
                downloadPath.lineTo(x, y)
            }
        }

        drawPath(
            path = downloadPath,
            color = downloadColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw upload speed line
        val uploadPath = Path()
        results.forEachIndexed { index, result ->
            val x = index * pointSpacing
            val y = height - (result.uploadSpeed / maxSpeed * height).toFloat()

            if (index == 0) {
                uploadPath.moveTo(x, y)
            } else {
                uploadPath.lineTo(x, y)
            }
        }

        drawPath(
            path = uploadPath,
            color = uploadColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw points
        results.forEachIndexed { index, result ->
            val x = index * pointSpacing
            val downloadY = height - (result.downloadSpeed / maxSpeed * height).toFloat()
            val uploadY = height - (result.uploadSpeed / maxSpeed * height).toFloat()

            // Download points
            drawCircle(
                color = downloadColor,
                radius = 4.dp.toPx(),
                center = Offset(x, downloadY)
            )

            // Upload points
            drawCircle(
                color = uploadColor,
                radius = 4.dp.toPx(),
                center = Offset(x, uploadY)
            )
        }
    }
}

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * Composable for displaying ping history chart
 */
@Composable
fun PingHistoryChart(
    results: List<SpeedTestResult>,
    modifier: Modifier = Modifier
) {
    if (results.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data to display",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val pingColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Column(modifier = modifier) {
        Text(
            text = "Ping History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(16.dp)
        ) {
            val width = size.width
            val height = size.height

            if (results.isEmpty()) return@Canvas

            val maxPing = results.maxOf { it.ping }.coerceAtLeast(1)
            val barWidth = (width / results.size) * 0.7f
            val spacing = width / results.size

            // Draw grid
            for (i in 0..4) {
                val y = height * i / 4
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw bars
            results.forEachIndexed { index, result ->
                val x = index * spacing + (spacing - barWidth) / 2
                val barHeight = (result.ping.toFloat() / maxPing) * height
                val y = height - barHeight

                drawRect(
                    color = pingColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
            }
        }
    }
}
