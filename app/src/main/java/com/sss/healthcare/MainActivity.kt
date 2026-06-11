package com.sss.healthcare


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sss.healthcare.ui.theme.HealthCareTheme
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import kotlin.math.roundToInt



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Android 13(API 33) 이상 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        setContent {
            HealthCareTheme {
                HealthCareAppScreen()
            }
        }
    }
}

@Composable
fun HealthCareAppScreen() {
    val context = LocalContext.current
    val sleepDataManager = remember { SleepDataManager(context) }
    val exerciseDataManager = remember { ExerciseDataManager(context) }
    val mealDataManager = remember { MealDataManager(context) }
    val diseaseDataManager = remember { DiseaseDataManager(context) }
    val medicationListManager = remember { MedicationListManager(context) }
    val scope = rememberCoroutineScope()

    // 0: 홈, 1: 기록, 2: 통계, 3: 정보
    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfileSettings by remember { mutableStateOf(false) }
    var showInitialSetup by remember { mutableStateOf(true) }

    // 수면 기록 상태
    var bedtime by remember { mutableStateOf(LocalTime.of(23, 0)) }
    var wakeupTime by remember { mutableStateOf(LocalTime.of(7, 0)) }
    var showSleepDialog by remember { mutableStateOf(false) }

    // 운동 기록 상태
    var showExerciseDialog by remember { mutableStateOf(false) }
    var exerciseRecord by remember { mutableStateOf(ExerciseRecord()) }

    // 오늘 식사 기록 상태
    var mealRecord by remember { mutableStateOf(MealRecord()) }
    var weeklyHealthScore by remember { mutableIntStateOf(0) }

    // 운동 카드 순환 노출을 위한 상태 (홈 화면용)
    var exerciseDisplayIndex by remember { mutableStateOf(0) }
    val exerciseDisplayItems = listOf(
        Triple("걷기", exerciseRecord.walkTime, "분"),
        Triple("달리기", exerciseRecord.runTime, "분"),
        Triple("달리기", exerciseRecord.runDistance, "km"),
        Triple("자전거", exerciseRecord.cycleTime, "분"),
        Triple("자전거", exerciseRecord.cycleDistance, "km")
    )

    // 2초마다 정보 전환 애니메이션 타이머
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)//홈화면 (운동버튼 정보 변경주기)
            exerciseDisplayIndex = (exerciseDisplayIndex + 1) % exerciseDisplayItems.size
        }
    }

    LaunchedEffect(selectedTab, showInitialSetup, showProfileSettings) {
        val selectedDisease = ProfileSettingsStore.load(context).selectedDisease
        val diseaseType = diseaseTypeFromName(selectedDisease)
        val recentDates = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
        val scores = recentDates.map { date ->
            val diseaseRecord = diseaseDataManager.getDiseaseData(date).first()
            val medicationItems = medicationListManager.getMedicationList(date).first()
            calculateHealthScore(
                diseaseType = diseaseType,
                record = diseaseRecord,
                medicationStatus = calculateMedicationStatus(medicationItems, date)
            )
        }
        weeklyHealthScore = scores.average().roundToInt().coerceIn(0, 100)
    }

    // 오늘 날짜 데이터 로드
    LaunchedEffect(Unit) {
        // 수면 데이터 로드
        launch {
            sleepDataManager.getSleepData(LocalDate.now()).collect { record ->
                if (record != null) {
                    bedtime = record.bedtime
                    wakeupTime = record.wakeupTime
                }
            }
        }
        // 운동 데이터 로드
        launch {
            exerciseDataManager.getExerciseData(LocalDate.now()).collect { record ->
                exerciseRecord = record ?: ExerciseRecord()
            }
        }
        // 식사 데이터 로드
        launch {
            mealDataManager.getMealData(LocalDate.now()).collect { record ->
                mealRecord = record ?: MealRecord()
            }
        }
        // 발표/시연용 최근 2주 데이터 자동 생성
        launch {
            DemoDataSeeder.seedRecentTwoWeeksIfNeeded(
                context = context,
                sleepDataManager = sleepDataManager,
                exerciseDataManager = exerciseDataManager,
                diseaseDataManager = diseaseDataManager,
                medicationListManager = medicationListManager
            )
        }
    }

    val sleepDuration = Duration.between(bedtime, wakeupTime).let {
        if (it.isNegative || it.isZero) it.plusDays(1) else it
    }
    val sleepHours = sleepDuration.toHours().toString()

    if (showSleepDialog) {
        SleepTimeDialog(
            initialBedtime = bedtime,
            initialWakeupTime = wakeupTime,
            onDismiss = { showSleepDialog = false },
            onConfirm = { newBedtime, newWakeupTime ->
                scope.launch {
                    sleepDataManager.saveSleepData(LocalDate.now(), SleepRecord(newBedtime, newWakeupTime))
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
                    exerciseDataManager.saveExerciseData(LocalDate.now(), newRecord)
                }
                showExerciseDialog = false
            }
        )
    }

    val navItems = listOf(
        BottomNavItem("홈", R.drawable.home_icon, 30.dp),
        BottomNavItem("기록", R.drawable.ic_calander, 28.dp),
        BottomNavItem("통계", R.drawable.ic_find, 28.dp),
        BottomNavItem("정보", R.drawable.ic_disease, 28.dp)
    )
    val indicatorWidth = 44.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF2FFFF),
            bottomBar = {
                val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2FFFF))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val horizontalPadding = 18.dp
                                val availableWidth = maxWidth - (horizontalPadding * 2)
                                val tabWidth = availableWidth / navItems.size
                                val indicatorOffset by animateDpAsState(
                                    targetValue = horizontalPadding + (tabWidth * selectedTab) + ((tabWidth - indicatorWidth) / 2),
                                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                                    label = "bottomNavIndicatorOffset"
                                )

                                Box(
                                    modifier = Modifier
                                        .offset(x = indicatorOffset)
                                        .width(indicatorWidth)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFF20C4C9))
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = horizontalPadding, end = horizontalPadding, top = 0.dp, bottom = 4.dp),
                                ) {
                                    navItems.forEachIndexed { index, item ->
                                        BottomNavTab(
                                            item = item,
                                            selected = selectedTab == index,
                                            onClick = { selectedTab = index },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(bottomInset)
                                .background(Color.White)
                        )
                    }
                }
            }
        )
        { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        // 팀원이 작성한 슬라이드 애니메이션 로직 (그대로 유지)
                        if (targetState > initialState) {
                            slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> fullWidth / 3 }) + fadeIn(animationSpec = tween(220)) togetherWith
                                    slideOutHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> -fullWidth / 3 }) + fadeOut(animationSpec = tween(220))
                        } else {
                            slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> -fullWidth / 3 }) + fadeIn(animationSpec = tween(220)) togetherWith
                                    slideOutHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> fullWidth / 3 }) + fadeOut(animationSpec = tween(220))
                        }
                    },
                    label = "screenTransition"
                ) { tab ->
                    // 💡 이 분기문을 수정하여 화면을 연결합니다!
                    when (tab) {
                        0 -> MainLayout(
                            sleepHours = sleepHours,
                            onSleepClick = { showSleepDialog = true },
                            exerciseTitle = exerciseDisplayItems[exerciseDisplayIndex].first,
                            exerciseValue = exerciseDisplayItems[exerciseDisplayIndex].second,
                            exerciseUnit = exerciseDisplayItems[exerciseDisplayIndex].third,
                            onExerciseClick = { showExerciseDialog = true },
                            mealRecord = mealRecord,
                            weeklyHealthScore = weeklyHealthScore,
                            onProfileClick = { showProfileSettings = true }
                        )
                        1 -> CalendarLayout() // 👈 DummyScreen 대신 방금 만든 달력 화면 파일 연결!
                        2 -> StatisticsLayout()
                        3 -> InfoLayout()
                    }
                }
            }
        }

        if (showProfileSettings) {
            ProfileSettingsDialog(onDismiss = { showProfileSettings = false })
        }
        if (showInitialSetup) {
            ProfileSettingsDialog(
                onDismiss = { showInitialSetup = false },
                title = "최초 설정",
                description = "사용자님이 관리할 질환을 선택해주세요.",
                noticeText = "이 앱은 의료 진단 및 치료를 위한 서비스가 아니며, 기록을 바탕으로 참고 정보를 제공합니다.",
                noticeFontSize = 8.sp,
                noticeMaxLines = 2,
                showBackButton = false,
                dismissOnOutsideClick = false
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val iconResId: Int,
    val iconSize: Dp
)

@Composable
fun BottomNavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = Color(0xFF20C4C9)
    val unselectedColor = Color(0xFFBFC5CC)

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(top = 12.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconResId),
                contentDescription = item.label,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(if (selected) selectedColor else unselectedColor),
                modifier = Modifier.size(item.iconSize)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            color = if (selected) selectedColor else unselectedColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MainLayout(
    sleepHours: String,
    onSleepClick: () -> Unit,
    exerciseTitle: String,
    exerciseValue: String,
    exerciseUnit: String,
    onExerciseClick: () -> Unit,
    mealRecord: MealRecord = MealRecord(),
    weeklyHealthScore: Int = 0,
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2FFFF))
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 12.dp // Scaffold가 하단 탭바 여백을 이미 잡아주므로 최소 여백만 둠
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "안녕하세요!",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp,
                    color = Color(0xFF3A3A3A)
                )
                Text(
                    text = "오늘의 건강을 기록해요",
                    fontSize = 17.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.3).sp,
                    color = Color(0xFF5A5A5A)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_user2),
                contentDescription = "프로필",
                modifier = Modifier
                    .size(50.dp)
                    .clickable(onClick = onProfileClick)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.64f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_main_score_background),
                    contentDescription = "주간 종합 상태 점수 배경",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 26.dp, vertical = 26.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "주간 종합 상태 점수",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = weeklyHealthScore.toString(), color = Color.White, fontSize = 74.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = " 점", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(text = "일주일간 입력된 기록을 바탕으로", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "계산된 종합 상태 점수입니다", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SleepSummaryCard(
                modifier = Modifier.weight(1f),
                value = sleepHours,
                onClick = onSleepClick
            )
            ExerciseSummaryCard(
                modifier = Modifier.weight(1f),
                title = exerciseTitle,
                value = exerciseValue,
                unit = exerciseUnit,
                onClick = onExerciseClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        MealCard(
            meals = listOf(
                MealRowData("아침", mealRecord.breakfast.ifBlank { "아직 입력하지 않음" }, Color(0xFFFFF5E8), Color(0xFFFF6B00)),
                MealRowData("점심", mealRecord.lunch.ifBlank { "아직 입력하지 않음" }, Color(0xFFEAF9F0), Color(0xFF148847)),
                MealRowData("저녁", mealRecord.dinner.ifBlank { "아직 입력하지 않음" }, Color(0xFFF4ECFF), Color(0xFF8E2DE2))
            )
        )

        Spacer(modifier = Modifier.height(0.dp))
    }
}

data class MealRowData(
    val label: String,
    val content: String,
    val badgeBackground: Color,
    val badgeTextColor: Color
)

@Composable
fun SleepSummaryCard(
    modifier: Modifier = Modifier,
    value: String = "7",
    onClick: () -> Unit = {}
) {
    Card(
        // 카드 전체 세로 크기
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 카드 내부 전체 여백
                .padding(horizontal = 6.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    // 아이콘이 차지하는 영역 크기
                    .size(64.dp)
                    .align(Alignment.CenterStart)
                    // 아이콘 영역 좌우 위치
                    .offset(x = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_moon),
                    contentDescription = "수면",
                    // 실제 아이콘 표시 크기
                    modifier = Modifier.size(55.dp),
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    // 구분선 길이
                    .height(64.dp)
                    .align(Alignment.CenterStart)
                    // 구분선 좌우 위치
                    .offset(x = 66.dp)
                    .background(Color(0xFFE7EDF3))
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    // 오른쪽 텍스트 영역 시작 위치와 폭
                    .padding(start = 78.dp, end = 18.dp, top = 2.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "수면",
                    color = Color(0xFF111111),
                    // 제목 글자 크기
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color(0xFF1776C9),
                        // 큰 숫자 크기
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 34.sp
                    )
                    Text(
                        text = "시간",
                        color = Color(0xFF1776C9),
                        // 숫자 옆 단위 글자 크기
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        // 단위 위치 미세 조정
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
            }
            Text(
                text = "›",
                color = Color(0xFFB9C5CA),
                // 오른쪽 화살표 크기
                fontSize = 24.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    // 화살표 위치 미세 조정
                    .padding(end = 0.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ExerciseSummaryCard(
    modifier: Modifier = Modifier,
    title: String = "운동",
    value: String = "30",
    unit: String = "분",
    onClick: () -> Unit = {}
) {
    Card(
        // 카드 전체 세로 크기
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 카드 내부 전체 여백
                .padding(horizontal = 6.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    // 아이콘이 차지하는 영역 크기
                    .size(56.dp)
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_walk),
                    contentDescription = title,
                    // 실제 아이콘 표시 크기
                    modifier = Modifier.size(48.dp),
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    // 구분선 길이
                    .height(64.dp)
                    .align(Alignment.CenterStart)
                    // 구분선 좌우 위치
                    .offset(x = 58.dp)
                    .background(Color(0xFFE7EDF3))
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    // 오른쪽 텍스트 영역 시작 위치와 폭
                    .padding(start = 70.dp, end = 12.dp, top = 2.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // 자연스러운 전환 애니메이션 (제목과 내용 모두 포함)
                androidx.compose.animation.AnimatedContent(
                    targetState = Triple(title, value, unit),
                    transitionSpec = {
                        (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                    },
                    label = "HomeExerciseCardAnimation"
                ) { (currentTitle, currentValue, currentUnit) ->
                    Column {
                        Text(
                            text = currentTitle,
                            color = Color(0xFF111111),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            val fontSize = when {
                                currentValue.length > 5 -> 22.sp
                                currentValue.length > 4 -> 26.sp
                                else -> 32.sp
                            }
                            Text(
                                text = currentValue,
                                color = Color(0xFF21B8BE),
                                fontSize = fontSize,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            Text(
                                text = currentUnit,
                                color = Color(0xFF21B8BE),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Text(
                text = "›",
                color = Color(0xFFB9C5CA),
                // 오른쪽 화살표 크기
                fontSize = 24.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    // 화살표 위치 미세 조정
                    .padding(end = 0.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun MealCard(
    modifier: Modifier = Modifier,
    meals: List<MealRowData>
) {
    Card(
        // 식사 카드 전체 가로 폭
        // 카드 자체 높이를 직접 줄이고 싶으면 .height(...) 또는 .heightIn(...)를 여기 modifier에 추가
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            // 식사 카드 내부 전체 여백
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_food),
                    contentDescription = "식사",
                    // 왼쪽 식사 아이콘 크기
                    modifier = Modifier.size(30.dp)
                )
                // 아이콘과 제목 사이 간격
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "오늘의 식사",
                    color = Color(0xFF12223C),
                    // 카드 제목 글자 크기
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "›",
                    color = Color(0xFFB9C5CA),
                    // 오른쪽 화살표 크기
                    fontSize = 42.sp,
                    lineHeight = 36.sp
                )
            }

            // 제목 영역과 첫 식사 줄 사이 간격
            Spacer(modifier = Modifier.height(10.dp))

            meals.forEachIndexed { index, meal ->
                MealRow(meal = meal)
                if (index != meals.lastIndex) {
                    // 식사 줄 사이 간격
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // 줄 구분선 두께
                            .height(1.dp)
                            .background(Color(0xFFE6EEF4))
                    )
                    // 구분선 아래 간격
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun MealRow(meal: MealRowData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                // 아침/점심/저녁 배지 가로 크기
                .width(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(meal.badgeBackground)
                // 배지 내부 여백
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = meal.label,
                color = meal.badgeTextColor,
                // 배지 텍스트 크기
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // 배지와 식사 내용 사이 간격
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = meal.content,
            color = Color(0xFF1D2D45),
            // 식사 내용 글자 크기
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RecordCard() {
    Card(
        // 오늘의 기록 카드 전체 가로 폭
        // 카드 자체 높이를 직접 줄이고 싶으면 .height(...) 또는 .heightIn(...)를 여기 modifier에 추가
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 카드 내부 전체 여백
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = "오늘의 기록",
                // 왼쪽 하트 아이콘 크기
                modifier = Modifier.size(55.dp)
            )
            // 아이콘과 구분선 사이 간격
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    // 세로 구분선 두께/길이
                    .width(1.dp)
                    .height(56.dp)
                    .background(Color(0xFFE7EDF3))
            )
            // 구분선과 텍스트 사이 간격
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "오늘의 기록",
                    color = Color(0xFF111111),
                    // 카드 제목 글자 크기
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "오늘의 컨디션을 기록해요",
                    color = Color(0xFF7B8086),
                    // 카드 설명 글자 크기
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "›",
                color = Color(0xFFB9C5CA),
                // 오른쪽 화살표 크기
                fontSize = 42.sp,
                lineHeight = 36.sp
            )
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, fontSize = 20.sp, color = Color.Gray)
    }
}
