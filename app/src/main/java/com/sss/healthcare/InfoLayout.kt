package com.sss.healthcare

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class PolicyInfo(
    val title: String,
    val tags: List<String>,
    val description: String,
    val url: String
)

private data class OfficialSite(
    val title: String,
    val description: String,
    val url: String,
    val iconRes: Int
)

private data class DailyRecordInfo(
    val label: String,
    val iconRes: Int
)

private val policyItems = listOf(
    PolicyInfo(
        title = "국가건강검진 안내",
        tags = listOf("고혈압", "건강검진", "국가사업"),
        description = "고혈압 등 만성질환의 조기 발견을 위해 국가에서 제공하는 건강검진 제도입니다.",
        url = "https://www.nhis.or.kr/nhis/healthin/wbhaca04500m01.do"
    ),
    PolicyInfo(
        title = "고혈압·당뇨병 등록교육센터",
        tags = listOf("고혈압", "당뇨", "교육·상담"),
        description = "보건소에서 전문 교육과 상담을 통해 질환 관리 방법을 안내받을 수 있습니다.",
        url = "https://health.kdca.go.kr/healthinfo/biz/health/main/mainPage/main.do"
    ),
    PolicyInfo(
        title = "만성질환관리제 시범사업",
        tags = listOf("고혈압", "당뇨", "지원사업"),
        description = "만성질환자의 지속적인 관리를 위해 지역사회 기반의 서비스를 제공합니다.",
        url = "https://www.mohw.go.kr/react/search/search.jsp?searchTerm=%EC%9D%BC%EC%B0%A8%EC%9D%98%EB%A3%8C%20%EB%A7%8C%EC%84%B1%EC%A7%88%ED%99%98%EA%B4%80%EB%A6%AC"
    ),
    PolicyInfo(
        title = "의료비 지원 사업 안내",
        tags = listOf("의료비", "지원", "복지"),
        description = "저소득층 대상으로 의료비 부담을 줄이기 위한 다양한 지원 제도입니다.",
        url = "https://www.gov.kr/search?srhQuery=%EC%9D%98%EB%A3%8C%EB%B9%84%20%EC%A7%80%EC%9B%90"
    )
)

private val officialSites = listOf(
    OfficialSite("질병관리청", "국가건강정보포털", "https://health.kdca.go.kr", R.drawable.ic_official_kdca),
    OfficialSite("보건복지부", "정책 정보", "https://www.mohw.go.kr", R.drawable.ic_official_mohw),
    OfficialSite("정부24", "통합검색", "https://www.gov.kr", R.drawable.ic_official_gov24)
)

@Composable
fun InfoLayout() {
    val context = LocalContext.current
    val selectedDisease = remember { ProfileSettingsStore.load(context).selectedDisease }
    var searchText by remember { mutableStateOf("") }
    val trimmedSearch = searchText.trim()
    val isSearching = trimmedSearch.isNotEmpty()
    val filteredPolicies = remember(trimmedSearch) {
        if (trimmedSearch.isEmpty()) {
            policyItems
        } else {
            policyItems.filter { item ->
                item.title.contains(trimmedSearch, ignoreCase = true) ||
                    item.description.contains(trimmedSearch, ignoreCase = true) ||
                    item.tags.any { tag -> tag.contains(trimmedSearch, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2FFFF))
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
    ) {
        if (isSearching) {
            SearchResultHeader(onBackClick = { searchText = "" })
        } else {
            DiseaseInfoCard(selectedDisease = selectedDisease)
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "정책 검색",
                color = Color(0xFF1D252C),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "지원 정책, 복지 정보, 건강검진 정보를 찾아보세요.",
                color = Color(0xFF6E777C),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        PolicySearchField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = if (isSearching) "검색어를 입력하세요" else "검색어를 입력하세요 (예: 고혈압, 의료비)"
        )

        if (isSearching) {
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "검색 결과 ${filteredPolicies.size}건",
                color = Color(0xFF4D565C),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(14.dp))
            filteredPolicies.forEach { policy ->
                PolicyResultCard(
                    policy = policy,
                    onOpenClick = { openUrl(context, policy.url) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            SearchNoticeCard()
        } else {
            Spacer(modifier = Modifier.height(18.dp))
            KeywordSection(
                keywords = listOf("고혈압", "당뇨", "의료비", "건강검진"),
                onKeywordClick = { searchText = it }
            )
            Spacer(modifier = Modifier.height(28.dp))
            OfficialSiteSection(onSiteClick = { openUrl(context, it.url) })
        }
    }
}

@Composable
private fun DiseaseInfoCard(selectedDisease: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedDisease,
                            color = Color(0xFF20262C),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = diseaseCategory(selectedDisease),
                            color = Color(0xFF6D8587),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFEAF8F7))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = diseaseDescription(selectedDisease),
                        color = Color(0xFF4E5960),
                        fontSize = 13.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF4FEFE))
                    .padding(16.dp)
            ) {
                Text(
                    text = "매일 기록하면 좋은 항목",
                    color = Color(0xFF2B3338),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dailyRecordItems(selectedDisease).forEach { item ->
                        DailyRecordItem(iconRes = item.iconRes, label = item.label)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(Color(0xFFE9FBF8))
                    .padding(16.dp)
            ) {
                Text(
                    text = "관리 팁",
                    color = Color(0xFF1E9893),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = diseaseTip(selectedDisease),
                    color = Color(0xFF4E5960),
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DailyRecordItem(iconRes: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            color = Color(0xFF29343A),
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PolicySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        leadingIcon = {
            Text(text = "⌕", color = Color(0xFF5B656B), fontSize = 26.sp)
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                Text(
                    text = "×",
                    color = Color(0xFF697278),
                    fontSize = 25.sp,
                    modifier = Modifier.clickable { onValueChange("") }
                )
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF7D878D),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFFC8DADD),
            unfocusedBorderColor = Color(0xFFD8E4E6),
            cursorColor = Color(0xFF20C4C0),
            focusedTextColor = Color(0xFF20262C),
            unfocusedTextColor = Color(0xFF20262C)
        )
    )
}

@Composable
private fun KeywordSection(
    keywords: List<String>,
    onKeywordClick: (String) -> Unit
) {
    Text(
        text = "많이 찾는 키워드",
        color = Color(0xFF424B51),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keywords.forEach { keyword ->
            Text(
                text = "# $keyword",
                color = Color(0xFF168F8A),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE6F8F7))
                    .clickable { onKeywordClick(keyword) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun OfficialSiteSection(onSiteClick: (OfficialSite) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE7FAF8))
            .padding(16.dp)
    ) {
        Text(
            text = "외부 정보 바로가기",
            color = Color(0xFF1D252C),
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "신뢰할 수 있는 공식 사이트에서 더 많은 정보를 확인하세요.",
            color = Color(0xFF59656B),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            officialSites.forEach { site ->
                OfficialSiteCard(
                    site = site,
                    onClick = { onSiteClick(site) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OfficialSiteCard(
    site: OfficialSite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = site.iconRes),
            contentDescription = site.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = site.title,
            color = Color(0xFF253039),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = site.description,
            color = Color(0xFF4F5B62),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SearchResultHeader(onBackClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "‹",
            color = Color(0xFF1C252B),
            fontSize = 38.sp,
            lineHeight = 32.sp,
            modifier = Modifier.clickable(onClick = onBackClick)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "정책 검색",
            color = Color(0xFF17212A),
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun PolicyResultCard(
    policy: PolicyInfo,
    onOpenClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = policy.title,
                    color = Color(0xFF20262C),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                policy.tags.forEach { tag ->
                    Text(
                        text = tag,
                        color = Color(0xFF168F8A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFEAF8F7))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = policy.description,
                color = Color(0xFF4B555C),
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "더 알아보기  ›",
                color = Color(0xFF188F89),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable(onClick = onOpenClick)
            )
        }
    }
}

@Composable
private fun SearchNoticeCard() {
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "※ 제공되는 정보는 변경될 수 있으므로, 자세한 내용은 각 기관의 공식 사이트를 통해 확인해주세요.",
        color = Color(0xFF6F797F),
        fontSize = 11.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(22.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE0F8F5))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NoticeIcon()
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "알려드립니다",
                color = Color(0xFF168F8A),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "이 앱은 의료 진단이나 치료를 위한 목적이 아니며, 입력된 기록과 제공되는 정보는 참고용입니다. 정확한 진단 및 치료 판단은 반드시 의료진과 상담하시기 바랍니다.",
                color = Color(0xFF223038),
                fontSize = 12.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun NoticeIcon() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFFD0F2EF)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val teal = Color(0xFF159E98)
            drawCircle(
                color = teal,
                style = Stroke(width = 3.2.dp.toPx())
            )
            drawLine(
                color = teal,
                start = center.copy(y = size.height * 0.27f),
                end = center.copy(y = size.height * 0.58f),
                strokeWidth = 3.4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = teal,
                radius = 2.2.dp.toPx(),
                center = center.copy(y = size.height * 0.75f)
            )
        }
    }
}

private fun diseaseDescription(disease: String): String {
    return when (disease) {
        "당뇨" -> "혈당 조절이 꾸준히 필요한 상태로, 식사·운동·복약 기록을 함께 관리하는 것이 중요해요."
        "류마티스 관절염" -> "관절 통증과 피로가 반복될 수 있어 증상 변화와 활동량을 꾸준히 기록하는 것이 좋아요."
        else -> "혈압이 지속적으로 높은 상태로, 심장·뇌혈관·신장에 영향을 줄 수 있어 꾸준한 관리가 중요해요."
    }
}

private fun diseaseTip(disease: String): String {
    return when (disease) {
        "당뇨" -> "식사 시간과 운동량, 복약 여부를 함께 기록하면 혈당 관리 패턴을 파악하는 데 도움이 돼요."
        "류마티스 관절염" -> "통증이 심한 시간대와 활동 후 변화를 기록하면 컨디션 관리에 도움이 돼요."
        else -> "정해진 시간의 복약과 규칙적인 생활습관이 혈압 관리에 도움이 돼요."
    }
}

private fun diseaseCategory(disease: String): String {
    return when (disease) {
        "당뇨" -> "대사성 만성질환"
        "류마티스 관절염" -> "면역계 만성질환"
        else -> "심혈관계 만성질환"
    }
}

private fun dailyRecordItems(disease: String): List<DailyRecordInfo> {
    return when (disease) {
        "당뇨" -> listOf(
            DailyRecordInfo("혈당", R.drawable.ic_diabetes_blood_sugar),
            DailyRecordInfo("측정 시점", R.drawable.ic_diabetes_measure_time),
            DailyRecordInfo("저혈당 증상", R.drawable.ic_diabetes_low_sugar),
            DailyRecordInfo("갈증/소변", R.drawable.ic_diabetes_thirst_urine)
        )
        "류마티스 관절염" -> listOf(
            DailyRecordInfo("통증 점수", R.drawable.ic_rheumatoid_pain_score),
            DailyRecordInfo("통증 부위", R.drawable.ic_rheumatoid_pain_area),
            DailyRecordInfo("아침 뻣뻣함", R.drawable.ic_rheumatoid_morning_stiffness),
            DailyRecordInfo("관절 붓기", R.drawable.ic_rheumatoid_joint_swelling)
        )
        else -> listOf(
            DailyRecordInfo("혈압", R.drawable.ic_hypertension_blood_pressure),
            DailyRecordInfo("맥박", R.drawable.ic_hypertension_pulse),
            DailyRecordInfo("두통", R.drawable.ic_hypertension_headache),
            DailyRecordInfo("어지러움", R.drawable.ic_hypertension_dizziness)
        )
    }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
