# Vibe Music Server

一个基于Spring Boot的音乐服务后端系统。

## 技术栈

- Java 17
- Spring Boot 3.x
- Maven
- MySQL (或其他数据库，根据配置)

## 项目结构

```
src/
├── main/
│   ├── java/org/amis/vibemusicserver/
│   │   ├── config/         # 配置类
│   │   ├── constant/       # 常量定义
│   │   ├── controller/     # 控制器层
│   │   ├── enumeration/    # 枚举类
│   │   ├── handler/        # 处理器
│   │   ├── interceptor/    # 拦截器
│   │   ├── mapper/         # 数据访问层
│   │   ├── model/          # 数据模型
│   │   │   ├── dto/        # 数据传输对象
│   │   │   ├── entity/     # 实体类
│   │   │   ├── vo/         # 视图对象
│   │   ├── result/         # 统一返回结果
│   │   ├── service/impl/   # 服务实现
│   │   ├── utils/          # 工具类
│   │   └── VibeMusicServerApplication.java # 启动类
│   ├── resources/
│   │   ├── mapper/         # MyBatis映射文件
│   │   └── application.yml # 配置文件
└── test/                   # 测试代码
```

## 快速开始

1. 克隆项目
2. 配置数据库连接信息
3. 运行 `mvn spring-boot:run`
4. 访问 http://localhost:8080

## 功能特性

- 用户管理
- 音乐资源管理
- API接口服务
- 数据持久化

## 许可证

MIT License