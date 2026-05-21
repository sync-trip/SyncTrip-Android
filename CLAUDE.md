# SyncTrip Android — CLAUDE.md

## 프로젝트 개요

- **앱명:** SyncTrip (그룹 여행 의사결정 + AI 일정 초안 생성)
- **패키지:** `com.example.synctrip`
- **백엔드:** Spring Boot (`/api/bands/**`) — `C:\IntelliJprojects\SyncTrip-Spring`
- **백엔드 구현현황 참조:** `C:\IntelliJprojects\SyncTrip-And\docs\SyncTrip_구현현황.md`

---

## ★ 절대 규칙: 문서 업데이트 (모든 작업 시 필수, 예외 없음)

### 1) 구현 현황 문서 (`docs/Android_구현현황.md`)

코드를 추가·수정·삭제할 때마다 반드시 함께 업데이트한다.

- 인수인계 문서(v6)에 있는 기능 → ✅ 구현 / ⚠️ 부분 구현 / ❌ 미구현으로 상태 갱신
- 인수인계 문서에 없는 추가 기능 → ➕ 추가 구현으로 표시
- 각 기능에 구현일(YYYY-MM-DD) 기재
- 인수인계 문서와 다르게 결정된 사항은 14번 섹션에 추가
- 미구현 요약(11번 섹션)의 우선순위 갱신
- 변경 이력(하단 표)에 날짜와 변경 내용 한 줄 추가
- 문서 맨 마지막 줄의 "마지막 수정" 날짜 갱신

---

## 프로젝트 구조

```
app/src/main/java/com/example/synctrip/
├── LoginActivity.kt          # 카카오 로그인, 자동 로그인 체크
├── MainActivity.kt           # 그룹 목록, 방 만들기/참여 진입
├── CreateRoomActivity.kt     # 그룹 생성
├── SubActivity.kt            # 그룹 내 5탭 컨테이너
├── PlaceSearchActivity.kt    # KakaoMap + 장소 검색
├── TokenManager.kt           # JWT/userId SharedPreferences 관리
├── RetrofitClient.kt         # 백엔드 Retrofit 인스턴스
├── ApiService.kt             # 백엔드 API 인터페이스
├── KakaoRetrofitClient.kt    # 카카오 API Retrofit 인스턴스
├── KakaoApiService.kt        # 카카오 장소 검색 API 인터페이스
├── adapter/                  # RecyclerView 어댑터 6종
├── dto/                      # 데이터 클래스 (DTO)
└── fragment/                 # 5개 탭 프래그먼트
    ├── HomeFragment.kt       # 장소 담기, 상태 표시, 초대 코드
    ├── VoteFragment.kt       # 투표 화면
    ├── ScheduleFragment.kt   # 일정 표시 (탭+리스트)
    ├── MoneyFragment.kt      # 가계부
    └── PhotoFragment.kt      # 공유 앨범
```

---

## 핵심 현황 (코드 기준)

### 백엔드 통신 설정

| 항목 | 현재 상태 |
|---|---|
| Base URL | `https://test.sync-trip.app/` |
| JWT 인터셉터 | ❌ 없음 — 인증 필요 API 전부 401 |
| API 경로 | ❌ `/api/groups` (백엔드는 `/api/bands`) — 404 |
| Refresh Token | ❌ 미구현 |

### 데이터 현황

모든 핵심 Fragment(VoteFragment, ScheduleFragment, MoneyFragment, HomeFragment의 장소 목록)는 **하드코딩 더미 데이터**를 사용하며 서버 API 미연동 상태.

---

## 백엔드 API 경로 참조 (Spring Boot 기준)

| 기능 | 메서드 | 경로 |
|---|---|---|
| 카카오 로그인 | POST | `/auth/kakao/login` |
| 구글 로그인 | POST | `/auth/google/login` |
| 토큰 갱신 | POST | `/auth/kakao/refresh` |
| 로그아웃 | POST | `/auth/kakao/logout` |
| 내 그룹 목록 | GET | `/api/bands` |
| 그룹 생성 | POST | `/api/bands` |
| 그룹 참여 | POST | `/api/bands/join` |
| 초대 코드 조회 | POST | `/api/bands/{bandId}/invite-code` |
| Ready 전환 | POST | `/api/bands/{bandId}/ready` |
| 상태 진행 | POST | `/api/bands/{bandId}/status/advance` |
| 장바구니 추가 | POST | `/api/bands/{bandId}/picks` |
| 장바구니 조회 | GET | `/api/bands/{bandId}/picks` |
| 장바구니 삭제 | DELETE | `/api/bands/{bandId}/picks/{placeId}` |
| 장소 검색 | GET | `/api/bands/{bandId}/places/search` |
| 투표 대상 조회 | GET | `/api/bands/{bandId}/votes/places` |
| 투표 | POST | `/api/bands/{bandId}/votes` |
| 일정 조회 | GET | `/api/bands/{bandId}/schedule` |
| Plan B 추천 | POST | `/api/bands/{bandId}/schedule/plan-b` |
| 일정 교체 | POST | `/api/bands/{bandId}/schedule/swap` |
| 지출 목록 | GET | `/api/bands/{bandId}/expenses` |
| 지출 추가 | POST | `/api/bands/{bandId}/expenses` |
| OCR 스캔 | POST | `/api/bands/{bandId}/expenses/ocr` |
| 정산 조회 | GET | `/api/bands/{bandId}/settlement` |
| FCM 토큰 등록 | POST | `/api/users/fcm-token` |
| 알림 목록 | GET | `/api/notifications` |
