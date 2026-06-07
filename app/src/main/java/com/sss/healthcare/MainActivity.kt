package com.sss.healthcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedContent
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
    // 0: 홈, 1: 기록, 2: 통계, 3: 정보
    var selectedTab by remember { mutableIntStateOf(0) }
    val navItems = listOf(
        BottomNavItem("홈", R.drawable.home_icon, 30.dp),
        BottomNavItem("기록", R.drawable.ic_calander, 28.dp),
        BottomNavItem("통계", R.drawable.ic_find, 28.dp),
        BottomNavItem("정보", R.drawable.ic_disease, 28.dp)
    )
    val indicatorWidth = 44.dp

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
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth / 3 }
                        ) + fadeIn(animationSpec = tween(220)) togetherWith slideOutHorizontally(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeOut(animationSpec = tween(220))
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeIn(animationSpec = tween(220)) togetherWith slideOutHorizontally(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth / 3 }
                        ) + fadeOut(animationSpec = tween(220))
                    }
                },
                label = "screenTransition"
            ) { tab ->
                when (tab) {
                    0 -> MainLayout()
                    1 -> DummyScreen(title = "기록 화면 준비 중")
                    2 -> DummyScreen(title = "통계 화면 준비 중")
                    3 -> DummyScreen(title = "정보 화면 준비 중")
                }
            }
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
fun MainLayout() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2FFFF))
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // 타이틀 및 점수 카드는 기존과 동일 (생략 가능)
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
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 주간 점수 카드
        Card(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.62f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_main_score_background),
                    contentDescription = "주간 종합 상태 점수 배경",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
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
        }

        Spacer(modifier = Modifier.height(28.dp))
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
