# 业务事件草稿笔记
## 事件风暴讨论记录
本次事件风暴围绕问答平台 CampusOverflow 梳理业务：
1. 用户可以发布问题 Question
2. 用户可以回答问题 Answer
3. 用户可以对问题/回答进行点赞 Vote
4. 问题可以被标记、修改标题内容
5. 回答可以被编辑、删除

识别出的业务事件：
- QuestionCreated 问题已创建
- AnswerCreated 回答已提交
- VoteAdded 新增点赞
- QuestionUpdated 问题信息修改
- AnswerUpdated 回答内容修改

业务歧义点讨论：
- 点赞是否允许重复点赞：业务规则：同一个用户对同一个目标只能点赞一次，重复点赞视为无效。
- 删除回答：删除回答后，该回答下的点赞一并失效。

业务术语统一：
- 聚合根：Question、Answer
- 值对象：Vote
