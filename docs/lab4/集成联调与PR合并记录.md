# 实验四 · 成员五 集成联调与 PR 合并记录

> 仓库：<https://github.com/hbuas-se-2026fall/cs2412-team02-campus-overflow>
> 角色：成员5（全局串联 / PR 管理 / 冲突处理 / 数据库配置 / 全量编译 / 端到端验证）
> 集成分支：`develop`（所有 PR 的 base 均为 `develop`，`main` 只接收发布 PR）
> 时间：2026-11-17

---

## 1. 本轮合并的 PR

| PR | 分支 | 作者 | 内容 | 评审结论 | 合并提交 |
| --- | --- | --- | --- | --- | --- |
| #7 | `feature/chenyuan2-lab4-domain` | Xie666-yx | 领域层 Question / Answer / Vote / Comment 实现与单测 | 分支领域 API 与骨架不一致，由成员5 重写后合并 | `28f4fe4` |
| #12 | `feature/lab4-qzl-qa-interface` | TineCounting | QuestionCommandService / QueryService、Postman 集合 | Approve | `4c7536f` |
| #11 | `feature/lab4-twy-jpa-flyway-exception-long` | rz576013 | JPA 实体、Flyway 脚本、全局异常、Logback JSON | 与 develop 重复/冲突，裁剪为 Logback JSON 后合并 | `386ec0a` |
| #9 | `feature/member1-lab3-po` | TineCounting | Lab3 事件风暴笔记与 PO 评审意见（base: main） | Approve（文档与代码模型有两处出入，已批注） | `d8d6714` |
| 本 PR | `feature/lab4-m5-integration` | zxuan11231 | 剩余应用服务实现 + 数据源修复 + 集成记录 | 成员5 自检 | 见仓库 |

每个 PR 均留下了实质性评审意见（见 Pull requests → Conversation），没有空泛的 “LGTM”。

---

## 2. 冲突与问题清单（本轮真正的集成工作）

### 2.1 领域层 API 与骨架不一致（PR #7）

- **现象**：分支把 `Question.create(Long, Title, MarkdownBody, Set<TagName>)` 当作领域入口，而 `develop` 骨架（PR #1）定义的是
  `Question.post(long authorId, Long courseId, Title, MarkdownBody, Tags, Instant now)`；`Set<TagName>` 与 `Tags` 值对象也不一致。
- **后果**：分支还删掉了 `courseId`、`acceptedAnswerId`、`score/answerCount/viewCount`、`version` 字段，
  而成员4 的接口层（#12）调用的正是骨架签名，两者合并必然编译失败。
- **解决**：以骨架 API 为准重写领域实现（commit `4cb0cc6`），保留全部字段与计数器；删除与骨架不兼容的 `QuestionDomainTest`；
  骨架已有的 `QuestionTest/AnswerTest/VoteAndCommentTest` 原样保留作为规格。
- **验证**：`co-qa-domain` 18 个用例全部通过。

### 2.2 JPA 实体重复 + 表名不匹配（PR #11）

- **现象**：`co-qa-application/.../persistence/{Question,Answer,Vote}.java` 与 `co-qa-infrastructure` 已有实体重复，
  且映射 `question/answer/vote`，实际 Flyway 建的是 `qa_question/qa_answer/qa_vote`。
- **后果**：`ddl-auto=none` 时启动不报错但读写对不上表；若改成 `validate` 直接启动失败；同时把 JPA 注解带进应用层，违反 ADR-008。
- **解决**：删除这三个实体。

### 2.3 Flyway 脚本不在扫描路径（PR #11）

- **现象**：`co-qa-application/src/main/resources/db/migration/V1__init_schema.sql`。
- **后果**：`spring.flyway.locations` 只扫描 `db/migration/{platform,identity,qa,reputation,discovery}`，该脚本永远不会执行；
  其中的 `user` 表还与 identity 上下文的 `id_user` 冲突。
- **解决**：删除该脚本，沿用 `db/migration/qa/V202609010200__qa_init.sql`。

### 2.4 全局异常处理器重复（PR #11）

- **现象**：`co-qa-application` 新增了一个 `@RestControllerAdvice`，而 `co-bootstrap` 已有完整的 RFC 9457 `GlobalExceptionHandler`。
- **后果**：两个 advice 的 `Exception` 处理会冲突；新加的 catch-all 会把 404/405 也变成 500。
- **解决**：删除重复 handler 与 `ErrorResult`，只保留成员3 的 Logback JSON 配置。

### 2.5 数据源 URL 拼错导致应用无法启动（develop 既有缺陷）

- **现象**：`application.yml` 的 `characterEncoding=utf8mb4`。
- **后果**：MySQL Connector/J 只接受 Java 字符集名，启动直接抛
  `Unsupported character encoding 'utf8mb4'`，Flyway 拿不到连接，`mvn spring-boot:run` 必定失败。
- **解决**：改成 `characterEncoding=UTF-8&connectionCollation=utf8mb4_0900_ai_ci`，
  并补 `allowPublicKeyRetrieval=true&useSSL=false` 以适配 MySQL 8 的 `caching_sha2_password`（本地无 SSL 时必需）。

### 2.6 剩余应用服务仍是 TODO

- `AnswerCommandService`（7 个方法）、`VoteService`（2 个）、`CommentService`（1 个）、`QaQueryApiImpl`（1 个）仍抛
  `UnsupportedOperationException`，导致 `AnswerAndVoteServiceTest` 4 个用例失败。
- **解决**：本 PR 按骨架的端口接口补齐（原子计数、改投增量、事件新旧值、幂等投票、评论一层回复），
  `co-qa-application` 4 个用例由失败转为通过。

---

## 3. 构建与测试结果

```
mvn -f backend/pom.xml test-compile        → 27 个模块 BUILD SUCCESS
mvn -f backend/pom.xml -pl co-qa/co-qa-domain -am test        → 18 tests, 0 failures
mvn -f backend/pom.xml -pl co-qa/co-qa-application -am test   → 4 tests, 0 failures
mvn -f backend/pom.xml install -DskipTests                    → BUILD SUCCESS（产出 campus-overflow.jar）
```

> 说明：`co-reputation-domain` 的 `BountyAndAccountTest` / `ReputationRulebookTest` 仍失败，
> 原因是 `TODO(S3)`（悬赏、徽章、每日上限属于 Sprint 3 范围），不属于实验四交付范围；
> 仓库 CI 也以 `-DskipTests` 运行，与本机结论一致。运行期还会看到 `BountyService.expireDue` 的定时任务告警，同属 S3 未实现。

---

## 4. 端到端验证（真实 MySQL 8 + Redis）

环境：MySQL 8.0.46（`campus_overflow`，utf8mb4）+ Redis 7 + `java -jar campus-overflow.jar`

| 步骤 | 请求 | 结果 |
| --- | --- | --- |
| 1 | `GET /api/v1/auth/csrf` | 200，下发 `XSRF-TOKEN` |
| 2 | `POST /api/v1/auth/register` | 201 `{"id":1}` |
| 3 | `POST /api/v1/auth/login` | 200，返回当前用户与角色 |
| 4 | `POST /api/v1/questions` | 201 `{"id":1}` |
| 5 | `GET /api/v1/questions/1` | 200，返回 Markdown 原文 + 渲染后的 `bodyHtml` + `permissions` |
| 6 | `POST /api/v1/questions/1/answers` | 201 `{"id":1}`，`answerCount` 变为 1 |
| 7 | `PUT /api/v1/votes`（+1 再改 -1） | 200 `{"myVote":1,"scoreDelta":1}` → `{"myVote":-1,"scoreDelta":-2}`，问题 `score` 变 -1 |
| 8 | `GET /api/v1/questions?page=1&size=10` | 200，`total` = 1 |

Flyway 启动日志：

```
Migrating schema `campus_overflow` to version "202609010001 - platform init"
Migrating schema `campus_overflow` to version "202609010100 - identity init"
Migrating schema `campus_overflow` to version "202609010200 - qa init"
Migrating schema `campus_overflow` to version "202609010300 - reputation init"
Migrating schema `campus_overflow` to version "202609010400 - discovery init"
Successfully applied 5 migrations to schema `campus_overflow`, now at version v202609010400
Started CampusOverflowApplication in 11.181 seconds
```

数据库核对（`SELECT id, author_id, title, score, answer_count, version FROM qa_question;`）：

```
id  author_id  title                                  score  answer_count  version
1   1         红黑树插入后如何判断该左旋还是右旋？        -1        1         0
```

`qa_question_tag` 有 2 行（数据结构、红黑树），`flyway_schema_history` 5 条 `success=1`。

---

## 5. 本地复现步骤

```bash
# 1) 数据库与缓存
mysql -u root -p -e "CREATE DATABASE campus_overflow CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -u root -p -e "CREATE USER 'campus'@'%' IDENTIFIED BY 'campus_dev_pwd'; GRANT ALL ON campus_overflow.* TO 'campus'@'%';"
redis-server --port 6379

# 2) 编译
mvn -f backend/pom.xml clean install -DskipTests

# 3) 启动（默认配置即可，全部走环境变量默认值）
java -jar backend/co-bootstrap/target/campus-overflow.jar

# 4) 冒烟
curl -c c.txt http://localhost:8080/api/v1/auth/csrf
```

---

## 6. 遗留事项（Sprint 3 之前）

1. `reputation` 上下文的 `BountyService.expireDue`、`Bounty`、`ReputationRulebook` 等仍是 `TODO(S3)`。
2. `detail()` 的浏览计数没有“同一用户去重”，作者自己刷新也会 +1。
3. `Answer.accept` 没有 `now` 参数，采纳后 `updated_at` 不刷新。
4. 前端 `frontend/` 本轮未启动验证，仅确认 CI 的前端构建为 green。
