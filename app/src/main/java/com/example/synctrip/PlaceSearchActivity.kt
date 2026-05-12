package com.example.synctrip

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.adapter.PlaceSearchAdapter
import com.example.synctrip.dto.kakao.PlaceDocument
import com.example.synctrip.dto.kakao.PlaceSearchResponse
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.synctrip.BuildConfig

class PlaceSearchActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var kakaoMap: KakaoMap? = null
    private val searchResults = mutableListOf<PlaceDocument>()
    private lateinit var rvSearchResults: RecyclerView

    private val REST_API_KEY = BuildConfig.KAKAO_REST_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_search)

        mapView = findViewById(R.id.mapView)
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    android.util.Log.d("KakaoMap", "지도 종료")
                }
                override fun onMapError(error: Exception) {
                    android.util.Log.e("KakaoMap", "지도 에러: ${error.message}")
                }
            },
            object : KakaoMapReadyCallback() {
                override fun onMapReady(map: KakaoMap) {
                    kakaoMap = map
                    android.util.Log.d("KakaoMap", "지도 준비 완료!")
                }
            }
        )

        // 검색 결과 RecyclerView
        rvSearchResults = findViewById(R.id.rvSearchResults)
        rvSearchResults.layoutManager = LinearLayoutManager(this)
        rvSearchResults.adapter = PlaceSearchAdapter(searchResults) { place ->
            // 결과 클릭 → 지도 이동 + 리스트 숨기기
            val lat = place.y.toDouble()
            val lng = place.x.toDouble()
            kakaoMap?.moveCamera(
                CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng))
            )
            rvSearchResults.visibility = android.view.View.GONE
            android.widget.Toast.makeText(this, place.place_name, android.widget.Toast.LENGTH_SHORT).show()
        }

        // 검색 버튼
        val etSearch = findViewById<android.widget.EditText>(R.id.etSearch)
        findViewById<android.widget.Button>(R.id.btnSearch).setOnClickListener {
            val query = etSearch.text.toString()
            if (query.isEmpty()) {
                android.widget.Toast.makeText(this, "검색어를 입력해주세요", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchPlaces(query)
        }
    }

    private fun searchPlaces(query: String) {
        KakaoRetrofitClient.api.searchPlaces("KakaoAK $REST_API_KEY", query)
            .enqueue(object : Callback<PlaceSearchResponse> {
                override fun onResponse(call: Call<PlaceSearchResponse>, response: Response<PlaceSearchResponse>) {
                    if (response.isSuccessful) {
                        val results = response.body()?.documents ?: emptyList()
                        searchResults.clear()
                        searchResults.addAll(results)
                        rvSearchResults.adapter?.notifyDataSetChanged()
                        rvSearchResults.visibility = android.view.View.VISIBLE
                        android.util.Log.d("PlaceSearch", "검색 성공: ${results.size}개")
                    } else {
                        android.util.Log.e("PlaceSearch", "검색 실패: ${response.code()}")
                        android.widget.Toast.makeText(this@PlaceSearchActivity, "검색 실패: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PlaceSearchResponse>, t: Throwable) {
                    android.util.Log.e("PlaceSearch", "서버 연결 실패: ${t.message}")
                }
            })
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }
}