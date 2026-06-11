package com.sss.healthcare

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

// DataStore for Medication list
private val Context.medicationListStore by preferencesDataStore(name = "medication_list_records")

class MedicationListManager(private val context: Context) {
    fun getMedicationList(date: LocalDate): Flow<List<MedicationItem>> {
        val dateStr = date.toString()
        return context.medicationListStore.data.map { preferences ->
            val rawData = preferences[stringPreferencesKey("meds_$dateStr")] ?: ""
            if (rawData.isEmpty()) emptyList()
            else {
                rawData.split("^").mapNotNull { token ->
                    val parts = token.split("|")
                    if (parts.size >= 6) {
                        MedicationItem(
                            name = parts[0],
                            time = parts[1],
                            period = parts[2],
                            useNotification = parts[3].toBoolean(),
                            notificationTime = parts[4],
                            isTaken = parts[5].toBoolean()
                        )
                    } else null
                }
            }
        }
    }

    suspend fun saveMedicationList(date: LocalDate, items: List<MedicationItem>) {
        val dateStr = date.toString()
        val serialized = items.joinToString("^") {
            "${it.name}|${it.time}|${it.period}|${it.useNotification}|${it.notificationTime}|${it.isTaken}"
        }
        context.medicationListStore.edit { preferences ->
            preferences[stringPreferencesKey("meds_$dateStr")] = serialized
        }
    }
}

data class MedicationItem(
    val name: String,
    val time: String,
    val period: String,
    val useNotification: Boolean,
    val notificationTime: String,
    val isTaken: Boolean = false
)

enum class MedicationDialogScreen {
    MAIN, ADD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationRecordDialog(
    date: LocalDate,
    dataManager: MedicationListManager,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(MedicationDialogScreen.MAIN) }
    var medicationList by remember { mutableStateOf(listOf<MedicationItem>()) }

    // Form inputs for Screen B (Add Medication)
    var newMedName by remember { mutableStateOf("") }
    var newMedTime by remember { mutableStateOf("08:00") }
    var newMedPeriod by remember { mutableStateOf("매일") }
    var useNotification by remember { mutableStateOf(true) }
    var notificationTime by remember { mutableStateOf("정각에 알림") }

    LaunchedEffect(date) {
        dataManager.getMedicationList(date).collect { list ->
            medicationList = list
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
            if (currentScreen == MedicationDialogScreen.MAIN) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "오늘 복약",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${date.monthValue}월 ${date.dayOfMonth}일 복용 목록",
                        fontSize = 13.sp,
                        color = Color(0xFF7B8086)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (medicationList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("등록된 복약 기록이 없습니다.", color = Color(0xFFB9C5CA), fontSize = 14.sp)
                            }
                        } else {
                            medicationList.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF5F7F9), RoundedCornerShape(12.dp))
                                        .clickable {
                                            val updated = medicationList.toMutableList().apply {
                                                this[index] = item.copy(isTaken = !item.isTaken)
                                            }
                                            medicationList = updated
                                            coroutineScope.launch {
                                                dataManager.saveMedicationList(date, updated)
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (item.isTaken) "☑" else "☐",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isTaken) Color(0xFF20C4C9) else Color(0xFFB9C5CA)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF111111),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = item.time,
                                        fontSize = 14.sp,
                                        color = Color(0xFF7B8086),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF20C4C9), RoundedCornerShape(12.dp))
                            .clickable { currentScreen = MedicationDialogScreen.ADD }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF20C4C9), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "약 추가",
                            color = Color(0xFF20C4C9),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20C4C9)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("확인", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "약 추가",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("약 이름", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newMedName,
                        onValueChange = { newMedName = it },
                        placeholder = { Text("예: 메토트렉세이트, 영양제", color = Color(0xFFB9C5CA), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF20C4C9),
                            unfocusedBorderColor = Color(0xFFE7EDF3)
                        )
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Text("복용 시간", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newMedTime,
                        onValueChange = { newMedTime = it },
                        placeholder = { Text("예: 08:00", color = Color(0xFFB9C5CA), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF20C4C9),
                            unfocusedBorderColor = Color(0xFFE7EDF3)
                        )
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Text("복용 주기", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(4.dp))
                    val periods = listOf("매일", "특정 요일", "N일마다")
                    periods.forEach { periodOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newMedPeriod = periodOption }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (newMedPeriod == periodOption),
                                onClick = { newMedPeriod = periodOption },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF20C4C9))
                            )
                            Text(text = periodOption, fontSize = 14.sp, color = Color(0xFF333333))
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))

                    Text("알림 설정", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { useNotification = !useNotification }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useNotification,
                            onCheckedChange = { useNotification = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF20C4C9))
                        )
                        Text(text = "복용 알림 사용", fontSize = 14.sp, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (useNotification) {
                        Text("알림 시간", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                        Spacer(modifier = Modifier.height(4.dp))
                        val notiOptions = listOf("정각에 알림", "10분 전", "30분 전")
                        notiOptions.forEach { notiOption ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { notificationTime = notiOption }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (notificationTime == notiOption),
                                    onClick = { notificationTime = notiOption },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF20C4C9))
                                )
                                Text(text = notiOption, fontSize = 14.sp, color = Color(0xFF333333))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { currentScreen = MedicationDialogScreen.MAIN },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7EDF3)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("취소", color = Color(0xFF7B8086), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (newMedName.isNotBlank()) {
                                    val newItem = MedicationItem(
                                        name = newMedName,
                                        time = newMedTime,
                                        period = newMedPeriod,
                                        useNotification = useNotification,
                                        notificationTime = if (useNotification) notificationTime else "없음",
                                        isTaken = false
                                    )
                                    val updatedList = medicationList + newItem
                                    medicationList = updatedList
                                    coroutineScope.launch {
                                        dataManager.saveMedicationList(date, updatedList)
                                    }
                                    newMedName = ""
                                    newMedTime = "08:00"
                                    currentScreen = MedicationDialogScreen.MAIN
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
}