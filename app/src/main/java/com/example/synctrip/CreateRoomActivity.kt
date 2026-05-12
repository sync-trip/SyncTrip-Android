package com.example.synctrip

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.synctrip.dto.CreateGroupRequest
import com.example.synctrip.dto.CreateGroupResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class CreateRoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val btnCreateDone = findViewById<android.widget.Button>(R.id.btnCreateDone)
        val tvStartDate = findViewById<android.widget.TextView>(R.id.tvStartDate)
        val tvEndDate = findViewById<android.widget.TextView>(R.id.tvEndDate)
        val rgTravelStyle = findViewById<android.widget.RadioGroup>(R.id.rgTravelStyle)

        // 시작일 선택
        tvStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                tvStartDate.text = "${year}-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 종료일 선택
        tvEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                tvEndDate.text = "${year}-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnCreateDone.setOnClickListener {
            val roomName = findViewById<android.widget.EditText>(R.id.etRoomName).text.toString()
            val country = findViewById<android.widget.EditText>(R.id.etCountry).text.toString()
            val city = findViewById<android.widget.EditText>(R.id.etCity).text.toString()
            val memberCount = findViewById<android.widget.EditText>(R.id.etMemberCount).text.toString()
            val startDate = tvStartDate.text.toString()
            val endDate = tvEndDate.text.toString()
            val travelStyle = if (rgTravelStyle.checkedRadioButtonId == R.id.rbRelaxed) "RELAXED" else "PACKED"

            // 입력값 검증
            if (roomName.isEmpty() || country.isEmpty() || city.isEmpty() || memberCount.isEmpty()
                || startDate == "시작일 선택" || endDate == "종료일 선택") {
                android.widget.Toast.makeText(this, "모든 항목을 입력해주세요!", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 서버에 그룹 생성 요청
            val request = CreateGroupRequest(
                title = roomName,
                destination = "$country $city",
                maxMembers = memberCount.toInt(),
                startDate = startDate,
                endDate = endDate,
                travelStyle = travelStyle
            )

            RetrofitClient.api.createGroup(request)
                .enqueue(object : Callback<CreateGroupResponse> {
                    override fun onResponse(call: Call<CreateGroupResponse>, response: Response<CreateGroupResponse>) {
                        if (response.isSuccessful) {
                            val groupId = response.body()?.groupId
                            val inviteCode = response.body()?.inviteCode
                            android.util.Log.d("CreateGroup", "그룹 생성 성공! groupId: $groupId, inviteCode: $inviteCode")
                            android.widget.Toast.makeText(this@CreateRoomActivity, "여행 방이 생성되었습니다!", android.widget.Toast.LENGTH_SHORT).show()

                            val intent = android.content.Intent(this@CreateRoomActivity, SubActivity::class.java)
                            intent.putExtra("ROOM_NAME", roomName)
                            intent.putExtra("GROUP_ID", groupId)
                            intent.putExtra("INVITE_CODE", inviteCode)
                            startActivity(intent)
                            finish()
                        } else {
                            android.widget.Toast.makeText(this@CreateRoomActivity, "방 생성 실패: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CreateGroupResponse>, t: Throwable) {
                        android.util.Log.e("CreateGroup", "서버 연결 실패: ${t.message}")
                        android.widget.Toast.makeText(this@CreateRoomActivity, "서버 연결 실패!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}