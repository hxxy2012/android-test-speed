# 🚀 快速开始指南

## 立即运行项目

### 1️⃣ 克隆项目
```bash
git clone <your-repo-url>
cd android-test-speed
git checkout claude/android-speedtest-app-01SWpvK7RagWQrB8MdYHPTnz
```

### 2️⃣ 在Android Studio中打开
1. 打开Android Studio
2. 选择 `File → Open`
3. 选择项目目录
4. 等待Gradle同步完成

### 3️⃣ 配置测试服务器（重要！）

**当前应用使用示例服务器地址，需要配置真实服务器才能测速。**

编辑文件：`app/src/main/java/com/speedtest/app/data/repository/ServerRepositoryImpl.kt`

找到 `getDefaultServers()` 方法，修改为：

```kotlin
private fun getDefaultServers(): List<Server> {
    return listOf(
        Server(
            id = "server_1",
            name = "测试服务器1",
            host = "your-speedtest-server.com",  // 修改为你的服务器地址
            port = 8080,                          // 修改端口
            city = "北京",
            country = "中国",
            latitude = 39.9042,
            longitude = 116.4074,
            sponsor = "你的公司",
            isActive = true
        )
        // 可以添加更多服务器
    )
}
```

### 4️⃣ 运行应用
- 连接Android设备或启动模拟器
- 点击绿色播放按钮 ▶️ 或按 `Shift + F10`

---

## 📱 测试服务器搭建（可选）

如果你需要搭建自己的测试服务器，以下是简单的方案：

### 方案1: 使用现成的测试服务器

```bash
# 使用公共测试服务器（不推荐用于生产）
host: "speedtest.net"
port: 8080

# 或者使用Fast.com
host: "fast.com"
```

### 方案2: 自建简单测试服务器

使用Node.js快速搭建：

```javascript
// server.js
const express = require('express');
const app = express();
const port = 8080;

// Ping端点
app.head('/ping', (req, res) => {
    res.status(200).end();
});

// 下载端点
app.get('/download', (req, res) => {
    const size = parseInt(req.query.size) || 1024 * 1024; // 默认1MB
    const data = Buffer.alloc(size, 'x');
    res.send(data);
});

// 上传端点
app.post('/upload', express.raw({ limit: '100mb' }), (req, res) => {
    res.status(200).json({
        size: req.body.length,
        message: 'Upload successful'
    });
});

app.listen(port, () => {
    console.log(`Speed test server running on port ${port}`);
});
```

运行服务器：
```bash
npm install express
node server.js
```

### 方案3: 使用Docker

```dockerfile
# Dockerfile
FROM node:16
WORKDIR /app
COPY server.js .
RUN npm install express
EXPOSE 8080
CMD ["node", "server.js"]
```

```bash
docker build -t speedtest-server .
docker run -p 8080:8080 speedtest-server
```

---

## 🔧 常见问题

### Q1: 编译失败，提示找不到MPAndroidChart
**A**: 在 `settings.gradle.kts` 中添加：
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // 添加这行
    }
}
```

### Q2: 运行时没有测速结果
**A**: 请确保：
1. 已配置真实的测试服务器
2. 设备/模拟器有网络连接
3. 服务器端点正确响应

### Q3: 权限请求失败
**A**: 在设备设置中手动授予权限：
- 设置 → 应用 → SpeedTest → 权限

### Q4: 通知不显示
**A**: 检查：
- Android 13+需要手动授予通知权限
- 设置 → 通知 → SpeedTest → 允许通知

### Q5: APK太大
**A**: 启用ProGuard优化：
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
    }
}
```

---

## 🎯 快速功能测试

### 测试核心功能
1. ✅ **启动应用** - 查看主界面
2. ✅ **点击测速按钮** - 开始测试（需配置服务器）
3. ✅ **查看历史** - 切换到历史标签
4. ✅ **选择服务器** - 测试服务器列表
5. ✅ **修改设置** - 测试主题切换、语言切换
6. ✅ **导出数据** - 测试CSV/JSON导出
7. ✅ **自动测试** - 启用定时测试

### 测试高级功能
1. ✅ **通知** - 完成测试后查看通知
2. ✅ **后台测试** - 测试时切换到其他应用
3. ✅ **权限** - 重新安装后测试权限请求
4. ✅ **深色模式** - 切换主题测试
5. ✅ **多语言** - 切换中英文

---

## 📊 性能测试

### 测试性能指标
```bash
# 查看启动时间
adb shell am start -W com.speedtest.app/.MainActivity

# 查看内存占用
adb shell dumpsys meminfo com.speedtest.app

# 查看CPU使用
adb shell top | grep speedtest

# 查看APK大小
ls -lh app/build/outputs/apk/release/
```

---

## 🔑 发布前检查清单

### 代码检查
- [ ] 移除所有Log.d()调试日志
- [ ] 移除TODO注释
- [ ] 更新版本号
- [ ] 配置签名密钥
- [ ] 启用ProGuard

### 配置检查
- [ ] 真实服务器地址
- [ ] 正确的应用ID
- [ ] 隐私政策链接
- [ ] 用户协议链接
- [ ] Google Play应用ID

### 资源检查
- [ ] 所有尺寸的图标
- [ ] 应用截图
- [ ] Feature Graphic
- [ ] 应用描述
- [ ] 更新日志

---

## 🚢 发布到Google Play

### 1. 生成签名APK
```bash
# 在Android Studio中
Build → Generate Signed Bundle/APK → APK
选择 release variant
输入密钥信息
```

### 2. 上传到Google Play Console
1. 登录 [Google Play Console](https://play.google.com/console)
2. 创建新应用
3. 填写应用详情
4. 上传APK/AAB
5. 设置定价和分发
6. 提交审核

### 3. 等待审核
- 通常1-3天
- 关注邮件通知
- 处理任何反馈

---

## 📞 需要帮助？

- 📖 查看完整文档: `README.md`
- 📋 查看实现总结: `IMPLEMENTATION_SUMMARY.md`
- 🐛 报告问题: GitHub Issues
- 💬 技术支持: [email protected]

---

## 🎉 开始使用吧！

现在你已经准备好了！按照上面的步骤，你将能够：
1. ✅ 在5分钟内运行应用
2. ✅ 在10分钟内配置服务器
3. ✅ 在30分钟内完成测试
4. ✅ 在1小时内准备发布

**祝你开发顺利！** 🚀
