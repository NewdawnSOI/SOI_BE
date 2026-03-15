# FCM Only Notification Structure

## 결론
- 댓글, 태그, 초대, 친구추가 알림만 필요하면 `FCM only`로 가는 편이 맞다.
- 서버는 `알림 저장`과 `FCM 발송`만 책임지고, 앱이 열려 있을 때의 화면 갱신도 FCM 수신 콜백으로 처리한다.
- `WebSocket`은 일단 넣지 않는다.

## 왜 WebSocket을 빼는가
- 지금 요구사항은 채팅, presence, typing, live counter 같은 세션성 실시간이 아니다.
- 모바일 앱에서 백그라운드/종료 상태까지 안정적으로 커버하려면 결국 FCM이 필요하다.
- 그러면 WebSocket은 포그라운드 UX 개선용 보조 채널이 되는데, 현재 요구사항에서는 비용 대비 이득이 작다.

## 전체 흐름
```text
Flutter App
  ├─ 로그인/앱 시작 시 FCM token 발급
  ├─ token 등록 API 호출
  ├─ foreground: onMessage 수신 후 UI 갱신 또는 local notification 표시
  └─ background/terminated: OS push 표시, 앱 진입 시 목록 API 조회

Spring Boot
  ├─ 댓글/태그/초대/친구요청 이벤트 발생
  ├─ Notification 저장
  ├─ NotificationOutbox 저장
  ├─ Scheduler 또는 이벤트 리스너가 outbox 처리
  └─ FCM Admin SDK로 사용자 device token 들에 push 발송
```

## 추천 파일 구조
```text
src/main/java/com/soi/backend
├── config
│   ├── security
│   │   └── SecurityConfig.java
│   └── firebase
│       ├── FirebaseConfig.java
│       └── FirebaseProperties.java
├── external
│   └── fcm
│       ├── FcmClient.java
│       └── FcmMessageFactory.java
└── domain
    └── notification
        ├── controller
        │   ├── NotificationController.java
        │   └── NotificationDeviceController.java
        ├── dto
        │   ├── NotificationGetAllRespDto.java
        │   ├── NotificationRespDto.java
        │   ├── NotificationRegisterTokenReqDto.java
        │   ├── NotificationDeleteTokenReqDto.java
        │   ├── NotificationSendPayloadDto.java
        │   └── NotificationDataPayloadDto.java
        ├── entity
        │   ├── Notification.java
        │   ├── NotificationType.java
        │   ├── NotificationOutbox.java
        │   ├── NotificationOutboxStatus.java
        │   ├── UserDeviceToken.java
        │   └── DevicePlatform.java
        ├── repository
        │   ├── NotificationRepository.java
        │   ├── NotificationOutboxRepository.java
        │   └── UserDeviceTokenRepository.java
        ├── service
        │   ├── NotificationCommandService.java
        │   ├── NotificationQueryService.java
        │   ├── NotificationDeviceTokenService.java
        │   ├── NotificationOutboxService.java
        │   ├── NotificationPushService.java
        │   └── NotificationMessageFactory.java
        ├── scheduler
        │   └── NotificationOutboxScheduler.java
        └── event
            └── NotificationCreatedEvent.java
```

## 역할 분리

### `config/firebase`
- `FirebaseConfig`
  - Firebase Admin SDK 초기화
  - 서비스 계정 JSON 로드
- `FirebaseProperties`
  - credentials path, project id 같은 설정 바인딩

### `external/fcm`
- `FcmClient`
  - Firebase SDK 직접 호출
  - 단건/멀티캐스트 발송 처리
- `FcmMessageFactory`
  - title, body, data payload 생성

### `domain/notification/controller`
- `NotificationController`
  - 알림 목록 조회
  - 읽음 처리
- `NotificationDeviceController`
  - FCM token 등록
  - 로그아웃/토큰 만료 시 삭제 또는 비활성화

### `domain/notification/entity`
- `Notification`
  - 앱 내 알림 목록 원본
- `NotificationOutbox`
  - 발송 대기/재시도 상태
- `UserDeviceToken`
  - 사용자별 디바이스 token 저장
  - 한 명이 여러 기기를 쓸 수 있으므로 `1:N`

### `domain/notification/service`
- `NotificationCommandService`
  - 다른 도메인에서 호출하는 유일한 진입점
  - `Notification` 저장
  - `NotificationOutbox` 저장
- `NotificationQueryService`
  - 현재 `NotificationService`의 조회 책임 분리
- `NotificationDeviceTokenService`
  - token 등록, 중복 제거, 비활성화
- `NotificationOutboxService`
  - outbox 상태 조회/갱신
- `NotificationPushService`
  - outbox를 읽어 FCM 발송
- `NotificationMessageFactory`
  - 알림 타입별 title/body/data 구성

### `domain/notification/scheduler`
- `NotificationOutboxScheduler`
  - 미발송 outbox polling
  - 성공 시 `SENT`
  - 실패 시 `FAILED` 또는 `RETRY`

## 현재 코드 기준으로 어떻게 나누면 되는가
- 지금 `NotificationService`는 저장과 조회가 한 곳에 섞여 있다.
- FCM만 쓰더라도 이후 유지보수를 위해 아래처럼 쪼개는 게 낫다.

### 유지할 것
- `Notification.java`
- `NotificationType.java`
- `NotificationRepository.java`
- `NotificationController.java`의 조회 API 골격

### 분리할 것
- 기존 `NotificationService`의 조회 로직 -> `NotificationQueryService`
- 알림 생성 로직 -> `NotificationCommandService`
- FCM 관련 로직 -> `NotificationPushService`
- FCM token 관리 -> `NotificationDeviceTokenService`

## 추천 이벤트 처리 흐름
```text
CommentService / CategoryService / FriendService / PostService
    -> NotificationCommandService.create(...)
        -> notification 저장
        -> notification_outbox 저장

NotificationOutboxScheduler
    -> NotificationPushService.sendPending()
        -> 사용자 device token 조회
        -> FCM 발송
        -> 성공/실패 상태 저장
```

## 왜 outbox를 두는가
- 알림 DB 저장과 FCM 발송을 한 트랜잭션에 묶으면 실패 처리 복잡도가 커진다.
- 예를 들어 DB는 저장됐는데 FCM만 실패하면 재시도 근거가 사라진다.
- `notification_outbox`를 두면 재시도, 장애 복구, 실패 로그 추적이 쉬워진다.

## API 추천
```text
POST   /notification/device-tokens
DELETE /notification/device-tokens
GET    /notification
PATCH  /notification/{notificationId}/read
```

## DB 테이블 추천

### `notification`
- 사용자에게 보여줄 알림 데이터
- 기존 테이블 유지 가능

### `notification_outbox`
- `id`
- `notification_id`
- `receiver_user_id`
- `status`
- `retry_count`
- `last_error`
- `next_retry_at`
- `sent_at`
- `created_at`

### `user_device_token`
- `id`
- `user_id`
- `token`
- `platform`
- `enabled`
- `last_seen_at`
- `created_at`

## FCM payload 전략
- 알림성 요구라면 `notification + data` 둘 다 넣는 편이 안전하다.
- `notification`
  - OS가 백그라운드/종료 상태에서 푸시를 표시
- `data`
  - 앱이 열렸을 때 어떤 화면으로 이동할지 결정
  - `notificationId`, `type`, `targetId`, `categoryId` 같은 값 전달

예시 data:
```json
{
  "notificationId": "123",
  "type": "COMMENT_ADDED",
  "targetId": "456",
  "categoryId": "10"
}
```

## Flutter 쪽 처리 기준
- foreground
  - `onMessage` 수신
  - 알림센터 목록 새로고침 또는 local notification 표시
- background/opened
  - `onMessageOpenedApp`에서 화면 이동
- terminated
  - 앱 시작 시 초기 메시지 확인 후 화면 이동

즉, 앱이 열려 있을 때도 WebSocket 없이 FCM 수신 이벤트로 대응한다.

## 설정 파일 위치
```text
src/main/resources/application.properties
src/main/resources/application-dev.properties
src/main/resources/application-prod.properties
```

추가할 설정 예시:
- `app.fcm.enabled=true`
- `app.fcm.credentials-path=/opt/secrets/firebase-admin.json`
- `app.notification.outbox.poll-interval-ms=3000`
- `app.notification.outbox.batch-size=100`

서비스 계정 JSON은 저장소에 커밋하지 않는다.

## Gradle에 추가할 의존성
```gradle
implementation 'com.google.firebase:firebase-admin:9.4.3'
```

## 구현 순서 추천
1. `UserDeviceToken` 엔티티와 token 등록 API 추가
2. `firebase-admin` 연동
3. `NotificationService`를 command/query로 분리
4. `NotificationOutbox` 추가
5. `NotificationOutboxScheduler`로 비동기 발송
6. Flutter에서 foreground/background 수신 분기 처리

## 최종 판단
- 지금 요구사항에는 `FCM only`가 더 적합하다.
- 서버 구조는 단순해지고, 모바일 백그라운드 대응도 자연스럽다.
- 나중에 채팅이나 live state가 필요해질 때만 WebSocket을 추가하면 된다.
