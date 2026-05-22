package com.example.synctrip

import android.content.Intent
import android.net.Uri
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
import com.example.synctrip.adapter.CartPickAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
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
    private var currentCategory: String? = null
    private var currentPickCount = 0
    private var maxPickCount = 5
    private val pickedExternalIds = mutableSetOf<String>()
    private var isSearching = false
    private lateinit var cartCard: MaterialCardView

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
        adapter = PlaceSearchAdapter(filteredResults, { place -> addPick(place) }, { place -> openDetail(place) })
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
                R.id.chipFood     -> "FOOD"
                R.id.chipCulture  -> "CULTURE"
                R.id.chipActivity -> "ACTIVITY"
                R.id.chipShopping -> "SHOPPING"
                R.id.chipNature   -> "NATURE"
                else              -> null
            }
            applyFilter()
        }

        loadPickCount()

        cartCard = findViewById(R.id.layoutCartBar)
        cartCard.setOnClickListener { showCartBottomSheet() }
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
        if (isSearching) return
        val keyword = findViewById<EditText>(R.id.etSearch).text.toString().trim().ifBlank { null }
        if (keyword == null) return
        isSearching = true
        loadingOverlay.visibility = View.VISIBLE
        // category는 항상 null로 전송 — 전체 결과를 받아 로컬에서 필터링
        RetrofitClient.api.searchOverseasPlaces(bandId, keyword, null)
            .enqueue(object : Callback<List<PlaceSearchResult>> {
                override fun onResponse(call: Call<List<PlaceSearchResult>>, response: Response<List<PlaceSearchResult>>) {
                    isSearching = false
                    loadingOverlay.visibility = View.GONE
                    if (response.isSuccessful) {
                        val results = response.body() ?: emptyList()
                        allResults.clear()
                        allResults.addAll(results.map { it.toPlaceDocument() })
                        applyFilter()
                    } else {
                        android.widget.Toast.makeText(this@PlaceSearchActivity, "검색 실패: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<PlaceSearchResult>>, t: Throwable) {
                    isSearching = false
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
        place_url = thumbnailUrl,
        rating = rating
    )

    private fun applyFilter() {
        filteredResults.clear()
        filteredResults.addAll(
            if (currentCategory == null) allResults
            else allResults.filter { matchesCategory(it.category_name, currentCategory!!) }
        )
        adapter.notifyDataSetChanged()
    }

    private fun matchesCategory(categoryName: String, tab: String): Boolean {
        // 해외 결과: 백엔드가 반환하는 PlaceCategory enum 이름과 직접 비교
        if (categoryName == tab) return true
        // 국내 결과: 카카오 카테고리 문자열(한국어) 키워드 매핑
        return when (tab) {
            "FOOD"     -> categoryName.contains("음식") || categoryName.contains("식당") || categoryName.contains("카페") || categoryName.contains("제과")
            "CULTURE"  -> categoryName.contains("관광") || categoryName.contains("문화") || categoryName.contains("박물관") || categoryName.contains("미술관") || categoryName.contains("역사")
            "ACTIVITY" -> categoryName.contains("스포츠") || categoryName.contains("레저") || categoryName.contains("오락") || categoryName.contains("테마파크")
            "SHOPPING" -> categoryName.contains("쇼핑") || categoryName.contains("마트") || categoryName.contains("백화점") || categoryName.contains("시장")
            "NATURE"   -> categoryName.contains("공원") || categoryName.contains("산") || categoryName.contains("해변") || categoryName.contains("자연")
            else       -> true
        }
    }

    private fun addPick(place: PlaceDocument) {
        if (bandId == -1L) return
        if (pickedExternalIds.contains(place.id)) {
            android.widget.Toast.makeText(this, "이미 담은 장소예요", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (currentPickCount >= maxPickCount) {
            Snackbar.make(cartCard, "장소를 ${maxPickCount}개 모두 담았어요!", Snackbar.LENGTH_SHORT)
                .setAnchorView(cartCard)
                .show()
            return
        }
        val request = PlacePickRequest(
            apiSource = if (overseas) "GOOGLE" else "KAKAO",
            externalId = place.id,
            name = place.place_name,
            category = if (overseas) place.category_name else kakaoToCategory(place.category_name),
            latitude = place.y.toDoubleOrNull() ?: 0.0,
            longitude = place.x.toDoubleOrNull() ?: 0.0,
            address = place.road_address_name.ifEmpty { place.address_name },
            rating = place.rating,
            thumbnailUrl = if (overseas) place.place_url?.takeIf { it.isNotBlank() } else null
        )
        RetrofitClient.api.addPick(bandId, request)
            .enqueue(object : Callback<PlacePickResponse> {
                override fun onResponse(call: Call<PlacePickResponse>, response: Response<PlacePickResponse>) {
                    when {
                        response.isSuccessful -> {
                            Snackbar.make(cartCard, "${place.place_name} 담았어요!", Snackbar.LENGTH_SHORT)
                                .setAnchorView(cartCard)
                                .show()
                            pickedExternalIds.add(place.id)
                            currentPickCount++
                            updateCartBadge()
                        }
                        response.code() == 403 -> android.widget.Toast.makeText(this@PlaceSearchActivity, "투표 이후 합류한 멤버는 장소를 담을 수 없어요", android.widget.Toast.LENGTH_SHORT).show()
                        response.code() == 409 -> android.widget.Toast.makeText(this@PlaceSearchActivity, "일정이 이미 생성됐어요. 장소를 더 담을 수 없어요", android.widget.Toast.LENGTH_SHORT).show()
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
                        pickedExternalIds.clear()
                        pickedExternalIds.addAll(data.items.map { it.externalId })
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

    private fun openDetail(place: PlaceDocument) {
        val url = if (overseas) {
            "https://www.google.com/maps/search/?api=1&query_place_id=${place.id}&query=${Uri.encode(place.place_name)}"
        } else {
            place.place_url?.takeIf { it.isNotBlank() }
                ?: "https://place.map.kakao.com/${place.id}"
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun showCartBottomSheet() {
        val sheet = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_cart, null)
        sheet.setContentView(sheetView)

        val rvCart = sheetView.findViewById<RecyclerView>(R.id.rvCartPicks)
        rvCart.layoutManager = LinearLayoutManager(this)

        RetrofitClient.api.getPicks(bandId).enqueue(object : Callback<PlacePickListResponse> {
            override fun onResponse(call: Call<PlacePickListResponse>, response: Response<PlacePickListResponse>) {
                if (!response.isSuccessful) return
                val data = response.body() ?: return
                sheetView.findViewById<TextView>(R.id.tvCartTitle).text =
                    "내 장바구니 (${data.currentCount}/${data.maxCount})"
                rvCart.adapter = CartPickAdapter(data.items.toMutableList()) { pick ->
                    deletePickFromSheet(pick, sheet, rvCart)
                }
            }
            override fun onFailure(call: Call<PlacePickListResponse>, t: Throwable) {
                android.widget.Toast.makeText(this@PlaceSearchActivity, "목록 불러오기 실패", android.widget.Toast.LENGTH_SHORT).show()
            }
        })

        sheet.show()
    }

    private fun deletePickFromSheet(pick: PlacePickResponse, sheet: BottomSheetDialog, rvCart: RecyclerView) {
        RetrofitClient.api.deletePick(bandId, pick.placeId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (!response.isSuccessful) {
                    android.widget.Toast.makeText(this@PlaceSearchActivity, "삭제 실패 (${response.code()})", android.widget.Toast.LENGTH_SHORT).show()
                    return
                }
                pickedExternalIds.remove(pick.externalId)
                currentPickCount--
                updateCartBadge()
                (rvCart.adapter as? CartPickAdapter)?.removeItem(pick.placeId)
                if (currentPickCount == 0) sheet.dismiss()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                android.widget.Toast.makeText(this@PlaceSearchActivity, "서버 연결 실패", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
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
