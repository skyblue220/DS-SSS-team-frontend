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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.Duration
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val sleepDataManager = remember { SleepDataManager(context) }
    val exerciseDataManager = remember { ExerciseDataManager(context) }
    val diseaseDataManager = remember { DiseaseDataManager(context) }
    val medicationListManager = remember { MedicationListManager(context) }
    val selectedDisease = remember { ProfileSettingsStore.load(context).selectedDisease }
    val diseaseType = remember { diseaseTypeFromName(selectedDisease) }
    val insightService = remember { AiInsightService() }
    val coroutineScope = rememberCoroutineScope()
    val recentRecords = recentSevenHealthRecords(LocalDate.now())
    val dateLabels = recentRecords.map { it.date.format(ChartDateFormatter) }
    var sleepValues by remember { mutableStateOf(recentRecords.map { 0f }) }
    var exerciseValues by remember { mutableStateOf(recentRecords.map { 0f }) }
    var diseaseScoreValues by remember { mutableStateOf(recentRecords.map { 0f }) }
    var insightText by remember {
        mutableStateOf(
            fallbackInsight(
                InsightInput(
                    sleepHours = sleepValues,
                    exerciseMinutes = exerciseValues,
                    diseaseScores = diseaseScoreValues,
                    selectedDisease = selectedDisease
                )
            )
        )
    }
    var isInsightLoading by remember { mutableStateOf(false) }
    val averageSleep = sleepValues.average().toFloat()
    val totalExercise = exerciseValues.sum().roundToInt()
    val currentDiseaseScore = diseaseScoreValues.lastOrNull()?.roundToInt() ?: 0

    LaunchedEffect(Unit) {
        sleepValues = recentRecords.map { record ->
            val sleepRecord = sleepDataManager.getSleepData(record.date).first()
            sleepRecord?.sleepHours() ?: 0f
        }
        exerciseValues = recentRecords.map { record ->
            val exerciseRecord = exerciseDataManager.getExerciseData(record.date).first()
            (exerciseRecord?.totalExerciseMinutes() ?: 0).toFloat()
        }
        diseaseScoreValues = recentRecords.map { record ->
            val diseaseRecord = diseaseDataManager.getDiseaseData(record.date).first()
            val medicationItems = medicationListManager.getMedicationList(record.date).first()
            calculateHealthScore(
                diseaseType = diseaseType,
                record = diseaseRecord,
                medicationStatus = calculateMedicationStatus(medicationItems, record.date)
            ).toFloat()
        }
        insightText = fallbackInsight(
            InsightInput(
                sleepHours = sleepValues,
                exerciseMinutes = exerciseValues,
                diseaseScores = diseaseScoreValues,
                selectedDisease = selectedDisease
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatisticsBackground)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp)
    ) {
        LineChartCard(
            iconResId = R.drawable.ic_moon,
            title = "수면 기록",
            topLabel = "평균 수면",
            topValue = formatStatValue(averageSleep),
            topUnit = "시간",
            recentLabel = null,
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
            topValue = currentDiseaseScore.toString(),
            topUnit = "점",
            recentLabel = null,
            values = diseaseScoreValues,
            xLabels = dateLabels,
            maxValue = 100f,
            yLabels = listOf("100", "75", "50", "25", "0"),
            lineColor = SleepBlue,
            fillColor = SleepBlue.copy(alpha = 0.09f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        InsightCard(
            insightText = if (isInsightLoading) "인사이트를 생성 중입니다..." else insightText,
            isLoading = isInsightLoading,
            onGenerateClick = {
                coroutineScope.launch {
                    isInsightLoading = true
                    val input = InsightInput(
                        sleepHours = sleepValues,
                        exerciseMinutes = exerciseValues,
                        diseaseScores = diseaseScoreValues,
                        selectedDisease = selectedDisease
                    )
                    insightText = insightService.createInsight(input)
                    isInsightLoading = false
                }
            }
        )
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
        val left = 4.dp.toPx()
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
        yLabels.forEachIndexed { index, _ ->
            val y = top + chartHeight * (index.toFloat() / gridCount)
            if (index == gridCount) {
                drawLine(
                    color = Color(0xFFC7D0D5),
                    start = Offset(left, y),
                    end = Offset(size.width - right, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
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
        val left = 4.dp.toPx()
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
        yLabels.forEachIndexed { index, _ ->
            val y = top + chartHeight * (index.toFloat() / gridCount)
            if (index == gridCount) {
                drawLine(
                    color = Color(0xFFC7D0D5),
                    start = Offset(left, y),
                    end = Offset(size.width - right, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
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
private fun InsightCard(
    insightText: String,
    isLoading: Boolean,
    onGenerateClick: () -> Unit
) {
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
                insightText.lines()
                    .filter { it.isNotBlank() }
                    .take(3)
                    .forEachIndexed { index, text ->
                        if (index > 0) Spacer(modifier = Modifier.height(5.dp))
                        InsightRow(text = text.removePrefix("-").removePrefix("•").trim())
                    }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGenerateClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = ExerciseTeal),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isLoading) "생성 중..." else "AI 인사이트 생성",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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

private fun SleepRecord.sleepHours(): Float {
    val sleepDuration = Duration.between(bedtime, wakeupTime).let {
        if (it.isNegative || it.isZero) it.plusDays(1) else it
    }
    return sleepDuration.toMinutes() / 60f
}
