# Vibe Music Server

<p align="center">
  <img src="icons/icon.png" alt="Vibe Music App Icon" width="100" height="100">
</p>

<p align="center">
<a href="README_EN.md">English</a> | <a href="README.md">简体中文</a> | <a href="README_ZH_TW.md">繁體中文</a>
</p>

## 專案概述

Vibe Music Server 是一個基於 Spring Boot 3 构建的高性能音樂服務後端系統，為現代音樂串流平台提供完整的後端解決方案。

## 核心功能

### ?? 音樂內容管理
- **歌手管理**: 完整的 CRUD 操作，支持多條件查詢和分頁
- **歌曲管理**: 音頻文件上傳、元數據管理、在線播放支持
- **歌單系統**: 用戶自定義歌單、官方推薦歌單、智能推薦

### 👥 用戶服務
- **身份認證**: JWT 安全認證，支持多端登錄
- **用戶管理**: 個人信息維護、頭像管理、密碼安全
- **權限控制**: 基於角色的精細權限管理系統

### 💬 社交互動
- **評論系統**: 歌曲評論、回覆功能、點讚機制
- **收藏功能**: 收藏歌曲和歌單、個人化音樂庫
- **用戶反饋**: 意見提交和改進建議收集

### 📱 多端支持
- **全平台兼容**: Android、iOS、Web 三端統一 API
- **設備管理**: 自動識別設備類型、記錄使用信息
- **智能適應**: 根據客戶端類型提供差異化服務

### ⚡ 高級特性
- **接口防抖**: 基於 Redis 的分散式防抖機制，防止惡意請求
- **緩存優化**: Redis 熱點數據緩存，提升響應速度
- **文件存儲**: MinIO 分散式對象存儲，支持大文件上傳
- **實時統計**: 用戶行為分析、設備使用情況監控

## 技術棧

### 後端框架
- **Spring Boot 3.2.0** - 現代Java開發框架
- **Java 17** - 長期支持版本，性能優異
- **Maven** - 項目構建和依賴管理

### 數據存儲
- **MySQL 8.0+** - 關係型數據庫，數據持久化
- **Redis 7.0+** - 內存數據庫，緩存和會話管理
- **MinIO** - 高性能對象存儲，文件管理

### 安全認證
- **JWT** - 無狀態身份認證
- **Spring Security** - 安全框架支持
- **參數校驗** - 全面的輸入驗證機制

### 工具庫
- **MyBatis-Plus** - 增強型ORM框架
- **Lombok** - 代碼簡化工具
- **Hutool** - Java工具庫
- **Druid** - 高性能數據庫連接池

## 快速開始

### 環境要求
- JDK 17 或更高版本
- MySQL 8.0+
- Redis 7.0+
- MinIO 最新版本
- Maven 3.6+

### 部署步驟

1. **數據庫初始化**
   ```sql
   CREATE DATABASE vibe_music CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **配置文件設置**
   複製 `application.yml.template` 為 `application.yml` 並配置相關參數

3. **構建項目**
   ```bash
   mvn clean package -DskipTests
   ```

4. **啟動服務**
   ```bash
   java -jar target/vibe-music-server-*.jar
   ```

### Docker 部署（可選）
項目支持 Docker 容器化部署，提供完整的容器編排方案。

## API 文檔

系統提供完整的 RESTful API 接口，支持：

- **用戶認證**: `/auth/**`
- **歌手管理**: `/artist/**`
- **歌曲管理**: `/song/**`
- **歌單管理**: `/playlist/**`
- **評論系統**: `/comment/**`
- **文件服務**: `/file/**`
- **設備管理**: `/device/**`

詳細的 API 文檔可通過啟動後訪問 `/swagger-ui.html` 查看。

## 性能特性

- ⚡ **響應快速**: 平均響應時間 < 100ms
- 🔒 **安全可靠**: 多層次安全防護機制
- 📊 **可擴展**: 支持水平擴展和負載均衡
- 🎯 **高可用**: 故障自動轉移和恢復
- 📈 **實時監控**: 完整的日誌和性能監控

## 特色功能

### 智能防抖機制
系統內置基於注解的接口防抖功能，有效防止惡意請求和重複提交：

```java
@RequestDebounce(key = "sendCode", expire = 60, message = "操作過於頻繁")
public Result sendVerificationCode(String email) {
    // 業務邏輯
}
```

### 多端設備識別
自動識別並記錄客戶端設備信息，支持精細化運營：

```json
{
  "clientType": "android",
  "deviceInfo": {
    "model": "iPhone13,4",
    "brand": "Apple",
    "os": "iOS 15.4.1"
  }
}
```

### 分散式緩存
基於 Redis 的分散式緩存方案，確保多實例環境下的數據一致性。

## 項目優勢

1. **現代化架構**: 採用最新的 Spring Boot 3 和 Java 17
2. **高性能設計**: 優化數據庫查詢和緩存策略
3. **安全可靠**: 完善的異常處理和日誌記錄
4. **易於擴展**: 模組化設計，便於功能擴展
5. **多端支持**: 統一的API接口，適配各種客戶端

## 支持與貢獻

歡迎提交 Issue 和 Pull Request 來改進項目。詳細貢獻指南請參考項目文檔。

## 許可證

MIT License - 詳見 [LICENSE](LICENSE) 文件

---
*持續更新中，最新功能請查看項目提交記錄*