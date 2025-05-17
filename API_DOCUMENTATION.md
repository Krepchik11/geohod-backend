# GeoHod API Documentation

## Общая информация

- Базовый URL: `/api/v1`
- Формат данных: JSON
- Аутентификация: Telegram WebApp token (заголовок `Authorization`)

## Аутентификация

Все запросы требуют аутентификации через Telegram WebApp. Токен должен быть передан в заголовке `Authorization`.

```
Authorization: <telegram_token>
```

## Эндпоинты

### События

#### Получение всех событий

```
GET /api/v1/events
```

**Параметры запроса:**
- `iamAuthor` (boolean, по умолчанию: true) - фильтр по событиям, где пользователь автор
- `iamParticipant` (boolean, по умолчанию: true) - фильтр по событиям, где пользователь участник
- `statuses` (массив, опционально) - фильтр по статусам событий
- `page` (число, по умолчанию: 0) - номер страницы
- `size` (число, по умолчанию: 30) - количество элементов на странице

**Пример ответа:**
```json
{
  "content": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "name": "Поход в горы",
      "date": "2023-07-15T10:00:00Z",
      "description": "Поход в горы на выходных",
      "maxParticipants": 10,
      "status": "ACTIVE",
      "author": {
        "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "firstName": "Иван",
        "lastName": "Иванов",
        "tgUsername": "ivanov",
        "tgImageUrl": "https://example.com/avatar.jpg"
      },
      "participantsCount": 5,
      "iamParticipant": true,
      "createdAt": "2023-07-01T12:00:00Z",
      "updatedAt": "2023-07-01T12:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 30,
  "number": 0
}
```

#### Получение события по ID

```
GET /api/v1/events/{eventId}
```

**Пример ответа:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Поход в горы",
  "date": "2023-07-15T10:00:00Z",
  "description": "Поход в горы на выходных",
  "maxParticipants": 10,
  "status": "ACTIVE",
  "author": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "firstName": "Иван",
    "lastName": "Иванов",
    "tgUsername": "ivanov",
    "tgImageUrl": "https://example.com/avatar.jpg"
  },
  "participantsCount": 5,
  "iamParticipant": true,
  "createdAt": "2023-07-01T12:00:00Z",
  "updatedAt": "2023-07-01T12:00:00Z"
}
```

#### Создание события

```
POST /api/v1/events
```

**Тело запроса:**
```json
{
  "name": "Поход в горы",
  "date": "2023-07-15T10:00:00Z",
  "description": "Поход в горы на выходных",
  "maxParticipants": 10
}
```

**Пример ответа:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "success"
}
```

#### Обновление события

```
PUT /api/v1/events/{eventId}
```

**Тело запроса:**
```json
{
  "name": "Поход в горы (обновлено)",
  "date": "2023-07-16T10:00:00Z",
  "description": "Поход в горы на выходных (обновленное описание)",
  "maxParticipants": 15
}
```

**Пример ответа:**
```json
{
  "status": "success"
}
```

#### Отмена события

```
PATCH /api/v1/events/{eventId}/cancel
```

**Пример ответа:**
```json
{
  "status": "success"
}
```

#### Завершение события

```
PATCH /api/v1/events/{eventId}/finish
```

**Тело запроса:**
```json
{
  "summary": "Мероприятие прошло успешно"
}
```

**Пример ответа:**
```json
{
  "status": "success"
}
```

### Участие в событиях

#### Регистрация на мероприятие

```
POST /api/v1/events/{eventId}/register
```

**Пример ответа:**
```json
{
  "status": "success"
}
```

#### Отмена регистрации

```
DELETE /api/v1/events/{eventId}/unregister
```

**Пример ответа:**
```json
{
  "status": "success"
}
```

#### Удаление участника (только для автора события)

```
DELETE /api/v1/events/{eventId}/participants/{participantId}
```

**Пример ответа:**
```json
{
  "status": "success"
}
```

#### Получение списка участников события

```
GET /api/v1/events/{eventId}/participants
```

**Пример ответа:**
```json
{
  "participants": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "firstName": "Иван",
      "lastName": "Иванов",
      "tgUsername": "ivanov",
      "tgImageUrl": "https://example.com/avatar.jpg"
    },
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "firstName": "Петр",
      "lastName": "Петров",
      "tgUsername": "petrov",
      "tgImageUrl": "https://example.com/avatar2.jpg"
    }
  ]
}
```

## Коды ошибок

| Код  | Описание                     |
|------|------------------------------|
| 400  | Неверный запрос              |
| 401  | Не авторизован               |
| 403  | Доступ запрещен              |
| 404  | Не найдено                   |
| 500  | Внутренняя ошибка сервера    |

## Статусы событий

| Статус    | Описание               |
|-----------|------------------------|
| ACTIVE    | Активное               |
| CANCELED  | Отмененное             |
| FINISHED  | Завершенное            |

## Примеры использования

### Пример создания события и регистрации участника

1. Создание события:
```
POST /api/v1/events
Authorization: <telegram_token>
Content-Type: application/json

{
  "name": "Поход в горы",
  "date": "2023-07-15T10:00:00Z",
  "description": "Поход в горы на выходных",
  "maxParticipants": 10
}
```

2. Регистрация на событие:
```
POST /api/v1/events/a1b2c3d4-e5f6-7890-abcd-ef1234567890/register
Authorization: <telegram_token>
``` 