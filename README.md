# version-resolver

Gradle-плагин, который задаёт версию проекта по git-тегам.

Версия имеет вид `a.b.N`:

- `a.b` берутся из ближайшего достижимого тега вида `a.b.x-dev` (например `1.20.x-dev`);
- `N` — число коммитов после этого тега (`git rev-list --count <тег>..HEAD`).

Пример: тег `1.20.x-dev` и 10 коммитов после него дают версию `1.20.10`.
Если git недоступен или подходящего тега нет, версия — `0.0.0-SNAPSHOT`.
Переопределить версию можно свойством `-PsdkVersion=1.2.3`.

Плагин добавляет в корневой проект задачу `printVersion`, она печатает версию:

```bash
./gradlew -q printVersion
```

Для расчёта нужна полная история с тегами. В GitHub Actions:

```yaml
- uses: actions/checkout@v4
  with:
    fetch-depth: 0
    fetch-tags: true
```

## Подключение

Координаты: `libs.common:version-resolver:<версия>`, id плагина: `libs.common.version-resolver`.

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/wefortis/version-resolver")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Корневой `build.gradle.kts`:

```kotlin
buildscript {
    dependencies {
        classpath("libs.common:version-resolver:1.0.0")
    }
}

apply {
    plugin("libs.common.version-resolver")
}
```

GitHub Packages требует токен даже для чтения: локально нужен PAT со scope `read:packages`. В GitHub Actions
`GITHUB_TOKEN` читает пакет другого репозитория, только если в Package settings → «Manage Actions access»
добавлен этот репозиторий; иначе используйте PAT в секрете.

## Публикация

Тег `vX.Y.Z` запускает workflow `Publish` и публикует плагин версии `X.Y.Z` в GitHub Packages
(или запустите его вручную с указанием версии). Локально: `GITHUB_ACTOR` и `GITHUB_TOKEN` (scope `write:packages`)
и `./gradlew publish -PpluginVersion=1.0.0`. Проверка без публикации: `./gradlew publishToMavenLocal`.

Одну и ту же версию GitHub Packages повторно не принимает (409), поэтому каждая публикация — новая версия.
