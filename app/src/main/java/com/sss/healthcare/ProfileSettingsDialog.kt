package com.sss.healthcare

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler

@Composable
fun ProfileSettingsDialog(
    onDismiss: () -> Unit,
    title: String = "개인 설정",
    description: String? = null,
    noticeText: String = "입력한 정보는 맞춤형 기록 항목을 구성하는 데 사용되며 의료 진단이나 치료 판단을 의미하지 않습니다",
    noticeFontSize: androidx.compose.ui.unit.TextUnit = 7.sp,
    noticeMaxLines: Int = 1,
    showBackButton: Boolean = true,
    dismissOnOutsideClick: Boolean = true
) {
    BackHandler(enabled = !dismissOnOutsideClick) {
        // 최초 설정 팝업은 저장하기 전까지 닫히지 않도록 뒤로가기를 막습니다.
    }

    val context = LocalContext.current
    val savedSettings = remember { ProfileSettingsStore.load(context) }
    var selectedDisease by remember { mutableStateOf(savedSettings.selectedDisease) }
    var medicationReminderEnabled by remember { mutableStateOf(savedSettings.medicationReminderEnabled) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f))
            .clickable {
                if (dismissOnOutsideClick) onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showBackButton) {
                        Text(
                            text = "‹",
                            color = Color(0xFFB6C2C8),
                            fontSize = 34.sp,
                            lineHeight = 28.sp,
                            modifier = Modifier.clickable(onClick = onDismiss)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Column {
                        Text(
                            text = title,
                            color = Color(0xFF3A3A3A),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (description != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                color = Color(0xFF626B70),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                ManagedDiseaseCard(
                    selectedDisease = selectedDisease,
                    onDiseaseClick = { disease -> selectedDisease = disease }
                )

                Spacer(modifier = Modifier.height(14.dp))

                MedicationNoticeCard(
                    checked = medicationReminderEnabled,
                    onCheckedChange = { medicationReminderEnabled = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF0F1F1))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = noticeText,
                        color = Color(0xFF9AA2A6),
                        fontSize = noticeFontSize,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = noticeMaxLines,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF20C4C0))
                            .clickable {
                                ProfileSettingsStore.save(
                                    context = context,
                                    settings = ProfileSettings(
                                        selectedDisease = selectedDisease,
                                        medicationReminderEnabled = medicationReminderEnabled
                                    )
                                )
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "저장하기",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManagedDiseaseCard(
    selectedDisease: String,
    onDiseaseClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = "관리 질환",
                color = Color(0xFF111111),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "기록할 만성질환을 선택해주세요",
                color = Color(0xFF7D868C),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                DiseaseChip(
                    text = "고혈압",
                    selected = selectedDisease == "고혈압",
                    onClick = { onDiseaseClick("고혈압") }
                )
                Spacer(modifier = Modifier.width(10.dp))
                DiseaseChip(
                    text = "당뇨",
                    selected = selectedDisease == "당뇨",
                    onClick = { onDiseaseClick("당뇨") }
                )
                Spacer(modifier = Modifier.width(10.dp))
                DiseaseChip(
                    text = "류마티스 관절염",
                    selected = selectedDisease == "류마티스 관절염",
                    onClick = { onDiseaseClick("류마티스 관절염") }
                )
            }
        }
    }
}

@Composable
private fun DiseaseChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val chipShape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(chipShape)
            .background(if (selected) Color(0xFF20C4C0) else Color.White)
            .border(width = 1.dp, color = Color(0xFFE0E3E5), shape = chipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF7B858B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MedicationNoticeCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = "복약 알림",
                color = Color(0xFF111111),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "복약 알림 여부를 선택해주세요",
                color = Color(0xFF7D868C),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(width = 1.dp, color = Color(0xFFE0E3E5), shape = RoundedCornerShape(8.dp))
                    .clickable { onCheckedChange(!checked) }
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "복약 알림 사용",
                    color = Color(0xFF222222),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF20C4C0),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFDDE3E6)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "복약 시간은 기록 화면에서 설정할 수 있어요",
                color = Color(0xFF7D868C),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
