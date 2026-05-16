package com.example.synctrip

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.synctrip.dto.group.CreateBandRequest
import com.example.synctrip.dto.group.CreateBandResponse
import com.example.synctrip.dto.kakao.PlaceSearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class CreateRoomActivity : AppCompatActivity() {

    private var destinationLat: Double? = null
    private var destinationLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val etCountry = findViewById<EditText>(R.id.etCountry)
        val etCity = findViewById<EditText>(R.id.etCity)
        val tvStartDate = findViewById<TextView>(R.id.tvStartDate)
        val tvEndDate = findViewById<TextView>(R.id.tvEndDate)
        val btnCreateDone = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreateDone)

        findViewById<android.widget.ImageButton>(R.id.btnClose).setOnClickListener { finish() }

        // Quick suggestion cards fill both country and city fields
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardTokyo).setOnClickListener {
            etCountry.setText("일본")
            etCity.setText("도쿄")
            searchCityLocation("도쿄")
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardParis).setOnClickListener {
            etCountry.setText("프랑스")
            etCity.setText("파리")
            searchCityLocation("파리")
        }

        // Trigger Kakao search when focus leaves city field
        etCity.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val city = etCity.text.toString().trim()
                if (city.isNotEmpty()) searchCityLocation(city)
            }
        }

        // Date pickers
        val calendar = Calendar.getInstance()
        findViewById<LinearLayout>(R.id.layoutStartDate).setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                tvStartDate.text = "%d-%02d-%02d".format(year, month + 1, day)
                tvStartDate.setTextColor(getColor(R.color.primary))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        findViewById<LinearLayout>(R.id.layoutEndDate).setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                tvEndDate.text = "%d-%02d-%02d".format(year, month + 1, day)
                tvEndDate.setTextColor(getColor(R.color.primary))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnCreateDone.setOnClickListener {
            val country = etCountry.text.toString().trim()
            val city = etCity.text.toString().trim()
            val startDate = tvStartDate.text.toString()
            val endDate = tvEndDate.text.toString()

            if (country.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "나라와 도시를 모두 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startDate == "날짜를 선택하세요") {
                Toast.makeText(this, "출발일을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (endDate == "날짜를 선택하세요") {
                Toast.makeText(this, "귀국일을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (destinationLat == null || destinationLng == null) {
                Toast.makeText(this, "목적지 위치를 검색 중입니다...", Toast.LENGTH_SHORT).show()
                searchCityLocation(city) {
                    createBand(country, city, startDate, endDate)
                }
                return@setOnClickListener
            }

            createBand(country, city, startDate, endDate)
        }
    }

    private fun searchCityLocation(query: String, onComplete: (() -> Unit)? = null) {
        KakaoRetrofitClient.api.searchPlaces(
            auth = "KakaoAK ${BuildConfig.KAKAO_REST_KEY}",
            query = query
        ).enqueue(object : Callback<PlaceSearchResponse> {
            override fun onResponse(call: Call<PlaceSearchResponse>, response: Response<PlaceSearchResponse>) {
                val place = response.body()?.documents?.firstOrNull()
                if (place != null) {
                    destinationLat = place.y.toDoubleOrNull()
                    destinationLng = place.x.toDoubleOrNull()
                    onComplete?.invoke()
                } else {
                    Toast.makeText(this@CreateRoomActivity, "목적지를 찾을 수 없어요. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<PlaceSearchResponse>, t: Throwable) {
                android.util.Log.e("CreateRoom", "도시 검색 실패: ${t.message}")
            }
        })
    }

    private fun createBand(country: String, city: String, startDate: String, endDate: String) {
        val lat = destinationLat ?: return
        val lng = destinationLng ?: return

        val isKorea = lat in 33.0..39.0 && lng in 124.0..132.0
        val name = "$city 여행"
        val destination = "$country $city"

        val request = CreateBandRequest(
            name = name,
            startDate = startDate,
            endDate = endDate,
            destination = destination,
            destinationLat = lat,
            destinationLng = lng,
            countryCode = if (isKorea) "KR" else null,
            overseas = !isKorea
        )

        android.util.Log.d("CreateBand", "요청: name=$name, country=$country, city=$city, lat=$lat, lng=$lng, isKorea=$isKorea")

        RetrofitClient.api.createBand(request)
            .enqueue(object : Callback<CreateBandResponse> {
                override fun onResponse(call: Call<CreateBandResponse>, response: Response<CreateBandResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(this@CreateRoomActivity, "여행 방이 생성되었습니다!", Toast.LENGTH_SHORT).show()
                        val intent = android.content.Intent(this@CreateRoomActivity, SubActivity::class.java)
                        intent.putExtra("ROOM_NAME", name)
                        intent.putExtra("BAND_ID", body?.id)
                        intent.putExtra("INVITE_CODE", body?.inviteCode)
                        intent.putExtra("START_DATE", startDate)
                        intent.putExtra("END_DATE", endDate)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: ""
                        android.util.Log.e("CreateBand", "실패: ${response.code()} $errorBody")
                        Toast.makeText(this@CreateRoomActivity, "방 생성 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CreateBandResponse>, t: Throwable) {
                    Toast.makeText(this@CreateRoomActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
