package com.sss.healthcare

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

// DataStore for Disease Records
private val Context.diseaseDataStore by preferencesDataStore(name = "disease_records")

class DiseaseDataManager(private val context: Context) {
    fun getDiseaseData(date: LocalDate): Flow<DiseaseRecord?> {
        val dateStr = date.toString()
        return context.diseaseDataStore.data.map { preferences ->
            val score = preferences[floatPreferencesKey("score_$dateStr")]
            val status = preferences[stringPreferencesKey("status_$dateStr")] ?: ""
            val memo = preferences[stringPreferencesKey("memo_$dateStr")] ?: ""

            if (score != null) {
                DiseaseRecord(conditionScore = score, diseaseStatus = status, memo = memo)
            } else null
        }
    }

    suspend fun saveDiseaseData(date: LocalDate, record: DiseaseRecord) {
        val dateStr = date.toString()
        context.diseaseDataStore.edit { preferences ->
            preferences[floatPreferencesKey("score_$dateStr")] = record.conditionScore
            preferences[stringPreferencesKey("status_$dateStr")] = record.diseaseStatus
            preferences[stringPreferencesKey("memo_$dateStr")] = record.memo
        }
    }
}

data class DiseaseRecord(
    val conditionScore: Float = 5f,
    val diseaseStatus: String = "",
    val memo: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseRecordDialog(
    date: LocalDate,
    dataManager: DiseaseDataManager,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var conditionScore by remember { mutableStateOf(5f) }
    var diseaseStatus by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    LaunchedEffect(date) {
        dataManager.getDiseaseData(date).collect { record ->
            if (record != null) {
                conditionScore = record.conditionScore
                diseaseStatus = record.diseaseStatus
                memo = record.memo
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 타이틀
                Text(
                    text = "${date.monthValue}월 ${date.dayOfMonth}일 질환 기록",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // a. 오늘의 상태/컨디션 슬라이더 (0~10점)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "오늘의 상태 / 컨디션",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF555555)
                        )
                        Text(
                            text = "${conditionScore.roundToInt()}점",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF20C4C9)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = conditionScore,
                        onValueChange = { conditionScore = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF20C4C9),
                            activeTrackColor = Color(0xFF20C4C9),
                            inactiveTrackColor = Color(0xFFE7EDF3)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("안좋음 (0)", fontSize = 11.sp, color = Color(0xFF999999))
                        Text("보통 (5)", fontSize = 11.sp, color = Color(0xFF999999))
                        Text("좋음 (10)", fontSize = 11.sp, color = Color(0xFF999999))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // b. 질환별 상태 (메모)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "질환별 상태",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = diseaseStatus,
                        onValueChange = { diseaseStatus = it },
                        placeholder = { Text("오늘 느낀 특별한 증상이나 통증을 기록하세요", color = Color(0xFFB9C5CA), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF20C4C9),
                            unfocusedBorderColor = Color(0xFFE7EDF3)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                // c. 메모
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "종합 메모",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF555555)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        placeholder = { Text("기타 특이사항이나 식단, 일상 기록을 남겨보세요", color = Color(0xFFB9C5CA), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF20C4C9),
                            unfocusedBorderColor = Color(0xFFE7EDF3)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                // 하단 버튼 구성
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7EDF3)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("취소", color = Color(0xFF7B8086), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                dataManager.saveDiseaseData(
                                    date = date,
                                    record = DiseaseRecord(
                                        conditionScore = conditionScore,
                                        diseaseStatus = diseaseStatus,
                                        memo = memo
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20C4C9)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("저장", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}