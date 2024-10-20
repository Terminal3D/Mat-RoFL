
# MAT для грамматики LISP

### Способы взаимодействия

Сервер генератора предоставляет три API-эндпоинта:

1. `POST /generate` — генерация автомата в заданном режиме (easy/normal/hard). Необходимо выполнить перед следующими запросами.
2. `POST /checkWord` — проверка слова на принадлежность автомату.
3. `POST /checkTable` — проверка таблицы на соответсвие угаданном автомату.

---

`*:8080/generate`
### Пример входных данных
```json
{
  "mode": "normal"
}
```
### Пример выходных данных
```json
{
  "maxLexemeSize": 3,
  "maxBracketNesting": 2
}
```
---

`*:8080/checkWord`
### Пример входных данных

```json
{
  "word": "012337712886"
}
```
### Пример выходных данных
```json
{
    "response": 1
}
```
---
`*:8080/checkTable`
Возвращает "true", если таблица соответсвует автомату или контрпример в противном случае.
### Пример входных данных
```json
{
  "main_prefixes": "ε b a ba bb",
  "non_main_prefixes": "aa ab baa bab bba bbb",
  "suffixes": "ε a ba aba",
  "table": "0 0 0 1 1 0 0 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0"
}
```

### Пример выходных данных
```json
{
  "response": "asa"
}
```
