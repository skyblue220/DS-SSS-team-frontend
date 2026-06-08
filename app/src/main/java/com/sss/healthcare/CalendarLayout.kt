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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarLayout() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val mainBrandColor = Color(0xFF00A896) // 피그마 브랜드 메인 컬러

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2FFFF)) // MainActivity와 배경색 일치
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 1. 연도/월 선택 헤더 (PNG 화살표 적용)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "이전 달",
                    tint = mainBrandColor,
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
                    tint = mainBrandColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 요일 헤더
        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = when(day) {
                        "일" -> Color(0xFFE53935)
                        "토" -> Color(0xFF1E73E8)
                        else -> Color(0xFF78909C)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. 7열 날짜 그리드 생성
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
                        val isSelected = day != null && currentMonth.atDay(day) == selectedDate

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) mainBrandColor else Color.Transparent)
                                .clickable(enabled = day != null) {
                                    if (day != null) selectedDate = currentMonth.atDay(day)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Text(
                                    text = day.toString(),
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

        // 4. 선택된 날짜 타이틀
        Text(
            text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일(${selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)})의 기록",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3A3A3A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. 하단 데이터 카드 배치 (MainActivity 리소스 네이밍 동기화 완료)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CalendarActivityCard(Modifier.weight(1f), "수면", "7", "시간", R.drawable.ic_moon, Color(0xFF1776C9))
            CalendarActivityCard(Modifier.weight(1f), "운동", "30", "분", R.drawable.ic_walk, Color(0xFF21B8BE))
        }

        Spacer(modifier = Modifier.height(18.dp))
        CalendarMealSection()
        Spacer(modifier = Modifier.height(18.dp))
        CalendarRecordSection()
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- 독립형 서브 컴포넌트 (MainActivity의 에셋 및 디자인 규격 반영) ---

@Composable
fun CalendarActivityCard(modifier: Modifier, title: String, value: String, unit: String, iconId: Int, themeColor: Color) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 12.dp)) {
            Box(modifier = Modifier.size(64.dp).align(Alignment.CenterStart), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = iconId), contentDescription = title, modifier = Modifier.size(55.dp))
            }
            Box(modifier = Modifier.width(1.dp).height(64.dp).align(Alignment.CenterStart).offset(x = 66.dp).background(Color(0xFFE7EDF3)))
            Column(modifier = Modifier.fillMaxHeight().padding(start = 78.dp, end = 18.dp, top = 2.dp), verticalArrangement = Arrangement.Top) {
                Text(title, color = Color(0xFF111111), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = value, color = themeColor, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 34.sp)
                    Text(text = unit, color = themeColor, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_check_right),
                contentDescription = null,
                tint = Color(0xFFB9C5CA),
                modifier = Modifier.align(Alignment.CenterEnd).size(16.dp).offset(x = (-4).dp)
            )
        }
    }
}

@Composable
fun CalendarMealSection() {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_food), contentDescription = "식사", modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("오늘의 식사", color = Color(0xFF12223C), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_right),
                    contentDescription = null,
                    tint = Color(0xFFB9C5CA),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            CalendarMealRow(MealRowData("아침", "바나나, 우유", Color(0xFFFFF5E8), Color(0xFFFF6B00)))
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE6EEF4)))
            Spacer(modifier = Modifier.height(10.dp))
            CalendarMealRow(MealRowData("점심", "김치볶음밥", Color(0xFFEAF9F0), Color(0xFF148847)))
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE6EEF4)))
            Spacer(modifier = Modifier.height(10.dp))
            CalendarMealRow(MealRowData("저녁", "아직 입력하지 않음", Color(0xFFF4ECFF), Color(0xFF8E2DE2)))
        }
    }
}

@Composable
fun CalendarMealRow(meal: MealRowData) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(60.dp).clip(RoundedCornerShape(8.dp)).background(meal.badgeBackground).padding(vertical = 3.dp), contentAlignment = Alignment.Center) {
            Text(meal.label, color = meal.badgeTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = meal.content,
            color = if (meal.label == "저녁") Color(0xFF7D868C) else Color(0xFF1D2D45),
            fontSize = 13.sp,
            fontWeight = if (meal.label == "저녁") FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun CalendarRecordSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.ic_heart), contentDescription = "오늘의 기록", modifier = Modifier.size(55.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.width(1.dp).height(56.dp).background(Color(0xFFE7EDF3)))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("오늘의 기록", color = Color(0xFF111111), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("오늘의 컨디션을 기록해요", color = Color(0xFF7B8086), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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