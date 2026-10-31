# CampusOverflow 统一语言术语表 v1.0

> 版本：v1.0  
> 维护者：成员5  
> 范围：四个限界上下文（Identity / Q&A / Reputation / Discovery）核心术语

---

## 1. Identity 身份与课程上下文

| 术语 | 英文 | 定义 |
|---|---|---|
| 用户 | User | 平台的注册账号，拥有唯一身份标识 |
| 学生 | Student | 选课学习的普通用户角色 |
| 教师 | Teacher | 课程的授课教师，可认证回答、发起悬赏 |
| 助教 | TA (Teaching Assistant) | 协助教师管理课程的助教角色 |
| 管理员 | Admin | 平台运维人员，可禁用账号、调整角色 |
| 课程 | Course | 一门课程的实体，包含课程成员与课程内容 |
| 课程成员 | CourseMember | 加入某门课程的用户记录，含学生/教师/助教身份 |
| 角色 | Role | 用户在系统中的权限身份（STUDENT / TEACHER / TA / ADMIN） |
| 密码策略 | PasswordPolicy | 用户注册时的密码强度校验规则 |
| 用户注册事件 | UserRegisteredEvent | 用户完成注册后发出的集成事件，触发声誉账户初始化 |

---

## 2. Q&A 问答上下文

| 术语 | 英文 | 定义 |
|---|---|---|
| 问题 | Question | 用户发布的提问帖子，包含标题、正文、标签 |
| 回答 | Answer | 针对某个问题给出的解答内容 |
| 评论 | Comment | 对问题或回答的追加评论，支持嵌套回复 |
| 投票 | Vote | 用户对问题或回答的点赞（+1）或点踩（-1） |
| 标签 | Tag | 给问题打的分类标签，如 Java、数据结构 |
| 问题发布事件 | QuestionPostedEvent | 用户发布问题后发出的领域事件 |
| 回答提交事件 | AnswerSubmittedEvent | 用户提交回答后发出的领域事件 |
| 回答采纳事件 | AnswerAcceptedEvent | 提问者采纳某个回答为最佳答案后发出的事件 |
| 投票事件 | VoteCastEvent | 用户对问题或回答进行投票后发出的事件 |
| 已采纳回答 | Accepted Answer | 被提问者标记为最佳答案的回答 |
| Markdown正文 | MarkdownBody | 问题与回答的正文内容，支持 Markdown 渲染 |
| 软删除 | Soft Delete | 标记内容为已删除但不物理删除，前端列表不可见 |
| 最佳答案 | Accepted Answer | 提问者认可的、标记为已解决的回答 |

---

## 3. Reputation 声誉与激励上下文

| 术语 | 英文 | 定义 |
|---|---|---|
| 声誉账户 | ReputationAccount | 用户的声誉积分账户，记录可用积分与冻结积分 |
| 声誉流水 | ReputationLedger | 每一次声誉变动的流水记录，保证可追溯 |
| 徽章 | Badge | 用户达成特定成就后获得的荣誉标识 |
| 徽章策略 | BadgePolicy | 触发徽章授予的规则，如首次采纳、首百赞 |
| 悬赏 | Bounty | 教师或用户为某个问题设置的积分奖励 |
| 排行榜 | Leaderboard | 按周/月/学期展示的用户声誉排名 |
| 学期 | AcademicTerm | 学期周期划分，用于排行榜按学期统计 |
| 对账 | Reconciliation | 定期核对声誉账户余额与流水之和是否一致 |
| 每日上限 | Daily Cap | 每日通过投票可获得的积分上限，防止刷分 |
| 积分 | Points | 用户的声誉分值，通过回答、采纳等行为获得 |
| 冻结积分 | Frozen Points | 悬赏中被冻结的积分，结算后扣除或退还 |

---

## 4. Discovery 内容发现上下文

| 术语 | 英文 | 定义 |
|---|---|---|
| 搜索查询 | SearchQuery | 用户输入的关键词搜索请求 |
| 通知 | Notification | 系统向用户推送的消息提醒，如新回答、被采纳 |
| @提及 | Mention | 在评论或回答中@其他用户，触发对方通知 |
| 相关问题 | Related Question | 与当前问题语义相似的推荐问题 |
| 搜索索引 | SearchIndex | 用于全文检索的问题索引数据 |
| 全文检索 | Full Text Search | 基于关键词的问题全文搜索功能 |
| 未读通知 | Unread Notification | 用户尚未查看的通知数量 |
| 相似问题推荐 | Related Question Recommendation | 基于文本相似度推荐相关问题 |

---

## 术语统计

- Identity 上下文：10 个
- Q&A 上下文：13 个
- Reputation 上下文：11 个
- Discovery 上下文：8 个

**合计：42 个术语**（≥30 要求）
