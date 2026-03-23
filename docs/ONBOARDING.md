# CrazyCar 项目入职指南

> 本文档基于项目知识图谱自动生成，帮助新开发者快速理解 CrazyCar 架构。

## 项目概览

**CrazyCar** 是一款网络联机赛车游戏解决方案，采用三端分布式架构：

| 模块 | 技术栈 | 职责 |
|------|--------|------|
| **Unity 客户端** | Unity 2021.3+, C#, QFramework | 游戏逻辑、物理模拟、双协议网络 |
| **Spring Boot 服务端** | Spring Boot 2.7, Java 17, MyBatis Plus | REST API、WebSocket 实时通信 |
| **Vue 管理后台** | Vue 2.6, Element UI | 用户管理、比赛配置、数据分析 |

### 核心技术栈
- **语言**: C#, Java, JavaScript, Vue
- **框架**: QFramework, Spring Boot, Vue.js, Element UI
- **网络**: KCP (UDP) + WebSocket (TCP) 双协议
- **数据库**: MySQL 8.0.32
- **热更新**: Addressables (资源) + HybridCLR (代码)

---

## 架构分层

### 1. Unity Client - QFramework 架构

QFramework 采用严格的 **Model-System-Utility** 三层架构：

```
ViewController (表现层)
    ↓ 可获取: System, Model
    ↓ 可发送: Command, Event
System (系统层)
    ↓ 可获取: System, Model, Utility
    ↓ 可发送/监听: Event
Model (数据层)
    ↓ 可获取: Utility
    ↓ 可发送: Event
Utility (工具层)
    ↓ 第三方库封装
```

**关键文件**
- `CrazyCarClient/Assets/Scripts/CrazyCar.cs` - 架构入口，注册所有 Systems 和 Models

**Model 层** (数据管理)
| 文件 | 职责 |
|------|------|
| `UserModel.cs` | 用户信息 (uid, 用户名, 星星数) |
| `AvatarModel.cs` | 头像数据模型 |
| `EquipModel.cs` | 装备/赛车数据模型 |
| `GameModel.cs` | 游戏状态管理 |
| `MatchModel.cs` | 匹配房间数据 |
| `TimeTrialModel.cs` | 计时赛数据 |
| `InputModel.cs` | 输入状态管理 |
| `SettingsModel.cs` | 系统设置 |

**System 层** (游戏逻辑)
| 文件 | 职责 |
|------|------|
| `IPlayerManagerSystem` | 玩家管理 |
| `INetworkSystem` | 网络系统编排 |
| `IWebSocketSystem` / `IKCPSystem` | 双协议网络实现 |
| `IMatchRoomSystem` | 匹配房间管理 |
| `ICheckpointSystem` | 检查点系统 |
| `IAddressableSystem` | Addressable 资源管理 |
| `IGuidanceSystem` | 新手教程系统 |

### 2. Spring Boot Server

标准 **MVC 架构**: `Controller → Service → Mapper → Model`

**REST API 控制器**
- `LoginController` - 登录/注册
- `UserInfoController` - 用户信息
- `AvatarController` - 头像管理
- `EquipController` - 装备管理

**游戏控制器**
- `TimeTrialController` - 计时赛模式
- `MatchController` - 多人匹配
- `GameController` - 游戏逻辑

**WebSocket 处理器**
- `TimeTrialWebSocket` - 计时赛实时通信
- `MatchRoomWebSocket` - 匹配房间通信
- `MatchWebSocket` - 比赛通信

**消息类型**
```java
CreatePlayer = 0, PlayerState = 1, DelPlayer = 2,
MatchRoomCreate = 3, MatchRoomJoin = 4, MatchRoomStatus = 5,
MatchRoomExit = 6, MatchRoomStart = 7
```

### 3. Vue Admin Dashboard

标准 Vue CLI 项目结构：
- `src/api/` - API 封装
- `src/components/` - 可复用组件
- `src/layout/` - 布局组件
- `src/router/` - 路由配置
- `src/store/` - Vuex 状态管理
- `src/views/` - 页面视图

---

## 核心概念

### 网络同步架构

CrazyCar 使用 **服务端权威** 的网络同步模型：

```
Client (输入) → Server (物理模拟) → State Sync → All Clients
```

- **KCP**: 可靠 UDP 协议，用于低延迟游戏状态传输
- **WebSocket**: TCP 回退方案，用于信令和备用通道
- **消息队列**: 高并发消息缓冲

### QFramework IoC 容器

所有 Systems 和 Models 都在 `CrazyCar.cs` 中注册，通过依赖注入获取：

```csharp
// 获取 System
var playerSystem = this.GetSystem<IPlayerManagerSystem>();

// 获取 Model
var userModel = this.GetModel<IUserModel>();

// 发送 Command
this.SendCommand(new SomeCommand());

// 监听 Event
this.RegisterEvent<SomeEvent>(OnSomeEvent);
```

### 认证系统

- 使用 JWT Token 认证
- Token 存储在 `Authorization` header
- Token payload 包含用户 ID (`jti` claim)
- 服务端工具类: `CrazyCarServer/src/main/java/com/tastsong/crazycar/utils/Util.java`

### 热更新系统

1. **资源热更新** (Addressables)
   - 资源打包为 AssetBundles
   - 运行时从服务器下载
   - 元数据存储在 `ab_resource` 表

2. **代码热更新** (HybridCLR)
   - AOT + 解释器模式
   - 支持 C# 代码热修复

---

## 学习导览

### Step 1: 项目入口点

从三个入口点开始理解项目启动流程：

**Unity 客户端**
```csharp
// CrazyCarClient/Assets/Scripts/CrazyCar.cs
// QFramework 架构初始化，注册所有 Systems 和 Models
```

**Spring Boot 服务端**
```java
// CrazyCarServer/src/main/java/com/tastsong/crazycar/CrazyCarApplication.java
// Spring Boot 应用启动类
```

**Vue 管理后台**
```javascript
// CrazyCarBackground/src/main.js
// Vue 应用入口，挂载根组件
```

### Step 2: Model 层 - 理解数据流

Model 层管理所有游戏状态，是 QFramework 架构的数据基础：

- `UserModel` - 用户核心数据
- `GameModel` - 游戏运行时状态
- `MatchModel` - 匹配和房间数据
- `TimeTrialModel` - 计时赛记录

Models 通过 Event 系统通知其他层数据变化。

### Step 3: System 层 - 理解业务逻辑

System 层包含所有游戏逻辑，处理业务规则：

- `INetworkSystem` - 网络通信编排
- `IPlayerManagerSystem` - 玩家生命周期管理
- `IMatchRoomSystem` - 匹配房间逻辑
- `ICheckpointSystem` - 检查点/路线系统

Systems 依赖 Models 获取数据，通过 Command 模式修改状态。

### Step 4: 网络通信 - 理解多人同步

网络系统是 CrazyCar 的核心，负责多人实时同步：

- **IKCPSystem** - KCP 协议实现
- **IWebSocketSystem** - WebSocket 实现
- **INetworkSystem** - 网络层抽象，统一两个协议

理解状态同步流程和消息队列处理机制。

---

## 文件映射

### Unity 客户端核心文件

| 路径 | 用途 |
|------|------|
| `Assets/Scripts/CrazyCar.cs` | QFramework 架构入口 |
| `Assets/Scripts/Game/NetworkController.cs` | 网络控制器 |
| `Assets/Scripts/Game/MPlayer.cs` | 玩家控制器 |
| `Assets/Scripts/Game/AIController.cs` | AI 机器人逻辑 |
| `Assets/Scripts/System/NetworkSystem.cs` | 网络系统编排 |

### 服务端核心文件

| 路径 | 用途 |
|------|------|
| `src/main/java/com/tastsong/crazycar/CrazyCarApplication.java` | Spring Boot 启动类 |
| `src/main/java/com/tastsong/crazycar/controller/` | REST API 控制器 |
| `src/main/java/com/tastsong/crazycar/utils/Util.java` | JWT 和工具函数 |
| `src/main/resources/application.properties` | 服务器配置 |
| `src/main/resources/data.sql` | 数据库初始化脚本 |

### 数据库关键表

| 表名 | 用途 |
|------|------|
| `all_user` | 用户账户 (uid, user_name, star, aid) |
| `avatar_name`, `avatar_uid` | 头像所有权 |
| `all_equip`, `equip_uid` | 赛车/装备所有权 |
| `time_trial_class`, `time_trial_record` | 计时赛数据 |
| `match_class`, `match_record` | 多人比赛数据 |
| `ab_resource` | Addressable 资源元数据 |
| `forced_updating` | 版本控制和强制更新 |

---

## 复杂度热点

需要特别注意的复杂区域：

| 区域 | 复杂度 | 注意事项 |
|------|--------|----------|
| **网络同步** | 高 | 双协议切换、消息队列、状态同步 |
| **物理同步** | 高 | 服务端权威、客户端预测 |
| **热更新** | 中 | Addressables 配置、HybridCLR 限制 |
| **匹配系统** | 中 | 房间状态机、玩家管理 |
| **AI 系统** | 中 | AIController 行为逻辑 |

---

## 构建和运行

### 服务端 (Maven)
```bash
cd CrazyCarServer
mvn clean package -DskipTests
java -jar target/crazy_car.jar                    # 开发环境
java -jar target/crazy_car.jar --spring.profiles.active=prod  # 生产环境
```

### 管理后台 (npm)
```bash
cd CrazyCarBackground
npm run dev              # 开发服务器
npm run build:prod       # 生产构建
npm run build:stage      # 预发布构建
npm run lint             # ESLint 检查
npm run test:unit        # 单元测试
```

### Unity 客户端
- 使用 Unity 编辑器构建系统
- 构建目标: Android (Gradle/IL2CPP), iOS (Xcode), WebGL
- 主要 Unity 包: Addressables 1.19.19, HybridCLR, URP 12.1.7

---

## 环境要求

| 组件 | 版本 |
|------|------|
| Unity | 2021.3+ |
| Java | 17 |
| Node.js | >= 8.9 |
| npm | >= 3.0.0 |
| MySQL | 8.0.32 |

---

## 相关资源

- **技术文档**: `README_Dev.md`
- **快速开始**: `README_QuickStart.md`
- **更新日志**: [GitHub Releases](https://github.com/TastSong/CrazyCar/releases)
- **设计文档**: `Document/` 目录
- **联系**: TastSong@163.com
- **QQ 群**: 577016553

---

*本文档由知识图谱自动生成 - 最后更新: 2026-03-23*
*基于: `.understand-anything/knowledge-graph.json` (102 nodes, 357 edges)*
