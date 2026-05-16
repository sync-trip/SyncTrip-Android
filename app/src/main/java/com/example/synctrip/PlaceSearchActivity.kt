package com.example.synctrip

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.adapter.PlaceSearchAdapter
import com.example.synctrip.dto.group.PlacePickListResponse
import com.example.synctrip.dto.group.PlacePickRequest
import com.example.synctrip.dto.group.PlacePickResponse
import com.example.synctrip.dto.kakao.PlaceDocument
import com.example.synctrip.dto.kakao.PlaceSearchResponse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaceSearchActivity : AppCompatActivity() {

    private val allResults = mutableListOf<PlaceDocument>()
    private val filteredResults = mutableListOf<PlaceDocument>()
    private lateinit var adapter: PlaceSearchAdapter
    private var bandId: Long = -1L
    private var currentCategory = "전체"
    private var currentPickCount = 0
    private var maxPickCount = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_search)

        bandId = intent.getLongExtra("BAND_ID", -1L)

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
                    searchPlaces(query)
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
            applyFilter()
        }

        loadPickCount()
    }

    private fun searchPlaces(query: String) {
        KakaoRetrofitClient.api.searchPlaces("KakaoAK ${BuildConfig.KAKAO_REST_KEY}", query)
            .enqueue(object : Callback<PlaceSearchResponse> {
                override fun onResponse(call: Call<PlaceSearchResponse>, response: Response<PlaceSearchResponse>) {
                    if (response.isSuccessful) {
                        allResults.clear()
                        allResults.addAll(response.body()?.documents ?: emptyList())
                        applyFilter()
                    } else {
                        android.widget.Toast.makeText(this@PlaceSearchActivity, "검색 실패: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PlaceSearchResponse>, t: Throwable) {
                    android.util.Log.e("PlaceSearch", "검색 실패: ${t.message}")
                }
            })
    }

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
        val request = PlacePickRequest(
            externalId = "${place.place_name}_${place.x}_${place.y}",
            name = place.place_name,
            category = kakaoToCategory(place.category_name),
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
