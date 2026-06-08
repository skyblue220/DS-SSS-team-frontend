package com.sss.healthcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var selectedTab by remember { mutableIntStateOf(0) }
    val mainBrandColor = Color(0xFF00A896) // 피그마 메인 민트색

    Scaffold(
        bottomBar = {
            // 피그마와 동일한 형태의 커스텀 하단바 구성
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple("홈", R.drawable.home_icon, 0),
                        Triple("기록", R.drawable.record_icon, 1),
                        Triple("통계", R.drawable.summary_icon, 2),
                        Triple("정보", R.drawable.disease_icon, 3)
                    )

                    tabs.forEach { (label, icon, index) ->
                        val isSelected = selectedTab == index
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedTab = index },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 💡 피그마 스타일: 선택되었을 때만 상단에 나타나는 민트색 라인 인디케이터
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .background(
                                        if (isSelected) mainBrandColor else Color.Transparent,
                                        shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = label,
                                tint = if (isSelected) mainBrandColor else Color(0xFF9E9E9E),
                                modifier = Modifier.size(26.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) mainBrandColor else Color(0xFF9E9E9E)
                            )

                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> MainLayout(mainBrandColor)
                else -> DummyScreen("준비 중인 화면입니다")
            }
        }
    }
}

@Composable
fun MainLayout(brandColor: Color) {
    val scrollState = rememberScrollState()

    // 피그마와 일치하는 우상단 그라데이션 컬러 매핑
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF00C2A0), Color(0xFF1E73E8))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF7F6)) // 피그마 특유의 은은한 민트빛 회색 배경
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // 1. 헤더 섹션
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "안녕하세요!", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF263238))
                Text(text = "오늘의 건강을 기록해요", fontSize = 13.sp, color = Color(0xFF78909C))
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.disease_icon),
                        contentDescription = "프로필",
                        tint = brandColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 주간 종합 점수 카드 (그라데이션 및 방패 백그라운드 반영)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(gradientBrush)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("주간 종합 상태 점수", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(4.dp))
                            // 외부 에러 유발 아이콘 대신 내부 리소스 안전하게 재사용
                            Icon(
                                painter = painterResource(id = R.drawable.summary_icon),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("83", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                            Text(" 점", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(bottom = 10.dp), fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "일주일간 입력된 기록을 바탕으로\n계산된 종합 상태 점수입니다",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    // 우측 방패 심볼 레이아웃 매칭
                    Icon(
                        painter = painterResource(id = R.drawable.disease_icon),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(90.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 수면 / 운동 수평 대칭 카드 (기존 Int 대신 Color 객체로 바로 전달)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 💡 Color(0xFF...) 형태로 직접 변경
            ActivityHorizontalCard(Modifier.weight(1f), "수면", "7 시간", R.drawable.moon_icon, Color(0xFF1E3A8A))
            ActivityHorizontalCard(Modifier.weight(1f), "운동", "30 분", R.drawable.walk_icon, Color(0xFF00A896))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 오늘의 식사 섹션
        TodayMealSection()

        Spacer(modifier = Modifier.height(16.dp))

        // 5. 오늘의 기록 섹션
        TodayRecordSection(brandColor)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ActivityHorizontalCard(modifier: Modifier, title: String, value: String, iconId: Int, themeColor: Color) { // 💡 Int -> Color로 변경
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = themeColor.copy(alpha = 0.08f), // 💡 Color(themeColor) 지우고 바로 복사본 사용
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconId),
                        contentDescription = null,
                        tint = themeColor, // 💡 바로 themeColor 적용
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 11.sp, color = Color(0xFF78909C), fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = value.split(" ")[0],
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                    Text(
                        text = " " + value.split(" ")[1],
                        fontSize = 13.sp,
                        color = Color(0xFF00A896),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun TodayMealSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFE0F2F1),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🍴", fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("오늘의 식사", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
                }
                Icon(
                    painter = painterResource(id = R.drawable.summary_icon),
                    contentDescription = null,
                    tint = Color(0xFFCFD8DC),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            MealItemRow("아침", "바나나, 우유", Color(0xFFFFE0B2), Color(0xFFE65100))
            Spacer(modifier = Modifier.height(10.dp))
            MealItemRow("점심", "김치볶음밥", Color(0xFFC8E6C9), Color(0xFF1B5E20))
            Spacer(modifier = Modifier.height(10.dp))
            MealItemRow("저녁", "아직 입력하지 않음", Color(0xFFF3E5F5), Color(0xFF4A148C))
        }
    }
}

@Composable
fun MealItemRow(tag: String, content: String, bgColor: Color, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(22.dp)
                .background(bgColor, shape = RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(tag, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = content,
            fontSize = 13.sp,
            color = if (content.contains("아직")) Color(0xFFB0BEC5) else Color(0xFF37474F),
            fontWeight = if (content.contains("아직")) FontWeight.Normal else FontWeight.Medium
        )
    }
}

@Composable
fun TodayRecordSection(brandColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = brandColor.copy(alpha = 0.08f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("❤️", fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("오늘의 기록", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
                Text("오늘의 컨디션을 기록해요", fontSize = 12.sp, color = Color(0xFF78909C))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.summary_icon),
                contentDescription = null,
                tint = Color(0xFFCFD8DC),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, color = Color.Gray, fontSize = 16.sp)
    }
}