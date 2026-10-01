# Валидация входных данных

Теоретический конспект по валидации входных данных в Spring-приложениях.
Охватывает принципы, слои, аннотации, кастомные constraints, обработку ошибок,
тестирование и эксплуатацию.

## Содержание

- [0. Введение](#0-введение)
- [1. Принципы и границы доверия](#1-принципы-и-границы-доверия)
- [2. Таксономия проверок](#2-таксономия-проверок)
- [3. Bean Validation в Spring](#3-bean-validation-в-spring)
- [4. Аннотации и свои constraints](#4-аннотации-и-свои-constraints)
- [5. Кросс-полевые и агрегатные правила](#5-кросс-полевые-и-агрегатные-правила)
- [6. Входные каналы](#6-входные-каналы)
- [7. Реактивность и асинхронность](#7-реактивность-и-асинхронность)
- [8. Ошибки и контракт ответа](#8-ошибки-и-контракт-ответа)
- [9. Производительность, безопасность, эксплуатация](#9-производительность-безопасность-эксплуатация)
- [10. Тестирование и инструменты](#10-тестирование-и-инструменты)
- [Шпаргалка](#шпаргалка)

---

## 0. Введение

### Что это

**Валидация** — проверка, что данные соответствуют ожиданиям, до того как они
попадут в бизнес-логику. Это контракт между клиентом и сервером: «я принимаю
только то, что описано».

### Зачем это

- **Защита** от некорректных данных и атак (injection, regex-DoS, огромные payload'ы).
- **Раннее обнаружение** проблем — дешевле чинить на входе, чем в БД.
- **Чистая бизнес-логика** — сервис уверен в данных, не проверяет одно и то же.
- **Понятные ошибки** клиенту — что именно не так.

### Где используется

- **REST API** — валидация DTO в контроллерах.
- **Message-driven** — валидация событий из Kafka/JMS.
- **CLI, batch** — валидация входных файлов.
- **Config** — валидация `@ConfigurationProperties`.

### Какие проблемы решает

- **SQL injection, XSS** — через нормализацию и whitelist.
- **Mass assignment** — лишние поля от клиента.
- **Ошибки округления** — `BigDecimal` для денег, не `double`.
- **Дубликаты** — уникальность через БД.
- **Неконсистентные состояния** — межполевые проверки.

---

## 1. Принципы и границы доверия

### Где валидировать

Три уровня — каждый ловит своё:

| Уровень | Что проверяет | Пример |
|---------|---------------|--------|
| **Периметр** (контроллер) | Формат, типы, обязательность | `@NotBlank`, `@Email`, `@Positive` |
| **Домен** (сервис) | Инварианты бизнес-логики | «Сумма перевода ≤ баланс», переходы статусов |
| **БД** | Constraint'ы — последний рубеж | `UNIQUE(email)`, `NOT NULL`, `CHECK(amount > 0)` |

**Правило:** проверяй на каждом уровне — дублирование это не баг, а защита в глубину.

### Trust boundary

**Всё извне — недостоверно**: тело запроса, path, query, headers, cookies,
сообщения из очередей.

**Внутри приложения** — можно доверять (если валидация прошла). Не валидируй
DTO ещё раз в сервисе — он уже валидирован.

### Слои ответственности

```
Синтаксис → Семантика → Существование → Права
  400         422           404          403
```

- **Синтаксис** — JSON, типы, формат. → **400 Bad Request**.
- **Семантика** — `age >= 18`, дата «с-до». → **422 Unprocessable Entity**.
- **Существование** — `userId` есть в БД? → **404 Not Found**.
- **Права** — может ли пользователь это делать? → **403 Forbidden**.

---

## 2. Таксономия проверок

### Синтаксические vs семантические

| Тип | Что | Пример |
|-----|-----|--------|
| **Синтаксис** | Формат поля | `@Email`, `@Pattern`, длина |
| **Семантика** | Правила бизнеса | `startDate < endDate`, допустимые статусы |

### Межполевые (cross-field)

Одно поле зависит от другого:

- `from < to`, `startDate < endDate`.
- `type == "CARD"` → `cardNumber` обязателен.
- `discount ≤ total`.

Реализуется **class-level валидатором** — аннотация на класс, не на поле.

### Агрегатные (object/collection)

Правила на коллекции:

- Сумма элементов ≤ 1000.
- Уникальность в списке (`@UniqueElements`).
- Минимум один элемент (`@NotEmpty`).

### Онлайн vs офлайн

| Тип | Когда | Пример |
|-----|-------|--------|
| **Онлайн** | Мгновенно, без внешних вызовов | `@NotBlank`, `@Email` |
| **Офлайн** | Требует БД/сети | Уникальность `email` |

**Правило:** онлайн — в DTO. Офлайн — в сервисе (отдельно, с таймаутами).

### Нормализация vs валидация

**Сначала нормализуй — потом валидируй:**

- `trim()` — убрать пробелы по краям.
- `toLowerCase()` — для email.
- ASCII-folding — `café` → `cafe`.
- Каноникализация Unicode — разные формы одного символа.

**Пример:** `" alice@example.com "` без trim — невалидный email. С trim — валидный.

---

## 3. Bean Validation в Spring

### Jakarta Validation 3.x

Стандарт **JSR-380**. Реализация — **Hibernate Validator**. Spring встроил поддержку.

**Где применять:**

- `@Valid @RequestBody` — валидация DTO в контроллере.
- `@Validated` на классе — для валидации методов сервиса.
- `@Validated` на `@ConfigurationProperties` — валидация конфига.

### BindingResult vs исключения

| Подход | Когда использовать |
|--------|-------------------|
| `BindingResult` в методе | Хочешь обработать ошибки сам, без исключения |
| Исключение (`MethodArgumentNotValidException`) | Стандарт: `@Valid` без `BindingResult` → Spring бросает исключение |

**Правило:** не пиши `BindingResult` в методе, если не собираешься его
обрабатывать тут же. Используй `@RestControllerAdvice`.

### Вложенные DTO и коллекции

Каскадная валидация — через `@Valid`:

```java
public record OrderRequest(
        @NotBlank String customerId,

        @NotEmpty
        @Valid                              // валидирует каждый элемент списка
        List<OrderItemRequest> items
) {}
```

**Без** `@Valid` на поле — элементы списка не валидируются.

---

## 4. Аннотации и свои constraints

### Базовые аннотации

| Аннотация | Проверяет |
|-----------|-----------|
| `@NotNull` | Значение не `null` |
| `@NotBlank` | Строка не `null`, не пустая, не пробелы |
| `@NotEmpty` | Не `null` и не пусто (для коллекций/строк) |
| `@Size(min, max)` | Длина строки / коллекции |
| `@Pattern(regexp)` | Regex |
| `@Email` | Формат email |
| `@Positive` / `@PositiveOrZero` | Число > 0 / ≥ 0 |
| `@Negative` / `@NegativeOrZero` | Число < 0 / ≤ 0 |
| `@Min` / `@Max` | Диапазон |
| `@DecimalMin` / `@DecimalMax` | Диапазон для BigDecimal |
| `@Digits(integer, fraction)` | Разряды числа |
| `@Past` / `@Future` / `@PastOrPresent` / `@FutureOrPresent` | Дата в прошлом/будущем |
| `@AssertTrue` / `@AssertFalse` | Boolean условие |

### Кастомный constraint

```java
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface ValidPhone {
    String message() default "invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;         // null — не наша забота (@NotNull)
        return value.matches("^\\+?[1-9]\\d{1,14}$");
    }
}
```

Тестируется **unit-тестом** — быстро, без Spring.

### Сообщения и локализация

- `message = "..."` в аннотации — дефолт.
- Message bundle — файлы `ValidationMessages.properties` / `ValidationMessages_ru.properties`.
- Placeholders — `{min}`, `{max}`, `${validatedValue}`.
- Локализация — по `Accept-Language` или `Locale`.

### Группы и сценарии

Разные правила для create и update:

```java
public interface OnCreate {}
public interface OnUpdate {}

public record UserRequest(
        @Null(groups = OnCreate.class)      // при создании id должен быть null
        @NotNull(groups = OnUpdate.class)   // при обновлении — обязателен
        Long id,

        @NotBlank(groups = { OnCreate.class, OnUpdate.class })
        String name
) {}
```

```java
@PostMapping
ResponseEntity<?> create(@Validated(OnCreate.class) @RequestBody UserRequest req) { ... }

@PutMapping("/{id}")
ResponseEntity<?> update(@Validated(OnUpdate.class) @RequestBody UserRequest req) { ... }
```

`@GroupSequence` — порядок групп (fail-fast: первая группа → ошибка → стоп).

---

## 5. Кросс-полевые и агрегатные правила

### Class-level валидаторы

```java
@ValidDateRange
public record BookingRequest(LocalDate from, LocalDate to) {}

@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange { ... }

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, BookingRequest> {
    @Override
    public boolean isValid(BookingRequest value, ConstraintValidatorContext ctx) {
        if (value.from() == null || value.to() == null) return true;
        return !value.to().isBefore(value.from());
    }
}
```

### Вычисляемые ограничения

- Сумма элементов в коллекции.
- Уникальность в списке (свой валидатор).
- Согласованность валют (все элементы — одна валюта).

### Ограничение состояний

Конечный автомат домена:

```java
Map<Status, Set<Status>> ALLOWED = Map.of(
        NEW,       Set.of(PAID, CANCELLED),
        PAID,      Set.of(SHIPPED, REFUNDED),
        SHIPPED,   Set.of(DELIVERED),
        DELIVERED, Set.of()
);

if (!ALLOWED.get(order.status()).contains(newStatus)) {
    throw new InvalidStatusTransitionException(...);
}
```

### Проверки существования

Внешние сущности — через сервисы/репозитории:

- Отдельный валидатор с доступом к БД.
- Таймаут — не висеть на проверке.
- Кэш — если данные меняются редко.
- Деградация — если БД недоступна, что делать (reject/allow).

---

## 6. Входные каналы

### Path / Query / Header / Cookie

- **Path** — `@PathVariable` с типом (`Long`, `UUID`). 400 при несоответствии.
- **Query** — `@RequestParam` с `required`, `defaultValue`, `@Min`/`@Max`.
- **Header** — `@RequestHeader` с `required`, валидация формата.
- **Cookie** — `@CookieValue` с проверкой подписи/формата.

**Enum'ы** — Spring конвертирует сам. Невалидное значение → 400.

### Тело запроса

- **JSON DTO** — валидация через `@Valid`.
- **Multipart** — проверка MIME, размера, расширения.
- **Потоки** — стриминг, не загружать всё в память.

### Ограничения на размер/глубину

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB
```

**JSON** — Jackson имеет лимиты по умолчанию (глубина, размер строки).
Можно настроить через `StreamReadConstraints`.

**Защита от regex-DoS** — сложные regex на больших строках могут вызвать
CPU-payload. Ограничивай длину строки до regex.

### Идемпотентность

`Idempotency-Key` — заголовок с UUID от клиента. Сервер сохраняет результат по
ключу. Повторный запрос с тем же ключом — тот же ответ, без повторного действия.

Критично для **POST** платежей, создания заказов, отправки email.

---

## 7. Реактивность и асинхронность

### WebFlux

- `WebExchangeBindException` — аналог `MethodArgumentNotValidException`.
- Функциональные маршруты — валидация через свой handler/filter.
- `Mono`/`Flux` — ошибки в цепочке.

### Reactor

- Не блокировать в валидации — никаких JDBC в reactor-потоке.
- Таймауты/ретраи — `.timeout(Duration.ofSeconds(5)).retry(3)`.

### Сообщения (Kafka/JMS)

- Валидация события до обработки — на границе.
- Схемы — JSON Schema/Avro/Protobuf — строгий контракт.
- DLQ — «ядовитые» сообщения не ломают consumer.

---

## 8. Ошибки и контракт ответа

### Единый формат — ProblemDetail (RFC 7807)

```json
{
  "type": "https://api.example.com/problems/validation-error",
  "title": "Validation failed",
  "status": 422,
  "detail": "Validation failed for 2 field(s)",
  "instance": "/api/registration",
  "violations": [
    { "field": "email", "code": "Email", "message": "email must be valid" }
  ],
  "timestamp": "...",
  "requestId": "abc-123"
}
```

### Разделение статусов

| Статус | Когда |
|--------|-------|
| **400** | Некорректный формат (кривой JSON, тип) |
| **422** | Семантическая ошибка (правила валидации) |
| **409** | Конфликт (уникальность, состояние) |
| **404** | Ресурс не найден |
| **403** | Нет прав |

### Коды ошибок

Machine-readable — клиент switch-case по `code`. Не парсить `message`.

Не утечка PII: не включать email, телефон, имя в сообщении без маскировки.

### Локализация и корреляция

- Сообщения — из message bundle, по `Accept-Language`.
- `traceId`/`requestId` — связка с логами.
- `retryable: true/false` — стоит ли клиенту повторять.

---

## 9. Производительность, безопасность, эксплуатация

### Fail-fast vs все ошибки

| Подход | Плюсы | Минусы |
|--------|-------|--------|
| **Fail-fast** | Быстро, меньше нагрузки | Клиент итерирует по одной |
| **Собрать все** | Клиент правит сразу | Дороже, сложнее |

**Лимит** на количество нарушений — не возвращай 1000 ошибок на большой список.

### Дедуп и rate-limit

- Одинаковые ошибки — дедуп.
- Логи — не спамить одинаковым сообщением 1000 раз/сек.
- Метрики — считать топ правил и всплески.

### Regex-DoS

ReDoS — катастрофический backtracking на определённых регексах:

- `(a+)+$` — опасный regex.
- Ограничивай длину строки до regex.
- Используй linear-time движки (RE2/J) для сложных случаев.

### БД-constraint'ы — последняя линия

- `UNIQUE` — уникальность (email).
- `NOT NULL` — обязательность.
- `CHECK` — простые правила (`amount > 0`).
- `FOREIGN KEY` — существование ссылок.

Даже если валидация пропустила — БД поймает.

### PATCH — частичные обновления

Валидируй только присланные поля:

- JSON Merge Patch (RFC 7396).
- JSON Patch (RFC 6902).
- `Optional<Field>` в DTO — различать «не прислано» и «прислано null».

---

## 10. Тестирование и инструменты

### Unit-тесты валидаторов

```java
@Test
void phoneValidator_rejectsInvalid() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<MyDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
}
```

Быстро, без Spring контекста.

### Slice-тесты контроллеров

- `@WebMvcTest` + `MockMvc` — проверить 400/422, структуру ProblemDetail.
- `WebTestClient` — для WebFlux.
- Проверка локализации — с `Accept-Language: ru`.

### Контракт-тесты

- OpenAPI — сгенерированный из кода.
- JSON Schema — генеративные проверки (schemathesis).
- Синхронизация — и код, и спека из одного источника.

### Метрики и наблюдаемость

- Доля отклонённых запросов — метрика.
- Топ правил — что чаще нарушают.
- Алерт — всплеск валидационных ошибок (атака или сломанный клиент).
- `traceId` — связка ответа и логов.

---

## Шпаргалка

| Понятие | Суть |
|---------|------|
| **Trust boundary** | Всё извне — недостоверно |
| **Синтаксис vs семантика** | 400 vs 422 |
| **Cross-field** | Class-level валидатор |
| **`@Valid` vs `@Validated`** | Поля DTO vs методы сервиса |
| **`BindingResult`** | Без исключения — обработать сам |
| **Группы** | Разные правила для create/update |
| **Нормализация** | Сначала trim/lowercase, потом валидация |
| **ReDoS** | Опасные regex — ограничивай длину |
| **DB constraints** | Последний рубеж |
| **ProblemDetail** | Единый формат ошибок |
| **`traceId`** | Связка с логами |

---

**Валидация — тема, где мелочи решают.** Хорошая валидация делает API
надёжным, безопасным и удобным для клиента. Плохая — пропускает дыры, даёт
непонятные ошибки и усложняет жизнь.