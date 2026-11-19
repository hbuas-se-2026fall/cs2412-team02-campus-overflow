# CampusOverflow 前端（Lab5 工程骨架）

React 18 + TypeScript 5 + Vite 5 的 TypeScript 严格模式工程。本目录是实验五的**起点**：
成员 A 负责骨架与契约，成员 B 加代码质量工具链，成员 C 做 API 层，成员 D 做页面与路由，成员 E 做集成验收。

---

## 1. 环境要求

| 工具 | 版本 | 说明 |
| --- | --- | --- |
| Node.js | >= 20（建议 22 LTS） | `package.json` 的 `engines` 已约束 |
| pnpm | >= 9 | 团队统一用 pnpm，不要混用 npm 生成 `package-lock.json` |
| 后端 | 本机 `localhost:8080` | `backend/co-bootstrap` 启动后提供 `/api/**` |

## 2. 启动

```bash
cd frontend
pnpm install          # 首次或依赖变更后执行
pnpm dev              # http://localhost:5173
```

开发服务器把 `/api` 代理到 `http://localhost:8080`（见 `vite.config.ts`）。
**必须走代理而不是直连 8080**：后端用 Cookie 会话 + CSRF 双提交，跨源会拿不到 `XSRF-TOKEN`。

其他命令：

```bash
pnpm typecheck        # tsc --noEmit，必须 0 错误
pnpm build            # 类型检查 + 生产构建，产物在 dist/
pnpm preview          # 预览 dist/
```

## 3. 目录规范

```
frontend/
├─ src/
│  ├─ api/            # 成员C：axios 实例、拦截器、请求函数
│  │  └─ contracts.d.ts   # 成员A：接口入参/出参契约（本文件是“唯一事实来源”）
│  ├─ features/       # 按业务特性组织：features/question/、features/auth/ ...
│  │  ├─ components/  #   该特性私有组件
│  │  └─ hooks/       #   该特性的数据 hooks（useQuestionList 等）
│  ├─ pages/          # 成员D：路由级页面（一个页面一个文件）
│  ├─ shared/         # 跨特性复用：shared/ui（通用组件）、shared/lib（工具）、shared/hooks
│  ├─ App.tsx         # 布局与路由装配
│  └─ main.tsx        # 入口
├─ public/
├─ index.html
├─ tsconfig.json      # strict + noUncheckedIndexedAccess + @/ 别名
└─ vite.config.ts     # @/ 别名 + /api 代理
```

**依赖方向**（避免循环依赖）：`pages → features → shared`，`api` 可被任意层引用，`shared` 不得反向依赖 `features/pages`。

## 4. API 契约怎么用

`src/api/contracts.d.ts` 已经把所有接口的**入参 / 出参**写成 TypeScript 类型（`ApiContract` 里还有一张“方法 + 路径 + 请求 + 响应”的索引表）。成员 C、D 直接 `import type` 即可，不要自己再定义一份：

```ts
import type { QuestionSummary, Page, CreateQuestionRequest } from '@/api/contracts'; // 通过别名
// 或者相对路径：import type { ... } from '../api/contracts';

async function listQuestions(page: number): Promise<Page<QuestionSummary>> {
  const res = await api.get<Page<QuestionSummary>>('/v1/questions', { params: { page } });
  return res.data;
}
```

要点：

- 错误响应统一是 RFC 9457 Problem Details，额外带业务码 `code`（如 `QA-1001`），拦截器里按 `code` 分类提示。
- 时间字段是 ISO-8601 UTC 字符串，展示时再转本地时区。
- 分页响应固定 `{ items, total, page, size }`。

## 5. 路径别名

`@/` 等价于 `src/`，在 `tsconfig.json`（`paths`）和 `vite.config.ts`（`resolve.alias`）两处同步配置，改一处必须改另一处。

```ts
import { AppShell } from '@/shared/ui/AppShell';
```

## 6. 协作约定（实验五）

| 成员 | 分支 | 负责 |
| --- | --- | --- |
| A | `feature/lab5-init-template` | 工程骨架、tsconfig、别名、API 契约、本 README |
| B | `feature/lab5-lint-quality` | ESLint / Prettier / Husky + lint-staged、CI 前端检查 |
| C | `feature/lab5-apiservice` | axios 实例与拦截器、TanStack QueryClient、mock 数据 |
| D | `feature/lab5-pages-ui` | AppShell 布局、路由、列表 / 新建 / 详情三个页面 |
| E | `feature/lab5-e2e-doc` | 集成验证、录屏、实验文档、PR 汇总 |

**顺序很重要**：A 的 PR 合入 `develop` 之后，B/C/D 先 `git pull origin develop` 再各自开分支，否则会因为 `package.json`、`tsconfig.json`、`vite.config.ts` 冲突返工。

```bash
git checkout develop
git pull origin develop
git checkout -b feature/lab5-xxx
```

## 7. 当前状态（成员A 交付）

- [x] Vite 5 + React 18 + TS 5，`strict` 与 `noUncheckedIndexedAccess` 全开
- [x] `@/` 别名（tsconfig + vite 双处）
- [x] 目录骨架：`pages/`、`features/`、`api/`、`shared/`
- [x] API 契约 `src/api/contracts.d.ts`（覆盖认证、问答、投票、评论、检索、声誉、通知）
- [x] `/api` 代理与 `5173` 固定端口
- [ ] 代码质量工具链（成员B）
- [ ] axios + TanStack Query（成员C）
- [ ] AppShell 与三个页面（成员D）—— 目前 `src/pages/` 里是课程模板自带的示例页面，D 的 PR 会替换
