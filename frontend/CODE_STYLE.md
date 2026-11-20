# 代码规范文档
## 1. ESLint 规范
- 使用 ESLint + TypeScript 插件做语法、代码质量检查
- 可以自动修复的问题：执行 `pnpm lint:fix` 修复
- 禁止存在无法修复的ESLint错误提交到仓库

## 2. Prettier 格式化规范
- 统一代码格式，由 Prettier 负责
- 提交前 pre‑commit 钩子自动执行格式化
- 格式化配置在 `.prettierrc`

## 3. TypeScript 规范
- tsconfig.json 开启 strict: true，开启严格类型校验
- 所有TS类型错误必须修复，`pnpm typecheck` 无报错才允许合并

## 4. Git提交规范
- 提交前自动触发 pre‑commit 钩子，运行 lint‑staged
- 暂存区内代码自动格式化；存在无法自动修复错误会阻断提交
