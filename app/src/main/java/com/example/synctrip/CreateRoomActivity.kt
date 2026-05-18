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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.adapter.CityListAdapter
import com.example.synctrip.adapter.PopularDestinationAdapter
import com.example.synctrip.dto.destination.DestinationCatalog
import com.example.synctrip.dto.destination.DestinationResponse
import com.example.synctrip.dto.group.BandSummary
import com.example.synctrip.dto.group.CreateBandRequest
import com.google.android.material.button.MaterialButton
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
    private var step2OverseasTab = true

    private lateinit var step1: View
    private lateinit var step2: View
    private lateinit var step3: View
    private lateinit var tvStepTitle: TextView
    private lateinit var tvStepIndicator: TextView
    private lateinit var btnPrev: MaterialButton
    private lateinit var btnNext: MaterialButton

    private lateinit var popularAdapter: PopularDestinationAdapter
    private lateinit var cityAdapter: CityListAdapter

    private lateinit var etSearch: EditText
    private lateinit var etBandName: EditText
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var tvSelectedCity: TextView
    private lateinit var tvSelectedFlag: TextView
    private lateinit var chipGroupRegion: ChipGroup
    private lateinit var chipGroupThemes: ChipGroup
    private lateinit var tabLayout: TabLayout
    private lateinit var tvErrorName: TextView
    private lateinit var tvErrorStartDate: TextView
    private lateinit var tvErrorEndDate: TextView

    private var startDate: String = ""
    private var endDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        step1 = findViewById(R.id.step1)
        step2 = findViewById(R.id.step2)
        step3 = findViewById(R.id.step3)
        tvStepTitle = findViewById(R.id.tvStepTitle)
        tvStepIndicator = findViewById(R.id.tvStepIndicator)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener { finish() }
        btnPrev.setOnClickListener { goToStep(currentStep - 1) }
        btnNext.setOnClickListener { onNextClicked() }

        setupStep1()
        setupStep2()
        setupStep3()
        goToStep(1)
    }

    override fun onBackPressed() {
        if (currentStep > 1) goToStep(currentStep - 1) else super.onBackPressed()
    }

    private fun goToStep(step: Int) {
        currentStep = step.coerceIn(1, 3)
        step1.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        step2.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
        step3.visibility = if (currentStep == 3) View.VISIBLE else View.GONE

        tvStepIndicator.text = "$currentStep/3"
        tvStepTitle.text = when (currentStep) {
            1 -> "여행지 선택"
            2 -> "도시 선택"
            else -> "여행 정보"
        }
        btnPrev.visibility = if (currentStep > 1) View.VISIBLE else View.GONE
        btnNext.text = if (currentStep == 3) "방 만들기" else "계속하기"

        if (currentStep == 3) refreshStep3Preview()
        updateNextButtonEnabled()
    }

    private fun updateNextButtonEnabled() {
        btnNext.isEnabled = when (currentStep) {
            1, 2 -> selectedDestination != null
            3    -> true  // 항상 활성화 — 클릭 시 validateStep3()에서 검사
            else -> false
        }
    }

    private fun onNextClicked() {
        when (currentStep) {
            1 -> goToStep(2)
            2 -> goToStep(3)
            3 -> createBand()
        }
    }

    // ─── Step 1 ─────────────────────────────────────────────

    private fun setupStep1() {
        etSearch = findViewById(R.id.etSearch)
        val rvPopular = findViewById<RecyclerView>(R.id.rvPopular)
        rvPopular.layoutManager = GridLayoutManager(this, 2)

        popularAdapter = PopularDestinationAdapter(DestinationCatalog.TOP_PICKS) { d ->
            selectDestination(d)
        }
        rvPopular.adapter = popularAdapter

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
    }

    private fun runLocalSearch(query: String) {
        val results = DestinationCatalog.search(query)
        if (results.isEmpty()) {
            Toast.makeText(this, "검색 결과가 없어요. 인기 여행지에서 골라보세요.", Toast.LENGTH_SHORT).show()
            return
        }
        // 첫 번째 결과 자동 선택 후 Step 2로 이동
        selectDestination(results.first())
        goToStep(2)
    }

    // ─── Step 2 ─────────────────────────────────────────────

    private fun setupStep2() {
        tabLayout = findViewById(R.id.tabLayout)
        chipGroupRegion = findViewById(R.id.chipGroupRegion)
        val rvCities = findViewById<RecyclerView>(R.id.rvCities)
        rvCities.layoutManager = LinearLayoutManager(this)
        cityAdapter = CityListAdapter(mutableListOf()) { d -> selectDestination(d) }
        rvCities.adapter = cityAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                step2OverseasTab = tab.position == 0
                rebuildRegionChips()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        rebuildRegionChips()
    }

    private fun rebuildRegionChips() {
        chipGroupRegion.removeAllViews()
        val regions = if (step2OverseasTab) DestinationCatalog.OVERSEAS_REGIONS else DestinationCatalog.DOMESTIC_REGIONS
        regions.forEachIndexed { idx, region ->
            val chip = Chip(this).apply {
                text = region
                isCheckable = true
                isChecked = idx == 0
                setOnClickListener { applyRegionFilter(region) }
            }
            chipGroupRegion.addView(chip)
        }
        applyRegionFilter(regions.first())
    }

    private fun applyRegionFilter(region: String) {
        val list = DestinationCatalog.byRegion(region)
        cityAdapter.submit(list)
    }

    // ─── Step 3 ─────────────────────────────────────────────

    private fun setupStep3() {
        etBandName = findViewById(R.id.etBandName)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvSelectedCity = findViewById(R.id.tvSelectedCity)
        tvSelectedFlag = findViewById(R.id.tvSelectedFlag)
        chipGroupThemes = findViewById(R.id.chipGroupThemes)
        tvErrorName = findViewById(R.id.tvErrorName)
        tvErrorStartDate = findViewById(R.id.tvErrorStartDate)
        tvErrorEndDate = findViewById(R.id.tvErrorEndDate)

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
                // 귀국일 순서 에러도 재검사
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

    private fun refreshStep3Preview() {
        val d = selectedDestination ?: return
        tvSelectedFlag.text = DestinationCatalog.flagOf(d.countryCode)
        tvSelectedCity.text = "${d.name}, ${d.country}"
        if (etBandName.text.isNullOrEmpty()) etBandName.setText("${d.name} 여행")
    }

    private fun selectedThemes(): List<String> {
        val themes = mutableListOf<String>()
        if (findViewById<Chip>(R.id.chipActivity).isChecked) themes.add("ACTIVITY")
        if (findViewById<Chip>(R.id.chipResort).isChecked) themes.add("RESORT")
        if (findViewById<Chip>(R.id.chipFoodie).isChecked) themes.add("FOODIE")
        if (findViewById<Chip>(R.id.chipShopping).isChecked) themes.add("SHOPPING")
        if (findViewById<Chip>(R.id.chipCulture).isChecked) themes.add("CULTURE")
        if (findViewById<Chip>(R.id.chipNature).isChecked) themes.add("NATURE")
        return themes
    }

    private fun validateStep3(): Boolean {
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

    // ─── 공통 ─────────────────────────────────────────────

    private fun selectDestination(d: DestinationResponse) {
        selectedDestination = d
        popularAdapter.selectedName = d.name
        cityAdapter.selectedName = d.name
        updateNextButtonEnabled()
    }

    private fun createBand() {
        if (!validateStep3()) return
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
            overseas = d.overseas
        )

        android.util.Log.d("CreateBand", "요청: name=$name, lat=${d.lat}, lng=${d.lng}, overseas=${d.overseas}, themes=${selectedThemes()}")

        btnNext.isEnabled = false
        btnNext.text = "생성 중..."

        RetrofitClient.api.createBand(request)
            .enqueue(object : Callback<BandSummary> {
                override fun onResponse(call: Call<BandSummary>, response: Response<BandSummary>) {
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
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: ""
                        android.util.Log.e("CreateBand", "실패: ${response.code()} $errorBody")
                        Toast.makeText(this@CreateRoomActivity, "방 생성 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                        btnNext.isEnabled = true
                        btnNext.text = "방 만들기"
                    }
                }
                override fun onFailure(call: Call<BandSummary>, t: Throwable) {
                    Toast.makeText(this@CreateRoomActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    btnNext.isEnabled = true
                    btnNext.text = "방 만들기"
                }
            })
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
