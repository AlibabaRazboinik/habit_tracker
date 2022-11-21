![jwt](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)

# Трекер привычек

1. Создать базовую архитектуру приложения. Реализовать сущности и репозиторий для CRUD операций
    1. User (поля: userId, username, password, habitListId)
    2. Habit (поля: listId, name, description, priority, colorId, interval, repeats) 
    3. HabitList (поля: listId, name)
    4. Colors (поля: colorId, colorCode)
2. Добавить авторизацию/аутентификацию/регистрацию (spring security)
3. crud операции с привычками + добавить swagger 
![схема запросов](https://sun9-88.userapi.com/impg/qKCBFcNbPy9bFTHYUo46jkP1PFZSUrkV5KQiug/rYUOznOjPRY.jpg?size=1280x599&quality=96&sign=dddc9a98260353489150229dbc5c5a2b&type=album)

4. Клиент на android (kotlin)
5. Добавление PMD стабилизация проекта

# Конфигурация приложения

## Путь к базе данных

Пример:
```properties
spring.datasource.url=jdbc:postgresql://localhost/habit_tracker
```

## Логин и пароль для базы данных

Пример:
```properties
spring.datasource.username=login
spring.datasource.password=password
```

## Описание стратегии работы с бд при перезапуске приложения

```properties
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=update
```

## Секрет JWT

```properties
app.jwt.secret=veryverybigsecretveryverybigsecretveryverybigsecretveryverybigsecretveryverybigsecretveryverybigsecret
```

## Время жизни токена JWT

```properties
app.jwt.expire=600000000000
```