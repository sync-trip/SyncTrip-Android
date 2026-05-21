package com.example.synctrip

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.adapter.CityListAdapter
import com.example.synctrip.dto.destination.DestinationCatalog
import com.example.synctrip.dto.destination.DestinationResponse
import com.example.synctrip.dto.group.BandSummary
import com.example.synctrip.dto.group.CreateBandRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class CreateRoomActivity : AppCompatActivity() {

    private var currentStep = 1
    private var selectedDestination: DestinationResponse? = null
    private var step1OverseasTab = true

    private lateinit var step1: View
    private lateinit var step2: View
    private lateinit var tvStepTitle: TextView
    private lateinit var tvStepIndicator: TextView
    private lateinit var btnPrev: MaterialButton
    private lateinit var btnNext: MaterialButton

    private lateinit var cityAdapter: CityListAdapter

    private lateinit var etSearch: EditText
    private lateinit var etBandName: EditText
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var tvSelectedCity: TextView
    private lateinit var tvSelectedFlag: TextView
    private lateinit var chipGroupRegion: ChipGroup
    private lateinit var cardRelaxed: MaterialCardView
    private lateinit var cardPacked: MaterialCardView
    private var selectedTravelStyle: String = "RELAXED"
    private lateinit var tabLayout: TabLayout
    private lateinit var tvErrorName: TextView
    private lateinit var tvErrorStartDate: TextView
    private lateinit var tvErrorEndDate: TextView

    private var startDate: String = ""
    private var endDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_room)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        step1 = findViewById(R.id.step1)
        step2 = findViewById(R.id.step2)
        tvStepTitle = findViewById(R.id.tvStepTitle)
        tvStepIndicator = findViewById(R.id.tvStepIndicator)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener { finish() }
        btnPrev.setOnClickListener { goToStep(currentStep - 1) }
        btnNext.setOnClickListener { onNextClicked() }

        setupStep1()
        setupStep2()
        goToStep(1)
    }

    override fun onBackPressed() {
        if (currentStep > 1) goToStep(currentStep - 1) else super.onBackPressed()
    }

    private fun goToStep(step: Int) {
        currentStep = step.coerceIn(1, 2)
        step1.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        step2.visibility = if (currentStep == 2) View.VISIBLE else View.GONE

        tvStepIndicator.text = "$currentStep/2"
        tvStepTitle.text = if (currentStep == 1) "여행지 선택" else "여행 정보"
        btnPrev.visibility = if (currentStep > 1) View.VISIBLE else View.GONE
        btnNext.text = if (currentStep == 2) "방 만들기" else "계속하기"

        if (currentStep == 2) refreshStep2Preview()
        updateNextButtonEnabled()
    }

    private fun updateNextButtonEnabled() {
        btnNext.isEnabled = when (currentStep) {
            1    -> selectedDestination != null
            2    -> true
            else -> false
        }
    }

    private fun onNextClicked() {
        when (currentStep) {
            1 -> goToStep(2)
            2 -> createBand()
        }
    }

    // ─── Step 1: 여행지 선택 (검색 + 탭 + 칩 + 리스트 통합) ────────────────

    private fun setupStep1() {
        etSearch = findViewById(R.id.etSearch)
        tabLayout = findViewById(R.id.tabLayout)
        chipGroupRegion = findViewById(R.id.chipGroupRegion)

        val rvCities = findViewById<RecyclerView>(R.id.rvCities)
        rvCities.layoutManager = LinearLayoutManager(this)
        cityAdapter = CityListAdapter(mutableListOf()) { d -> selectDestination(d) }
        rvCities.adapter = cityAdapter

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    hideKeyboard(etSearch)
                    runLocalSearch(query)
                }
                true
            } else false
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                step1OverseasTab = tab.position == 0
                etSearch.text?.clear()
                rebuildRegionChips()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        rebuildRegionChips()
    }

    private fun rebuildRegionChips() {
        chipGroupRegion.removeAllViews()

        val popularChip = Chip(this).apply {
            text = "인기"
            isCheckable = true
            isChecked = true
            setOnClickListener { applyRegionFilter("인기") }
        }
        chipGroupRegion.addView(popularChip)

        val regions = if (step1OverseasTab) DestinationCatalog.OVERSEAS_REGIONS else DestinationCatalog.DOMESTIC_REGIONS
        regions.forEach { region ->
            val chip = Chip(this).apply {
                text = region
                isCheckable = true
                isChecked = false
                setOnClickListener { applyRegionFilter(region) }
            }
            chipGroupRegion.addView(chip)
        }

        applyRegionFilter("인기")
    }

    private fun applyRegionFilter(region: String) {
        val list = if (region == "인기") {
            DestinationCatalog.TOP_PICKS.filter { it.overseas == step1OverseasTab }
        } else {
            DestinationCatalog.byRegion(region)
        }
        cityAdapter.submit(list)
    }

    private fun runLocalSearch(query: String) {
        val results = DestinationCatalog.search(query)
        if (results.isEmpty()) {
            Toast.makeText(this, "아직 준비 중인 여행지예요. 목록에서 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        cityAdapter.submit(results)
    }

    // ─── Step 2: 여행 정보 입력 ──────────────────────────────────────────────

    private fun setupStep2() {
        etBandName = findViewById(R.id.etBandName)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvSelectedCity = findViewById(R.id.tvSelectedCity)
        tvSelectedFlag = findViewById(R.id.tvSelectedFlag)
        cardRelaxed = findViewById(R.id.cardRelaxed)
        cardPacked = findViewById(R.id.cardPacked)
        tvErrorName = findViewById(R.id.tvErrorName)
        tvErrorStartDate = findViewById(R.id.tvErrorStartDate)
        tvErrorEndDate = findViewById(R.id.tvErrorEndDate)

        updateTravelStyleUI()
        cardRelaxed.setOnClickListener { selectedTravelStyle = "RELAXED"; updateTravelStyleUI() }
        cardPacked.setOnClickListener { selectedTravelStyle = "PACKED"; updateTravelStyleUI() }

        etBandName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s?.isNotEmpty() == true) tvErrorName.visibility = View.GONE
            }
        })

        val calendar = Calendar.getInstance()
        findViewById<LinearLayout>(R.id.layoutStartDate).setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                startDate = "%d-%02d-%02d".format(year, month + 1, day)
                tvStartDate.text = startDate
                tvStartDate.setTextColor(getColor(R.color.primary))
                tvErrorStartDate.visibility = View.GONE
                if (endDate.isNotEmpty() && endDate >= startDate) tvErrorEndDate.visibility = View.GONE
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        findViewById<LinearLayout>(R.id.layoutEndDate).setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                endDate = "%d-%02d-%02d".format(year, month + 1, day)
                tvEndDate.text = endDate
                tvEndDate.setTextColor(getColor(R.color.primary))
                tvErrorEndDate.visibility = View.GONE
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun refreshStep2Preview() {
        val d = selectedDestination ?: return
        tvSelectedFlag.text = DestinationCatalog.flagOf(d.countryCode)
        tvSelectedCity.text = "${d.name}, ${d.country}"
        if (etBandName.text.isNullOrEmpty()) etBandName.setText("${d.name} 여행")
    }

    private fun updateTravelStyleUI() {
        val primaryColor = getColor(R.color.primary)
        val outlineColor = getColor(R.color.outline_variant)
        val primaryContainer = getColor(R.color.primary_container)
        val surfaceLow = getColor(R.color.surface_container_low)

        if (selectedTravelStyle == "RELAXED") {
            cardRelaxed.strokeColor = primaryColor
            cardRelaxed.setCardBackgroundColor(primaryContainer)
            cardPacked.strokeColor = outlineColor
            cardPacked.setCardBackgroundColor(surfaceLow)
        } else {
            cardPacked.strokeColor = primaryColor
            cardPacked.setCardBackgroundColor(primaryContainer)
            cardRelaxed.strokeColor = outlineColor
            cardRelaxed.setCardBackgroundColor(surfaceLow)
        }
    }

    private fun validateStep2(): Boolean {
        var valid = true

        if (etBandName.text.toString().trim().isEmpty()) {
            tvErrorName.visibility = View.VISIBLE
            etBandName.requestFocus()
            valid = false
        }

        if (startDate.isEmpty()) {
            tvErrorStartDate.visibility = View.VISIBLE
            tvStartDate.setTextColor(getColor(R.color.danger))
            valid = false
        }

        if (endDate.isEmpty()) {
            tvErrorEndDate.text = "귀국일을 선택해주세요"
            tvErrorEndDate.visibility = View.VISIBLE
            tvEndDate.setTextColor(getColor(R.color.danger))
            valid = false
        } else if (startDate.isNotEmpty() && endDate < startDate) {
            tvErrorEndDate.text = "귀국일이 출발일보다 빠를 수 없어요"
            tvErrorEndDate.visibility = View.VISIBLE
            tvEndDate.setTextColor(getColor(R.color.danger))
            valid = false
        }

        return valid
    }

    // ─── 공통 ────────────────────────────────────────────────────────────────

    private fun selectDestination(d: DestinationResponse) {
        selectedDestination = d
        cityAdapter.selectedName = d.name
        updateNextButtonEnabled()
    }

    private fun createBand() {
        if (!validateStep2()) return
        val d = selectedDestination ?: return
        val name = etBandName.text.toString().trim().ifEmpty { "${d.name} 여행" }

        val request = CreateBandRequest(
            name = name,
            startDate = startDate,
            endDate = endDate,
            destination = "${d.country} ${d.name}",
            destinationLat = d.lat,
            destinationLng = d.lng,
            countryCode = d.countryCode,
            overseas = d.overseas,
            travelStyle = selectedTravelStyle
        )

        android.util.Log.d("CreateBand", "요청: name=$name, lat=${d.lat}, lng=${d.lng}, overseas=${d.overseas}, style=$selectedTravelStyle")

        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)
        btnNext.isEnabled = false
        loadingOverlay.visibility = View.VISIBLE

        RetrofitClient.api.createBand(request)
            .enqueue(object : Callback<BandSummary> {
                override fun onResponse(call: Call<BandSummary>, response: Response<BandSummary>) {
                    loadingOverlay.visibility = View.GONE
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(this@CreateRoomActivity, "여행 방이 생성되었습니다!", Toast.LENGTH_SHORT).show()
                        val intent = android.content.Intent(this@CreateRoomActivity, SubActivity::class.java)
                        intent.putExtra("ROOM_NAME", body?.name ?: name)
                        intent.putExtra("BAND_ID", body?.id ?: -1L)
                        intent.putExtra("INVITE_CODE", body?.inviteCode)
                        intent.putExtra("START_DATE", body?.startDate ?: startDate)
                        intent.putExtra("END_DATE", body?.endDate ?: endDate)
                        intent.putExtra("BAND_STATUS", body?.status ?: "PLANNING")
                        intent.putExtra("OVERSEAS", d.overseas)
                        intent.putExtra("DESTINATION", "${d.country} ${d.name}")
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: ""
                        android.util.Log.e("CreateBand", "실패: ${response.code()} $errorBody")
                        Toast.makeText(this@CreateRoomActivity, "방 생성 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                        btnNext.isEnabled = true
                    }
                }
                override fun onFailure(call: Call<BandSummary>, t: Throwable) {
                    loadingOverlay.visibility = View.GONE
                    Toast.makeText(this@CreateRoomActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    btnNext.isEnabled = true
                }
            })
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
