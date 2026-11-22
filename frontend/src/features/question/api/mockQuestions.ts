import type { AnswerView, Author, CourseRef, QuestionDetail } from '@/api/contracts';

interface SeedAnswer {
  author: Author;
  body: string;
  score: number;
  accepted?: boolean;
  endorsedBy?: Author;
  daysAgo: number;
}

interface SeedQuestion {
  id: number;
  title: string;
  body: string;
  tags: string[];
  courseId?: number;
  author: Author;
  score: number;
  viewCount: number;
  daysAgo: number;
  answers: SeedAnswer[];
}

const DAY = 24 * 60 * 60 * 1000;

const LIN_YIZHOU: Author = { id: 101, displayName: '林一舟', role: 'STUDENT', verified: true };
const ZHAO_MIN: Author = { id: 102, displayName: '赵敏', role: 'TA', verified: true };
const CHEN_ZHIYUAN: Author = { id: 103, displayName: '陈知远', role: 'TEACHER', verified: true };
const WANG_XINYI: Author = { id: 104, displayName: '王欣怡', role: 'STUDENT', verified: false };
const LIU_HAORAN: Author = { id: 105, displayName: '刘浩然', role: 'STUDENT', verified: false };
const SUN_QI: Author = { id: 106, displayName: '孙琪', role: 'STUDENT', verified: true };

const COURSES: Record<number, CourseRef> = {
  1: { id: 1, code: 'CS-2412', name: '软件工程实践' },
  2: { id: 2, code: 'CS-3101', name: '数据结构与算法' },
  3: { id: 3, code: 'CS-2208', name: '数据库系统' },
};

export const COURSE_OPTIONS = Object.values(COURSES).map((course) => ({
  value: course.id,
  label: `${course.code} ${course.name}`,
}));

export function findCourse(id: number | undefined): CourseRef | undefined {
  return id === undefined ? undefined : COURSES[id];
}

function isoBefore(days: number): string {
  return new Date(Date.now() - days * DAY).toISOString();
}

function toAnswers(questionId: number, seeds: SeedAnswer[]): AnswerView[] {
  return seeds.map((seed, index) => ({
    id: questionId * 10 + index + 1,
    body: seed.body,
    bodyHtml: '',
    author: seed.author,
    score: seed.score,
    accepted: seed.accepted ?? false,
    endorsedBy: seed.endorsedBy,
    myVote: 0,
    canEdit: false,
    createdAt: isoBefore(seed.daysAgo),
    updatedAt: isoBefore(seed.daysAgo),
    comments: [],
  }));
}

const SEEDS: SeedQuestion[] = [
  {
    id: 1,
    title: 'Spring Boot 启动时报 Flyway 校验和失败，如何回滚到上一次迁移？',
    body: '本地把 V3 迁移脚本改了一个字段名，重启后 Flyway 报 checksum mismatch，直接失败退出。\n请问正确的处理姿势是什么？能不能只回滚本地库？',
    tags: ['spring-boot', 'flyway', '数据库迁移'],
    courseId: 1,
    author: LIN_YIZHOU,
    score: 18,
    viewCount: 236,
    daysAgo: 2,
    answers: [
      {
        author: CHEN_ZHIYUAN,
        body: '已经合入别人的迁移脚本就不要再改它，新增一个 V4 去修。\n本地想重来可以对开发库执行 flyway repair 后再 clean + migrate，但 clean 会清空数据，只允许在本地开发库用。',
        score: 12,
        accepted: true,
        endorsedBy: CHEN_ZHIYUAN,
        daysAgo: 1,
      },
      {
        author: ZHAO_MIN,
        body: '补充一点：CI 上不要开 clean，只跑 migrate，否则测试数据会被清空。',
        score: 5,
        daysAgo: 1,
      },
    ],
  },
  {
    id: 2,
    title: 'React 18 严格模式下 useEffect 执行两次，导致提问接口被重复提交',
    body: '开发环境点一次发布，后端收到两条记录。查了是 StrictMode 故意双调用 effect。\n业务上应该怎么写才安全？',
    tags: ['react', 'useEffect', '前端工程'],
    courseId: 1,
    author: WANG_XINYI,
    score: 25,
    viewCount: 412,
    daysAgo: 3,
    answers: [
      {
        author: ZHAO_MIN,
        body: 'StrictMode 的双调用只在开发环境，用来暴露没有清理逻辑的副作用。\n写请求不要放在 useEffect 里直接发，应该由用户操作触发的 mutation（例如 TanStack Query 的 useMutation）来发，就不会重复。',
        score: 20,
        accepted: true,
        daysAgo: 3,
      },
    ],
  },
  {
    id: 3,
    title: 'Docker Compose 里后端连不上 MySQL，报 Communications link failure',
    body: '本地 docker compose up 之后后端日志一直重连 MySQL 失败。\n把 localhost 换成 127.0.0.1 也不行。',
    tags: ['docker', 'mysql', '后端'],
    courseId: 1,
    author: LIU_HAORAN,
    score: 9,
    viewCount: 178,
    daysAgo: 5,
    answers: [
      {
        author: CHEN_ZHIYUAN,
        body: '容器里的 localhost 指向容器自己，不是宿主机。\n同一 compose 网络里应该写服务名，例如 jdbc:mysql://mysql:3306/campus_overflow，并确认 depends_on 与 healthcheck 已经生效。',
        score: 14,
        accepted: true,
        daysAgo: 4,
      },
      {
        author: SUN_QI,
        body: '还要看 MySQL 8 的认证插件，老驱动要加 allowPublicKeyRetrieval=true。',
        score: 3,
        daysAgo: 4,
      },
    ],
  },
  {
    id: 4,
    title: 'TypeScript 严格模式下 noUncheckedIndexedAccess 打开后数组取值全是 undefined',
    body: '开了 noUncheckedIndexedAccess，arr[0].name 直接编译不过，项目里到处是索引访问，怎么改成本最低？',
    tags: ['typescript', 'strict', '前端工程'],
    courseId: 1,
    author: LIN_YIZHOU,
    score: 16,
    viewCount: 305,
    daysAgo: 6,
    answers: [
      {
        author: ZHAO_MIN,
        body: '这个开关是在提醒你索引访问本来就可能越界。\n优先用 for...of、map、find 配合判空；确实需要按下标时用 arr.at(0) 加显式判断，而不是 as 断言绕过去。',
        score: 11,
        accepted: true,
        daysAgo: 5,
      },
    ],
  },
  {
    id: 5,
    title: 'JPA 里 aggregate 根与实体混用，保存后领域事件没有发出去了',
    body: 'Question 聚合根里加了 Answer 列表，直接 repository.save(question) 后 @DomainEvents 不触发，只有改标题才触发。',
    tags: ['jpa', 'ddd', '领域事件'],
    courseId: 1,
    author: SUN_QI,
    score: 21,
    viewCount: 264,
    daysAgo: 8,
    answers: [
      {
        author: CHEN_ZHIYUAN,
        body: 'Spring Data 的 @DomainEvents 是在实体被保存时收集的，事件要挂在聚合根上。\n给 Answer 单独建 repository 保存会绕过聚合根，事件自然不发。所有修改都走聚合根的方法，再由 repository 保存聚合根。',
        score: 17,
        accepted: true,
        daysAgo: 7,
      },
    ],
  },
  {
    id: 6,
    title: 'Ant Design 表单里 Select mode=tags 怎么做数量校验？',
    body: '标签要求 1 到 5 个，用 required 只能校验非空，超过 5 个没有提示。',
    tags: ['antd', '表单校验', '前端工程'],
    courseId: 1,
    author: WANG_XINYI,
    score: 7,
    viewCount: 143,
    daysAgo: 9,
    answers: [
      {
        author: LIN_YIZHOU,
        body: '用自定义 validator，拿到 value 数组判断长度，不满足就 Promise.reject 一个新 Error，气泡里就会显示你写的文案。',
        score: 6,
        daysAgo: 8,
      },
    ],
  },
  {
    id: 7,
    title: 'pnpm install 之后 vite 报 Cannot find package esbuild 怎么解？',
    body: 'Windows 下 pnpm 装完依赖，pnpm dev 直接报找不到 esbuild，重装也没用。',
    tags: ['pnpm', 'vite', 'windows'],
    courseId: 1,
    author: LIU_HAORAN,
    score: 13,
    viewCount: 389,
    daysAgo: 11,
    answers: [
      {
        author: ZHAO_MIN,
        body: 'pnpm 默认用软链布局，Windows 上 junction 会让 Node 解析不到 vite 的传递依赖。\n在项目 .npmrc 里加 node-linker=hoisted 换成扁平布局，删掉 node_modules 重装即可。',
        score: 10,
        accepted: true,
        daysAgo: 10,
      },
    ],
  },
  {
    id: 8,
    title: '复杂度分析：为什么哈希表的平均查找是 O(1)，最坏却是 O(n)？',
    body: '课本上写平均 O(1)，但冲突严重时退化成链表。实际工程里怎么避免最坏情况？',
    tags: ['数据结构', '哈希表', '复杂度'],
    courseId: 2,
    author: SUN_QI,
    score: 30,
    viewCount: 512,
    daysAgo: 13,
    answers: [
      {
        author: CHEN_ZHIYUAN,
        body: '平均 O(1) 的前提是哈希函数把键均匀映射到桶里，负载因子受控。\n所有键落到同一个桶就退化成线性表，查找 O(n)。工程上用开放寻址或红黑树化的桶（如 Java HashMap 的树化）把最坏情况压到 O(log n)。',
        score: 24,
        accepted: true,
        endorsedBy: CHEN_ZHIYUAN,
        daysAgo: 12,
      },
    ],
  },
  {
    id: 9,
    title: 'MySQL 索引失效的常见场景有哪些？',
    body: '给 where 条件字段都建了索引，explain 还是全表扫描，想系统整理一下索引失效的原因。',
    tags: ['mysql', '索引', '数据库'],
    courseId: 3,
    author: LIN_YIZHOU,
    score: 22,
    viewCount: 421,
    daysAgo: 15,
    answers: [
      {
        author: ZHAO_MIN,
        body: '最常见的几类：字段上做函数运算或隐式类型转换、前导通配符 like、联合索引不满足最左前缀、or 连接了无索引字段、范围查询之后的列失效。',
        score: 19,
        accepted: true,
        daysAgo: 14,
      },
    ],
  },
  {
    id: 10,
    title: 'Sprint 评审时演示环境总是数据和本地不一致，有什么好办法？',
    body: '每次评审前都要临时改数据，演示完还得还原，很容易翻车。',
    tags: ['scrum', '团队协作', '工程实践'],
    courseId: 1,
    author: WANG_XINYI,
    score: 11,
    viewCount: 167,
    daysAgo: 18,
    answers: [
      {
        author: CHEN_ZHIYUAN,
        body: '把演示数据做成可重复执行的 seed 脚本，评审前一条命令重建环境，不要手工改库。\n演示脚本也进版本库，跟着 Sprint 一起维护。',
        score: 9,
        accepted: true,
        daysAgo: 17,
      },
    ],
  },
];

export function createSeedQuestions(): QuestionDetail[] {
  return SEEDS.map((seed) => {
    const answers = toAnswers(seed.id, seed.answers);
    return {
      id: seed.id,
      title: seed.title,
      body: seed.body,
      bodyHtml: '',
      tags: seed.tags,
      course: seed.courseId === undefined ? undefined : COURSES[seed.courseId],
      author: seed.author,
      score: seed.score,
      answerCount: answers.length,
      viewCount: seed.viewCount,
      acceptedAnswerId: answers.find((answer) => answer.accepted)?.id,
      myVote: 0,
      createdAt: isoBefore(seed.daysAgo),
      updatedAt: isoBefore(seed.daysAgo),
      permissions: { canEdit: false, canDelete: false, canAccept: false, canManage: false },
      comments: [],
      answers,
    };
  });
}
