# Инструкция по деплою на AWS EC2

Этот документ содержит инструкции по настройке нового инстанса AWS EC2 для деплоя бэкенда GeoHod.

## Предварительные требования

1. Инстанс AWS EC2 (рекомендуется t2.micro или выше)
2. Настроенная группа безопасности с открытыми портами:
   - 22 (SSH)
   - 8080 (для бэкенда)
   - 80/443 (если планируется использовать Nginx)

## Настройка инстанса AWS EC2

### 1. Подключение к инстансу

```bash
ssh -i your-key.pem ec2-user@your-ec2-ip
```

### 2. Установка Docker

```bash
# Обновление системы
sudo yum update -y

# Установка Docker
sudo amazon-linux-extras install docker -y
sudo service docker start
sudo systemctl enable docker

# Добавление пользователя ec2-user в группу docker
sudo usermod -a -G docker ec2-user

# Перезапуск сессии для применения изменений
exit
```

Повторно подключитесь к серверу.

### 3. Создание директории для деплоя

```bash
mkdir -p /tmp/geohod-backend
```

### 4. Настройка GitHub Actions

В настройках репозитория GitHub добавьте следующие секреты:

- `AWS_EC2_HOST`: Публичный IP-адрес или DNS-имя вашего EC2 инстанса
- `AWS_EC2_USER`: Имя пользователя для SSH-подключения (обычно `ec2-user`)
- `AWS_EC2_SSH_KEY`: Приватный SSH-ключ для подключения к EC2
- `SECRET_VARS`: Переменные окружения для приложения в формате `KEY1=VALUE1,KEY2=VALUE2,...`

Пример переменных окружения:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/geohod,SPRING_DATASOURCE_USERNAME=geohod,SPRING_DATASOURCE_PASSWORD=your-password,SPRING_PROFILES_ACTIVE=prod
```

### 5. (Опционально) Настройка базы данных PostgreSQL

Если вы используете локальную базу данных PostgreSQL на EC2:

```bash
# Устанавливаем Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Создаем файл docker-compose.yml
cat > ~/docker-compose.yml << 'EOF'
version: '3'
services:
  postgres:
    image: 'postgres:latest'
    container_name: geohod-postgres
    environment:
      - 'POSTGRES_DB=geohod'
      - 'POSTGRES_PASSWORD=вашпароль'
      - 'POSTGRES_USER=geohod'
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - '5432:5432'
    networks:
      - geohod
    restart: always

networks:
  geohod:
    external: true

volumes:
  postgres-data:
EOF

# Создаем сеть Docker
docker network create geohod

# Запускаем PostgreSQL
docker-compose up -d
```

### 6. Деплой

После настройки, запустите GitHub Action workflow для деплоя:

1. Перейдите в раздел "Actions" вашего GitHub репозитория
2. Выберите "Build and Deploy to AWS"
3. Нажмите "Run workflow"
4. Выберите нужную ветку/тег и нажмите "Run workflow"

## Проверка деплоя

После успешного деплоя вы можете проверить, что приложение работает:

```bash
# Проверка контейнера
docker ps

# Проверить логи
docker logs geohod-backend

# Проверить доступность API
curl http://localhost:8080/actuator/health
```

## Устранение неполадок

### Проблемы с Docker

```bash
# Перезапуск Docker
sudo service docker restart

# Перезапуск контейнера
docker restart geohod-backend

# Просмотр логов
docker logs geohod-backend
```

### Проблемы с сетью

Проверьте настройки группы безопасности AWS EC2, чтобы убедиться, что нужные порты открыты. 