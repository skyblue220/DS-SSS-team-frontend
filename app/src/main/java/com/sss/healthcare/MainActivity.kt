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

@Composable
fun MainLayout() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp)
    ) {
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E73E8)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "주간 종합 상태 점수", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(text = "83", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                    Text(text = " 점", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
                }

                Text(text = "일주일간 입력된 기록을 바탕으로 계산된 점수입니다.", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
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