package com.sss.healthcare

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val StatisticsBackground = Color(0xFFF2FFFF)
private val SleepBlue = Color(0xFF1F7BEA)
private val ExerciseTeal = Color(0xFF20C4C0)
private val MutedText = Color(0xFF7B858B)
private val DarkText = Color(0xFF1C1F23)
private val GridLine = Color(0xFFE3EBEF)
private val ChartDateFormatter = DateTimeFormatter.ofPattern("M/d")

@Composable
fun StatisticsLayout() {
    val scrollState = rememberScrollState()
    val recentRecords = recentSevenHealthRecords(LocalDate.now())
    val sleepValues = recentRecords.map { it.sleepHours }
    val exerciseValues = recentRecords.map { it.exerciseMinutes.toFloat() }
    val dateLabels = recentRecords.map { it.date.format(ChartDateFormatter) }
    val averageSleep = sleepValues.average().toFloat()
    val totalExercise = exerciseValues.sum().roundToInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatisticsBackground)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 96.dp)
    ) {
        LineChartCard(
            iconResId = R.drawable.ic_moon,
            title = "수면 기록",
            topLabel = "평균 수면",
            topValue = formatStatValue(averageSleep),
            topUnit = "시간",
            recentLabel = null,
            axisLabel = "(시간)",
            values = sleepValues,
            xLabels = dateLabels,
            maxValue = 10f,
            yLabels = listOf("10", "8", "6", "4", "2", "0"),
            lineColor = SleepBlue,
            fillColor = SleepBlue.copy(alpha = 0.10f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        BarChartCard(
            iconResId = R.drawable.ic_walk,
            title = "운동 시간",
            topLabel = "주간 운동",
            topValue = totalExercise.toString(),
            topUnit = "분",
            axisLabel = "(분)",
            values = exerciseValues,
            xLabels = dateLabels,
            maxValue = 70f,
            yLabels = listOf("70", "50", "30", "10", "0"),
            barColor = ExerciseTeal
        )

        Spacer(modifier = Modifier.height(14.dp))

        LineChartCard(
            iconResId = R.drawable.ic_disease_record,
            title = "상태 점수",
            topLabel = "현재 점수",
            topValue = "78",
            topUnit = "점",
            recentLabel = null,
            axisLabel = "(점)",
            values = listOf(72f, 74f, 70f, 76f, 78f, 80f, 78f),
            xLabels = dateLabels,
            maxValue = 100f,
            yLabels = listOf("100", "75", "50", "25", "0"),
            lineColor = SleepBlue,
            fillColor = SleepBlue.copy(alpha = 0.09f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        InsightCard()
    }
}

@Composable
private fun ChartCardShell(
    iconResId: Int,
    title: String,
    topLabel: String,
    topValue: String,
    topUnit: String,
    topColor: Color,
    badgeText: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = title,
                    modifier = Modifier.size(54.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    color = DarkText,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                )
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = topLabel,
                        color = MutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = topValue,
                            color = topColor,
                            fontSize = 30.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = topUnit,
                            color = topColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                        )
                    }
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD9E9FF))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = SleepBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun LineChartCard(
    iconResId: Int,
    title: String,
    topLabel: String,
    topValue: String,
    topUnit: String,
    recentLabel: String?,
    axisLabel: String,
    values: List<Float>,
    xLabels: List<String>,
    maxValue: Float,
    yLabels: List<String>,
    lineColor: Color,
    fillColor: Color,
    badgeText: String? = null
) {
    ChartCardShell(
        iconResId = iconResId,
        title = title,
        topLabel = topLabel,
        topValue = topValue,
        topUnit = topUnit,
        topColor = lineColor,
        badgeText = badgeText
    ) {
        if (recentLabel != null) {
            Text(
                text = recentLabel,
                color = MutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
            text = axisLabel,
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        LineChart(
            values = values,
            xLabels = xLabels,
            maxValue = maxValue,
            yLabels = yLabels,
            lineColor = lineColor,
            fillColor = fillColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
        )
    }
}

@Composable
private fun BarChartCard(
    iconResId: Int,
    title: String,
    topLabel: String,
    topValue: String,
    topUnit: String,
    axisLabel: String,
    values: List<Float>,
    xLabels: List<String>,
    maxValue: Float,
    yLabels: List<String>,
    barColor: Color
) {
    ChartCardShell(
        iconResId = iconResId,
        title = title,
        topLabel = topLabel,
        topValue = topValue,
        topUnit = topUnit,
        topColor = barColor
    ) {
        Text(
            text = axisLabel,
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        BarChart(
            values = values,
            xLabels = xLabels,
            maxValue = maxValue,
            yLabels = yLabels,
            barColor = barColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp)
        )
    }
}

@Composable
private fun LineChart(
    values: List<Float>,
    xLabels: List<String>,
    maxValue: Float,
    yLabels: List<String>,
    lineColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val left = 34.dp.toPx()
        val right = 8.dp.toPx()
        val top = 8.dp.toPx()
        val bottom = 24.dp.toPx()
        val chartWidth = size.width - left - right
        val chartHeight = size.height - top - bottom
        val stepX = chartWidth / (values.size - 1)
        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 11.sp.toPx()
            color = MutedText.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val valuePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 11.sp.toPx()
            color = lineColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val gridCount = yLabels.size - 1
        yLabels.forEachIndexed { index, label ->
            val y = top + chartHeight * (index.toFloat() / gridCount)
            drawLine(
                color = if (index == gridCount) Color(0xFFC7D0D5) else GridLine,
                start = Offset(left, y),
                end = Offset(size.width - right, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (index == gridCount) null else androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
            drawContext.canvas.nativeCanvas.drawText(label, left - 18.dp.toPx(), y + 4.dp.toPx(), textPaint)
        }

        val points = values.mapIndexed { index, value ->
            Offset(
                x = left + stepX * index,
                y = top + chartHeight * (1f - (value / maxValue).coerceIn(0f, 1f))
            )
        }
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fillPath = Path().apply {
            moveTo(points.first().x, top + chartHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, top + chartHeight)
            close()
        }

        drawPath(path = fillPath, brush = Brush.verticalGradient(listOf(fillColor, Color.Transparent)))
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        points.forEachIndexed { index, point ->
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = point)
            drawCircle(color = lineColor, radius = 5.dp.toPx(), center = point, style = Stroke(width = 2.dp.toPx()))
            drawContext.canvas.nativeCanvas.drawText(formatChartValue(values[index]), point.x, point.y - 12.dp.toPx(), valuePaint)
        }
        xLabels.forEachIndexed { index, label ->
            val x = left + stepX * index
            textPaint.color = if (index == xLabels.lastIndex) lineColor.toArgb() else Color(0xFF4F5960).toArgb()
            textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, if (index == xLabels.lastIndex) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            drawContext.canvas.nativeCanvas.drawText(label, x, size.height - 4.dp.toPx(), textPaint)
        }
    }
}

@Composable
private fun BarChart(
    values: List<Float>,
    xLabels: List<String>,
    maxValue: Float,
    yLabels: List<String>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val left = 34.dp.toPx()
        val right = 8.dp.toPx()
        val top = 8.dp.toPx()
        val bottom = 24.dp.toPx()
        val chartWidth = size.width - left - right
        val chartHeight = size.height - top - bottom
        val stepX = chartWidth / values.size
        val barWidth = 13.dp.toPx()
        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 11.sp.toPx()
            color = MutedText.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val valuePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 11.sp.toPx()
            color = barColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val gridCount = yLabels.size - 1
        yLabels.forEachIndexed { index, label ->
            val y = top + chartHeight * (index.toFloat() / gridCount)
            drawLine(
                color = if (index == gridCount) Color(0xFFC7D0D5) else GridLine,
                start = Offset(left, y),
                end = Offset(size.width - right, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (index == gridCount) null else androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
            drawContext.canvas.nativeCanvas.drawText(label, left - 18.dp.toPx(), y + 4.dp.toPx(), textPaint)
        }

        values.forEachIndexed { index, value ->
            val centerX = left + stepX * index + stepX / 2f
            val barHeight = chartHeight * (value / maxValue).coerceIn(0f, 1f)
            val barTop = top + chartHeight - barHeight
            drawRoundRect(
                color = barColor,
                topLeft = Offset(centerX - barWidth / 2f, barTop),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawContext.canvas.nativeCanvas.drawText(value.roundToInt().toString(), centerX, barTop - 8.dp.toPx(), valuePaint)
            textPaint.color = if (index == xLabels.lastIndex) barColor.toArgb() else Color(0xFF4F5960).toArgb()
            textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, if (index == xLabels.lastIndex) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            drawContext.canvas.nativeCanvas.drawText(xLabels[index], centerX, size.height - 4.dp.toPx(), textPaint)
        }
    }
}

@Composable
private fun InsightCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFDDF8F4)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "!", color = ExerciseTeal, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "오늘의 인사이트",
                    color = DarkText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InsightRow(text = "수면 시간이 전반적으로 안정적이에요")
                Spacer(modifier = Modifier.height(5.dp))
                InsightRow(text = "주중 운동 시간이 부족한 날이 있었어요")
            }
        }
    }
}

@Composable
private fun InsightRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(ExerciseTeal)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color(0xFF3E464B),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatChartValue(value: Float): String {
    return if (value % 1f == 0f) value.roundToInt().toString() else String.format("%.1f", value)
}

private fun formatStatValue(value: Float): String {
    return String.format("%.1f", value)
}
