## REST API Documentation

Base URL:

```text
http://localhost:7070
```

### Endpoints

| Method | URL | Request Body | Success Response | Errors |
|---|---|---|---|---|
| GET | `/api/health` | None | `200 OK` | `500 Internal Server Error` |
| POST | `/api/coach` | Coach request | `200 OK` | `400 Bad Request`, `500 Internal Server Error` |
| GET | `/api/exercises` | None | `200 OK` | `500 Internal Server Error` |
| GET | `/api/exercises/{id}` | None | `200 OK` | `400 Bad Request`, `404 Not Found` |
| POST | `/api/exercises` | Exercise request | `201 Created` | `400 Bad Request` |
| PUT | `/api/exercises/{id}` | Exercise request | `200 OK` | `400 Bad Request`, `404 Not Found` |
| DELETE | `/api/exercises/{id}` | None | `204 No Content` | `400 Bad Request`, `404 Not Found` |
| GET | `/api/users` | None | `200 OK` | `500 Internal Server Error` |
| GET | `/api/users/{id}` | None | `200 OK` | `400 Bad Request`, `404 Not Found` |
| POST | `/api/users` | User request | `201 Created` | `400 Bad Request` |
| POST | `/api/users/{id}/workout-program` | None | `201 Created` | `400 Bad Request`, `404 Not Found`, `500 Internal Server Error` |

---

## Health Check

Checks whether the REST API is running.

### Request

```http
GET /api/health
```

### Response

```text
200 OK
```

```json
{
  "status": "ok",
  "message": "API is running"
}
```

---

## AI Coach

### Ask AI Coach

Sends a short fitness-related question to the AI coach.

### Request

```http
POST /api/coach
Content-Type: application/json
```

```json
{
  "prompt": "How long should I rest between sets?"
}
```

### Response

```text
200 OK
```

```json
{
  "answer": "For most strength exercises, rest around 2-3 minutes between heavy sets."
}
```

### Errors

`400 Bad Request`

Returned if the prompt is missing or empty.

`500 Internal Server Error`

Returned if an unexpected error occurs while processing the request or communicating with the AI service.

---

# Exercise API

## Get All Exercises

Returns all exercises stored in the database.

### Request

```http
GET /api/exercises
```

### Response

```text
200 OK
```

```json
[
  {
    "id": 3,
    "name": "Bench Press",
    "muscleGroup": "CHEST",
    "sets": 4,
    "reps": 8
  }
]
```

---

## Get Exercise By ID

Returns an exercise with the given ID.

### Request

```http
GET /api/exercises/{id}
```

Example:

```http
GET /api/exercises/3
```

### Response

```text
200 OK
```

```json
{
  "id": 3,
  "name": "Bench Press",
  "muscleGroup": "CHEST",
  "sets": 4,
  "reps": 8
}
```

### Errors

`400 Bad Request`

Returned if the ID is invalid.

`404 Not Found`

Returned if no exercise exists with the given ID.

```json
{
  "status": 404,
  "msg": "Exercise not found"
}
```

---

## Create Exercise

Creates and stores a new exercise.

### Request

```http
POST /api/exercises
Content-Type: application/json
```

```json
{
  "name": "Bench Press",
  "muscleGroup": "CHEST",
  "sets": 4,
  "reps": 8
}
```

### Response

```text
201 Created
```

```json
{
  "id": 3,
  "name": "Bench Press",
  "muscleGroup": "CHEST",
  "sets": 4,
  "reps": 8
}
```

### Validation

- `name` must not be empty
- `muscleGroup` is required
- `sets` must be greater than `0`
- `reps` must be greater than `0`

Invalid input returns:

```text
400 Bad Request
```

---

## Update Exercise

Updates an existing exercise.

### Request

```http
PUT /api/exercises/{id}
Content-Type: application/json
```

Example:

```http
PUT /api/exercises/3
```

```json
{
  "name": "Barbell Bench Press",
  "muscleGroup": "CHEST",
  "sets": 5,
  "reps": 5
}
```

### Response

```text
200 OK
```

```json
{
  "id": 3,
  "name": "Barbell Bench Press",
  "muscleGroup": "CHEST",
  "sets": 5,
  "reps": 5
}
```

### Errors

`400 Bad Request`

Returned if the ID or request body is invalid.

`404 Not Found`

Returned if the exercise does not exist.

```json
{
  "status": 404,
  "msg": "Exercise not found"
}
```

---

## Delete Exercise

Deletes an exercise from the database.

### Request

```http
DELETE /api/exercises/{id}
```

Example:

```http
DELETE /api/exercises/3
```

### Response

```text
204 No Content
```

No response body is returned.

### Errors

`400 Bad Request`

Returned if the ID is invalid.

`404 Not Found`

Returned if the exercise does not exist.

```json
{
  "status": 404,
  "msg": "Exercise not found"
}
```

---

## Exercise JSON Format

### Request DTO

```json
{
  "name": "Bench Press",
  "muscleGroup": "CHEST",
  "sets": 4,
  "reps": 8
}
```

### Response DTO

```json
{
  "id": 3,
  "name": "Bench Press",
  "muscleGroup": "CHEST",
  "sets": 4,
  "reps": 8
}
```

### Valid Muscle Groups

```text
CHEST
BACK
SHOULDERS
BICEPS
TRICEPS
LEGS
CORE
```

---

# User API

## Get All Users

Returns all users stored in the database.

### Request

```http
GET /api/users
```

### Response

```text
200 OK
```

Example:

```json
[
  {
    "id": 3,
    "name": "Test User",
    "email": "test@test.com",
    "age": 24,
    "height": 180.0,
    "weight": 80.0,
    "experienceLevel": "BEGINNER",
    "trainingDaysPerWeek": 4,
    "goal": "MUSCLE_GAIN",
    "workoutProgramId": 5
  }
]
```

---

## Get User By ID

Returns a user with the given ID.

### Request

```http
GET /api/users/{id}
```

Example:

```http
GET /api/users/3
```

### Response

```text
200 OK
```

```json
{
  "id": 3,
  "name": "Test User",
  "email": "test@test.com",
  "age": 24,
  "height": 180.0,
  "weight": 80.0,
  "experienceLevel": "BEGINNER",
  "trainingDaysPerWeek": 4,
  "goal": "MUSCLE_GAIN",
  "workoutProgramId": 5
}
```

If the user does not yet have a workout program:

```json
{
  "workoutProgramId": null
}
```

### Errors

`400 Bad Request`

Returned if the ID is invalid.

`404 Not Found`

Returned if no user exists with the given ID.

```json
{
  "status": 404,
  "msg": "User not found"
}
```

---

## Create User

Creates and stores a new user.

### Request

```http
POST /api/users
Content-Type: application/json
```

```json
{
  "name": "Test User",
  "email": "test@test.com",
  "age": 24,
  "height": 180,
  "weight": 80,
  "experienceLevel": "BEGINNER",
  "trainingDaysPerWeek": 4,
  "goal": "MUSCLE_GAIN"
}
```

### Response

```text
201 Created
```

```json
{
  "id": 3,
  "name": "Test User",
  "email": "test@test.com",
  "age": 24,
  "height": 180.0,
  "weight": 80.0,
  "experienceLevel": "BEGINNER",
  "trainingDaysPerWeek": 4,
  "goal": "MUSCLE_GAIN",
  "workoutProgramId": null
}
```

### Validation

- `name` must not be empty
- `email` must not be empty
- `age` must be greater than `0`
- `height` must be greater than `0`
- `weight` must be greater than `0`
- `experienceLevel` is required
- `trainingDaysPerWeek` must be between `1` and `7`
- `goal` is required

Invalid input returns:

```text
400 Bad Request
```

### Valid Experience Levels

```text
BEGINNER
INTERMEDIATE
ADVANCED
```

### Valid Training Goals

```text
MUSCLE_GAIN
STRENGTH
WEIGHT_LOSS
GENERAL_FITNESS
```

---

# AI Workout Program Generation

## Generate Workout Program For User

Generates a personalized workout program using the AI service.

The user's stored profile information is used to generate the program:

- age
- height
- weight
- experience level
- training goal
- training days per week

The generated exercises and workout program are saved in the database, and the generated workout program is assigned to the user.

### Request

```http
POST /api/users/{id}/workout-program
```

Example:

```http
POST /api/users/3/workout-program
```

No request body is required.

### Response

```text
201 Created
```

Example:

```json
{
  "id": 5,
  "name": "Beginner Muscle Gain 4-Day Split",
  "description": "A balanced 4-day upper/lower workout routine designed for beginners to build muscle safely and effectively.",
  "trainingDaysPerWeek": 4,
  "exercises": [
    {
      "id": 18,
      "name": "Barbell Bench Press",
      "muscleGroup": "CHEST",
      "sets": 3,
      "reps": 8
    },
    {
      "id": 20,
      "name": "Lat Pulldown",
      "muscleGroup": "BACK",
      "sets": 3,
      "reps": 10
    },
    {
      "id": 26,
      "name": "Barbell Squat",
      "muscleGroup": "LEGS",
      "sets": 3,
      "reps": 8
    }
  ]
}
```

After generation, the generated program is linked to the user.

For example:

```http
GET /api/users/3
```

may return:

```json
{
  "id": 3,
  "name": "Test User",
  "email": "test@test.com",
  "age": 24,
  "height": 180.0,
  "weight": 80.0,
  "experienceLevel": "BEGINNER",
  "trainingDaysPerWeek": 4,
  "goal": "MUSCLE_GAIN",
  "workoutProgramId": 5
}
```

### Errors

`400 Bad Request`

Returned if the user ID is invalid.

`404 Not Found`

Returned if no user exists with the given ID.

```json
{
  "status": 404,
  "msg": "User not found"
}
```

`500 Internal Server Error`

Returned if an unexpected error occurs while generating or saving the workout program.

---

# Status Codes

| Status Code | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | A new resource was created successfully |
| `204 No Content` | A resource was deleted successfully |
| `400 Bad Request` | Request data or a path parameter was invalid |
| `404 Not Found` | Requested resource could not be found |
| `500 Internal Server Error` | An unexpected server-side error occurred |