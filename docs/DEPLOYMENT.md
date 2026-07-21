# Hướng dẫn Triển khai Hệ thống trên Production (Production Deployment Guide)

## 1. Yêu cầu Hệ thống Server
- **OS**: Ubuntu 22.04 LTS / Debian 12 / RHEL 9
- **CPU**: Tối thiểu 2 vCPU
- **RAM**: Tối thiểu 4 GB
- **Disk**: 40 GB SSD
- **Phần mềm**: Docker, Nginx, Certbot (SSL)

---

## 2. Thiết lập MySQL 8 Database Server
Khởi tạo MySQL 8 với bộ mã hóa utf8mb4:
```sql
CREATE DATABASE pho_bo_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'phobo_app'@'%' IDENTIFIED BY '${STRONG_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON pho_bo_management.* TO 'phobo_app'@'%';
FLUSH PRIVILEGES;
```

---

## 3. Biến Môi trường Production (.env)
Thiết lập đầy đủ các biến môi trường bắt buộc trên Server Production:
```env
# Backend Environment
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://production-db-host:3306/pho_bo_management?useSSL=true&requireSSL=true&serverTimezone=Asia/Ho_Chi_Minh
SPRING_DATASOURCE_USERNAME=phobo_app
SPRING_DATASOURCE_PASSWORD=${STRONG_DB_PASSWORD}

JWT_ACCESS_SECRET=${STRONG_JWT_ACCESS_SECRET_MIN_512_BITS}
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_SECRET=${STRONG_JWT_REFRESH_SECRET_MIN_512_BITS}
JWT_REFRESH_EXPIRATION=604800000

BACKEND_PORT=8080

# Frontend Environment
NEXT_PUBLIC_API_URL=https://api.phobo.yourdomain.com
```

---

## 4. Chạy Database Migration & Build Backend

1. **Build JAR Artifact**:
```bash
cd backend
./gradlew clean bootJar
```

2. **Chạy Spring Boot App**:
Khi ứng dụng khởi chạy, Flyway sẽ tự động kiểm tra và thực thi các migrations từ `V1` đến `V10` trên MySQL:
```bash
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.jpa.hibernate.ddl-auto=validate \
  --spring.jpa.show-sql=false
```

---

## 5. Build & Khởi Chạy Frontend Next.js

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm build
pnpm start -p 3000
```

---

## 6. Cấu hình Reverse Proxy Nginx & HTTPS

Cấu hình `/etc/nginx/sites-available/phobo.conf`:
```nginx
server {
    server_name phobo.yourdomain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}

server {
    server_name api.phobo.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Cấu hình SSE (Server-Sent Events)
    location /api/v1/sse/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Connection '';
        proxy_buffering off;
        proxy_cache off;
    }
}
```

Kích hoạt SSL miễn phí với Certbot:
```bash
sudo certbot --nginx -d phobo.yourdomain.com -d api.phobo.yourdomain.com
```

---

## 7. Kiểm tra Sức khỏe Hệ thống (Health Check)
- **Actuator Health Check Endpoint**: `GET https://api.phobo.yourdomain.com/actuator/health`
- **Swagger Documentation**: `GET https://api.phobo.yourdomain.com/swagger-ui/index.html`

---

## 8. Database Backup & Restore

### Backup
```bash
mysqldump -u phobo_app -p'${STRONG_DB_PASSWORD}' --single-transaction --quick pho_bo_management > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restore
```bash
mysql -u phobo_app -p'${STRONG_DB_PASSWORD}' pho_bo_management < backup_20260721_120000.sql
```

---

## 9. Quy trình Rollback Application

1. **Rollback Backend/Frontend Code**:
Khi phát hiện lỗi nghiêm trọng ở bản build mới, dừng service ứng dụng hiện tại và khởi chạy lại artifact JAR/Next.js build trước đó.

> [!WARNING]
> **CẢNH BÁO NGUY HIỂM VỀ MIGRATION DATABASE**:
> Tuyệt đối KHÔNG rollback các script Flyway (V1-V10) bằng cách thủ công hoặc xóa dòng trong bảng `flyway_schema_history` trên MySQL Production! Việc làm này có thể làm sai lệch dữ liệu kinh doanh, hỏng checksum của Flyway và gây mất mát dữ liệu không thể phục hồi. Mọi thay đổi schema sửa lỗi bắt buộc phải được thực hiện thông qua script migration tiến (`V11__...sql`).
