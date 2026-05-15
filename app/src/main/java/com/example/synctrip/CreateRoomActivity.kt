package com.example.synctrip

import android.app.DatePickerDialog
import android.os.Bundle
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

        val btnCreateDone = findViewById<android.widget.Button>(R.id.btnCreateDone)
        val tvStartDate = findViewById<android.widget.TextView>(R.id.tvStartDate)
        val tvEndDate = findViewById<android.widget.TextView>(R.id.tvEndDate)
        val etCity = findViewById<android.widget.EditText>(R.id.etCity)

        etCity.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val city = etCity.text.toString()
                if (city.isNotEmpty()) {
                    searchCityLocation(city)
                }
            }
        }

        tvStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                tvStartDate.text = "${year}-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                tvEndDate.text = "${year}-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnCreateDone.setOnClickListener {
            val country = findViewById<android.widget.EditText>(R.id.etCountry).text.toString().trim()
            val city = etCity.text.toString().trim()
            val name = findViewById<android.widget.EditText>(R.id.etRoomName).text.toString().trim()
            val startDate = tvStartDate.text.toString()
            val endDate = tvEndDate.text.toString()

            android.util.Log.d("CreateRoom", "버튼 클릭 - name=$name, country=$country, city=$city, start=$startDate, end=$endDate, lat=$destinationLat, lng=$destinationLng")

            if (name.isEmpty() || country.isEmpty() || city.isEmpty()
                || startDate == "시작일 선택" || endDate == "종료일 선택") {
                android.util.Log.d("CreateRoom", "유효성 검사 실패")
                android.widget.Toast.makeText(this, "모든 항목을 입력해주세요!", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isKorea = country == "한국" || country.lowercase() == "korea"
            val countryCode = if (isKorea) "KR" else null
            val overseas = !isKorea

            if (destinationLat == null || destinationLng == null) {
                android.util.Log.d("CreateRoom", "lat/lng null → 도시 검색 시작")
                android.widget.Toast.makeText(this, "도시 위치를 검색 중입니다...", android.widget.Toast.LENGTH_LONG).show()
                searchCityLocation(city) {
                    createBand(name, country, city, startDate, endDate, countryCode, overseas)
                }
                return@setOnClickListener
            }

            android.util.Log.d("CreateRoom", "createBand 호출")
            createBand(name, country, city, startDate, endDate, countryCode, overseas)
        }
    }

    private fun searchCityLocation(city: String, onComplete: (() -> Unit)? = null) {
        KakaoRetrofitClient.api.searchPlaces(
            auth = "KakaoAK ${BuildConfig.KAKAO_REST_KEY}",
            query = city
        ).enqueue(object : Callback<PlaceSearchResponse> {
            override fun onResponse(call: Call<PlaceSearchResponse>, response: Response<PlaceSearchResponse>) {
                if (response.isSuccessful) {
                    val place = response.body()?.documents?.firstOrNull()
                    if (place != null) {
                        destinationLat = place.y.toDoubleOrNull()
                        destinationLng = place.x.toDoubleOrNull()
                        android.util.Log.d("CreateRoom", "도시 위치 검색 성공: lat=$destinationLat, lng=$destinationLng")
                        onComplete?.invoke()
                    } else {
                        android.widget.Toast.makeText(this@CreateRoomActivity, "도시를 찾을 수 없어요. 다시 입력해주세요.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<PlaceSearchResponse>, t: Throwable) {
                android.util.Log.e("CreateRoom", "도시 검색 실패: ${t.message}")
            }
        })
    }

    private fun createBand(
        name: String,
        country: String,
        city: String,
        startDate: String,
        endDate: String,
        countryCode: String?,
        overseas: Boolean
    ) {
        val request = CreateBandRequest(
            name = name,
            startDate = startDate,
            endDate = endDate,
            destination = "$country $city",
            destinationLat = destinationLat!!,
            destinationLng = destinationLng!!,
            countryCode = countryCode,
            overseas = overseas
        )

        val token = TokenManager.getToken(this)
        android.util.Log.d("CreateBand", "토큰: ${if (token != null) "있음(${token.take(20)}...)" else "없음(null!)"}")
        android.util.Log.d("CreateBand", "요청 데이터: name=$name, start=$startDate, end=$endDate, dest=${request.destination}, lat=${request.destinationLat}, lng=${request.destinationLng}, countryCode=$countryCode, overseas=$overseas")

        RetrofitClient.api.createBand(request)
            .enqueue(object : Callback<CreateBandResponse> {
                override fun onResponse(call: Call<CreateBandResponse>, response: Response<CreateBandResponse>) {
                    if (response.isSuccessful) {
                        val bandId = response.body()?.id
                        val inviteCode = response.body()?.inviteCode
                        android.util.Log.d("CreateBand", "밴드 생성 성공! bandId: $bandId, inviteCode: $inviteCode")
                        android.widget.Toast.makeText(this@CreateRoomActivity, "여행 방이 생성되었습니다!", android.widget.Toast.LENGTH_SHORT).show()

                        val intent = android.content.Intent(this@CreateRoomActivity, SubActivity::class.java)
                        intent.putExtra("ROOM_NAME", name)
                        intent.putExtra("BAND_ID", bandId)
                        intent.putExtra("INVITE_CODE", inviteCode)
                        intent.putExtra("START_DATE", startDate)
                        intent.putExtra("END_DATE", endDate)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "없음"
                        android.util.Log.e("CreateBand", "방 생성 실패 - code=${response.code()}, error=$errorBody")
                        android.widget.Toast.makeText(this@CreateRoomActivity, "방 생성 실패: ${response.code()}\n$errorBody", android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CreateBandResponse>, t: Throwable) {
                    android.util.Log.e("CreateBand", "서버 연결 실패: ${t.message}")
                    android.widget.Toast.makeText(this@CreateRoomActivity, "서버 연결 실패!", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
    }
}
