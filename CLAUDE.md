# SyncTrip Android — CLAUDE.md

## 프로젝트 개요

- **앱명:** SyncTrip (그룹 여행 의사결정 + AI 일정 초안 생성)
- **패키지:** `com.example.synctrip`
- **백엔드:** Spring Boot — `C:\IntelliJprojects\SyncTrip-Spring`
- **인수인계 문서:** `docs/SyncTrip_인수인계문서_v6.md`
- **백엔드 구현현황:** `docs/SyncTrip_구현현황.md`
- **Android 구현현황:** `docs/Android_구현현황.md`

---

## ★ 절대 규칙: 문서 업데이트 (모든 작업 시 필수, 예외 없음)

### 1) Android 구현현황 문서 (`docs/Android_구현현황.md`)

코드를 추가·수정·삭제할 때마다 반드시 함께 업데이트한다.

- 인수인계 문서(v6)에 있는 기능 → ✅ 구현 / ⚠️ 부분 구현 / ❌ 미구현으로 상태 갱신
- 인수인계 문서에 없는 추가 기능 → ➕ 추가 구현으로 표시
- 각 기능에 구현일(YYYY-MM-DD) 기재
- 인수인계 문서와 다르게 결정된 사항은 **섹션 19** (설계 차이 기록)에 추가
- 미구현 기능 우선순위는 **섹션 16** 갱신
- 변경 이력(하단 표)에 날짜와 변경 내용 한 줄 추가
- 문서 맨 마지막 줄의 "마지막 수정" 날짜 갱신

### 2) Android 구현현황 문서 섹션 15 (`ApiService 현황`)

`ApiService.kt`에 엔드포인트를 추가하거나 실제 호출 위치가 바뀔 때마다 섹션 15를 함께 갱신한다.

- **구현된 엔드포인트 표**: 사용 위치(Activity/Fragment) 기재
- **백엔드에 있으나 ApiService에 없는 엔드포인트 표**: 추가하면 위 표로 이동, 우선순위 반영

---

## 프로젝트 구조

```
app/src/main/java/com/example/synctrip/
├── LoginActivity.kt          # 카카오/구글 로그인, 자동 로그인, 토큰 갱신
├── MainActivity.kt           # 그룹 목록, 방 만들기/참여, 딥링크 처리
├── CreateRoomActivity.kt     # 그룹 생성 (2단계: 여행지 선택 → 여행 정보)
├── SubActivity.kt            # 그룹 내 5탭 컨테이너 (하단 네비게이션)
├── PlaceSearchActivity.kt    # 장소 검색 (국내: 카카오, 해외: 서버 API)
├── TokenManager.kt           # JWT/refreshToken/userId SharedPreferences 관리
├── RetrofitClient.kt         # Retrofit + authInterceptor + 401 자동 refresh
├── ApiService.kt             # 백엔드 API Retrofit 인터페이스
├── KakaoRetrofitClient.kt    # 카카오 로컬 API Retrofit (dapi.kakao.com)
├── KakaoApiService.kt        # 카카오 장소 검색 API 인터페이스
├── VoteStompClient.kt        # WebSocket STOMP (실시간 투표)
├── adapter/                  # RecyclerView 어댑터
├── dto/                      # 데이터 클래스 (DTO)
└── fragment/
    ├── HomeFragment.kt       # 밴드 정보, 멤버, 장소, 초대, 상태 전환
    ├── VoteFragment.kt       # 스와이프 투표, WebSocket 실시간
    ├── ScheduleFragment.kt   # 일정 표시 (탭+리스트)
    ├── PassportFragment.kt   # 완료 여행 스탬프
    ├── MoneyFragment.kt      # 가계부 (더미 데이터, 미연동)
    └── PhotoFragment.kt      # 공유 앨범 (로컬만, 미업로드)
```

---

## 백엔드 API 경로 참조 (Spring Boot 기준)

> 전체 엔드포인트 및 Android 구현 상태는 `docs/Android_구현현황.md` 섹션 15 참조.

| 기능 | 메서드 | 경로 |
|---|---|---|
| 카카오 로그인 | POST | `/auth/kakao/login` |
| 구글 로그인 | POST | `/auth/google/login` |
| 토큰 갱신 | POST | `/auth/kakao/refresh` |
| 카카오 로그아웃 | POST | `/auth/kakao/logout` |
| 카카오 탈퇴 | DELETE | `/auth/kakao/withdraw` |
| 구글 로그아웃 | POST | `/auth/google/logout` |
| 구글 탈퇴 | DELETE | `/auth/google/withdraw` |
| 내 그룹 목록 | GET | `/api/bands` |
| 그룹 생성 | POST | `/api/bands` |
| 그룹 참여 | POST | `/api/bands/join` |
| 그룹 삭제 | DELETE | `/api/bands/{bandId}` |
| 멤버 목록 | GET | `/api/bands/{bandId}/members` |
| 초대 코드 조회 | POST | `/api/bands/{bandId}/invite-code` |
| 초대 코드 재발급 | POST | `/api/bands/{bandId}/invite-code/reissue` |
| Ready 전환 | POST | `/api/bands/{bandId}/ready` |
| Ready 취소 | DELETE | `/api/bands/{bandId}/ready` |
| 상태 진행 | POST | `/api/bands/{bandId}/status/advance` |
| 장바구니 조회 | GET | `/api/bands/{bandId}/picks` |
| 장바구니 추가 | POST | `/api/bands/{bandId}/picks` |
| 장바구니 삭제 | DELETE | `/api/bands/{bandId}/picks/{placeId}` |
| 장소 검색 | GET | `/api/bands/{bandId}/places/search` |
| 투표 대상 조회 | GET | `/api/bands/{bandId}/votes/places` |
| 투표 | POST | `/api/bands/{bandId}/votes` |
| 내 투표 현황 | GET | `/api/bands/{bandId}/votes/status` |
| 그룹 투표 현황 | GET | `/api/bands/{bandId}/votes/status/group` |
| 일정 조회 | GET | `/api/bands/{bandId}/schedule` |
| 일정 수동 생성 | POST | `/api/bands/{bandId}/schedule/generate` |
| 일정 대안 목록 | GET | `/api/bands/{bandId}/schedule/alts` |
| Plan B 추천 | POST | `/api/bands/{bandId}/schedule/plan-b` |
| 일정 교체 | POST | `/api/bands/{bandId}/schedule/swap` |
| 편집 락 시작 | POST | `/api/bands/{bandId}/schedule/edit/start` |
| 편집 락 해제 | POST | `/api/bands/{bandId}/schedule/edit/finish` |
| 지출 목록 | GET | `/api/bands/{bandId}/expenses` |
| 지출 추가 | POST | `/api/bands/{bandId}/expenses` |
| 지출 상세 | GET | `/api/bands/{bandId}/expenses/{expenseId}` |
| 지출 수정 | PUT | `/api/bands/{bandId}/expenses/{expenseId}` |
| 지출 삭제 | DELETE | `/api/bands/{bandId}/expenses/{expenseId}` |
| OCR 스캔 | POST | `/api/bands/{bandId}/expenses/ocr` |
| 정산 조회 | GET | `/api/bands/{bandId}/settlement` |
| 정산 요청 알림 | POST | `/api/bands/{bandId}/settlement/request` |
| 그룹 재정 | GET | `/api/bands/{bandId}/finance` |
| 기준 통화 변경 | PUT | `/api/bands/{bandId}/finance/currency` |
| 환율 갱신 | POST | `/api/bands/{bandId}/finance/rates/refresh` |
| 내 프로필 조회 | GET | `/api/users/me` |
| 프로필 수정 | PUT | `/api/users/me` |
| FCM 토큰 등록 | POST | `/api/users/fcm-token` |
| 알림 설정 조회 | GET | `/api/users/notification-settings` |
| 알림 설정 변경 | PATCH | `/api/users/notification-settings` |
| 알림 목록 | GET | `/api/notifications` |
| 미읽음 개수 | GET | `/api/notifications/unread-count` |
| 알림 읽음 | PATCH | `/api/notifications/{id}/read` |
| 전체 읽음 | PATCH | `/api/notifications/read-all` |
| 알림 삭제 | DELETE | `/api/notifications/{id}` |
| 알림 전체 삭제 | DELETE | `/api/notifications` |
| 인기 목적지 | GET | `/api/destinations/popular` |
| 목적지 검색 | GET | `/api/destinations/search` |
