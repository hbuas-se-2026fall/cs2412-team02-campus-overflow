# CampusOverflow 前端（Lab5 工程骨架）

React 18 + TypeScript 5 + Vite 5 的 TypeScript 严格模式工程。本目录是实验五的**起点**：
成员 A 负责骨架与契约，成员 B 加代码质量工具链，成员 C 做 API 层，成员 D 做页面与路由，成员 E 做集成验收。

---

## 1. 环境要求

| 工具 | 版本 | 说明 |
| --- | --- | --- |
| Node.js | **v20.19.0**（已验证） | `package.json` 的 `engines` 约束 `>=20`；团队统一 20 LTS，别用 24 生成锁文件 |
| pnpm | **9.15.0**（已验证） | `package.json` 的 `packageManager` 已锁定，装了 corepack 会自动切到该版本 |
| 后端 | 本机 `localhost:8080` | `backend/co-bootstrap` 启动后提供 `/api/**` |

> 不要用 npm 安装，会生成 `package-lock.json` 并和 `pnpm-lock.yaml` 打架（CI 里的 npm 只是为了跑构建，不参与依赖锁定）。

## 2. 启动

```bash
cd frontend
pnpm install          # 首次或依赖变更后执行
pnpm dev              # http://localhost:5173
```

`frontend/.npmrc` 里固定了 `node-linker=hoisted`：Windows 下 pnpm 默认的软链布局会让 Node 解析不到 vite 的传递依赖，报 `Cannot find package 'esbuild'`，改成扁平布局后 Windows / macOS / Linux 一致。

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
│  │  └─ question/    #   问答特性
│  │     ├─ api/      #     questionApi（当前为 mock 实现，成员C 接真实接口只改这里）
│  │     ├─ components/  #   该特性私有组件：TagList、AnswerList
│  │     └─ hooks/    #   该特性的数据 hooks：useQuestionList、useQuestionDetail、useCreateQuestion
│  ├─ pages/          # 成员D：路由级页面（列表 / 新建 / 详情，一个页面一个文件）
│  ├─ shared/         # 跨特性复用：shared/ui（AppShell）、shared/lib（工具）、shared/hooks
│  ├─ App.tsx         # 路由装配 + QueryClientProvider
│  └─ main.tsx        # 入口（ConfigProvider + antd App + BrowserRouter）
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

## 7. 常见问题

| 现象 | 原因与处理 |
| --- | --- |
| `Cannot find package 'esbuild'` | pnpm 隔离布局 + Windows junction 导致，本仓库已在 `.npmrc` 用 `node-linker=hoisted` 规避；若仍出现，删掉 `node_modules` 重新 `pnpm install` |
| pnpm 提示 `Ignored build scripts: esbuild` | pnpm 10+ 默认拦截依赖脚本，执行 `pnpm approve-builds` 勾选 esbuild（或把 pnpm 切到 9.15.0） |
| `corepack prepare` 报 `EPERM ... \AppData\Local\node\corepack` | 受限环境写不了用户目录，设置 `COREPACK_HOME` 到项目内目录再执行 |
| `pnpm dev` 首次请求转圈十几秒 | Vite 首次预构建依赖（optimizeDeps），第二次起就快了 |

## 8. 当前状态


- [x] Vite 5 + React 18 + TS 5，`strict` 与 `noUncheckedIndexedAccess` 全开
- [x] `@/` 别名（tsconfig + vite 双处）
- [x] 目录骨架：`pages/`、`features/`、`api/`、`shared/`
- [x] API 契约 `src/api/contracts.d.ts`（覆盖认证、问答、投票、评论、检索、声誉、通知）
- [x] `/api` 代理与 `5173` 固定端口
- [x] 环境已验证：Node **v20.19.0** + pnpm **9.15.0**，`pnpm typecheck` / `pnpm build` / `pnpm dev` 全部通过
- [ ] 代码质量工具链（成员B）
- [ ] axios + TanStack Query（成员C）
- [x] AppShell 布局与三个页面（成员D）—— 路由 `/questions`、`/questions/new`、`/questions/:id`

## 9. 成员D 交付说明（页面与路由）

| 路由 | 页面 | 数据来源 |
| --- | --- | --- |
| `/questions` | `QuestionListPage` | `useQuestionList`（分页 + 关键词 + 标签筛选）、`usePopularTags` |
| `/questions/new` | `NewQuestionPage` | `useCreateQuestion`，成功后跳转 `/questions/{id}` |
| `/questions/:id` | `QuestionDetailPage` | `useQuestionDetail`，含回答列表 |

- 布局：`src/shared/ui/AppShell.tsx`，`Header + Sider + Content(<Outlet/>)`，路由以无路径父路由挂在 AppShell 下。
- 约定：`pages` 只 import `features/question/hooks` 里的 hooks，不直接调用任何请求实现。
- 当前数据层是**内存 + localStorage 的 mock**（`features/question/api/questionApi.ts` + `mockQuestions.ts`），刷新页面数据不丢，方便录屏演示。
- 成员 C 合入 axios 与 TanStack QueryClient 后，只需替换 `features/question/api/` 里的实现（保持函数签名与 `@/api/contracts` 类型一致），页面与 hooks 的调用方无需改动。
