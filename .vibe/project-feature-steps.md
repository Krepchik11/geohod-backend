# Implementation Plan

> **Legend**  
> *"Files"* lists the **maximum** files expected to change/create.  
> If a file path ends with `/*` it counts as one directory with ≤ 20 new files total.  
> "Hints" are optional pointers for the code-gen agent.

---

## 0 – Liquibase Bootstrap
- [x] **0.1 Create master changelog**
  - **Task**: Add `db.changelog-master.xml` that `<include>`s the existing `db.changelog-1.0.xml`. Point Spring Boot to it.
  - **Files**
    - `src/main/resources/db/changelog/db.changelog-master.xml` (new)
    - `src/main/resources/application.yml` (edit): `spring.liquibase.change-log`
  - **Hints**: Keep original changelog immutable; future files go in the same folder.

---

## 1 – Schema Migrations (Liquibase)
- [x] **1.1 Table `reviews`**
  - **Task**: New changeSet with table, PK, FKs, indexes, `is_hidden` default false.
  - **Files**
    - `db.changelog-1.1-reviews.xml` (new)
    - `db.changelog-master.xml` (edit +1 include)
- [x] **1.2 Table `user_ratings`**
  - **Task**: changeSet with `average_rating NUMERIC(3,2)`, `total_reviews_count`, etc.
  - **Files**: `db.changelog-1.2-user_ratings.xml`, plus master include.
- [x] **1.3 Table `event_logs`**
  - **Task**: changeSet with `payload JSONB`, index on `created_at`.
  - **Files**: `db.changelog-1.3-event_logs.xml`, plus master include.
- [x] **1.4 Table `notification_processor_progress`**
  - **Task**: changeSet with unique `processor_name`.
  - **Files**: `db.changelog-1.4-npp.xml`, plus master include.
- [x] **1.5 Table `notifications`**
  - **Task**: changeSet with `is_read`, cursor index on `(user_id,is_read,created_at)`.
  - **Files**: `db.changelog-1.5-notifications.xml`, plus master include.
  - **Hints**: Run the app once after 1.x to let Liquibase apply all sets.

---

## 2 – Domain Model
- [x] **2.1 Create enum `EventType`**
  - **Files**: `domain/eventlog/EventType.java`
- [x] **2.2 Entity & Repository `Review`**
  - **Files**
    - `domain/review/Review.java`
    - `repository/review/ReviewRepository.java`
- [x] **2.3 Entity & Repository `UserRating`**
  - **Files**: analogous pair.
- [x] **2.4 Entity & Repository `EventLog`**
- [x] **2.5 Entity & Repository `NotificationProcessorProgress`**
- [x] **2.6 Entity & Repository `Notification`**

---

## 3 – DTOs & Mapping
- [x] **3.1 DTOs for Review**
  - **Files**: `api/dto/review/ReviewCreateRequest.java`, `ReviewResponse.java`
- [x] **3.2 DTOs for Notification (cursor)**
  - **Files**: `api/dto/notification/NotificationResponse.java`, `NotificationCursorRequest.java`
- [x] **3.3 MapStruct config module** (if not yet present)
  - **Task**: Ensure `@MapperConfig(componentModel = "spring")`
  - **Files**: `infrastructure/mapper/GlobalMapperConfig.java`
- [x] **3.4 Mapper `ReviewApiMapper`**
- [x] **3.5 Mapper `NotificationApiMapper`**

---

## 4 – Core Services (Interfaces)
- [x] **4.1 Service interface `EventLogService`**
- [x] **4.2 Service interface `NotificationProcessorProgressService`**
- [x] **4.3 Service interface `UserRatingService`**
- [x] **4.4 Service interface `ReviewService`**
- [x] **4.5 Service interface `NotificationService`**

---

## 5 – Core Services (Implementations)
- [x] **5.1 Implement `EventLogServiceImpl`**
  - createLogEntry + findUnprocessed(limit,processorName)
- [x] **5.2 Implement `NotificationProcessorProgressServiceImpl`**
- [x] **5.3 Implement `UserRatingServiceImpl`** (calculate & cache)
- [x] **5.4 Implement `ReviewServiceImpl` – submitReview() only**
- [x] **5.5 Implement `ReviewServiceImpl` – hide/unhide**
- [x] **5.6 Implement `NotificationServiceImpl` – fetch (cursor)**
- [x] **5.7 Implement `NotificationServiceImpl` – mark-one / mark-all**

---

## 6 – REST Controllers (Skeletons)
- [x] **6.1 Create `ReviewController` class with `@RequestMapping("/api/reviews")`**
  - No endpoints yet – just compile.
- [x] **6.2 Create `NotificationController` skeleton**

---

## 7 – REST Controllers (Endpoints)
- [x] **7.1 `POST /api/reviews`**
- [x] **7.2 `GET /api/users/{id}/reviews` (paged)**  
- [x] **7.3 `GET /api/users/{id}/rating`**
- [x] **7.4 `PATCH /api/reviews/{id}/hide`**
- [x] **7.5 `PATCH /api/reviews/{id}/unhide`**
- [x] **7.6 `GET /api/notifications`** (cursor params)
- [x] **7.7 `POST /api/notifications/{id}/mark-as-read`**
- [x] **7.8 `POST /api/notifications/mark-all-as-read`**

---

## 8 – Scheduled Processors
- [x] **8.1 Component `InAppNotificationProcessor`**
  - Read logs → create notifications → update progress.
- [x] **8.2 Component `TelegramNotificationProcessor`**
  - Read logs → publish via `ITelegramOutboxMessagePublisher`.
  - **Files**: new class + wire to existing publisher bean.

---

## 9 – Existing Code Refactor to EventLog
- [x] **9.1 `EventParticipationService` – registration path**  
  - Remove direct telegram send; add `createLogEntry(EVENT_REGISTERED, …)`
- [x] **9.2 `EventParticipationService` – unregistration path**
- [x] **9.3 `EventService.finishEvent` – add EVENT_FINISHED_FOR_REVIEW_LINK**
- [x] **9.4 `EventManager.cancelEvent` – add EVENT_CANCELED**
- [x] **9.5 Deprecate sending methods in `EventNotificationService` (keep formatter)**

---

## 10 – Configuration & Security
- [ ] **10.1 Update `SecurityConfiguration`** – secure new routes.
- [ ] **10.2 Add scheduler & batch props to `application.yml`**
- [ ] **10.3 Add `@EnableScheduling` if not already present**

---

## 11 – Testing Foundation
- [ ] **11.1 Test utilities: `TestContainersPostgresConfig`** (if not yet)
- [ ] **11.2 Abstract `@SpringBootTest` base for integration**

---

## 12 – Unit Tests
- [ ] **12.1 `EventLogServiceTest`**
- [ ] **12.2 `NotificationProcessorProgressServiceTest`**
- [ ] **12.3 `UserRatingServiceTest`**
- [ ] **12.4 `ReviewServiceTest` – submitReview()**
- [ ] **12.5 `ReviewServiceTest` – hide/unhide**
- [ ] **12.6 `NotificationServiceTest` – cursor pagination**
- [ ] **12.7 `InAppNotificationProcessorTest`**
- [ ] **12.8 `TelegramNotificationProcessorTest`**

---

## 13 – Integration Tests
- [ ] **13.1 Flow test: register → log → processors → notification visible**
- [ ] **13.2 Flow test: submit review → rating updated → organizer notified**

---

## 14 – Documentation & Cleanup
- [ ] **14.1 Update `README.md`** – new endpoints, how to run Liquibase
- [ ] **14.2 Remove/adjust obsolete tests & TODOs**

---

## 15 – Optional Frontend (Long-poll Hook)*
*(Skip if no frontend repo; otherwise continue.)*
- [ ] **15.1 Add `useNotificationsLongPoll` React hook** (TS)
- [ ] **15.2 Display unread badge + mark-as-read button**

