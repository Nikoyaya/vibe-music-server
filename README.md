# Vibe Music Server

## 项目概述

Vibe Music Server 是一个基于 Spring Boot 3 构建的音乐服务后端系统，提供用户管理、音乐资源管理、用户互动等核心功能。

## 技术栈

- **后端框架**: Spring Boot 3
- **开发语言**: Java 17
- **构建工具**: Maven
- **数据库**: MySQL 8.0+
- **ORM框架**: MyBatis-Plus
- **缓存**: Redis
- **对象存储**: MinIO
- **认证**: JWT (java-jwt)
- **数据库连接池**: Druid
- **工具库**: Lombok, Spring Boot Validation, Java Mail

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

## 学习编写步骤

### 步骤 1: 环境搭建

1. **安装 JDK 17**
    - 下载并安装 Oracle JDK 17 或 OpenJDK 17
    - 配置 JAVA_HOME 环境变量

2. **安装 Maven**
    - 下载 Maven 3.6+ 并解压
    - 配置 MAVEN_HOME 环境变量

3. **安装 MySQL**
    - 安装 MySQL 8.0+
    - 创建数据库 `vibe_music`
    - 配置数据库用户和密码

4. **安装 Redis**
    - 安装 Redis 6.0+
    - 启动 Redis 服务

5. **安装 MinIO**
    - 下载并安装 MinIO
    - 启动 MinIO 服务
    - 创建 `vibe-music-data` 存储桶

### 步骤 2: 项目初始化

1. **创建 Spring Boot 项目**
    - 使用 Spring Initializr 创建项目
    - 选择 Java 17 和 Spring Boot 3.x
    - 添加必要依赖：Web, MySQL Driver, MyBatis Plus, Redis, Lombok 等

2. **配置项目结构**
    - 创建上述项目结构中的目录
    - 配置 pom.xml 文件

### 步骤 3: 核心功能实现

#### 3.1 配置文件设置

```yaml
# application.yml
spring:
  application:
    name: vibe-music-server
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/vibe_music?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: your_password
    type: com.alibaba.druid.pool.DruidDataSource
  data:
    redis:
      host: localhost
      port: 6379
      database: 1
  mail:
    host: smtp.example.com
    username: your-email@example.com
    password: your-email-password

minio:
  endpoint: http://localhost:9000
  accessKey: your-minio-access-key
  secretKey: your-minio-secret-key
  bucket: vibe-music-data

server:
  port: 8080
```

#### 3.2 实体类设计

创建核心实体类，如：
- User (用户)
- Admin (管理员)
- Singer (歌手)
- Song (歌曲)
- SongList (歌单)
- Comment (评论)
- Collection (收藏)

#### 3.3 数据访问层 (Mapper)

使用 MyBatis Plus 创建 Mapper 接口：

```java
// UserMapper.java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 自定义查询方法
}
```

#### 3.4 服务层 (Service)

实现业务逻辑：

```java
// UserService.java
public interface UserService {
    Result login(UserDTO userDTO);
    Result register(UserDTO userDTO);
    Result updateUserInfo(UserDTO userDTO);
    // 其他方法
}

// UserServiceImpl.java
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public Result login(UserDTO userDTO) {
        // 登录逻辑实现
    }
    
    // 其他方法实现
}
```

#### 3.5 控制器层 (Controller)

暴露 API 接口：

```java
// UserController.java
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public Result login(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        // 参数校验
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.login(userDTO);
    }
    
    // 其他接口
}
```

#### 3.6 统一返回结果

```java
// Result.java
@Data
public class Result {
    private Integer code;
    private String message;
    private Object data;
    
    // 静态方法
    public static Result success() {
        Result result = new Result();
        result.setCode(200);
        result.setMessage("成功");
        return result;
    }
    
    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }
    
    public static Result error(String message) {
        Result result = new Result();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
```

#### 3.7 JWT 认证实现

```java
// JwtUtil.java
public class JwtUtil {
    private static final String SECRET = "your-secret-key";
    private static final long EXPIRATION_TIME = 86400000; // 24小时
    
    // 生成 token
    public static String generateToken(String userId) {
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
        return JWT.create()
                .withClaim("userId", userId)
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(SECRET));
    }
    
    // 验证 token
    public static DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
        return verifier.verify(token);
    }
    
    // 从 token 中获取 userId
    public static String getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userId").asString();
    }
}
```

#### 3.8 文件上传功能 (MinIO)

```java
// MinioUtil.java
@Service
public class MinioUtil {
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Value("${minio.bucket}")
    private String bucket;
    
    // 上传文件
    public String uploadFile(MultipartFile file) throws Exception {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        
        // 检查存储桶是否存在
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        
        // 上传文件
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        
        // 返回文件访问 URL
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .method(Method.GET)
                .build());
    }
}
```

### 步骤 4: 高级功能实现

#### 4.1 权限控制

实现基于角色的权限控制：

```java
// JwtInterceptor.java
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取 token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JSON.toJSONString(Result.error("未登录")));
            return false;
        }
        
        try {
            // 验证 token
            DecodedJWT decodedJWT = JwtUtil.verifyToken(token);
            String userId = decodedJWT.getClaim("userId").asString();
            // 将 userId 存入请求属性
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JSON.toJSONString(Result.error("登录过期")));
            return false;
        }
    }
}
```

#### 4.2 缓存实现

使用 Redis 缓存热点数据：

```java
// SongServiceImpl.java
@Service
public class SongServiceImpl implements SongService {
    @Autowired
    private SongMapper songMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Result getSongList() {
        // 尝试从缓存获取
        List<Song> songList = (List<Song>) redisTemplate.opsForValue().get("songList");
        if (songList == null) {
            // 缓存不存在，从数据库查询
            songList = songMapper.selectList(null);
            // 存入缓存，过期时间30分钟
            redisTemplate.opsForValue().set("songList", songList, 30, TimeUnit.MINUTES);
        }
        return Result.success(songList);
    }
}
```

### 步骤 5: 测试与部署

1. **单元测试**
    - 使用 JUnit 5 和 Spring Boot Test 编写单元测试
    - 测试核心功能和 API

2. **构建项目**
   ```bash
   mvn clean package -DskipTests
   ```

3. **运行项目**
   ```bash
   java -jar target/vibe-music-server-0.0.1-SNAPSHOT.jar
   ```

4. **部署到服务器**
    - 配置环境变量
    - 安装依赖服务 (MySQL, Redis, MinIO)
    - 上传并运行 JAR 文件

## 核心功能模块

### 1. 用户管理
- 注册/登录
- 信息修改
- 头像上传
- 注销

### 2. 内容管理
- 歌手管理 (CRUD)
- 歌曲管理 (CRUD)
- 歌单管理 (CRUD)
- 轮播图管理 (CRUD)

### 3. 用户互动
- 评论管理
- 收藏管理
- 反馈管理

### 4. 文件服务
- 音乐文件上传/下载
- 图片上传/下载

## 学习建议

1. **先理解整体架构**：熟悉 Spring Boot 和项目结构
2. **从核心功能入手**：先实现用户认证和基础 CRUD
3. **逐步添加高级功能**：缓存、文件存储、权限控制等
4. **学习优秀实践**：关注代码规范、异常处理、日志记录
5. **测试驱动开发**：编写单元测试确保功能正确性
6. **参考官方文档**：Spring Boot、MyBatis Plus、MinIO 等

## 项目扩展方向

1. 添加更多音乐服务功能
2. 实现推荐算法
3. 集成消息队列
4. 添加监控和日志系统
5. 支持多语言
6. 开发管理后台界面

## 参考资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus 文档](https://baomidou.com/)
- [MinIO 文档](https://docs.min.io/)
- [JWT 官方文档](https://github.com/auth0/java-jwt)
- [Redis 文档](https://redis.io/docs/)

---

## 许可证

MIT License