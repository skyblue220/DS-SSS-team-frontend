package com.sss.healthcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sss.healthcare.ui.theme.HealthCareTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthCareTheme {
                HealthCareAppScreen()
            }
        }
    }
}

@Composable
fun HealthCareAppScreen() {
    // 0: 홈, 1: 달력, 2: 통계, 3: 프로필
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // 1. 홈 탭
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home), // 💡 ic_home으로 변경 완료!
                            contentDescription = "홈",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("홈") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E73E8),
                        selectedTextColor = Color(0xFF1E73E8),
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )

                // 2. 캘린더 탭
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calander),
                            contentDescription = "달력",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("달력") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E73E8),
                        selectedTextColor = Color(0xFF1E73E8),
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )

                // 3. 통계 탭
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_find),
                            contentDescription = "통계",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("통계") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E73E8),
                        selectedTextColor = Color(0xFF1E73E8),
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )

                // 4. 프로필 탭
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "프로필",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("프로필") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E73E8),
                        selectedTextColor = Color(0xFF1E73E8),
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> MainLayout()
                1 -> DummyScreen(title = "달력 화면 준비 중")
                2 -> DummyScreen(title = "통계 화면 준비 중")
                3 -> DummyScreen(title = "프로필 화면 준비 중")
            }
        }
    }
}



data class HealthData(
    val walkCount: Int,
    val sleepHours: Int,
    val sleepMinutes: Int,
    val burnedCalories: Int
)

@Composable
fun MainLayout() {
    val scrollState = rememberScrollState()

    // 2. [예약석] 삼성 헬스 등에서 받아온 데이터가 저장될 '상태(State)' 변수
    // 지금은 임시로 값을 넣어두었지만, 나중에 API 연동 시 이 값만 바꾸면 UI가 자동 갱신됩니다.
    var todayHealthData by remember {
        mutableStateOf(
            HealthData(
                walkCount = 6432,       // 💡 나중에 삼성 헬스 실시간 걸음 수 연결
                sleepHours = 7,         // 💡 나중에 삼성 헬스 수면 시간 연결
                sleepMinutes = 12,
                burnedCalories = 345    // 💡 나중에 소모 칼로리 연결
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // 타이틀 및 점수 카드는 기존과 동일 (생략 가능)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "안녕하세요!", fontSize = 16.sp, color = Color.Gray)
                Text(text = "오늘의 건강을 기록해요", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = "👤", fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 주간 점수 카드
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E73E8))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "주간 종합 상태 점수", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "83", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                    Text(text = " 점", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
                }
                Text(text = "일주일간 입력된 기록을 바탕으로 계산된 점수입니다.", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "오늘의 활동 기록",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 3. 하드코딩된 글자 대신 'todayHealthData' 변수 값을 꽂아넣음
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActivityCard(
                modifier = Modifier.weight(1f),
                title = "걸음 수",
                value = String.format("%,d 걸음", todayHealthData.walkCount), // 숫자에 콤마(,) 포맷팅
                target = "목표 10,000 걸음",
                iconResId = R.drawable.ic_walk,
                iconColor = Color(0xFF4CAF50)
            )
            ActivityCard(
                modifier = Modifier.weight(1f),
                title = "수면 시간",
                value = "${todayHealthData.sleepHours}시간 ${todayHealthData.sleepMinutes}분",
                target = "적정 수면 달성",
                iconResId = R.drawable.ic_moon,
                iconColor = Color(0xFF673AB7)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActivityCard(
                modifier = Modifier.weight(1f),
                title = "소모 칼로리",
                value = "${todayHealthData.burnedCalories} kcal",
                target = "목표 500 kcal",
                iconResId = R.drawable.ic_find,
                iconColor = Color(0xFFFF5722)
            )
            Box(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
// 5. 재사용 가능한 활동 기록 카드 컴포저블
@Composable
fun ActivityCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    target: String,
    iconResId: Int,
    iconColor: Color
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = target, fontSize = 12.sp, color = Color.LightGray)
            }
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