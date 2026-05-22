# SyncTrip Android 구현현황 문서
**작성일:** 2026-05-22 | **마지막 수정:** 2026-05-22 (v2.1 — Skybound Wanderer UI 테마 적용)
**참조:** 전체 Android 소스코드 직독(3회 이상) + Spring Boot 백엔드 컨트롤러 코드 대조

> 이 문서는 `com.example.synctrip` 앱의 모든 Activity, Fragment, Adapter, DTO, 인프라 파일을 직접 읽어 작성한 현황 정리서입니다.
> 백엔드 API 경로는 Spring Boot 컨트롤러 코드 기준으로 검증하였습니다.

---

## 범례
| 기호 | 의미 |
|------|------|
| ✅ | 구현 완료 (코드에서 확인됨) |
| ⚠️ | 부분 구현 (동작하나 완전하지 않음) |
| ❌ | 미구현 (코드 없음) |
| ➕ | 추가 구현 (인수인계 문서에 없던 기능) |

---

## 1. 인증 / 로그인 (`LoginActivity.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 카카오톡 로그인 | ✅ | 2026-05 | `UserApiClient.loginWithKakaoTalk` → `POST auth/kakao/login` |
| 카카오 계정 로그인 (폴백) | ✅ | 2026-05 | `loginWithKakaoAccount` |
| 구글 로그인 | ✅ | 2026-05 | `play-services-auth:21.2.0`, ID Token → `POST auth/google/login` |
| JWT 저장 (access + refresh + 만료시각) | ✅ | 2026-05 | `TokenManager.saveLoginResponse()` |
| 자동 로그인 (앱 재진입 시) | ✅ | 2026-05 | `isLoggedIn()` + `isAccessTokenExpired()` 체크 |
| 토큰 만료 시 자동 갱신 후 메인 이동 | ✅ | 2026-05 | `refreshAndGoToMain()` → `POST auth/kakao/refresh` |
| 로그아웃 (메인화면 버튼) | ⚠️ | 2026-05 | `TokenManager.clear()`만 수행, 서버 `POST auth/kakao/logout` 미호출 |
| 회원탈퇴 | ❌ | - | `DELETE auth/kakao/withdraw` API 정의만 있음, UI 없음 |

---

## 2. 인프라 / 공통 (`RetrofitClient.kt`, `TokenManager.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| JWT 자동 첨부 (`Authorization: Bearer`) | ✅ | 2026-05 | `authInterceptor` — 모든 요청에 적용 |
| 401 응답 시 refresh → 재시도 | ✅ | 2026-05 | `sessionInterceptor` — refresh 성공 시 원래 요청 재전송 |
| refresh 실패 시 로그인 화면 이동 | ✅ | 2026-05 | `redirectToLogin()` — TokenManager.clear() + LoginActivity |
| 카카오 로컬 API 클라이언트 | ✅ | 2026-05 | `KakaoRetrofitClient` (베이스: `https://dapi.kakao.com/`) |
| API Key 빌드 시 주입 | ✅ | 2026-05 | `local.properties` → `BuildConfig` (KAKAO_NATIVE/REST_KEY, GOOGLE_WEB_CLIENT_ID) |

---

## 3. 메인 화면 (`MainActivity.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 내 밴드 목록 조회 | ✅ | 2026-05 | `GET api/bands` — 가로 스크롤 RecyclerView |
| 방 만들기 버튼 | ✅ | 2026-05 | `CreateRoomActivity` 이동 |
| 초대 코드로 참여 | ✅ | 2026-05 | `POST api/bands/join` — 다이얼로그 입력 |
| 딥링크 참여 처리 | ➕ | 2026-05 | `handleDeepLink()` — `?code=` 파라미터 처리, 앱 꺼진 상태에서도 동작 |
| 밴드 삭제 (방장) | ✅ | 2026-05 | `DELETE api/bands/{bandId}` — 팝업 메뉴 → 확인 다이얼로그 |
| 로그아웃 버튼 | ✅ | 2026-05 | 확인 다이얼로그 → `TokenManager.clear()` → `LoginActivity` |
| 빈 목록 UI | ✅ | 2026-05 | `tvEmpty` 표시/숨김 |
| SwipeRefresh 새로고침 | ✅ | 2026-05 | `SwipeRefreshLayout` |
| 에러 응답 처리 (401/403/404/409/410) | ✅ | 2026-05 | 각 코드별 토스트 메시지 |

---

## 4. 방 만들기 (`CreateRoomActivity.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 2단계 UI (여행지 선택 → 여행 정보) | ✅ | 2026-05 | 단계별 `View.VISIBLE/GONE` 전환 |
| 해외 / 국내 탭 전환 | ✅ | 2026-05 | `TabLayout` |
| 지역별 칩 필터 | ✅ | 2026-05 | 일본/동남아/유럽/미주-오세아니아/중화권/국내 |
| 인기 도시 필터 | ✅ | 2026-05 | `TOP_PICKS` (10개) |
| 로컬 여행지 검색 | ✅ | 2026-05 | `DestinationCatalog.search()` |
| 여행지 목록 (로컬 카탈로그) | ✅ | 2026-05 | 해외 18개 + 국내 8개 = 총 26개 도시 |
| 날짜 선택 (DatePicker) | ✅ | 2026-05 | 출발일/귀국일, 유효성 검사 포함 |
| 여행 스타일 선택 (RELAXED/PACKED) | ✅ | 2026-05 | 카드 UI, 시각 피드백 포함 |
| 밴드명 입력 + 유효성 검사 | ✅ | 2026-05 | 빈 칸/날짜 오류 인라인 표시 |
| 방 생성 API 호출 | ✅ | 2026-05 | `POST api/bands` — `CreateBandRequest` (이름/날짜/목적지/lat/lng/countryCode/overseas/style) |
| 생성 후 SubActivity 이동 | ✅ | 2026-05 | bandId, inviteCode 등 인텐트 전달 |
| 로딩 오버레이 | ✅ | 2026-05 | 생성 중 버튼 비활성화 + 오버레이 표시 |

---

## 5. 밴드 내부 화면 (`SubActivity.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 하단 네비게이션 (5탭) | ✅ | 2026-05 | 홈/일정/패스포트/머니/포토 |
| 뒤로가기 백스택 처리 | ✅ | 2026-05 | VoteFragment 백스택 추가 처리 |
| 홈에서 투표 탭 전환 (`switchToVoteTab`) | ✅ | 2026-05 | 백스택에 올려서 뒤로가기로 복귀 가능 |
| 밴드 상태 업데이트 전달 | ✅ | 2026-05 | `updateBandStatus()` — 프래그먼트 → 액티비티 상태 동기화 |

---

## 6. 홈 프래그먼트 (`HomeFragment.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 밴드 기본 정보 표시 (이름/날짜) | ✅ | 2026-05 | 인텐트로 전달받은 정보 |
| 여행지 썸네일 이미지 (Glide) | ✅ | 2026-05 | `DestinationCatalog`에서 URL 매핑 |
| 멤버 목록 조회 | ✅ | 2026-05 | `GET api/bands/{bandId}/members` — 가로 RecyclerView |
| 멤버 수 뱃지 | ✅ | 2026-05 | `tvMemberCountBadge` |
| 멤버 Ready 상태 배지 | ✅ | 2026-05 | `MemberAdapter`에서 isReady 표시 |
| 담은 장소 목록 조회 | ✅ | 2026-05 | `GET api/bands/{bandId}/picks` |
| 담은 장소 삭제 | ✅ | 2026-05 | `DELETE api/bands/{bandId}/picks/{placeId}` |
| 장소 담기 진행 상태 (프로그레스바) | ✅ | 2026-05 | `currentCount / maxCount` |
| 장소 담기 버튼 → `PlaceSearchActivity` 이동 | ✅ | 2026-05 | overseas 플래그 전달 |
| 방장 전용: 초대코드 조회 | ✅ | 2026-05 | `POST api/bands/{bandId}/invite-code` |
| 방장 전용: 초대코드 복사 | ✅ | 2026-05 | 클립보드 복사 |
| 방장 전용: 초대링크 공유 | ✅ | 2026-05 | `inviteShareLink` → `Intent.ACTION_SEND` |
| 방장 전용: 밴드 상태 전환 버튼 | ✅ | 2026-05 | `POST api/bands/{bandId}/status/advance` |
| 비방장: 준비완료 / 취소 | ✅ | 2026-05 | `POST/DELETE api/bands/{bandId}/ready` |
| 상태별 UI 변화 | ✅ | 2026-05 | PLANNING / VOTING / GENERATING / TRAVELLING / DONE |
| GENERATING 상태 폴링 | ✅ | 2026-05 | 3초 간격 → 일정 생성 완료 시 자동 UI 전환 |
| PLANNING 중 멤버 폴링 | ✅ | 2026-05 | 5초 간격 — 새 멤버 참여 감지 |
| picks 403 응답 시 VOTING 상태 전환 | ✅ | 2026-05 | 상태 API 없이 picks 403으로 상태 감지 |
| SwipeRefresh | ✅ | 2026-05 | 멤버 + 장소 새로고침 |
| 초대코드 재발급 | ❌ | - | `POST api/bands/{bandId}/invite-code/reissue` API 정의만 있음 |

---

## 7. 장소 검색 (`PlaceSearchActivity.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 국내 장소 검색 (카카오 로컬 API) | ✅ | 2026-05 | `KakaoApiService` — `v2/local/search/keyword.json` |
| 해외 장소 검색 (서버 API) | ✅ | 2026-05 | `GET api/bands/{bandId}/places/search?keyword=&category=` |
| 카테고리 칩 필터 (5종) | ✅ | 2026-05 | FOOD / CULTURE / ACTIVITY / SHOPPING / NATURE |
| 국내 카테고리 클라이언트 필터링 | ✅ | 2026-05 | `matchesCategory()` — 카카오 카테고리명 매핑 |
| 해외 카테고리 서버 필터링 | ✅ | 2026-05 | category 쿼리 파라미터로 전달 |
| 장소 담기 | ✅ | 2026-05 | `POST api/bands/{bandId}/picks` |
| 담기 시 최대 개수 제한 표시 | ✅ | 2026-05 | `maxPickCount` 로드 후 초과 시 안내 |
| 이미 담은 장소 중복 방지 | ✅ | 2026-05 | `pickedExternalIds` Set으로 추적 |
| 담은 장소 수 뱃지 | ✅ | 2026-05 | `tvPickCount` |
| 장소 상세 외부 연결 | ✅ | 2026-05 | 국내→카카오지도, 해외→구글지도 |
| 로딩 오버레이 | ✅ | 2026-05 | 검색 중 표시 |
| 에러 응답 처리 (403/409/400) | ✅ | 2026-05 | 각 코드별 안내 토스트 |

---

## 8. 투표 프래그먼트 (`VoteFragment.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 투표 장소 목록 조회 | ✅ | 2026-05 | `GET api/bands/{bandId}/votes/places` |
| 좋아요 / 별로에요 투표 | ✅ | 2026-05 | `POST api/bands/{bandId}/votes` — result: 1/-1 |
| 내 투표 현황 조회 | ✅ | 2026-05 | `GET api/bands/{bandId}/votes/status` |
| 내가 담은 장소 자동 좋아요 | ✅ | 2026-05 | `myBookmark == true`인 장소 진입 시 `autoLike()` |
| 투표 완료 표시 | ✅ | 2026-05 | `myVotedCount >= totalPlaces` 시 완료 메시지 |
| 실시간 WebSocket (STOMP) 투표 현황 | ✅ | 2026-05 | `VoteStompClient` — `wss://test.sync-trip.app/ws` → `/topic/bands/{bandId}/votes` |
| JWT로 WebSocket 인증 | ✅ | 2026-05 | STOMP CONNECT 프레임에 `Authorization: Bearer` 헤더 |
| 투표 전 단계 에러 처리 (403) | ✅ | 2026-05 | "아직 투표 단계 아님" 안내 |
| 그룹 전체 투표 현황 | ❌ | - | `GET api/bands/{bandId}/votes/status/group` API 정의만 있음 |
| SwipeRefresh | ✅ | 2026-05 | 목록 초기화 + 재로드 |

---

## 9. 일정 프래그먼트 (`ScheduleFragment.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 일정 조회 | ✅ | 2026-05 | `GET api/bands/{bandId}/schedule` |
| 일차별 탭 UI | ✅ | 2026-05 | `TabLayout` — 탭 동적 생성 |
| 시간 / 장소명 / 이동시간 표시 | ✅ | 2026-05 | `ScheduleAdapter` |
| 빈 상태 / 에러 메시지 | ✅ | 2026-05 | `tvScheduleEmpty` |
| SwipeRefresh | ✅ | 2026-05 | |
| 일정 대안 조회 | ❌ | - | `GET api/bands/{bandId}/schedule/alts` API 정의만 있음 |
| 일정 편집 (plan-b, swap) | ❌ | - | 백엔드에 있으나 안드로이드 미구현 |
| 일정 수정 모드 (edit/start, finish) | ❌ | - | 미구현 |

---

## 10. 패스포트 프래그먼트 (`PassportFragment.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 완료 여행 스탬프 표시 | ✅ | 2026-05 | `getMyBands()` 필터링 (status == "DONE") |
| 2열 그리드 UI | ✅ | 2026-05 | `GridLayoutManager(2)` |
| 완료 여행 수 표시 | ✅ | 2026-05 | `tvStampCount` |
| 빈 상태 UI | ✅ | 2026-05 | `layoutEmpty` |
| SwipeRefresh | ✅ | 2026-05 | |

---

## 11. 머니 프래그먼트 (`MoneyFragment.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 지출 목록 UI | ⚠️ | 2026-05 | `ExpenseAdapter`로 표시하지만 **더미 데이터**만 사용 |
| 지출 등록 서버 연동 | ❌ | - | 미구현 |
| 지출 조회 서버 연동 | ❌ | - | `ExpenseController` 백엔드 있으나 안드로이드 미연동 |
| 정산 기능 | ❌ | - | `SettlementController` 백엔드 있으나 안드로이드 미구현 |

---

## 12. 포토 프래그먼트 (`PhotoFragment.kt`)

| 기능 | 상태 | 구현일 | 비고 |
|------|------|--------|------|
| 갤러리에서 사진 선택 | ✅ | 2026-05 | `ActivityResultContracts.StartActivityForResult` |
| 3열 그리드 사진 표시 | ✅ | 2026-05 | `GridLayoutManager(3)` |
| 사진 서버 업로드 | ❌ | - | 로컬 표시만, 서버 저장 없음 |
| 사진 조회 서버 연동 | ❌ | - | 미구현 |
| 사진 확대 보기 | ❌ | - | TODO 주석 있음 |

---

## 13. DTO 및 데이터 모델 현황

### 구현된 DTO
| DTO | 파일 | 비고 |
|-----|------|------|
| `KakaoLoginResponse` | `dto/kakao/` | userId, email, name, profileImageUrl, newUser, accessToken, refreshToken, 만료시각 |
| `KakaoLoginRequest` / `GoogleLoginRequest` | `dto/kakao/` | |
| `TokenRefreshRequest` | `dto/kakao/` | |
| `BandSummary` | `dto/group/` | id, name, destination, startDate, endDate, inviteCode, status, isOwner, isOverseas, travelStyle |
| `CreateBandRequest` | `dto/group/` | 이름/날짜/목적지/lat/lng/countryCode/overseas/travelStyle |
| `BandMemberResponse` | `dto/group/` | userId, name, profileImageUrl, role, isReady |
| `BandInviteCodeResponse` | `dto/group/` | inviteCode, inviteShareLink, inviteDeepLink |
| `PlacePickRequest` / `PlacePickResponse` / `PlacePickListResponse` | `dto/group/` | |
| `VotePlaceResponse` | `dto/vote/` | placeId, name, category, address, rating, thumbnailUrl, myBookmark |
| `VoteRequest` | `dto/vote/` | placeId, result (1/-1) |
| `VoteStatusResponse` | `dto/vote/` | totalPlaces, myVotedCount, myComplete |
| `GroupVoteStatusResponse` | `dto/vote/` | totalPlaces, totalVotingMembers, memberStatuses |
| `ScheduleResponse` | `dto/schedule/` | bandId, startDate, endDate, days |
| `ScheduleAltResponse` | `dto/schedule/` | scheduleAltId, category, priorityScore, place |
| `BandReadyResponse` / `BandStatusTransitionResponse` | `dto/band/` | |
| `DestinationResponse` / `DestinationCatalog` | `dto/destination/` | 26개 도시, 지역/국기/썸네일 |
| `PlaceSearchResult` | `dto/place/` | |
| `PlaceSearchResponse` / `PlaceDocument` | `dto/kakao/` | 카카오 로컬 API 응답 |

### 미구현 DTO
| 필요 DTO | 관련 기능 |
|----------|-----------|
| ExpenseRequest/Response | 지출 서버 연동 |
| SettlementResponse | 정산 |
| NotificationResponse | 알림 |
| UserProfileResponse | 사용자 프로필 |

---

## 14. 주요 의존성 (build.gradle.kts)

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| `kakao.sdk:v2-user` | 2.20.1 | 카카오 로그인 |
| `kakao.maps.open:android` | 2.12.8 | 카카오 지도 |
| `play-services-auth` | 21.2.0 | 구글 로그인 |
| `retrofit2:retrofit` | 2.9.0 | HTTP 클라이언트 |
| `retrofit2:converter-gson` | 2.9.0 | JSON 파싱 |
| `okhttp3:okhttp` | 4.12.0 | HTTP + WebSocket |
| `glide:glide` | 4.16.0 | 이미지 로딩 |
| `swiperefreshlayout` | 1.1.0 | 당겨서 새로고침 |
| `material` | 1.11.0 | Material Design |
| minSdk | 24 | Android 7.0+ |
| targetSdk | 36 | |

---

## 15. ApiService 현황 (`ApiService.kt`)

### 구현된 엔드포인트
| 구분 | 엔드포인트 | 사용 위치 |
|------|-----------|-----------|
| 인증 | `POST auth/kakao/login` | LoginActivity |
| 인증 | `POST auth/google/login` | LoginActivity |
| 인증 | `POST auth/kakao/refresh` | LoginActivity, RetrofitClient |
| 인증 | `POST auth/kakao/logout` | 미사용 (정의만) |
| 인증 | `DELETE auth/kakao/withdraw` | 미사용 (정의만) |
| 목적지 | `GET api/destinations/popular` | 미사용 (정의만) |
| 목적지 | `GET api/destinations/search` | 미사용 (정의만) |
| 밴드 | `GET api/bands` | MainActivity, HomeFragment, PassportFragment |
| 밴드 | `POST api/bands` | CreateRoomActivity |
| 밴드 | `POST api/bands/join` | MainActivity |
| 밴드 | `DELETE api/bands/{bandId}` | MainActivity |
| 밴드 | `POST api/bands/{bandId}/invite-code` | HomeFragment |
| 밴드 | `POST api/bands/{bandId}/invite-code/reissue` | 미사용 (정의만) |
| 밴드 | `GET api/bands/{bandId}/members` | HomeFragment |
| 밴드 | `POST api/bands/{bandId}/ready` | HomeFragment |
| 밴드 | `DELETE api/bands/{bandId}/ready` | HomeFragment |
| 밴드 | `POST api/bands/{bandId}/status/advance` | HomeFragment |
| 장소 | `GET api/bands/{bandId}/places/search` | PlaceSearchActivity |
| 픽 | `GET api/bands/{bandId}/picks` | HomeFragment, PlaceSearchActivity |
| 픽 | `POST api/bands/{bandId}/picks` | PlaceSearchActivity |
| 픽 | `DELETE api/bands/{bandId}/picks/{placeId}` | HomeFragment |
| 투표 | `GET api/bands/{bandId}/votes/places` | VoteFragment |
| 투표 | `POST api/bands/{bandId}/votes` | VoteFragment |
| 투표 | `GET api/bands/{bandId}/votes/status` | VoteFragment |
| 투표 | `GET api/bands/{bandId}/votes/status/group` | 미사용 (정의만) |
| 일정 | `POST api/bands/{bandId}/schedule/generate` | 미사용 (정의만) |
| 일정 | `GET api/bands/{bandId}/schedule` | ScheduleFragment, HomeFragment |
| 일정 | `GET api/bands/{bandId}/schedule/alts` | 미사용 (정의만) |

### 백엔드에 있으나 ApiService에 없는 엔드포인트
| 엔드포인트 | 백엔드 컨트롤러 | 우선순위 |
|-----------|----------------|---------|
| `GET api/bands/{bandId}/expenses` | ExpenseController | 높음 |
| `POST api/bands/{bandId}/expenses` | ExpenseController | 높음 |
| `GET api/bands/{bandId}/expenses/{expenseId}` | ExpenseController | 높음 |
| `PUT api/bands/{bandId}/expenses/{expenseId}` | ExpenseController | 높음 |
| `DELETE api/bands/{bandId}/expenses/{expenseId}` | ExpenseController | 높음 |
| `POST api/bands/{bandId}/expenses/ocr` | ExpenseController | 높음 |
| `GET api/bands/{bandId}/settlement` | SettlementController | 높음 |
| `POST api/bands/{bandId}/settlement/request` | SettlementController | 높음 |
| `POST auth/google/logout` | GoogleAuthController | 중간 |
| `DELETE auth/google/withdraw` | GoogleAuthController | 중간 |
| `GET api/users/me` | UserController | 중간 |
| `PUT api/users/me` | UserController | 중간 |
| `POST api/users/fcm-token` | NotificationController | 중간 |
| `GET api/users/notification-settings` | NotificationController | 중간 |
| `PATCH api/users/notification-settings` | NotificationController | 중간 |
| `GET api/notifications` | NotificationController | 낮음 |
| `GET api/notifications/unread-count` | NotificationController | 낮음 |
| `PATCH api/notifications/{id}/read` | NotificationController | 낮음 |
| `PATCH api/notifications/read-all` | NotificationController | 낮음 |
| `DELETE api/notifications/{id}` | NotificationController | 낮음 |
| `DELETE api/notifications` | NotificationController | 낮음 |
| `GET api/bands/{bandId}/finance` | GroupFinanceController | 낮음 |
| `PUT api/bands/{bandId}/finance/currency` | GroupFinanceController | 낮음 |
| `POST api/bands/{bandId}/finance/rates/refresh` | GroupFinanceController | 낮음 |
| `POST api/bands/{bandId}/update` | BandController | 낮음 |
| `POST api/bands/{bandId}/schedule/plan-b` | ScheduleController | 낮음 |
| `POST api/bands/{bandId}/schedule/swap` | ScheduleController | 낮음 |
| `POST api/bands/{bandId}/schedule/edit/start` | ScheduleController | 낮음 |
| `POST api/bands/{bandId}/schedule/edit/finish` | ScheduleController | 낮음 |

---

## 16. 미구현 기능 우선순위 요약

### 🔴 높음 (핵심 기능 미완성)
1. **지출/가계부 서버 연동** — `MoneyFragment`가 더미 데이터 상태. `ExpenseController` 백엔드 완성됨. **선행 조건:** `SubActivity`에서 `MoneyFragment`로 `bandId` 전달 추가 필요
2. **정산 기능** — `SettlementController` 백엔드 완성됨 (더치페이 알고리즘 포함)
3. **로그아웃 서버 API 호출** — `POST auth/kakao/logout` 미호출 (로컬 토큰만 삭제, 서버 refresh token 무효화 안 됨)

### 🟡 중간 (사용성 개선)
4. **사진 서버 저장** — `PhotoFragment` 로컬만, 멀티미디어 업로드 미구현 (백엔드 album_photos 테이블은 있음, Controller 미구현)
5. **FCM 푸시 알림** — 백엔드 NotificationController 있음, 앱에 FCM 설정 없음
6. **그룹 투표 현황 UI** — `GroupVoteStatusResponse` DTO 있음, UI만 없음
7. **회원탈퇴 UI** — `DELETE auth/kakao/withdraw`, `DELETE auth/google/withdraw` UI 없음
8. **Google 로그아웃/탈퇴 ApiService 추가** — `POST auth/google/logout`, `DELETE auth/google/withdraw` ApiService에 미정의

### 🟢 낮음 (고도화)
9. **일정 대안(Alts) UI** — DTO, API 있음, UI 없음
10. **일정 편집 (plan-b, swap, edit lock)** — 백엔드 완성됨
11. **초대코드 재발급 버튼** — API 정의됨, 버튼 없음
12. **사용자 프로필 조회/수정** — `GET/PUT api/users/me`
13. **알림 화면** — 전체 Notification API
14. **그룹 재정/환율** — GroupFinance API
15. **`api/destinations/popular`, `api/destinations/search` 활용** — 현재 로컬 카탈로그 사용
16. **WebSocket 재연결 로직** — `VoteStompClient` 네트워크 불안정 대응

---

## 17. 잘 구현된 점

1. **RetrofitClient 토큰 관리**: authInterceptor + sessionInterceptor 분리, 401 시 refresh → 재시도 → 실패 시 로그인 이동까지 완전히 자동화됨
2. **밴드 상태 머신 UI**: PLANNING → VOTING → GENERATING → TRAVELLING → DONE 단계별 UI/버튼 분기, 폴링으로 GENERATING 완료 자동 감지
3. **WebSocket 실시간 투표**: STOMP over WebSocket으로 투표 현황 실시간 업데이트, JWT 인증 포함
4. **딥링크 초대 처리**: 앱이 꺼진 상태/실행 중 상태 모두 처리, 처리 후 즉시 null 처리로 재진입 방지
5. **국내/해외 이중 검색**: 국내는 카카오 로컬 API, 해외는 서버 API로 분기, 공통 `PlaceDocument` 타입으로 통합
6. **자동 투표 (autoLike)**: 내가 담은 장소는 투표 화면 진입 시 자동 좋아요 — UX 자동화
7. **로컬 여행지 카탈로그**: 서버 의존 없이 26개 도시를 오프라인으로 제공, 썸네일/플래그/지역 포함

---

## 18. 보완이 필요한 점

1. **로그아웃 시 서버 API 미호출** — `TokenManager.clear()`로 로컬만 지움. 서버에서 refresh token 무효화 안 됨
2. **STOMP WebSocket 재연결 로직 없음** — `VoteStompClient`에 재연결 시도 없어 네트워크 불안정 시 끊김
3. **`scheduleGenerate` API (`POST api/bands/{bandId}/schedule/generate`)** — ApiService에 있지만 UI에서 직접 호출 안 함. 방장이 `advanceBandStatus()`로 VOTING → GENERATING 전환 시 서버에서 자동 트리거되는지 확인 필요
4. **MoneyFragment bandId 없음** — `SubActivity`에서 bandId를 전달 안 해서 해당 밴드의 지출만 조회할 수 없는 구조
5. **`api/destinations/popular/search` 미사용** — 서버 API가 있음에도 로컬 카탈로그만 사용. 서버 검색 활용 시 더 다양한 목적지 지원 가능
6. **메모리 누수 가능성** — `HomeFragment` handler/polling은 `onDestroyView`에서 제거되지만 `onPause` 기준으로 관리되어 복잡

---

---

## 19. 인수인계 문서(v6) 대비 설계 차이 기록

> 인수인계 문서와 실제 구현이 달라진 사항을 여기에 기록한다. 코드가 문서와 다른 방향으로 구현될 때마다 추가.

| 항목 | 인수인계 v6 내용 | 실제 구현 | 사유/비고 |
|------|----------------|-----------|---------|
| 알림 방식 | "In-App 알림만 (FCM 미사용)" | 백엔드에 FCM 포함 구현됨 (`FcmService`, `FirebaseConfig`) | 팀원이 FCM도 추가 구현. Android 미연동 상태 |
| 영수증 OCR | "GPT-4o or Gemini Vision, 추후 결정" | Gemini Vision으로 확정 구현 (`GeminiProperties`) | 백엔드 결정 완료 |
| Google 토큰 갱신 | 명시 없음 | Google 사용자도 `POST auth/kakao/refresh` 동일 엔드포인트 사용 | 백엔드 단일화 결정 |
| MoneyFragment bandId | SubActivity 5탭 컨테이너 설계 | MoneyFragment에 bandId 미전달 | 구현 누락, 지출 연동 시 선행 수정 필요 |

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|-----------|
| 2026-05-22 | v1.0 | 초기 문서 생성 (잘못된 코드 읽기 기반) |
| 2026-05-22 | v2.0 | **전체 재작성** — 모든 소스 파일 재정독, 현재 코드 기준으로 완전 갱신 |
| 2026-05-22 | v2.1 | 섹션 15 누락 엔드포인트 보완 (Google auth, 전체 Notification/Finance/Settlement API), 섹션 16 우선순위 개정, 섹션 19 신규 추가 (설계 차이 기록) |

**마지막 수정:** 2026-05-22
