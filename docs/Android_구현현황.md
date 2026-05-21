# SyncTrip Android 구현현황 문서
**작성일:** 2026-05-22 | **참조:** SyncTrip_인수인계문서_v6 + Spring Boot 백엔드 코드 + Android 앱 코드 직접 분석

> 이 문서는 안드로이드 앱 코드(`com.example.synctrip`)를 3회 이상 정독하여 백엔드 구현현황(`SyncTrip_구현현황.md`)과 대조 작성한 현황 정리서입니다.
> 백엔드 API 경로 기준은 Spring Boot 컨트롤러 코드 직접 확인.

---

## 범례

| 표시 | 의미 |
|---|---|
| ✅ 구현 | 완전히 구현되어 백엔드와 실제 연동되는 기능 |
| ⚠️ 부분 구현 | UI/뼈대는 있으나 API 연동 없이 하드코딩 데이터로 동작 |
| ❌ 미구현 | 백엔드에 API가 있으나 안드로이드 코드가 전혀 없음 |
| ➕ 추가 구현 | 인수인계 문서에 없었으나 안드로이드에서 자체 추가된 기능 |
| 🔥 긴급 결함 | 기능 동작 여부와 무관하게 반드시 고쳐야 하는 심각한 버그/누락 |

---

## 1. 인증 / 회원 관리

### 1-1. 로그인

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| USR-001 | 카카오 로그인 (UI + 서버 연동) | ✅ 구현 | `LoginActivity.kt` | 카카오 SDK → 서버 JWT 교환 정상 동작 |
| USR-001 | 자동 로그인 (토큰 로컬 저장 확인) | ✅ 구현 | `LoginActivity.kt` | `TokenManager.isLoggedIn()` 체크 후 `MainActivity` 이동 |
| USR-001 | 구글 로그인 (UI) | ⚠️ 부분 구현 | `LoginActivity.kt` | 버튼 있음. 클릭 시 Toast 후 `MainActivity`로 이동만 함. 실제 Google Sign-In SDK 미연동, `/auth/google/login` API 미호출 |
| USR-029 | 로그아웃 | ✅ 구현 | `MainActivity.kt` | 설정 다이얼로그 → `TokenManager.clear()` → `LoginActivity` 이동 |

**보완할 점**
- **🔥 Refresh Token 미사용:** `KakaoLoginResponse`에 `refreshToken` 필드 존재하나 저장 및 갱신 로직 없음. Access Token 만료 시 모든 API 호출 자동 실패 → 로그인 화면으로 이동도 없이 조용히 에러만 남음
- 로그아웃 시 서버 `POST /auth/kakao/logout` API 미호출 (로컬 삭제만)
- 회원탈퇴(`DELETE /auth/kakao/withdraw`) 미구현

### 1-2. 프로필

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| USR-002 | 프로필 조회 (`GET /api/users/me`) | ❌ 미구현 | — | API 없음, UI 없음 |
| USR-002 | 프로필 수정 (`PUT /api/users/me`) | ❌ 미구현 | — | API 없음, UI 없음 |
| USR-002 | 회원 탈퇴 | ❌ 미구현 | — | API 없음, UI 없음 |

---

## 2. 공통 네트워크 / 인프라

### 2-1. RetrofitClient

| 항목 | 상태 | 비고 |
|---|---|---|
| Base URL 설정 | ✅ | `https://test.sync-trip.app/` |
| **🔥 JWT Authorization 헤더 자동 주입** | ❌ 미구현 | `RetrofitClient`에 `OkHttpClient` + `Interceptor` 없음. `TokenManager.getToken()`을 읽어 모든 요청 헤더에 `Bearer {token}` 추가하는 인터셉터가 전혀 없음 → 그룹 목록, 장바구니 등 인증이 필요한 모든 API 호출이 401로 실패 |
| API 경로 불일치 (`/api/groups` vs `/api/bands`) | 🔥 결함 | `ApiService.kt`는 `/api/groups`를 사용하나, 백엔드는 `/api/bands`. 실제 그룹 생성/목록 호출이 404로 실패 |
| Refresh Token 자동 갱신 (401 Interceptor) | ❌ 미구현 | 토큰 만료 대응 없음 |

### 2-2. WebSocket / FCM

| 항목 | 상태 | 비고 |
|---|---|---|
| WebSocket (STOMP) 연결 | ❌ 미구현 | 백엔드에 WebSocket 구현 완료. 안드로이드 미연동 |
| FCM 토큰 등록 (`POST /api/users/fcm-token`) | ❌ 미구현 | Firebase SDK 의존성 없음, 토큰 등록 미구현 |

---

## 3. 그룹(밴드) 관리

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| — | 내 그룹 목록 조회 (`GET /api/bands`) | ⚠️ 부분 구현 | `MainActivity.kt` | API 호출 코드 존재하나 경로 불일치(`/api/groups`) + JWT 헤더 없어 실패. 실패 시 하드코딩 더미 데이터 표시 |
| USR-003 | 그룹 생성 (`POST /api/bands`) | ⚠️ 부분 구현 | `CreateRoomActivity.kt` | API 호출 코드 있으나 경로 불일치(`/api/groups`) + JWT 헤더 없음. **숙소 정보 입력 필드 없음** (방장 숙소 설정 인수인계 문서 명세 누락) |
| USR-004 | 초대 코드로 그룹 참여 | ⚠️ 부분 구현 | `MainActivity.kt` | 6자리 코드 입력 UI는 있으나 서버 API 호출 없음 (길이 유효성 검사만) |
| USR-005 | 최대 인원 제한 (8명) | ❌ 미구현 | — | 그룹 생성 시 `etMemberCount` 입력받으나 서버로 전달만 함, 8명 초과 방어 로직 UI 없음 |
| USR-006 | 초대 코드 재발급 | ❌ 미구현 | — | 홈 프래그먼트에 하드코딩 초대 코드만 표시 |
| — | 그룹 상세 조회 (`GET /api/bands/{id}`) | ❌ 미구현 | — | `ApiService`에 정의(`/api/groups/{id}`)되어 있으나 실제 호출 코드 없음 |
| — | 그룹 멤버 목록 (`GET /api/bands/{bandId}/members`) | ❌ 미구현 | — | — |
| — | 그룹 삭제 | ❌ 미구현 | — | — |
| — | 그룹 정보 수정 | ❌ 미구현 | — | — |
| USR-009 | Ready 상태 전환 | ❌ 미구현 | — | 홈 프래그먼트에 "투표 시작" 버튼만 있고 `POST /api/bands/{bandId}/ready` 호출 없음 |
| USR-014 | 상태 진행 (`POST /api/bands/{bandId}/status/advance`) | ❌ 미구현 | — | — |

**보완할 점**
- 그룹 목록에서 각 그룹의 상태(PLANNING/VOTING/TRAVELLING 등) 표시가 실데이터 기반이 아님
- `RoomAdapter`에서 `status` 필드 받아 `Room` 모델에 저장하나 UI에 반영되지 않음
- `CreateRoomActivity`에 숙소 위치 입력 없음 (백엔드 `BandCreateRequest`는 위도/경도/숙소명 포함 가능)

---

## 4. 장소 탐색 / 장바구니

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| USR-007 | 카카오 지도 + 장소 키워드 검색 | ✅ 구현 | `PlaceSearchActivity.kt` | KakaoMap + KakaoApiService 직접 호출. 검색 → 결과 목록 → 지도 이동 정상 동작 |
| USR-007 | 장소 검색 (백엔드 프록시) | ❌ 미구현 | — | 백엔드 `GET /api/bands/{bandId}/places/search` 미연동 (안드로이드가 카카오 REST 키를 직접 노출하고 있음) |
| USR-008 | 장바구니 담기 (`POST /api/bands/{bandId}/picks`) | ❌ 미구현 | — | `PlaceSearchActivity`에서 장소 클릭 시 지도 이동만, 서버에 pick 저장 없음 |
| USR-008 | 내 장바구니 목록 (`GET /api/bands/{bandId}/picks`) | ❌ 미구현 | — | `HomeFragment` 장소 목록은 하드코딩 더미 데이터 |
| USR-008 | 장바구니 삭제 (`DELETE /api/bands/{bandId}/picks/{placeId}`) | ⚠️ 부분 구현 | `PlaceAdapter.kt` | UI에 삭제 버튼은 있으나 서버 API 미호출 (로컬 리스트에서만 제거) |

**보완할 점**
- 장소 담기 시 서버 PlacePickRequest (`placeId`, `name`, `category`, `latitude`, `longitude`, `address`)와 매핑하는 로직 없음
- 1인당 5개 제한 UI 안내 없음 (`HomeFragment`에 `내가 담은 장소 (x/5)` 표시는 있으나 하드코딩)
- 백엔드 장소 검색 API 프록시 미사용으로 앱에 카카오 REST 키 직접 노출됨 (보안 위험)

---

## 5. 투표

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| USR-010 | 투표 UI (좋아요/싫어요 버튼) | ⚠️ 부분 구현 | `VoteFragment.kt` | UI만 구현. 하드코딩 5개 장소 목록. `POST /api/bands/{bandId}/votes` 미호출 |
| USR-010 | 투표 대상 장소 목록 (`GET /api/bands/{bandId}/votes/places`) | ❌ 미구현 | — | — |
| USR-010 | SKIP 기능 | ❌ 미구현 | — | 버튼 2개(좋아요/싫어요)만 있음. SKIP 버튼 없음 |
| USR-010 | 스와이프 제스처 | ❌ 미구현 | — | 버튼 클릭만. 카드 스와이프 애니메이션 없음 |
| USR-011 | 카테고리별 순위 풀 확인 | ❌ 미구현 | — | — |
| USR-013 | 투표 결과 확인 (`GET /api/bands/{bandId}/votes/status`) | ❌ 미구현 | — | — |
| USR-014 | 투표 강제 시작/마감 (방장 기능) | ❌ 미구현 | — | — |

**보완할 점**
- 투표 진행 중 실시간 현황 (`GET /api/bands/{bandId}/votes/status/group`) 미구현
- WebSocket으로 실시간 투표 이벤트 수신 미구현
- 투표 완료 후 자동 상태 전환 인식 미구현

---

## 6. 일정 관리

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| USR-015 | 일정 표시 UI (탭+목록) | ⚠️ 부분 구현 | `ScheduleFragment.kt` | 탭 레이아웃 + RecyclerView 뼈대 구현. 3일치 하드코딩 더미 데이터. `GET /api/bands/{bandId}/schedule` 미호출 |
| USR-015 | 경고 배지(warning) 표시 | ⚠️ 부분 구현 | `ScheduleAdapter.kt` | `Schedule.warning` 필드 → `tvWarning` 표시 로직 있음. 하드코딩 예시 배지("🌙 늦은 일정") 정상 표시. 실데이터 연동 없음 |
| USR-017 | Drag & Drop 순서 변경 | ❌ 미구현 | — | RecyclerView에 ItemTouchHelper 없음. `POST /api/bands/{bandId}/schedule/swap` 미호출 |
| USR-018 | Plan B 추천 (Long Press → 바텀시트) | ❌ 미구현 | — | — |
| — | 일정 수동 생성 트리거 (`POST /api/bands/{bandId}/schedule/generate`) | ❌ 미구현 | — | 홈 프래그먼트의 "일정 생성하기" 버튼이 Toast만 표시 |
| — | 편집 락 (start/finish) | ❌ 미구현 | — | — |

**보완할 점**
- `Schedule` DTO가 서버 응답 구조(`ScheduleSlotResponse`: `scheduleId`, `placeId`, `placeName`, `visitOrder`, `visitTime`, `travelTimeMinutes`, `badges` 등)와 완전히 다름 → 실데이터 연동 시 DTO 전면 재설계 필요
- 숙소 변경 UI 없음

---

## 7. 가계부 / 정산

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| USR-019 | 영수증 OCR | ❌ 미구현 | — | — |
| USR-020 | 지출 목록 표시 UI | ⚠️ 부분 구현 | `MoneyFragment.kt` | RecyclerView + 하드코딩 3건 더미 데이터. `GET /api/bands/{bandId}/expenses` 미호출 |
| USR-020 | 지출 추가/수정/삭제 | ❌ 미구현 | — | 버튼 없음 |
| USR-021 | 다통화 환율 | ❌ 미구현 | — | `Expense` DTO에 통화 필드 없음 |
| USR-022 | 더치페이 정산 | ❌ 미구현 | — | `GET /api/bands/{bandId}/settlement` 미구현 |
| — | 정산 요청 알림 발송 | ❌ 미구현 | — | `POST /api/bands/{bandId}/settlement/request` 미구현 |

**보완할 점**
- `ExpenseAdapter`에 통화 단위 표시 없음 (원화 고정)
- 분담자 목록이 문자열 배열 → 실제 그룹 멤버 ID 기반으로 변경 필요

---

## 8. 알림

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| USR-026 | 알림 센터 (목록 조회) | ❌ 미구현 | — | `GET /api/notifications` 미구현, UI 없음 |
| USR-027 | 알림 수신 설정 토글 | ❌ 미구현 | — | — |
| — | FCM 푸시 알림 수신 | ❌ 미구현 | — | `build.gradle`에 Firebase SDK 없음. `google-services.json` 없음 |

**보완할 점**
- `build.gradle.kts`에 `implementation("com.google.firebase:firebase-messaging:...")` 의존성 추가 필요
- FCM 토큰 발급 후 `POST /api/users/fcm-token` 등록 로직 필요

---

## 9. 아카이빙

| USR | 기능명 | 상태 | 구현 위치 | 비고 |
|---|---|---|---|---|
| USR-023 | 공유 앨범 (로컬 사진 추가) | ⚠️ 부분 구현 | `PhotoFragment.kt` | 갤러리에서 이미지 Uri 선택 → RecyclerView 표시. 서버 업로드 없음 |
| USR-023 | 공유 앨범 서버 업로드/조회 | ❌ 미구현 | — | 백엔드에도 미구현 (DDL만 존재) |
| USR-024 | 여권 스탬프 | ❌ 미구현 | — | 백엔드에도 미구현 (DDL만 존재) |
| USR-025 | 과거 여행 기록 | ❌ 미구현 | — | DONE 밴드 전용 뷰 없음 |

---

## 10. 시스템 / 기타

| 항목 | 상태 | 비고 |
|---|---|---|
| KakaoMap SDK 지도 표시 | ✅ 구현 | `PlaceSearchActivity`. 지도 초기화, 이동, 라이프사이클 관리 정상 |
| 하단 네비게이션 (5탭) | ✅ 구현 | `SubActivity` + `HomeFragment`, `ScheduleFragment`, `VoteFragment`, `MoneyFragment`, `PhotoFragment` |
| GroupStatus enum UI 분기 | ✅ 구현 | `HomeFragment.updateStatusUI()` 5개 상태별 버튼 활성화/비활성화 로직 |
| `local.properties` 키 관리 | ✅ 구현 | `KAKAO_NATIVE_KEY`, `KAKAO_REST_KEY` — BuildConfig 통해 관리 |
| 인터넷 권한 | ✅ 구현 | `AndroidManifest`에 `INTERNET` 권한 |

---

## 11. 미구현 요약 (우선순위 순)

### 🔥 긴급 (앱이 정상 동작하지 않는 근본 결함)

| 우선순위 | 항목 | 문제 | 수정 방향 |
|---|---|---|---|
| 1 | JWT 헤더 자동 주입 | 인증 필요 모든 API 401 실패 | `OkHttpClient` + `Interceptor`로 `Authorization: Bearer {token}` 추가 |
| 2 | API 경로 불일치 | `/api/groups` → `/api/bands` (모든 그룹 API) | `ApiService.kt` 경로 일괄 수정 |
| 3 | Refresh Token 갱신 | Access Token 만료 시 자동 재발급 없음 | 401 응답 시 `POST /auth/kakao/refresh` 호출 후 재시도 |

### 🔴 높음 (핵심 기능)

| 항목 | 관련 USR | 설명 |
|---|---|---|
| 그룹 목록 실데이터 연동 | — | JWT 인터셉터 수정 후 API 호출 정상화 |
| 초대 코드로 그룹 참여 API 연동 | USR-004 | `POST /api/bands/join` 호출 |
| 장바구니 담기 / 삭제 실연동 | USR-008 | `POST/DELETE /api/bands/{bandId}/picks` |
| 내 장바구니 목록 서버 조회 | USR-008 | `GET /api/bands/{bandId}/picks` |
| Ready 상태 전환 | USR-009 | `POST /api/bands/{bandId}/ready` 버튼 추가 |
| 투표 API 연동 | USR-010 | `GET /api/bands/{bandId}/votes/places`, `POST /api/bands/{bandId}/votes` |
| 일정 API 연동 | USR-015 | `GET /api/bands/{bandId}/schedule` + DTO 재설계 |
| 구글 로그인 실구현 | USR-001 | Google Sign-In SDK + `POST /auth/google/login` |

### 🟡 중간 (주요 기능)

| 항목 | 관련 USR | 설명 |
|---|---|---|
| 그룹 상태 전환 API | USR-014 | `POST /api/bands/{bandId}/status/advance` |
| 지출 CRUD | USR-020 | 지출 추가/수정/삭제 UI + API |
| 영수증 OCR | USR-019 | 이미지 업로드 → Gemini OCR API |
| 더치페이 정산 | USR-022 | `GET /api/bands/{bandId}/settlement` |
| Plan B 추천 UI | USR-018 | Long Press → 바텀시트 + `POST /api/bands/{bandId}/schedule/plan-b` |
| Drag & Drop 순서 변경 | USR-017 | `ItemTouchHelper` + `POST /api/bands/{bandId}/schedule/swap` |
| FCM 푸시 알림 설정 | USR-026 | Firebase SDK 추가 + `POST /api/users/fcm-token` |
| 알림 센터 UI | USR-026 | `GET /api/notifications` |

### 🟢 낮음 (부가 기능)

| 항목 | 관련 USR | 설명 |
|---|---|---|
| 프로필 조회/수정 | USR-002 | `GET/PUT /api/users/me` |
| 회원 탈퇴 | USR-002 | `DELETE /auth/kakao/withdraw` |
| 로그아웃 서버 API 호출 | USR-029 | `POST /auth/kakao/logout` |
| 초대 코드 재발급 | USR-006 | `POST /api/bands/{bandId}/invite-code` |
| 그룹 멤버 목록 | — | `GET /api/bands/{bandId}/members` |
| 실시간 WebSocket 연동 | — | Ready/투표/일정 변경 실시간 반영 |
| 알림 수신 설정 토글 | USR-027 | `PATCH /api/users/notification-settings` |
| 공유 앨범 서버 연동 | USR-023 | (백엔드 구현 후 연동) |
| 여권 스탬프 | USR-024 | (백엔드 구현 후 연동) |
| 과거 여행 아카이브 | USR-025 | DONE 밴드 전용 뷰 |

---

## 12. 잘 된 점

| 항목 | 설명 |
|---|---|
| **KakaoMap + 검색 연동** | `PlaceSearchActivity`: KakaoMap SDK 라이프사이클 관리(onResume/onPause) 정상, 키워드 검색 → 결과 목록 → 지도 카메라 이동까지 완성도 있게 구현 |
| **카카오 로그인 + JWT 저장** | `LoginActivity` → `sendTokenToServer()` → `TokenManager.saveToken()` 플로우 완결. 자동 로그인 체크도 정상 동작 |
| **GroupStatus enum 상태 UI** | `HomeFragment.updateStatusUI()`: 5개 상태 (PLANNING/VOTING/GENERATING/TRAVELLING/DONE) 별로 버튼 활성화/비활성화/텍스트 변경을 깔끔하게 분리. 확장 용이한 구조 |
| **ScheduleAdapter 경고 배지** | `tvWarning` 조건부 표시 로직 + 마지막 슬롯 이동시간 숨기기 처리 — 실데이터 연동 시 그대로 사용 가능 |
| **보안 키 관리** | `local.properties` + `BuildConfig` 패턴으로 카카오 키를 코드에서 분리. `.gitignore` 처리 필요 여부 확인 필요 |
| **앱 전체 뼈대 구조** | Activity 4개 + Fragment 5개 + Adapter 6개로 실제 앱 흐름(로그인→목록→방→5탭)이 명확하게 구성됨 |

---

## 13. 잘 안 된 점 / 보완 필요

| 항목 | 문제 | 영향 |
|---|---|---|
| **JWT 인터셉터 없음** | Retrofit 요청에 인증 헤더 없음 | 인증 필요 API 전체 실패 |
| **API 경로 불일치** | `/api/groups` ≠ `/api/bands` | 그룹 생성/조회 404 |
| **모든 핵심 데이터 하드코딩** | 장소/투표/일정/지출 모두 더미 데이터 | 실제 서버 데이터 미반영 |
| **DTO 구조 불일치** | `Schedule`(`time`, `placeName`, `duration`, `travelTime`) ↔ 백엔드 `ScheduleSlotResponse`(`scheduleId`, `placeId`, `visitTime`, `badges[]`) — 필드명/구조 불일치 | 일정 API 연동 불가 |
| **구글 로그인 미구현** | 버튼 클릭 시 무조건 `MainActivity`로 이동 | 구글 사용자 로그인 불가 |
| **카카오 REST 키 앱 직접 노출** | `PlaceSearchActivity`가 Kakao API에 직접 호출 | 키 노출 보안 위험 (백엔드 프록시 미사용) |
| **장소 담기 연결 단절** | 지도 검색 후 장바구니 담기 불가 | USR-007 → USR-008 플로우 단절 |
| **HomeFragment groupId 미전달** | `SubActivity`에서 `intent.getStringExtra("GROUP_ID")` 수신 후 프래그먼트에 전달 없음 | 모든 Fragment가 bandId를 모름 → API 호출 불가 |
| **더미 데이터 fallback** | 서버 연결 실패 시 `showTempData()` 로 더미 표시 | 개발 중 실오류 숨겨짐 |

---

## 14. 인수인계 문서 대비 구현 차이

| 항목 | 인수인계 문서 v6 | 안드로이드 현재 | 비고 |
|---|---|---|---|
| 소셜 로그인 | 카카오 + 구글 | 카카오만 실구현, 구글은 Toast만 | 구글 미구현 |
| FCM | "In-App 알림만 (FCM 미사용)" → 백엔드가 FCM 추가 구현 | Firebase SDK 없음 | 백엔드 방침 변경 미반영 |
| 장소 검색 | 백엔드 프록시 (`/api/bands/{bandId}/places/search`) | 앱에서 카카오 API 직접 호출 | 보안 위험 |
| 블라인드 장바구니 | 담기/조회/삭제 API 연동 | UI만, 서버 연동 없음 | — |
| 스와이프 투표 | 카드 스와이프 + 좋아요/싫어요/SKIP | 버튼만, 스와이프 없음, SKIP 없음 | — |

---

## 15. 변경 이력

| 날짜 | 변경 내용 |
|---|---|
| 2026-05-22 | 문서 최초 작성. 안드로이드 코드 + Spring Boot 코드 + 인수인계 문서 대조 분석 전체 정리 |

---

**마지막 수정:** 2026-05-22 | **참조 백엔드:** `SyncTrip_구현현황.md` (2026-05-21 기준)
