# Android SpeedTest App - 完整设置指南

## 🎯 概述

这是一个完整的网速测试应用，包含Android客户端和PHP后端服务器。

---

## 📱 Part 1: Android 客户端设置

### 1.1 构建应用

```bash
cd /path/to/android-test-speed

# 停止所有daemon进程（重要！）
./gradlew --stop
pkill -f "kotlin" || true

# 清理并构建
./gradlew clean assembleDebug

# 安装到设备/模拟器
./gradlew installDebug
```

### 1.2 配置服务器地址

应用已内置以下默认服务器：

1. **localhost:8080** - 用于真机测试（手机上运行PHP服务器）
2. **10.0.2.2:8080** - 用于Android模拟器（指向主机）
3. **192.168.1.100:8080** - 局域网服务器（需修改为实际IP）

#### 修改服务器IP地址

编辑 `app/src/main/java/com/speedtest/app/data/repository/ServerRepositoryImpl.kt`:

```kotlin
Server(
    id = "server_local_network",
    name = "Local Network Server",
    host = "你的服务器IP",  // 改这里
    port = 8080,
    ...
)
```

---

## 🖥️ Part 2: PHP 后端服务器设置

### 方式1: PHP内置服务器（最简单，适合开发测试）

```bash
cd backend
php -S 0.0.0.0:8080
```

**访问测试**: `http://localhost:8080/ping`

### 方式2: Nginx + PHP-FPM（推荐生产环境）

#### 2.1 安装依赖

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install nginx php-fpm

# CentOS/RHEL
sudo yum install nginx php-fpm
```

#### 2.2 配置Nginx

```bash
# 复制配置文件
sudo cp backend/nginx.conf /etc/nginx/sites-available/speedtest

# 启用站点
sudo ln -s /etc/nginx/sites-available/speedtest /etc/nginx/sites-enabled/

# 复制PHP文件
sudo mkdir -p /var/www/speedtest
sudo cp backend/index.php /var/www/speedtest/
sudo chown -R www-data:www-data /var/www/speedtest

# 重启服务
sudo systemctl restart nginx
sudo systemctl restart php-fpm
```

#### 2.3 验证

```bash
curl http://localhost:8080/ping
# 应该返回: {"status":"ok",...}
```

### 方式3: Apache + PHP（备选方案）

```bash
# 安装Apache和PHP
sudo apt-get install apache2 php libapache2-mod-php

# 配置虚拟主机
sudo cp backend/apache.conf /etc/apache2/sites-available/speedtest.conf
sudo a2ensite speedtest
sudo systemctl restart apache2
```

---

## 📲 Part 3: 真机测试配置

### 3.1 手机和电脑在同一WiFi

1. **查找电脑IP**:
```bash
# Linux/Mac
ip addr show | grep inet

# Windows
ipconfig
```

2. **假设电脑IP是 192.168.1.100**:

   - 在电脑上启动PHP服务器: `php -S 0.0.0.0:8080`
   - Android应用会自动使用 `server_local_network` (192.168.1.100:8080)

3. **修改Android应用中的IP**（如果需要）:

编辑 `ServerRepositoryImpl.kt` 第161行:
```kotlin
host = "192.168.1.100",  // 改成你的电脑IP
```

### 3.2 手机本地测试（Termux）

在Android手机上安装 Termux，然后:

```bash
# 在Termux中
pkg install php
cd /storage/emulated/0/Download/backend
php -S localhost:8080
```

Android应用使用 `server_localhost` 即可。

---

## 🔧 Part 4: 模拟器测试配置

### 4.1 Android Studio模拟器

1. 在电脑上启动PHP服务器:
```bash
cd backend
php -S 0.0.0.0:8080
```

2. 模拟器会自动使用 `10.0.2.2:8080` 访问主机

3. 验证连接:
```bash
# 在模拟器的Terminal Emulator中
curl http://10.0.2.2:8080/ping
```

---

## 🧪 Part 5: 测试后端API

### 测试所有端点

```bash
# 1. Ping测试
curl http://YOUR_IP:8080/ping

# 2. 下载测试 (下载1MB数据)
curl http://YOUR_IP:8080/download?size=1048576 -o /dev/null -w "Downloaded in %{time_total}s\n"

# 3. 上传测试 (上传1MB数据)
dd if=/dev/zero bs=1M count=1 2>/dev/null | \
  curl -X POST http://YOUR_IP:8080/upload \
  --data-binary @- \
  -w "Upload time: %{time_total}s\n"

# 4. 服务器列表
curl http://YOUR_IP:8080/servers

# 5. 服务器信息
curl http://YOUR_IP:8080/info
```

---

## ❓ 常见问题

### Q1: 应用显示"No network connection"

**解决方案**:
1. 检查手机WiFi是否开启
2. 检查应用权限（设置 → 应用 → SpeedTest → 权限）
3. 查看logcat日志:
   ```bash
   adb logcat | grep -E "HomeViewModel|NetworkRepository"
   ```

### Q2: 按钮可以点击但测试失败

**原因**: 服务器未启动或IP配置错误

**解决方案**:
1. 确认PHP服务器正在运行:
   ```bash
   curl http://YOUR_IP:8080/ping
   ```
2. 检查Android应用中配置的服务器IP
3. 确保手机和服务器在同一网络
4. 检查防火墙设置:
   ```bash
   sudo ufw allow 8080
   ```

### Q3: 编译失败 "KAPT error"

**解决方案**:
```bash
./gradlew --stop
pkill -f "kotlin" || true
rm -rf .gradle build app/build
./gradlew clean build
```

### Q4: 模拟器无法连接到主机

**解决方案**:
```bash
# 确保使用0.0.0.0监听，而不是localhost
php -S 0.0.0.0:8080

# 在模拟器中测试
adb shell curl http://10.0.2.2:8080/ping
```

### Q5: 真机无法连接到电脑

**检查清单**:
- [ ] 手机和电脑在同一WiFi
- [ ] 电脑防火墙允许8080端口
- [ ] PHP服务器使用0.0.0.0监听
- [ ] Android应用中IP地址正确
- [ ] 电脑IP没有改变（路由器DHCP）

**测试连接**:
```bash
# 在手机浏览器中访问
http://YOUR_COMPUTER_IP:8080/ping
```

---

## 📊 Part 6: 查看日志调试

### Android日志

```bash
# 实时查看所有日志
adb logcat

# 只看应用日志
adb logcat | grep "com.speedtest.app"

# 看关键组件
adb logcat | grep -E "MainActivity|HomeViewModel|SpeedTest"

# 看错误日志
adb logcat | grep -E "ERROR|FATAL|Exception"

# 保存日志到文件
adb logcat > app_log.txt
```

### PHP服务器日志

```bash
# PHP内置服务器会直接显示访问日志

# Nginx日志
sudo tail -f /var/log/nginx/speedtest-error.log
sudo tail -f /var/log/nginx/speedtest-access.log

# Apache日志
sudo tail -f /var/log/apache2/speedtest-error.log
sudo tail -f /var/log/apache2/speedtest-access.log
```

---

## 🚀 快速开始检查清单

### 开发环境（电脑 + 模拟器）

- [ ] 启动PHP服务器: `cd backend && php -S 0.0.0.0:8080`
- [ ] 测试服务器: `curl http://localhost:8080/ping`
- [ ] 构建应用: `./gradlew clean assembleDebug`
- [ ] 安装应用: `./gradlew installDebug`
- [ ] 应用会自动使用 `10.0.2.2:8080`
- [ ] 点击开始按钮测试

### 真机测试（手机 + 电脑）

- [ ] 确认手机和电脑在同一WiFi
- [ ] 查找电脑IP: `ip addr` 或 `ipconfig`
- [ ] 启动PHP服务器: `php -S 0.0.0.0:8080`
- [ ] 修改应用服务器IP（如果需要）
- [ ] 在手机浏览器测试: `http://电脑IP:8080/ping`
- [ ] 构建并安装应用
- [ ] 点击开始按钮测试

---

## 🎓 技术栈

### Android
- Kotlin
- Jetpack Compose
- Hilt (依赖注入)
- Room (数据库)
- DataStore (偏好设置)
- OkHttp (网络请求)
- Coroutines & Flow

### Backend
- PHP 7.4+
- Nginx/Apache (可选)

---

## 📝 下一步优化建议

1. **添加HTTPS支持** - 使用Let's Encrypt免费证书
2. **实现服务器自动发现** - mDNS/Bonjour
3. **添加认证机制** - JWT tokens
4. **实现历史图表** - 可视化测速历史
5. **多线程测试** - 提高测试准确性
6. **CDN集成** - 使用Cloudflare等

---

## 📧 支持

如有问题，请：
1. 检查日志: `adb logcat`
2. 查看本指南的FAQ部分
3. 提供错误日志和详细描述

---

**Happy Testing! 🚀**
