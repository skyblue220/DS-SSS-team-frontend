package com.sss.healthcare

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

private val Context.diseaseDataStore by preferencesDataStore(name = "disease_records")

class DiseaseDataManager(private val context: Context) {
    fun getDiseaseData(date: LocalDate): Flow<DiseaseRecord?> {
        val dateStr = date.toString()
        return context.diseaseDataStore.data.map { preferences ->
            val score = preferences[floatPreferencesKey("score_$dateStr")]
            val status = preferences[stringPreferencesKey("status_$dateStr")] ?: ""
            val memo = preferences[stringPreferencesKey("memo_$dateStr")] ?: ""

            if (score != null) {
                DiseaseRecord(
                    conditionScore = score,
                    diseaseStatus = status,
                    memo = memo,
                    selectedDisease = preferences[stringPreferencesKey("disease_$dateStr")] ?: "",
                    systolicBloodPressure = preferences[stringPreferencesKey("systolic_$dateStr")] ?: "",
                    diastolicBloodPressure = preferences[stringPreferencesKey("diastolic_$dateStr")] ?: "",
                    pulse = preferences[stringPreferencesKey("pulse_$dateStr")] ?: "",
                    bloodSugar = preferences[stringPreferencesKey("bloodSugar_$dateStr")] ?: "",
                    glucoseTiming = preferences[stringPreferencesKey("glucoseTiming_$dateStr")] ?: "식전",
                    jointPainScore = preferences[floatPreferencesKey("jointPainScore_$dateStr")] ?: 5f,
                    painAreas = preferences[stringPreferencesKey("painAreas_$dateStr")].toSetValue(),
                    morningStiffness = preferences[stringPreferencesKey("morningStiffness_$dateStr")] ?: "없음",
                    jointSwelling = preferences[stringPreferencesKey("jointSwelling_$dateStr")] ?: "없음",
                    fatigueScore = preferences[floatPreferencesKey("fatigueScore_$dateStr")] ?: 5f,
                    symptoms = preferences[stringPreferencesKey("symptoms_$dateStr")].toSetValue()
                )
            } else {
                null
            }
        }
    }

    suspend fun saveDiseaseData(date: LocalDate, record: DiseaseRecord) {
        val dateStr = date.toString()
        context.diseaseDataStore.edit { preferences ->
            preferences[floatPreferencesKey("score_$dateStr")] = record.conditionScore
            preferences[stringPreferencesKey("status_$dateStr")] = record.diseaseStatus
            preferences[stringPreferencesKey("memo_$dateStr")] = record.memo
            preferences[stringPreferencesKey("disease_$dateStr")] = record.selectedDisease
            preferences[stringPreferencesKey("systolic_$dateStr")] = record.systolicBloodPressure
            preferences[stringPreferencesKey("diastolic_$dateStr")] = record.diastolicBloodPressure
            preferences[stringPreferencesKey("pulse_$dateStr")] = record.pulse
            preferences[stringPreferencesKey("bloodSugar_$dateStr")] = record.bloodSugar
            preferences[stringPreferencesKey("glucoseTiming_$dateStr")] = record.glucoseTiming
            preferences[floatPreferencesKey("jointPainScore_$dateStr")] = record.jointPainScore
            preferences[stringPreferencesKey("painAreas_$dateStr")] = record.painAreas.joinToString("|")
            preferences[stringPreferencesKey("morningStiffness_$dateStr")] = record.morningStiffness
            preferences[stringPreferencesKey("jointSwelling_$dateStr")] = record.jointSwelling
            preferences[floatPreferencesKey("fatigueScore_$dateStr")] = record.fatigueScore
            preferences[stringPreferencesKey("symptoms_$dateStr")] = record.symptoms.joinToString("|")
        }
    }
}

data class DiseaseRecord(
    val conditionScore: Float = 5f,
    val diseaseStatus: String = "",
    val memo: String = "",
    val selectedDisease: String = "",
    val systolicBloodPressure: String = "",
    val diastolicBloodPressure: String = "",
    val pulse: String = "",
    val bloodSugar: String = "",
    val glucoseTiming: String = "식전",
    val jointPainScore: Float = 5f,
    val painAreas: Set<String> = emptySet(),
    val morningStiffness: String = "없음",
    val jointSwelling: String = "없음",
    val fatigueScore: Float = 5f,
    val symptoms: Set<String> = emptySet()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseRecordDialog(
    date: LocalDate,
    dataManager: DiseaseDataManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val selectedDisease = remember { ProfileSettingsStore.load(context).selectedDisease }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var conditionScore by remember { mutableFloatStateOf(5f) }
    var diseaseStatus by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf(emptySet<String>()) }
    var bloodSugar by remember { mutableStateOf("") }
    var glucoseTiming by remember { mutableStateOf("식전") }
    var jointPainScore by remember { mutableFloatStateOf(5f) }
    var painAreas by remember { mutableStateOf(emptySet<String>()) }
    var morningStiffness by remember { mutableStateOf("없음") }
    var jointSwelling by remember { mutableStateOf("없음") }
    var fatigueScore by remember { mutableFloatStateOf(5f) }

    LaunchedEffect(date, selectedDisease) {
        dataManager.getDiseaseData(date).collect { record ->
            if (record != null) {
                conditionScore = record.conditionScore
                diseaseStatus = record.diseaseStatus
                memo = record.memo
                systolic = record.systolicBloodPressure
                diastolic = record.diastolicBloodPressure
                pulse = record.pulse
                symptoms = record.symptoms
                bloodSugar = record.bloodSugar
                glucoseTiming = record.glucoseTiming
                jointPainScore = record.jointPainScore
                painAreas = record.painAreas
                morningStiffness = record.morningStiffness
                jointSwelling = record.jointSwelling
                fatigueScore = record.fatigueScore
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 14.dp)
                .widthIn(max = 420.dp)
                .heightIn(max = 720.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 22.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${date.monthValue}월 ${date.dayOfMonth}일 질환 기록",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedDisease,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColor,
                    modifier = Modifier
                        .background(Color(0xFFE9FAFA), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
                Spacer(modifier = Modifier.height(22.dp))

                RecordSection(title = "공통 기록") {
                    ScoreSlider(
                        title = "오늘 상태 / 컨디션 점수",
                        value = conditionScore,
                        onValueChange = { conditionScore = it }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                RecordSection(title = "$selectedDisease 세부 기록") {
                    when (selectedDisease) {
                        "당뇨" -> DiabetesFields(
                            bloodSugar = bloodSugar,
                            onBloodSugarChange = { bloodSugar = it },
                            glucoseTiming = glucoseTiming,
                            onGlucoseTimingChange = { glucoseTiming = it },
                            symptoms = symptoms,
                            onSymptomsChange = { symptoms = it }
                        )
                        "류마티스 관절염" -> RheumatoidFields(
                            jointPainScore = jointPainScore,
                            onJointPainScoreChange = { jointPainScore = it },
                            painAreas = painAreas,
                            onPainAreasChange = { painAreas = it },
                            morningStiffness = morningStiffness,
                            onMorningStiffnessChange = { morningStiffness = it },
                            jointSwelling = jointSwelling,
                            onJointSwellingChange = { jointSwelling = it },
                            fatigueScore = fatigueScore,
                            onFatigueScoreChange = { fatigueScore = it }
                        )
                        else -> HypertensionFields(
                            systolic = systolic,
                            onSystolicChange = { systolic = it },
                            diastolic = diastolic,
                            onDiastolicChange = { diastolic = it },
                            pulse = pulse,
                            onPulseChange = { pulse = it },
                            symptoms = symptoms,
                            onSymptomsChange = { symptoms = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                RecordSection(title = "종합 메모") {
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        placeholder = {
                            Text("기타 특이사항이나 일상 기록을 남겨보세요", color = MutedText, fontSize = 13.sp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = appTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7EDF3)),
                        shape = RoundedCornerShape(15.dp)
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
                                        memo = memo,
                                        selectedDisease = selectedDisease,
                                        systolicBloodPressure = systolic,
                                        diastolicBloodPressure = diastolic,
                                        pulse = pulse,
                                        bloodSugar = bloodSugar,
                                        glucoseTiming = glucoseTiming,
                                        jointPainScore = jointPainScore,
                                        painAreas = painAreas,
                                        morningStiffness = morningStiffness,
                                        jointSwelling = jointSwelling,
                                        fatigueScore = fatigueScore,
                                        symptoms = symptoms
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("저장", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HypertensionFields(
    systolic: String,
    onSystolicChange: (String) -> Unit,
    diastolic: String,
    onDiastolicChange: (String) -> Unit,
    pulse: String,
    onPulseChange: (String) -> Unit,
    symptoms: Set<String>,
    onSymptomsChange: (Set<String>) -> Unit
) {
    NumberInputField("수축기 혈압", systolic, "예: 120", "mmHg", onSystolicChange)
    Spacer(modifier = Modifier.height(10.dp))
    NumberInputField("이완기 혈압", diastolic, "예: 80", "mmHg", onDiastolicChange)
    Spacer(modifier = Modifier.height(10.dp))
    NumberInputField("맥박", pulse, "예: 72", "bpm", onPulseChange)
    Spacer(modifier = Modifier.height(16.dp))
    SelectableChipGroup(
        title = "증상 체크",
        options = listOf("두통", "어지러움", "가슴 답답함", "숨참"),
        selected = symptoms,
        onSelectedChange = onSymptomsChange,
        multiSelect = true
    )
}

@Composable
private fun DiabetesFields(
    bloodSugar: String,
    onBloodSugarChange: (String) -> Unit,
    glucoseTiming: String,
    onGlucoseTimingChange: (String) -> Unit,
    symptoms: Set<String>,
    onSymptomsChange: (Set<String>) -> Unit
) {
    NumberInputField("혈당 수치", bloodSugar, "예: 110", "mg/dL", onBloodSugarChange)
    Spacer(modifier = Modifier.height(16.dp))
    SelectableChipGroup(
        title = "측정 시점",
        options = listOf("식전", "식후", "취침 전"),
        selected = setOf(glucoseTiming),
        onSelectedChange = { onGlucoseTimingChange(it.firstOrNull() ?: "식전") },
        multiSelect = false
    )
    Spacer(modifier = Modifier.height(16.dp))
    SelectableChipGroup(
        title = "증상 체크",
        options = listOf("손떨림", "식은땀", "어지러움", "심한 갈증", "소변 증가", "피로감"),
        selected = symptoms,
        onSelectedChange = onSymptomsChange,
        multiSelect = true
    )
}

@Composable
private fun RheumatoidFields(
    jointPainScore: Float,
    onJointPainScoreChange: (Float) -> Unit,
    painAreas: Set<String>,
    onPainAreasChange: (Set<String>) -> Unit,
    morningStiffness: String,
    onMorningStiffnessChange: (String) -> Unit,
    jointSwelling: String,
    onJointSwellingChange: (String) -> Unit,
    fatigueScore: Float,
    onFatigueScoreChange: (Float) -> Unit
) {
    ScoreSlider("관절 통증 점수", jointPainScore, onJointPainScoreChange)
    Spacer(modifier = Modifier.height(16.dp))
    SelectableChipGroup(
        title = "통증 부위",
        options = listOf("손", "손목", "무릎", "발", "어깨", "기타"),
        selected = painAreas,
        onSelectedChange = onPainAreasChange,
        multiSelect = true
    )
    Spacer(modifier = Modifier.height(16.dp))
    SelectableChipGroup(
        title = "아침 뻣뻣함",
        options = listOf("없음", "30분 이하", "30분~1시간", "1시간 이상"),
        selected = setOf(morningStiffness),
        onSelectedChange = { onMorningStiffnessChange(it.firstOrNull() ?: "없음") },
        multiSelect = false
    )
    Spacer(modifier = Modifier.height(16.dp))
    SelectableChipGroup(
        title = "관절 붓기",
        options = listOf("있음", "없음"),
        selected = setOf(jointSwelling),
        onSelectedChange = { onJointSwellingChange(it.firstOrNull() ?: "없음") },
        multiSelect = false
    )
    Spacer(modifier = Modifier.height(16.dp))
    ScoreSlider("피로감", fatigueScore, onFatigueScoreChange)
}

@Composable
private fun RecordSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7FBFC), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = DarkText,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ScoreSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LabelText)
            Text(
                text = "${value.roundToInt()}점",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandColor
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = BrandColor,
                activeTrackColor = BrandColor,
                inactiveTrackColor = Color(0xFFE7EDF3)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", fontSize = 11.sp, color = MutedText)
            Text("5", fontSize = 11.sp, color = MutedText)
            Text("10", fontSize = 11.sp, color = MutedText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberInputField(
    label: String,
    value: String,
    placeholder: String,
    suffix: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LabelText)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.filter { it.isDigit() }.take(4)) },
            placeholder = { Text(placeholder, color = MutedText, fontSize = 13.sp) },
            suffix = { Text(suffix, color = MutedText, fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = appTextFieldColors()
        )
    }
}

@Composable
private fun SelectableChipGroup(
    title: String,
    options: List<String>,
    selected: Set<String>,
    onSelectedChange: (Set<String>) -> Unit,
    multiSelect: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LabelText)
        Spacer(modifier = Modifier.height(8.dp))
        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    SelectableChip(
                        text = option,
                        selected = option in selected,
                        modifier = Modifier.weight(1f)
                    ) {
                        val nextSelected = if (multiSelect) {
                            if (option in selected) selected - option else selected + option
                        } else {
                            setOf(option)
                        }
                        onSelectedChange(nextSelected)
                    }
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) Color(0xFFE1FAFA) else Color.White
    val border = if (selected) BrandColor else Color(0xFFE1E8ED)
    val textColor = if (selected) BrandColor else Color(0xFF4A5560)

    Box(
        modifier = modifier
            .height(42.dp)
            .background(background, RoundedCornerShape(999.dp))
            .border(1.2.dp, border, RoundedCornerShape(999.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandColor,
    unfocusedBorderColor = Color(0xFFE7EDF3),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

private fun String?.toSetValue(): Set<String> {
    return this
        ?.split("|")
        ?.filter { it.isNotBlank() }
        ?.toSet()
        ?: emptySet()
}

private val BrandColor = Color(0xFF20C4C9)
private val DarkText = Color(0xFF12223C)
private val LabelText = Color(0xFF555555)
private val MutedText = Color(0xFF9EABB8)
