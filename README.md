
# MAT для грамматики LISP

### Способы взаимодействия

Сервер генератора предоставляет три API-эндпоинта:

1. `POST /generate` — генерация автомата в заданном режиме (easy/normal/hard/fixed). Необходимо выполнить перед следующими запросами.
2. `POST /checkWord` — проверка слова на принадлежность автомату.
3. `POST /check-word-batch` - проверка списка слов на принадлежность автомату.
4. `POST /checkTable` — проверка таблицы на соответсвие угаданном автомату.

---

`*:8080/generate`
### Пример входных данных
```json
{
  "mode": "normal"
}
```
```json
{
  "mode": "fixed",
  "size": 543
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

`*:8080/check-word-batch`
### Пример входных данных

```json
{
  "wordList": [
    "0123",
    "1233",
    "4323"
  ]
}
```
### Пример выходных данных
```json
{
    "responseList": [
      true,
      false,
      false
    ]
}
```

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

`response`: Возвращает "true", если таблица соответсвует автомату или контрпример в противном случае.

`type`: **true**, если контрпример такой, что его распознает МАТ, но не распознает автомат по таблице; **false** 
если распознает автомат из таблицы, но не распознает МАТ; **null** если автоматы совпали.
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
  "response": "asa",
  "type": "true"
}
```
