package com.example.synctrip

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.synctrip.dto.kakao.GoogleLoginRequest
import com.example.synctrip.dto.kakao.KakaoLoginRequest
import com.example.synctrip.dto.kakao.KakaoLoginResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.appcompat.app.AppCompatDelegate
import com.example.synctrip.dto.notification.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.kakao.vectormap.KakaoMapSdk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken == null) {
                Toast.makeText(this, "Google ID Token을 받지 못했어요", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            sendGoogleTokenToServer(idToken)
        } catch (e: ApiException) {
            android.util.Log.e("GoogleLogin", "구글 로그인 실패: ${e.statusCode} ${e.message}")
            Toast.makeText(this, "구글 로그인 실패 (${e.statusCode})", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        RetrofitClient.init(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (TokenManager.isLoggedIn(this)) {
            if (TokenManager.isAccessTokenExpired(this)) {
                val refreshToken = TokenManager.getRefreshToken(this)
                if (refreshToken != null) refreshAndGoToMain(refreshToken) else goToMain()
            } else {
                goToMain()
            }
            return
        }

        findViewById<Button>(R.id.btnKakaoLogin).setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) Toast.makeText(this, "카카오톡 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    else if (token != null) sendKakaoTokenToServer(token.accessToken)
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                    if (error != null) Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    else if (token != null) sendKakaoTokenToServer(token.accessToken)
                }
            }
        }

        findViewById<Button>(R.id.btnGoogleLogin).setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    }

    private fun sendKakaoTokenToServer(accessToken: String) {
        android.util.Log.d("KakaoToken", "서버로 카카오 토큰 전송")
        RetrofitClient.api.kakaoLogin(KakaoLoginRequest(accessToken))
            .enqueue(object : Callback<KakaoLoginResponse> {
                override fun onResponse(call: Call<KakaoLoginResponse>, response: Response<KakaoLoginResponse>) {
                    android.util.Log.d("KakaoToken", "응답: ${response.code()}")
                    if (response.isSuccessful) {
                        saveLoginAndGoToMain(response.body())
                    } else {
                        android.util.Log.e("KakaoToken", "실패: ${response.errorBody()?.string()}")
                        Toast.makeText(this@LoginActivity, "서버 로그인 에러: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    android.util.Log.e("KakaoToken", "서버 연결 실패: ${t.message}")
                    Toast.makeText(this@LoginActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendGoogleTokenToServer(idToken: String) {
        android.util.Log.d("GoogleLogin", "서버로 구글 ID Token 전송")
        RetrofitClient.api.googleLogin(GoogleLoginRequest(idToken))
            .enqueue(object : Callback<KakaoLoginResponse> {
                override fun onResponse(call: Call<KakaoLoginResponse>, response: Response<KakaoLoginResponse>) {
                    android.util.Log.d("GoogleLogin", "응답: ${response.code()}")
                    if (response.isSuccessful) {
                        saveLoginAndGoToMain(response.body())
                    } else {
                        android.util.Log.e("GoogleLogin", "실패: ${response.errorBody()?.string()}")
                        Toast.makeText(this@LoginActivity, "구글 로그인 서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    android.util.Log.e("GoogleLogin", "서버 연결 실패: ${t.message}")
                    Toast.makeText(this@LoginActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveLoginAndGoToMain(body: KakaoLoginResponse?) {
        body ?: return
        if (body.accessToken.isNotEmpty() && body.userId > 0) {
            TokenManager.saveLoginResponse(
                this,
                body.accessToken,
                body.refreshToken,
                body.userId,
                body.accessTokenExpiresIn
            )
            registerFcmToken()
        }
        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
        goToMain()
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            RetrofitClient.api.registerFcmToken(FcmTokenRequest(token))
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
        }
    }

    private fun refreshAndGoToMain(refreshToken: String) {
        RetrofitClient.api.refreshToken(com.example.synctrip.dto.kakao.TokenRefreshRequest(refreshToken))
            .enqueue(object : Callback<KakaoLoginResponse> {
                override fun onResponse(call: Call<KakaoLoginResponse>, response: Response<KakaoLoginResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        TokenManager.saveLoginResponse(this@LoginActivity, body.accessToken, body.refreshToken, body.userId, body.accessTokenExpiresIn)
                        goToMain()
                    } else {
                        TokenManager.clear(this@LoginActivity)
                    }
                }
                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    goToMain()
                }
            })
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
