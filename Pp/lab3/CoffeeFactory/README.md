# Фабрика по сборке различных кофеварок — часть 1 (пункты 1–11)

Консольное приложение на Java (JDK 17+, проверено на JDK 21). Внешних библиотек нет.

## Запуск

Из папки проекта (где лежат `src` и `data`):

```
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out coffeefactory.Main
```

Windows (cmd): `dir /s /b src\*.java > sources.txt`, затем `javac -encoding UTF-8 -d out @sources.txt`
и `chcp 65001` перед запуском, чтобы русский текст в консоли отображался правильно.

IntelliJ IDEA: открыть папку как проект, пометить `src` как Sources Root,
в Run Configuration задать Working directory = папка проекта (чтобы находились `data/` и `logs/`).

## Соответствие пунктам задания

| Пункт | Где реализовано |
|---|---|
| 1. `main` только создаёт объекты и вызывает методы, меню — в отдельной функции | `Main` (`startMenu`) |
| 2. Абстрактные классы | `Appliance`, `AbstractStorage`, `AbstractFileHandler` |
| 3. Классы для хранения List и Map (наследники абстрактного) | `ListStorage`, `MapStorage` (TreeMap) |
| 4. Чтение / запись данных в *.txt | `TxtFileReader`, `TxtFileWriter` |
| 5. Класс объекта (наследник абстрактного), Date, get/set, toString | `CoffeeMaker` + `CoffeeMakerType` |
| 6. Меню: добавление / редактирование с консоли, чтение / запись | `ConsoleMenu` |
| 7. Проверка входных данных, запись об ошибке в консоль и `logs/errors.log` | `Validator`, `DateUtil`, `ErrorLogger` |
| 8. Запись объектов в List и Map | `FactoryService` (оба хранилища всегда синхронны) |
| 9. Ввод с консоли, добавление / обновление / удаление, обновление List, Map и txt | `ConsoleMenu` + `FactoryService` |
| 10. Итераторы для вывода | `ConsoleMenu.printList / printMap / printWithIterator` |
| 11. Сортировка: Collections, Comparator, лямбды | `CoffeeMakerComparators`, `AbstractStorage.getSorted` |

## Формат txt-файла

```
id;тип;модель;мощность(Вт);бак(мл);дата выпуска;цена
1;DRIP;Philips HD7432;1000;1200;12.05.2021;49.90
```

Типы: `DRIP`, `ESPRESSO`, `CAPSULE`, `GEYSER`, `AUTOMATIC`. Дата — `дд.мм.гггг`.
Строки с `#` и пустые игнорируются. Некорректная строка читается целиком не будет:
ошибка и сама строка выводятся в консоль и дописываются в `logs/errors.log`.

Файлы данных:
- `data/coffee_makers.txt` — рабочий файл, загружается при старте, обновляется после каждого добавления / изменения / удаления;
- `data/coffee_makers_with_errors.txt` — демонстрация проверки данных (пункт 7). Загрузите его через пункт меню 7.
  Он становится рабочим файлом, поэтому после правок в нём останутся только корректные строки.
