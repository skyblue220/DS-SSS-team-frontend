package com.sss.healthcare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import android.app.TimePickerDialog
import java.time.LocalTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon

private val MINT_COLOR = Color(0xFF20C4C0)

@Composable
fun CalendarLayout() {
    val MINT_COLOR = Color(0xFF00A896)
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // ✅ [확인] selectedDate 상태 변수 — 초기값 LocalDate.now() 유지
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val context = LocalContext.current
    val sleepDataManager = remember { SleepDataManager(context) }
    val exerciseDataManager = remember { ExerciseDataManager(context) }
    val mealDataManager = remember { MealDataManager(context) }

    // 기존에 존재하던 질환/복약 매니저 정상 연결 ✅
    val diseaseDataManager = remember { DiseaseDataManager(context) }
    val medicationListManager = remember { MedicationListManager(context) }

    val scope = rememberCoroutineScope()

    // 수면 기록 상태
    var bedtime by remember { mutableStateOf(LocalTime.of(23, 0)) }
    var wakeupTime by remember { mutableStateOf(LocalTime.of(7, 0)) }
    var showSleepDialog by remember { mutableStateOf(false) }

    // 운동 기록 상태
    var showExerciseDialog by remember { mutableStateOf(false) }
    var exerciseRecord by remember { mutableStateOf(ExerciseRecord()) }

    // 식사 기록 상태
    var showMealDialog by remember { mutableStateOf(false) }
    var mealRecord by remember { mutableStateOf(MealRecord()) }

    // 다이얼로그 오픈 여부 상태 변수
    var showDiseaseDialog by remember { mutableStateOf(false) }
    var showMedicineDialog by remember { mutableStateOf(false) }

    // 운동 카드 순환 노출을 위한 상태
    var exerciseDisplayIndex by remember { mutableStateOf(0) }
    val exerciseDisplayItems = listOf(
        Triple("걷기", exerciseRecord.steps, "steps"),
        Triple("달리기", exerciseRecord.runTime, "분"),
        Triple("달리기", exerciseRecord.runDistance, "km"),
        Triple("자전거", exerciseRecord.cycleTime, "분"),
        Triple("자전거", exerciseRecord.cycleDistance, "km")
    )

    // 4초마다 정보 전환 애니메이션 타이머
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            exerciseDisplayIndex = (exerciseDisplayIndex + 1) % exerciseDisplayItems.size
        }
    }

    // 선택된 날짜의 데이터 로드
    LaunchedEffect(selectedDate) {
        launch {
            sleepDataManager.getSleepData(selectedDate).collect { record ->
                if (record != null) {
                    bedtime = record.bedtime
                    wakeupTime = record.wakeupTime
                } else {
                    bedtime = LocalTime.of(23, 0)
                    wakeupTime = LocalTime.of(7, 0)
                }
            }
        }
        launch {
            exerciseDataManager.getExerciseData(selectedDate).collect { record ->
                exerciseRecord = record ?: ExerciseRecord()
            }
        }
        launch {
            mealDataManager.getMealData(selectedDate).collect { record ->
                mealRecord = record ?: MealRecord()
            }
        }
    }

    // 수면 시간 계산
    val sleepDuration = Duration.between(bedtime, wakeupTime).let {
        if (it.isNegative || it.isZero) it.plusDays(1) else it
    }
    val sleepHours = sleepDuration.toHours().toString()

    // --- [다이얼로그 제어 블록] ---
    if (showSleepDialog) {
        SleepTimeDialog(
            initialBedtime = bedtime,
            initialWakeupTime = wakeupTime,
            onDismiss = { showSleepDialog = false },
            onConfirm = { newBedtime, newWakeupTime ->
                scope.launch {
                    sleepDataManager.saveSleepData(selectedDate, SleepRecord(newBedtime, newWakeupTime))
                }
                showSleepDialog = false
            }
        )
    }

    if (showExerciseDialog) {
        ExerciseTimeDialog(
            initialRecord = exerciseRecord,
            onDismiss = { showExerciseDialog = false },
            onConfirm = { newRecord ->
                scope.launch {
                    exerciseDataManager.saveExerciseData(selectedDate, newRecord)
                }
                showExerciseDialog = false
            }
        )
    }

    if (showMealDialog) {
        MealInputDialog(
            initialRecord = mealRecord,
            onDismiss = { showMealDialog = false },
            onConfirm = { newRecord ->
                scope.launch {
                    mealDataManager.saveMealData(selectedDate, newRecord)
                }
                showMealDialog = false
            }
        )
    }

    // 개별 파일에 정의된 진짜 다이얼로그 컴포저블 호출 ✅
    if (showDiseaseDialog) {
        DiseaseRecordDialog(
            dataManager = diseaseDataManager,
            date = selectedDate,
            onDismiss = { showDiseaseDialog = false }
        )
    }

    if (showMedicineDialog) {
        // 💡 참고: 만약 MedicationRecordDialog.kt 내부의 컴포저블 함수명이 다르면 그 이름으로 매칭해주세요!
        MedicationRecordDialog(
            dataManager = medicationListManager,
            date = selectedDate,
            onDismiss = { showMedicineDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2FFFF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 1. 연도/월 선택 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "이전 달",
                    tint = MINT_COLOR,          // ✅ 통일된 민트 컬러 적용
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A3A3A)
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "다음 달",
                    tint = MINT_COLOR,          // ✅ 통일된 민트 컬러 적용
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 2. 요일 헤더 ─────────────────────────────────────────────
        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = when (day) {
                        "일" -> Color(0xFFE53935)
                        "토" -> Color(0xFF1E73E8)
                        else -> Color(0xFF78909C)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. 날짜 그리드
        val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()

        val calendarDays = mutableListOf<Int?>()
        repeat(firstDayOfMonth) { calendarDays.add(null) }
        for (i in 1..daysInMonth) { calendarDays.add(i) }

        val rows = (calendarDays.size + 6) / 7
        Column {
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val index = r * 7 + c
                        val day = if (index < calendarDays.size) calendarDays[index] else null

                        // ✅ [핵심] 선택 여부 판별: 현재 달 + 해당 일 == selectedDate
                        val isSelected = day != null &&
                                currentMonth.atDay(day) == selectedDate

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                // ✅ [하이라이트] 선택된 날짜: 민트 원형 배경 / 미선택: 투명
                                .background(
                                    if (isSelected) MINT_COLOR else Color.Transparent
                                )
                                // ✅ [클릭] day가 null이 아닐 때만 클릭 활성화
                                .clickable(enabled = day != null) {
                                    if (day != null) {
                                        selectedDate = currentMonth.atDay(day)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Text(
                                    text = day.toString(),
                                    // ✅ [텍스트 색상] 선택됨 → 흰색 / 미선택 → 기존 색상 유지
                                    color = if (isSelected) Color.White else Color(0xFF3A3A3A),
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── 4. 선택된 날짜 타이틀 ────────────────────────────────────
        Text(
            text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일" +
                    "(${selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)})의 기록",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3A3A3A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. 하단 데이터 카드 배치
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CalendarActivityCard(
                modifier = Modifier.weight(1f),
                title = "수면",
                value = sleepHours,
                unit = "시간",
                iconId = R.drawable.ic_moon,
                themeColor = Color(0xFF1776C9),
                onClick = { showSleepDialog = true }
            )
            CalendarActivityCard(
                modifier = Modifier.weight(1f),
                title = exerciseDisplayItems[exerciseDisplayIndex].first,
                value = exerciseDisplayItems[exerciseDisplayIndex].second,
                unit = exerciseDisplayItems[exerciseDisplayIndex].third,
                iconId = R.drawable.ic_walk,
                themeColor = Color(0xFF21B8BE),
                onClick = { showExerciseDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        CalendarMealSection(mealRecord) { showMealDialog = true }

        Spacer(modifier = Modifier.height(18.dp))
        CalendarDiseaseRecordSection(onClick = { showDiseaseDialog = true })

        Spacer(modifier = Modifier.height(18.dp))
        CalendarMedicineRecordSection(onClick = { showMedicineDialog = true })

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- 운동 기록 팝업 ---
@Composable
fun ExerciseTimeDialog(
    initialRecord: ExerciseRecord,
    onDismiss: () -> Unit,
    onConfirm: (ExerciseRecord) -> Unit
) {
    var steps by remember { mutableStateOf(initialRecord.steps) }
    var runTime by remember { mutableStateOf(initialRecord.runTime) }
    var runDist by remember { mutableStateOf(initialRecord.runDistance) }
    var cycleTime by remember { mutableStateOf(initialRecord.cycleTime) }
    var cycleDist by remember { mutableStateOf(initialRecord.cycleDistance) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(340.dp).wrapContentHeight(),
            shape = RoundedCornerShape(40.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text("운동 기록", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2D45))
                Text("오늘의 운동 정보를 입력해주세요", fontSize = 16.sp, color = Color(0xFF9EABB8), modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(24.dp))

                ExerciseInputSection(title = "걷기", label1 = "걸음 수") {
                    ExerciseInputField(value = steps, unit = "steps", onValueChange = { steps = it })
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExerciseInputSection(title = "달리기", label1 = "달린 시간", label2 = "달린 거리") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExerciseInputField(modifier = Modifier.weight(1f), value = runTime, unit = "min", onValueChange = { runTime = it })
                        ExerciseInputField(modifier = Modifier.weight(1f), value = runDist, unit = "km", onValueChange = { runDist = it })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExerciseInputSection(title = "자전거", label1 = "운행 시간", label2 = "이동 거리") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExerciseInputField(modifier = Modifier.weight(1f), value = cycleTime, unit = "min", onValueChange = { cycleTime = it })
                        ExerciseInputField(modifier = Modifier.weight(1f), value = cycleDist, unit = "km", onValueChange = { cycleDist = it })
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .width(110.dp).height(56.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF21B8BE), Color(0xFF5DB3FF))), RoundedCornerShape(24.dp))
                            .clickable { onConfirm(ExerciseRecord(steps, runTime, runDist, cycleTime, cycleDist)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("기록 저장", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseInputSection(title: String, label1: String, label2: String? = null, content: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.width(60.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2D45))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(label1, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2D45))
                if (label2 != null) {
                    Text(label2, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2D45))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
fun ExerciseInputField(modifier: Modifier = Modifier, value: String, unit: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFF0F9FA), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF21B8BE), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                modifier = Modifier.width(IntrinsicSize.Min).defaultMinSize(minWidth = 20.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text(" $unit", color = Color(0xFF21B8BE), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- 수면 기록 팝업 ---
@Composable
fun SleepTimeDialog(
    initialBedtime: LocalTime,
    initialWakeupTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, LocalTime) -> Unit
) {
    val context = LocalContext.current
    var bedtime by remember { mutableStateOf(initialBedtime) }
    var wakeupTime by remember { mutableStateOf(initialWakeupTime) }
    val timeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.US)

    var duration = Duration.between(bedtime, wakeupTime)
    if (duration.isNegative || duration.isZero) {
        duration = duration.plusDays(1)
    }

    val totalHours = duration.toHours()
    val totalMinutes = duration.toMinutes() % 60
    val durationText = if (totalMinutes > 0) "${totalHours}시간 ${totalMinutes}분" else "${totalHours}시간"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(340.dp).wrapContentHeight(),
            shape = RoundedCornerShape(40.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.Start) {
                Text(text = "수면 기록", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2D45))
                Text(text = "오늘의 수면 시간을 입력해보세요", fontSize = 16.sp, color = Color(0xFF9EABB8), modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(28.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("기상 시간", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2D45))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFF0F9FA), RoundedCornerShape(20.dp) )
                                .clickable {
                                    TimePickerDialog(context, { _, h, m ->
                                        wakeupTime = LocalTime.of(h, m)
                                    }, wakeupTime.hour, wakeupTime.minute, false).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(wakeupTime.format(timeFormatter), color = Color(0xFF21B8BE), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("취침 시간", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2D45))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFF0F9FA), RoundedCornerShape(20.dp))
                                .clickable {
                                    TimePickerDialog(context, { _, h, m ->
                                        bedtime = LocalTime.of(h, m)
                                    }, bedtime.hour, bedtime.minute, false).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(bedtime.format(timeFormatter), color = Color(0xFF21B8BE), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFF21B8BE), Color(0xFF5DB3FF))), shape = RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("총 수면 시간", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(" $durationText", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .width(100.dp).height(56.dp)
                            .background(Color(0xFF21B8BE), RoundedCornerShape(20.dp))
                            .clickable { onConfirm(bedtime, wakeupTime) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("기록 저장", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 활동 카드 UI 컴포넌트 ---
@Composable
fun CalendarActivityCard(
    modifier: Modifier,
    title: String,
    value: String,
    unit: String,
    iconId: Int,
    themeColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 12.dp)) {
            Box(modifier = Modifier.size(56.dp).align(Alignment.CenterStart), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = iconId), contentDescription = title, modifier = Modifier.size(48.dp))
            }
            Box(modifier = Modifier.width(1.dp).height(64.dp).align(Alignment.CenterStart).offset(x = 58.dp).background(Color(0xFFE7EDF3)))
            Column(modifier = Modifier.fillMaxHeight().padding(start = 70.dp, end = 12.dp, top = 2.dp), verticalArrangement = Arrangement.Top) {
                androidx.compose.animation.AnimatedContent(
                    targetState = Triple(title, value, unit),
                    transitionSpec = {
                        (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                    },
                    label = "ActivityCardAnimation"
                ) { (currentTitle, currentValue, currentUnit) ->
                    Column {
                        Text(currentTitle, color = Color(0xFF111111), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            val fontSize = when {
                                currentValue.length > 5 -> 22.sp
                                currentValue.length > 4 -> 26.sp
                                else -> 36.sp
                            }
                            Text(
                                text = currentValue,
                                color = themeColor,
                                fontSize = fontSize,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 34.sp,
                                maxLines = 1
                            )
                            Text(
                                text = currentUnit,
                                color = themeColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_check_right),
                contentDescription = null,
                tint = Color(0xFFB9C5CA),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(16.dp)
                    .offset(x = (-4).dp)
            )
        }
    }
}

// --- 식사 기록 섹션 ---
@Composable
fun CalendarMealSection(meal: MealRecord, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_food),
                    contentDescription = "식사",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "오늘의 식사",
                    color = Color(0xFF12223C),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_right),
                    contentDescription = null,
                    tint = Color(0xFFB9C5CA),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            val displayMeals = listOf(
                Triple("아침", meal.breakfast, Color(0xFFFFF5E8) to Color(0xFFFF6B00)),
                Triple("점심", meal.lunch, Color(0xFFEAF9F0) to Color(0xFF148847)),
                Triple("저녁", meal.dinner, Color(0xFFF4ECFF) to Color(0xFF8E2DE2))
            )

            displayMeals.forEachIndexed { index, (label, content, colors) ->
                CalendarMealRow(
                    MealRowData(
                        label,
                        if (content.isEmpty()) "아직 입력하지 않음" else content,
                        colors.first,
                        colors.second
                    )
                )
                if (index < displayMeals.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE6EEF4)))
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarMealRow(meal: MealRowData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(meal.badgeBackground)
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = meal.label,
                color = meal.badgeTextColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = meal.content,
            color = if (meal.content == "아직 입력하지 않음") Color(0xFF7D868C) else Color(0xFF1D2D45),
            fontSize = 13.sp,
            fontWeight = if (meal.content == "아직 입력하지 않음") FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

// --- 식사 기록 팝업 ---
@Composable
fun MealInputDialog(
    initialRecord: MealRecord,
    onDismiss: () -> Unit,
    onConfirm: (MealRecord) -> Unit
) {
    var breakfast by remember { mutableStateOf(initialRecord.breakfast) }
    var lunch by remember { mutableStateOf(initialRecord.lunch) }
    var dinner by remember { mutableStateOf(initialRecord.dinner) }
    var snack by remember { mutableStateOf(initialRecord.snack) }
    var nightSnack by remember { mutableStateOf(initialRecord.nightSnack) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(340.dp).wrapContentHeight(),
            shape = RoundedCornerShape(40.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text("식사 기록", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2D45))
                Text("오늘 먹은 식사를 기록해보세요", fontSize = 16.sp, color = Color(0xFF9EABB8), modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(24.dp))

                val categories = listOf(
                    Triple("아침", breakfast, Color(0xFFFF6B00)),
                    Triple("점심", lunch, Color(0xFF148847)),
                    Triple("저녁", dinner, Color(0xFF8E2DE2)),
                    Triple("간식", snack, Color(0xFF21B8BE)),
                    Triple("야식", nightSnack, Color(0xFFE53935))
                )

                categories.forEach { (label, value, color) ->
                    MealInputRow(label, value, color) { newValue ->
                        when(label) {
                            "아침" -> breakfast = newValue
                            "점심" -> lunch = newValue
                            "저녁" -> dinner = newValue
                            "간식" -> snack = newValue
                            "야식" -> nightSnack = newValue
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .width(110.dp).height(56.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF21B8BE), Color(0xFF5DB3FF))), RoundedCornerShape(24.dp))
                            .clickable { onConfirm(MealRecord(breakfast, lunch, dinner, snack, nightSnack)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("기록 저장", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MealInputRow(label: String, value: String, color: Color, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFFF0F9FA), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF5A5A5A), fontSize = 13.sp),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text("${label} 식사를 입력하세요", color = Color(0xFF9EABB8), fontSize = 13.sp)
                        }
                        innerTextField()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(brush = Brush.horizontalGradient(listOf(Color(0xFF21B8BE), Color(0xFF5DB3FF))), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 하단 카드 섹션 컴포저블 ---
@Composable
fun CalendarDiseaseRecordSection(onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.disease_record_icon),
                contentDescription = "질환기록부",
                modifier = Modifier.size(55.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.width(1.dp).height(56.dp).background(Color(0xFFE7EDF3)))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("질환기록부", color = Color(0xFF111111), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("질환 내역을 기록해요", color = Color(0xFF7B8086), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_check_right),
                contentDescription = null,
                tint = Color(0xFFB9C5CA),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CalendarMedicineRecordSection(onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_medicine),
                contentDescription = "복약기록부",
                modifier = Modifier.size(55.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(56.dp)
                    .background(Color(0xFFE7EDF3))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("복약기록부", color = Color(0xFF111111), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("복약 내역을 기록해요", color = Color(0xFF7B8086), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_check_right),
                contentDescription = null,
                tint = Color(0xFFB9C5CA),
                modifier = Modifier
                    .size(16.dp)
            )
        }
    }
}
