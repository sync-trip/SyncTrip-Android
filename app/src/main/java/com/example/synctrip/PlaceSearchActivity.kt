package com.example.synctrip

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.adapter.PlaceSearchAdapter
import com.example.synctrip.dto.group.PlacePickListResponse
import com.example.synctrip.dto.group.PlacePickRequest
import com.example.synctrip.dto.group.PlacePickResponse
import com.example.synctrip.dto.kakao.PlaceDocument
import com.example.synctrip.dto.kakao.PlaceSearchResponse
import com.example.synctrip.dto.place.PlaceSearchResult
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaceSearchActivity : AppCompatActivity() {

    private val allResults = mutableListOf<PlaceDocument>()
    private val filteredResults = mutableListOf<PlaceDocument>()
    private lateinit var adapter: PlaceSearchAdapter
    private lateinit var loadingOverlay: android.view.View
    private var bandId: Long = -1L
    private var overseas: Boolean = false
    private var currentCategory = "전체"
    private var currentPickCount = 0
    private var maxPickCount = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_place_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        bandId = intent.getLongExtra("BAND_ID", -1L)
        overseas = intent.getBooleanExtra("OVERSEAS", false)
        android.util.Log.d("PlaceSearch", "bandId=$bandId overseas=$overseas")

        loadingOverlay = findViewById(R.id.loadingOverlay)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvSearchResults = findViewById<RecyclerView>(R.id.rvSearchResults)
        rvSearchResults.layoutManager = LinearLayoutManager(this)
        adapter = PlaceSearchAdapter(filteredResults) { place -> addPick(place) }
        rvSearchResults.adapter = adapter

        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    hideKeyboard(etSearch)
                    if (overseas) searchOverseasPlaces() else searchPlaces(query)
                }
                true
            } else false
        }

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategory)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentCategory = when (checkedIds.firstOrNull()) {
                R.id.chipFood    -> "맛집"
                R.id.chipTourism -> "관광지"
                R.id.chipCafe    -> "카페"
                R.id.chipAccom   -> "숙소"
                else             -> "전체"
            }
            if (overseas) searchOverseasPlaces() else applyFilter()
        }

        loadPickCount()
    }

    private fun searchPlaces(query: String) {
        loadingOverlay.visibility = View.VISIBLE
        KakaoRetrofitClient.api.searchPlaces("KakaoAK ${BuildConfig.KAKAO_REST_KEY}", query)
            .enqueue(object : Callback<PlaceSearchResponse> {
                override fun onResponse(call: Call<PlaceSearchResponse>, response: Response<PlaceSearchResponse>) {
                    loadingOverlay.visibility = View.GONE
                    if (response.isSuccessful) {
                        allResults.clear()
                        allResults.addAll(response.body()?.documents ?: emptyList())
                        applyFilter()
                    } else {
                        android.widget.Toast.makeText(this@PlaceSearchActivity, "검색 실패: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PlaceSearchResponse>, t: Throwable) {
                    loadingOverlay.visibility = View.GONE
                    android.util.Log.e("PlaceSearch", "검색 실패: ${t.message}")
                }
            })
    }

    private fun searchOverseasPlaces() {
        if (bandId == -1L) return
        val serverCategory = when (currentCategory) {
            "맛집"  -> "FOOD"
            "관광지" -> "CULTURE"
            "카페"  -> "FOOD"
            "숙소"  -> "ETC"
            else   -> null
        }
        loadingOverlay.visibility = View.VISIBLE
        RetrofitClient.api.searchOverseasPlaces(bandId, serverCategory)
            .enqueue(object : Callback<List<PlaceSearchResult>> {
                override fun onResponse(call: Call<List<PlaceSearchResult>>, response: Response<List<PlaceSearchResult>>) {
                    loadingOverlay.visibility = View.GONE
                    if (response.isSuccessful) {
                        val results = response.body() ?: emptyList()
                        allResults.clear()
                        filteredResults.clear()
                        filteredResults.addAll(results.map { it.toPlaceDocument() })
                        adapter.notifyDataSetChanged()
                    } else {
                        android.widget.Toast.makeText(this@PlaceSearchActivity, "검색 실패: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<PlaceSearchResult>>, t: Throwable) {
                    loadingOverlay.visibility = View.GONE
                    android.util.Log.e("PlaceSearch", "해외 장소 검색 실패: ${t.message}")
                }
            })
    }

    private fun PlaceSearchResult.toPlaceDocument() = PlaceDocument(
        id = externalId,
        place_name = name,
        category_name = category,
        address_name = address,
        road_address_name = address,
        x = longitude.toString(),
        y = latitude.toString(),
        phone = null,
        place_url = thumbnailUrl
    )

    private fun applyFilter() {
        filteredResults.clear()
        filteredResults.addAll(
            if (currentCategory == "전체") allResults
            else allResults.filter { matchesCategory(it.category_name, currentCategory) }
        )
        adapter.notifyDataSetChanged()
    }

    private fun matchesCategory(categoryName: String, tab: String): Boolean = when (tab) {
        "맛집"  -> categoryName.contains("음식") || categoryName.contains("식당") || categoryName.contains("맛집")
        "관광지" -> categoryName.contains("관광") || categoryName.contains("문화") || categoryName.contains("박물관") || categoryName.contains("미술관")
        "카페"  -> categoryName.contains("카페") || categoryName.contains("커피")
        "숙소"  -> categoryName.contains("숙박") || categoryName.contains("호텔") || categoryName.contains("펜션") || categoryName.contains("게스트하우스")
        else   -> true
    }

    private fun addPick(place: PlaceDocument) {
        if (bandId == -1L) return
        if (currentPickCount >= maxPickCount) {
            android.widget.Toast.makeText(this, "장소를 ${maxPickCount}개 모두 담았어요!\n더 담으려면 홈에서 기존 장소를 삭제해주세요.", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        val request = PlacePickRequest(
            apiSource = if (overseas) "GOOGLE" else "KAKAO",
            externalId = place.id,
            name = place.place_name,
            category = if (overseas) place.category_name else kakaoToCategory(place.category_name),
            latitude = place.y.toDouble(),
            longitude = place.x.toDouble(),
            address = place.road_address_name.ifEmpty { place.address_name }
        )
        RetrofitClient.api.addPick(bandId, request)
            .enqueue(object : Callback<PlacePickResponse> {
                override fun onResponse(call: Call<PlacePickResponse>, response: Response<PlacePickResponse>) {
                    when {
                        response.isSuccessful -> {
                            android.widget.Toast.makeText(this@PlaceSearchActivity, "${place.place_name} 담았어요!", android.widget.Toast.LENGTH_SHORT).show()
                            currentPickCount++
                            updateCartBadge()
                        }
                        response.code() == 403 -> android.widget.Toast.makeText(this@PlaceSearchActivity, "투표가 시작돼서 장소를 더 담을 수 없어요", android.widget.Toast.LENGTH_SHORT).show()
                        response.code() == 409 -> android.widget.Toast.makeText(this@PlaceSearchActivity, "이미 담은 장소예요", android.widget.Toast.LENGTH_SHORT).show()
                        response.code() == 400 -> android.widget.Toast.makeText(this@PlaceSearchActivity, "장소는 최대 ${maxPickCount}개까지 담을 수 있어요", android.widget.Toast.LENGTH_SHORT).show()
                        else -> android.widget.Toast.makeText(this@PlaceSearchActivity, "담기 실패 (${response.code()})", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PlacePickResponse>, t: Throwable) {
                    android.widget.Toast.makeText(this@PlaceSearchActivity, "서버 연결 실패", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadPickCount() {
        if (bandId == -1L) return
        RetrofitClient.api.getPicks(bandId)
            .enqueue(object : Callback<PlacePickListResponse> {
                override fun onResponse(call: Call<PlacePickListResponse>, response: Response<PlacePickListResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return
                        currentPickCount = data.currentCount
                        maxPickCount = data.maxCount
                        updateCartBadge()
                    }
                }
                override fun onFailure(call: Call<PlacePickListResponse>, t: Throwable) {}
            })
    }

    private fun updateCartBadge() {
        findViewById<TextView>(R.id.tvPickCount).text = "$currentPickCount / $maxPickCount"
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun kakaoToCategory(categoryName: String): String = when {
        categoryName.contains("음식") || categoryName.contains("카페") || categoryName.contains("식당") -> "FOOD"
        categoryName.contains("관광") || categoryName.contains("문화") || categoryName.contains("박물관") || categoryName.contains("미술관") -> "CULTURE"
        categoryName.contains("스포츠") || categoryName.contains("레저") || categoryName.contains("놀이") || categoryName.contains("테마파크") -> "ACTIVITY"
        categoryName.contains("쇼핑") -> "SHOPPING"
        categoryName.contains("자연") || categoryName.contains("공원") || categoryName.contains("해변") -> "NATURE"
        else -> "ETC"
    }
}
