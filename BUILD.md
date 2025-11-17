# Android SpeedTest App - 构建说明 (Build Instructions)

## 项目概述 (Project Overview)

完整功能的Android网速测试应用，采用Clean Architecture + MVVM架构，使用Jetpack Compose构建现代化UI。

**核心功能：**
- ✅ 网速测试（下载/上传/延迟/抖动）
- ✅ 服务器选择与Ping测试
- ✅ 测试历史记录与导出
- ✅ 自动定时测试
- ✅ 完整设置界面
- ✅ 深色模式支持
- ✅ 中英文双语支持
- ✅ Material Design 3 UI

## 环境要求 (Requirements)

### 必需软件 (Required Software)
- **Java JDK**: 17 或更高版本
- **Android SDK**: API 34 (Android 14)
- **Gradle**: 8.2 (已包含wrapper，自动下载)
- **网络连接**: 首次构建需要下载依赖

### 推荐 IDE (Recommended IDE)
- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **IntelliJ IDEA**: 2023.2+ (需安装Android插件)

## 快速开始 (Quick Start)

### 1. 克隆项目 (Clone Repository)
```bash
git clone <repository-url>
cd android-test-speed
```

### 2. 检查 Java 版本 (Check Java Version)
```bash
java -version
# 应显示 Java 17 或更高版本
```

### 3. 构建项目 (Build Project)

#### 方式一：使用 Gradle Wrapper (推荐)
```bash
# Linux/Mac
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

#### 方式二：使用 Android Studio
1. 打开 Android Studio
2. File → Open → 选择项目目录
3. 等待 Gradle 同步完成
4. Build → Build Bundle(s) / APK(s) → Build APK(s)

### 4. 输出文件位置 (Output Location)
构建成功后，APK文件位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

## 详细构建步骤 (Detailed Build Steps)

### 步骤 1: 环境准备

#### 安装 Java 17
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Mac (使用 Homebrew)
brew install openjdk@17

# 验证安装
java -version
```

#### 设置 JAVA_HOME (如需要)
```bash
# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# 添加到 ~/.bashrc 或 ~/.zshrc 使其永久生效
```

### 步骤 2: 下载 Android SDK

如果没有安装 Android Studio，可以单独下载 SDK：

```bash
# 下载 Android SDK Command-line Tools
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip

# 解压到指定目录
mkdir -p ~/android-sdk/cmdline-tools
unzip commandlinetools-linux-9477386_latest.zip -d ~/android-sdk/cmdline-tools
mv ~/android-sdk/cmdline-tools/cmdline-tools ~/android-sdk/cmdline-tools/latest

# 设置环境变量
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
export PATH=$ANDROID_HOME/platform-tools:$PATH

# 安装必需的 SDK 组件
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 步骤 3: 配置本地属性

创建 `local.properties` 文件（如不存在）：
```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### 步骤 4: 构建不同变体

#### Debug 版本（开发测试用）
```bash
./gradlew assembleDebug
```

#### Release 版本（需要签名）
```bash
# 首先创建签名密钥（仅首次）
keytool -genkey -v -keystore release-key.jks \
  -alias speedtest-key \
  -keyalg RSA -keysize 2048 -validity 10000

# 构建 Release APK
./gradlew assembleRelease
```

**注意**: Release版本需要配置签名信息，在 `app/build.gradle.kts` 中添加：
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../release-key.jks")
            storePassword = "your-password"
            keyAlias = "speedtest-key"
            keyPassword = "your-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

### 步骤 5: 安装到设备

#### 使用 ADB 安装
```bash
# 确保设备已连接并启用USB调试
adb devices

# 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 强制覆盖安装（如已安装旧版本）
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### 手动安装
将 `app-debug.apk` 文件传输到Android设备，直接点击安装。

## 构建命令参考 (Build Commands Reference)

### 清理项目 (Clean)
```bash
./gradlew clean
```

### 运行测试 (Run Tests)
```bash
# 单元测试
./gradlew test

# 仪器测试（需要连接设备或模拟器）
./gradlew connectedAndroidTest
```

### 代码检查 (Lint)
```bash
./gradlew lint
```

### 查看依赖树 (Dependency Tree)
```bash
./gradlew app:dependencies
```

### 查看所有任务 (List Tasks)
```bash
./gradlew tasks
```

### 构建并安装 (Build and Install)
```bash
./gradlew installDebug
```

## 常见问题 (Troubleshooting)

### 问题 1: "java.net.UnknownHostException: services.gradle.org"
**原因**: 无网络连接或DNS问题
**解决**:
- 检查网络连接
- 配置代理（如需要）：在 `gradle.properties` 添加
  ```properties
  systemProp.http.proxyHost=proxy.example.com
  systemProp.http.proxyPort=8080
  systemProp.https.proxyHost=proxy.example.com
  systemProp.https.proxyPort=8080
  ```

### 问题 2: "SDK location not found"
**原因**: Android SDK 路径未配置
**解决**: 创建或编辑 `local.properties`：
```properties
sdk.dir=/path/to/android/sdk
```

### 问题 3: "Unsupported Java version"
**原因**: Java版本过低
**解决**: 安装 Java 17 或更高版本

### 问题 4: "Out of memory" 错误
**原因**: Gradle 内存不足
**解决**: 在 `gradle.properties` 增加内存：
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

### 问题 5: 构建缓存问题
**解决**: 清理并重建
```bash
./gradlew clean
rm -rf .gradle build app/build
./gradlew assembleDebug
```

### 问题 6: KSP 处理失败
**原因**: Room 注解处理器错误
**解决**:
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

## 项目结构 (Project Structure)

```
android-test-speed/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/speedtest/app/
│   │   │   │   ├── data/          # 数据层
│   │   │   │   ├── domain/        # 业务逻辑层
│   │   │   │   ├── presentation/  # UI层
│   │   │   │   ├── di/           # 依赖注入
│   │   │   │   ├── service/      # 后台服务
│   │   │   │   └── utils/        # 工具类
│   │   │   ├── res/              # 资源文件
│   │   │   └── AndroidManifest.xml
│   │   └── test/                 # 单元测试
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts              # 根构建脚本
├── settings.gradle.kts
├── gradle.properties
├── gradlew                       # Gradle Wrapper (Linux/Mac)
├── gradlew.bat                   # Gradle Wrapper (Windows)
└── BUILD.md                      # 本文档
```

## 技术栈 (Tech Stack)

- **语言**: Kotlin 1.9.20
- **最小SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **架构**: Clean Architecture + MVVM
- **UI**: Jetpack Compose 1.5.4
- **依赖注入**: Hilt 2.48.1
- **数据库**: Room 2.6.1
- **网络**: OkHttp 4.12.0 + Retrofit 2.9.0
- **异步**: Kotlin Coroutines + Flow
- **导航**: Navigation Compose 2.7.5
- **后台任务**: WorkManager 2.9.0

## 性能优化 (Performance)

构建优化配置已启用：
- ✅ Gradle 构建缓存
- ✅ 并行编译
- ✅ 增量编译
- ✅ 配置缓存
- ✅ R8 代码压缩（Release）
- ✅ ProGuard 混淆（Release）

## 版本信息 (Version Info)

- **应用版本**: 1.0.0
- **版本号**: 1
- **Gradle**: 8.2
- **AGP**: 8.1.4
- **Kotlin**: 1.9.20

## 持续集成 (CI/CD)

示例 GitHub Actions 配置：
```yaml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
      - name: Build with Gradle
        run: ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

## 许可证 (License)

请根据项目实际情况添加许可证信息。

## 支持 (Support)

如有问题，请提交 Issue 或联系开发团队。

---

**最后更新**: 2025-11-17
**构建状态**: ✅ 所有功能完整，代码已优化，准备生产环境使用
