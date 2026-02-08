# Vibe Music Server

<p align="center">
  <img src="icons/icon.png" alt="Vibe Music App Icon" width="100" height="100">
</p>

<p align="center">
<a href="README_EN.md">English</a> | <a href="README.md">简体中文</a> | <a href="README_ZH_TW.md">繁體中文</a>
</p>

## Overview

Vibe Music Server is a high-performance music service backend system built with Spring Boot 3, providing a complete backend solution for modern music streaming platforms. 

## Core Features

### 🎵 Music Content Management
- **Artist Management**: Complete CRUD operations with multi-condition queries and pagination
- **Song Management**: Audio file upload, metadata management, online playback support
- **Playlist System**: User-created playlists, official recommendations, smart recommendations

### 👥 User Services
- **Authentication**: JWT secure authentication with multi-terminal login support
- **User Management**: Personal information maintenance, avatar management, password security
- **Access Control**: Role-based fine-grained permission management system

### 💬 Social Interaction
- **Comment System**: Song comments, reply functionality, like mechanism
- **Collection Function**: Collect songs and playlists, personalized music library
- **User Feedback**: Suggestions and improvement feedback collection

### 📱 Multi-Platform Support
- **Full Platform Compatibility**: Android, iOS, Web unified API
- **Device Management**: Automatic device type identification, usage information recording
- **Smart Adaptation**: Differentiated services based on client type

### ⚡ Advanced Features
- **Request Debouncing**: Redis-based distributed debouncing mechanism to prevent malicious requests
- **Cache Optimization**: Redis hot data caching for improved response speed
- **File Storage**: MinIO distributed object storage with large file upload support
- **Real-time Statistics**: User behavior analysis, device usage monitoring

## Tech Stack

### Backend Framework
- **Spring Boot 3.2.0** - Modern Java development framework
- **Java 17** - Long-term support version with excellent performance
- **Maven** - Project build and dependency management

### Data Storage
- **MySQL 8.0+** - Relational database for data persistence
- **Redis 7.0+** - In-memory database for caching and session management
- **MinIO** - High-performance object storage for file management

### Security & Authentication
- **JWT** - Stateless authentication
- **Spring Security** - Security framework support
- **Parameter Validation** - Comprehensive input validation mechanism

### Libraries
- **MyBatis-Plus** - Enhanced ORM framework
- **Lombok** - Code simplification tool
- **Hutool** - Java utility library
- **Druid** - High-performance database connection pool

## Quick Start

### Requirements
- JDK 17 or higher
- MySQL 8.0+
- Redis 7.0+
- MinIO latest version
- Maven 3.6+

### Deployment Steps

1. **Database Initialization**
   ```sql
   CREATE DATABASE vibe_music CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Configuration**
   Copy `application.yml.template` to `application.yml` and configure parameters

3. **Build Project**
   ```bash
   mvn clean package -DskipTests
   ```

4. **Start Service**
   ```bash
   java -jar target/vibe-music-server-*.jar
   ```

### Docker Deployment (Optional)
The project supports Docker containerized deployment with complete container orchestration solutions.

## API Documentation

The system provides complete RESTful API interfaces:

- **Authentication**: `/auth/**`
- **Artist Management**: `/artist/**`
- **Song Management**: `/song/**`
- **Playlist Management**: `/playlist/**`
- **Comment System**: `/comment/**`
- **File Service**: `/file/**`
- **Device Management**: `/device/**`

Detailed API documentation available at `/swagger-ui.html` after startup.

## Performance Features

- ⚡ **Fast Response**: Average response time < 100ms
- 🔒 **Secure & Reliable**: Multi-layer security protection mechanism
- 📊 **Scalable**: Supports horizontal scaling and load balancing
- 🎯 **High Availability**: Automatic failover and recovery
- ?? **Real-time Monitoring**: and performance monitoring

 Complete logging## Special Features

### Smart Debouncing Mechanism
Built-in annotation-based request debouncing to effectively prevent malicious requests and duplicate submissions:

```java
@RequestDebounce(key = "sendCode", expire = 60, message = "Operation too frequent")
public Result sendVerificationCode(String email) {
    // Business logic
}
```

### Multi-Platform Device Recognition
Automatic client device identification and recording for refined operations:

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

### Distributed Caching
Redis-based distributed caching solution ensuring data consistency in multi-instance environments.

## Project Advantages

1. **Modern Architecture**: Built with latest Spring Boot 3 and Java 17
2. **High Performance**: Optimized database queries and caching strategies
3. **Secure & Reliable**: Complete exception handling and logging
4. **Easy to Extend**: Modular design for easy feature expansion
5. **Multi-Platform Support**: Unified API adapting to various clients

## Support & Contribution

Issues and Pull Requests are welcome to improve the project. See project documentation for detailed contribution guidelines.

## License

MIT License - See [LICENSE](LICENSE) file

---
*Continuously updated, latest features in project commit history*