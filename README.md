# 念日

[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/best-bo-cai/nian-ri/CI)](https://github.com/best-bo-cai/nian-ri/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF.svg?logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Compose-2024.12.01-4285F4.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-8.7.3-3DDC84.svg?logo=android)](https://developer.android.com/build/releases/gradle-plugin)
[![minSdk](https://img.shields.io/badge/minSdk-29-3DDC84.svg?logo=android)](https://developer.android.com/about/versions/android-10)

> 一款温馨、纯粹的本地日子与事件提醒应用，帮你记住每一个值得纪念的日子。

## 简介

念日（NianRi）是一款基于 Kotlin 与 Jetpack Compose 构建的本地 Android 应用，用于管理生日、纪念日、节假日与临时事件。它通过倒计时/正计时、循环规则、本地通知与邮件提醒，帮助用户不再遗忘重要日子；并内置 AI 对话能力，支持用自然语言快速创建日子与事件。所有数据均存储于本地，无账号体系，注重隐私与简洁。

## 核心特性

- **日子管理**：生日、纪念日、节假日、普通事件统一管理，支持农历/阳历、倒计时/正计时与循环规则（每年/每月/每周）。
- **事件管理**：区间型事件，起止日期与完成状态标记，按"进行中 → 未开始 → 已结束"智能排序。
- **日历面板**：月视图日历，节假日颜色标记，支持左右滑动切换月份。
- **AI 对话创建**：自然语言 + 语音输入，AI 自动提取事件字段并生成预览卡片，一键确认创建。
- **双重提醒**：本地通知（AlarmManager + NotificationManager）+ 邮件通知（SMTP），多时间点可自由勾选。
- **节假日看板**：第三方 API 动态获取法定节假日与调休，展示自然日/工作日倒计时，支持自定义节假日。
- **数据安全可控**：纯本地存储（Room），支持 JSON 导入导出备份，API Key 与 SMTP 密码本地加密。

## 目录

- [快速开始](#快速开始)
- [使用说明](#使用说明)
- [配置说明](#配置说明)
- [项目结构](#项目结构)
- [开发指南](#开发指南)
- [如何贡献](#如何贡献)
- [行为准则](#行为准则)
- [常见问题](#常见问题)
- [许可证](#许可证)

## 快速开始

### 环境要求

- Android Studio（推荐最新稳定版）
- JDK 17
- Android SDK（compileSdk 36）
- Android 10（API 29）及以上设备

### 构建

```bash
# 1. 克隆仓库
git clone https://github.com/best-bo-cai/nian-ri.git
cd nian-ri

# 2. 配置本地 SDK 路径（首次需手动创建 local.properties）
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties

# 3. 构建 Debug APK（macOS / Linux）
./gradlew assembleDebug
# Windows 使用
gradlew.bat assembleDebug
```

安装包位于 `app/build/outputs/apk/debug/`。

## 使用说明

### 四个主页面

| 页面 | 功能 |
|------|------|
| **日子** | 双列卡片展示即将到来的日子与倒计时天数，下方按日期排序的横条列表 |
| **事件** | 区间型事件列表，显示起止日期与状态（未开始/进行中/已结束） |
| **对话** | 通过自然语言或语音让 AI 解析并创建日子/事件 |
| **我的** | 数据统计、SMTP / AI 配置、数据备份恢复、提醒设置、主题外观 |

### AI 创建流程

1. 在「我的」页面完成 AI 配置（Base URL + API Key + Model）。
2. 进入「对话」页，输入文字或长按麦克风录音。
3. AI 解析字段并返回预览卡片，点击「确认创建」即可。

### 提醒配置

创建/编辑日子或事件时，可勾选提醒方式（本地通知 / 邮件）与提醒时间点（提前 1 天 / 提前 3 天 / 当天 9 点 / 当天 10 点）。

## 配置说明

应用内配置均在「我的」页面完成，无需配置文件：

### AI 大模型配置

| 配置项 | 类型 | 说明 |
|--------|------|------|
| Base URL | String | 大模型接口地址 |
| API Key | String | 本地简单加密存储 |
| Model | String | 模型名称 |

### SMTP 邮件配置

| 配置项 | 类型 | 说明 |
|--------|------|------|
| 邮箱地址 | String | 发件邮箱（如 `xxx@qq.com`） |
| SMTP 服务器 | String | 如 `smtp.qq.com` |
| 端口 | Number | 如 `465` / `587` |
| 密码 | String | 授权码，本地简单加密存储 |

## 项目结构

```bash
.
├── app/
│   ├── src/main/
│   │   ├── java/com/nianri/
│   │   │   ├── data/            # 数据层（Room 数据库、DAO、实体、仓库）
│   │   │   ├── ui/              # 界面层（页面 + ViewModel）
│   │   │   │   ├── calendar/    # 日历面板
│   │   │   │   ├── chat/        # AI 对话页
│   │   │   │   ├── config/      # AI / SMTP 配置页
│   │   │   │   ├── days/        # 日子页
│   │   │   │   ├── edit/        # 创建/编辑面板
│   │   │   │   ├── events/      # 事件页
│   │   │   │   ├── navigation/  # 导航
│   │   │   │   ├── profile/     # 我的页面
│   │   │   │   ├── search/      # 搜索页
│   │   │   │   └── theme/       # 主题（颜色、字体）
│   │   │   ├── util/            # 工具函数（日期、加密、节假日等）
│   │   │   ├── MainActivity.kt  # 入口 Activity
│   │   │   └── NianRiApp.kt     # 应用根组件
│   │   └── res/                 # 资源文件
│   ├── build.gradle.kts         # 应用模块构建配置
│   └── proguard-rules.pro       # 混淆规则
├── build.gradle.kts             # 根构建配置
├── settings.gradle.kts          # 项目设置
├── gradle.properties            # Gradle 属性
├── prd.md                       # 产品需求文档
└── README.md                    # 项目说明
```

## 开发指南

### 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin 2.1.0 |
| UI | Jetpack Compose（BOM 2024.12.01）+ Material 3 |
| 架构 | MVVM（ViewModel + Repository） |
| 数据库 | Room 2.6.1（KSP） |
| 网络 | OkHttp 4.12.0 |
| 序列化 | Gson 2.11.0 |
| 偏好存储 | DataStore Preferences |
| 构建 | Android Gradle Plugin 8.7.3 |

### 本地开发

```bash
# 编译检查
./gradlew assembleDebug

# 运行单元测试（如已配置）
./gradlew test

# 生成 Release 包（需配置签名）
./gradlew assembleRelease
```

## 常见问题

### Q: 应用需要联网吗？

**A:** 基础功能（日子/事件管理、本地通知）完全离线可用。节假日数据获取、AI 对话与邮件发送需要联网，并需在「我的」页面完成相应配置。

### Q: 数据存储在哪里？如何备份？

**A:** 数据通过 Room 存储在本机，无账号与云端同步。可在「我的」页面通过 JSON 导入/导出进行备份与恢复。

### Q: AI 功能支持哪些大模型？

**A:** 采用通用接入模式，用户自填 Base URL、API Key 与 Model，兼容 OpenAI 兼容接口的主流模型服务。

### Q: 支持农历吗？

**A:** 支持，创建日子/事件时可选择农历或阳历日历类型。

## 许可证

本项目基于 [MIT License](LICENSE) 开源。
