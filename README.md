# Vibe Music Server

<p align="center">
  <img src="icons/icon.png" alt="Vibe Music App Icon" width="100" height="100">
</p>

<p align="center">
<a href="README_EN.md">English</a> | <a href="README.md">简体中文</a> | <a href="README_ZH_TW.md">繁體中文</a>
</p>

## 项目概述

Vibe Music Server 是一个基于 Spring Boot 3 构建的高性能音乐服务后端系统，为现代音乐流媒体平台提供完整的后端解决方案。

## 核心功能

### 🎵 音乐内容管理
- **歌手管理**: 完整的 CRUD 操作，支持多条件查询和分页
- **歌曲管理**: 音频文件上传、元数据管理、在线播放支持
- **歌单系统**: 用户自定义歌单、官方推荐歌单、智能推荐

### 👥 用户服务
- **身份认证**: JWT 安全认证，支持多端登录
- **用户管理**: 个人信息维护、头像管理、密码安全
- **权限控制**: 基于角色的精细权限管理系统

### 💬 社交互动
- **评论系统**: 歌曲评论、回复功能、点赞机制
- **收藏功能**: 收藏歌曲和歌单，个性化音乐库
- **用户反馈**: 意见提交和改进建议收集

### 📱 多端支持
- **全平台兼容**: Android、iOS、Web 三端统一 API
- **设备管理**: 自动识别设备类型，记录使用信息
- **智能适应**: 根据客户端类型提供差异化服务

### ⚡ 高级特性
- **接口防抖**: 基于 Redis 的分布式防抖机制，防止恶意请求
- **缓存优化**: Redis 热点数据缓存，提升响应速度
- **文件存储**: MinIO 分布式对象存储，支持大文件上传
- **实时统计**: 用户行为分析，设备使用情况监控

## 技术栈

### 后端框架
- **Spring Boot 3.2.0** - 现代Java开发框架
- **Java 17** - 长期支持版本，性能优异
- **Maven** - 项目构建和依赖管理

### 数据存储
- **MySQL 8.0+** - 关系型数据库，数据持久化
- **Redis 7.0+** - 内存数据库，缓存和会话管理
- **MinIO** - 高性能对象存储，文件管理

### 安全认证
- **JWT** - 无状态身份认证
- **Spring Security** - 安全框架支持
- **参数校验** - 全面的输入验证机制

### 工具库
- **MyBatis-Plus** - 增强型ORM框架
- **Lombok** - 代码简化工具
- **Hutool** - Java工具库
- **Druid** - 高性能数据库连接池

## 快速开始

### 环境要求
- JDK 17 或更高版本
- MySQL 8.0+
- Redis 7.0+
- MinIO 最新版本
- Maven 3.6+

### 部署步骤

1. **数据库初始化**
   ```sql
   CREATE DATABASE vibe_music CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **配置文件设置**
   复制 `application.yml.template` 为 `application.yml` 并配置相关参数

3. **构建项目**
   ```bash
   mvn clean package -DskipTests
   ```

4. **启动服务**
   ```bash
   java -jar target/vibe-music-server-*.jar
   ```

### Docker 部署（可选）
项目支持 Docker 容器化部署，提供完整的容器编排方案。

## API 文档

系统提供完整的 RESTful API 接口，支持：

- **用户认证**: `/auth/**`
- **歌手管理**: `/artist/**`
- **歌曲管理**: `/song/**`
- **歌单管理**: `/playlist/**`
- **评论系统**: `/comment/**`
- **文件服务**: `/file/**`
- **设备管理**: `/device/**`

详细的 API 文档可通过启动后访问 `/swagger-ui.html` 查看。

## 性能特性

- ⚡ **响应快速**: 平均响应时间 < 100ms
- 🔒 **安全可靠**: 多层次安全防护机制
- 📊 **可扩展**: 支持水平扩展和负载均衡
- 🎯 **高可用**: 故障自动转移和恢复
- 📈 **实时监控**: 完整的日志和性能监控

## 特色功能

### 智能防抖机制
系统内置基于注解的接口防抖功能，有效防止恶意请求和重复提交：

```java
@RequestDebounce(key = "sendCode", expire = 60, message = "操作过于频繁")
public Result sendVerificationCode(String email) {
    // 业务逻辑
}
```

### 多端设备识别
自动识别并记录客户端设备信息，支持精细化运营：

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

### 分布式缓存
基于 Redis 的分布式缓存方案，确保多实例环境下的数据一致性。

## 项目优势

1. **现代化架构**: 采用最新的 Spring Boot 3 和 Java 17
2. **高性能设计**: 优化数据库查询和缓存策略
3. **安全可靠**: 完善的异常处理和日志记录
4. **易于扩展**: 模块化设计，便于功能扩展
5. **多端支持**: 统一的API接口，适配各种客户端

## 支持与贡献

欢迎提交 Issue 和 Pull Request 来改进项目。详细贡献指南请参考项目文档。

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

---
*持续更新中，最新功能请查看项目提交记录*