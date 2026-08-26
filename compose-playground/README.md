# Compose Multiplatform — гайд для новичка в Kotlin

Этот модуль — новый GUI плеера, который **портируется на Android/iOS/Desktop/Web**.
Он написан на **Kotlin** и **Compose Multiplatform**. Ниже — объяснение «с нуля», для человека,
который впервые работает с Kotlin-UI.

---

## 1. Что это и почему «самый актуальный метод 2026»

Есть два разных мира UI:

| Подход | Как делается разметка | Платформы |
|--------|----------------------|-----------|
| JavaFX (старый GUI плеера) | FXML (XML) + CSS | только Desktop |
| Swing / AWT | программно или XML | только Desktop |
| **Compose Multiplatform** | **декларативные Kotlin-функции** | **Android + iOS + Desktop + Web** |

Compose Multiplatform (JetBrains, на базе Android Jetpack Compose) — это **декларативный UI**:
ты описываешь, **что** должно быть на экране, а фреймворк сам перерисовывает, когда меняются данные.
Это современный способ, потому что **один и тот же код UI работает на всех платформах** —
в отличие от JavaFX/Swing, которые на телефоне не работают вовсе.

Ключевая идея: **разметки нет отдельным файлом**. UI — это обычные Kotlin-функции.

---

## 2. Как устроен UI (не CSS и не Swing)

В Swing ты писал: `new JButton(...)`, `add(...)`, слушатели.
В JavaFX: FXML-файл + CSS.
В Compose ты пишешь **composable-функции**:

```kotlin
@Composable
fun Greeting(name: String) {
    Text("Привет, $name")
}
```

Аннотация `@Composable` означает «это кусок UI». Внутри — обычные вызовы других composable
(`Text`, `Button`, `Column`, `Row` ...). Компилятор Kotlin превращает это в дерево элементов.

**Стили** задаются не CSS, а `Modifier`-ами и параметрами прямо в коде:

```kotlin
Text(
    "Сейчас играет",
    style = MaterialTheme.typography.titleLarge,   // типографика
    modifier = Modifier.fillMaxWidth().padding(16.dp)  // размеры/отступы
)
```

`Modifier` — это цепочка «украшений»: `.fillMaxWidth()`, `.padding(16.dp)`, `.weight(1f)` и т.д.
`.dp` — единица измерения (как `px`, но плотностно-независимая, важна для мобильных).

**Раскладка** — через контейнеры-композаблы:
- `Column` — вертикально;
- `Row` — горизонтально;
- `Box` — поверх/с перекрытием;
- `LazyColumn` — вертикальный список (аналог `RecyclerView`/`ListView`, «ленивый»).

Компоненты — из **Material 3** (`androidx.compose.material3.*`): `Button`, `OutlinedTextField`, `Text` и т.д.

---

## 3. Состояние (самое важное для понимания)

UI в Compose **реактивный**: когда меняется состояние, экран перерисовывается автоматически.

```kotlin
var current by remember { mutableStateOf("") }
```

- `mutableStateOf("")` — реактивная переменная («состояние»).
- `remember { ... }` — «запомнить значение между перерисовками» (иначе при каждом обновлении создавалось бы заново).
- `by` — делегирование: читаешь/пишешь `current` как обычную строку, а Compose сам видит изменение.

Когда ты делаешь `current = "..."`, все `@Composable`, которые читали `current`, **перерисовываются**.
Никаких `setText()`, `repaint()`, `invalidate()` — как в Swing — не нужно.

Списки — через `mutableStateListOf(...)`, чтобы изменения коллекции тоже были реактивными:

```kotlin
val playlist = remember { mutableStateListOf("Track 01", "Track 02") }
playlist.add("Track 03")   // список на экране обновится сам
```

---

## 4. Разбор `src/commonMain/kotlin/Main.kt`

```kotlin
fun main() = application {                       // точка входа desktop-приложения
    Window(onCloseRequest = ::exitApplication, title = "Compose Playground") {
        MaterialTheme {                           // тема Material 3
            Surface(modifier = Modifier.fillMaxSize()) {
                PlayerDemo()                      // наш экран
            }
        }
    }
}
```

- `application { }` — запускает окно (только desktop-таргет).
- `Window(...)` — само окно.
- `MaterialTheme` / `Surface` — «обёртки» темы и фона.
- `PlayerDemo()` — наша composable-функция с содержимым.

```kotlin
@Composable
fun PlayerDemo() {
    var current by remember { mutableStateOf("") }
    val playlist = remember { mutableStateListOf("Track 01", "Track 02", "Track 03") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Сейчас играет: ${current.ifEmpty { "—" }}", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = current,
                onValueChange = { current = it },   // пишем в состояние — UI перерисуется
                label = { Text("Название трека") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { playlist.add(current); current = "" }) {
                Text("Добавить")
            }
        }

        Text("Плейлист (${playlist.size})", style = MaterialTheme.typography.titleMedium)

        LazyColumn(Modifier.weight(1f)) {
            items(playlist) { track ->
                Text(track, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }
        }
    }
}
```

Что здесь видно:
- `Column` = вертикальный список элементов.
- `Row` = горизонтальный.
- `OutlinedTextField(value, onValueChange, label)` — поле ввода; значение и его изменение — это **состояние**.
- `Button(onClick) { Text(...) }` — кнопка; тело кнопки — тоже composable.
- `items(playlist) { ... }` — проход по реактивному списку и отрисовка каждого элемента.
- `Modifier.weight(1f)` — «занять всё оставшееся место».

---

## 5. Структура проекта (Kotlin Multiplatform)

```
compose-playground/
├── build.gradle.kts          # зависимости и таргеты
├── settings.gradle.kts       # репозитории
└── src/
    └── commonMain/kotlin/    # ОБЩИЙ код для всех платформ
        └── Main.kt
```

Ключевая папка — `commonMain`: код здесь **не привязан к платформе** и переиспользуется
на Android/iOS/Desktop/Web. Сейчас объявлен только `jvm("desktop")` таргет; Android добавится позже.

В `build.gradle.kts` главное:
- `kotlin("multiplatform")` — Kotlin Multiplatform плагин;
- `org.jetbrains.compose` — сам Compose;
- `compose.desktop.currentOs` в `desktopMain` — нативный рантайм (Skiko) для запуска на desktop.

---

## 6. Как собрать и запустить

```powershell
cd compose-playground
gradle build   # компиляция/упаковка
gradle run     # запустить окно
```

> Нужен **Gradle 8.11.1** (или через обёртку `gradlew.bat`). Первый запуск скачивает зависимости.

---

## 7. Куда смотреть дальше

- Официальный туториал: https://www.jetbrains.com/compose-multiplatform/
- Основы Compose (Android): https://developer.android.com/develop/ui/compose
- Kotlin с нуля: https://kotlinlang.org/docs/home.html

**Короткая шпаргалка:**
- UI = `@Composable`-функции, вложенные друг в друга.
- Раскладка = `Column`/`Row`/`Box` + `Modifier`.
- Данные = `remember { mutableStateOf(...) }` / `mutableStateListOf(...)`.
- События = лямбды `onClick = { ... }`, `onValueChange = { ... }`.
