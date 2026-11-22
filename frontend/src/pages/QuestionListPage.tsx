import { useState } from 'react';
import { Alert, Button, Input, Select, Space, Table, Typography } from 'antd';
import type { TableProps } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import type { QuestionSummary } from '@/api/contracts';
import { TagList } from '@/features/question/components/TagList';
import { usePopularTags } from '@/features/question/hooks/usePopularTags';
import { useQuestionList } from '@/features/question/hooks/useQuestionList';
import { DEFAULT_PAGE_SIZE } from '@/shared/lib/app';
import { formatDateTime } from '@/shared/lib/datetime';

export function QuestionListPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(1);
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [tag, setTag] = useState<string | undefined>(undefined);

  const listQuery = useQuestionList({ page, pageSize: DEFAULT_PAGE_SIZE, keyword, tag });
  const tagsQuery = usePopularTags();

  const selectTag = (next: string) => {
    setTag(next);
    setPage(1);
  };

  const columns: TableProps<QuestionSummary>['columns'] = [
    {
      title: '问题',
      key: 'title',
      render: (_value, record) => (
        <Space direction="vertical" size={2}>
          <Link to={`/questions/${record.id}`}>
            <Typography.Text strong>{record.title}</Typography.Text>
          </Link>
          <Typography.Text type="secondary">{record.excerpt}</Typography.Text>
        </Space>
      ),
    },
    {
      title: '标签',
      key: 'tags',
      width: 220,
      render: (_value, record) => (
        <TagList tags={record.tags} activeTag={tag} onSelect={selectTag} />
      ),
    },
    {
      title: '作者',
      key: 'author',
      width: 110,
      render: (_value, record) => record.author.displayName,
    },
    {
      title: '回答',
      key: 'answerCount',
      width: 110,
      render: (_value, record) =>
        record.accepted ? (
          <Typography.Text type="success">{record.answerCount} · 已采纳</Typography.Text>
        ) : (
          <Typography.Text>{record.answerCount}</Typography.Text>
        ),
    },
    {
      title: '浏览',
      dataIndex: 'viewCount',
      key: 'viewCount',
      width: 90,
    },
    {
      title: '发布时间',
      key: 'createdAt',
      width: 170,
      render: (_value, record) => formatDateTime(record.createdAt),
    },
  ];

  const tagOptions = (tagsQuery.data ?? []).map((item) => ({
    value: item.name,
    label: `${item.name}（${item.questionCount}）`,
  }));

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Space wrap style={{ width: '100%', justifyContent: 'space-between' }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          问题列表
        </Typography.Title>
        <Space wrap>
          <Input.Search
            allowClear
            placeholder="搜索标题、正文或标签"
            style={{ width: 280 }}
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            onSearch={(value) => {
              setKeyword(value.trim());
              setPage(1);
            }}
          />
          <Select
            allowClear
            placeholder="按标签筛选"
            style={{ width: 180 }}
            loading={tagsQuery.isPending}
            options={tagOptions}
            value={tag}
            onChange={(value) => {
              setTag(value);
              setPage(1);
            }}
          />
          <Button type="primary" onClick={() => navigate('/questions/new')}>
            我要提问
          </Button>
        </Space>
      </Space>

      {listQuery.isError ? (
        <Alert
          type="error"
          showIcon
          message="问题列表加载失败"
          description={listQuery.error?.message ?? '请稍后重试'}
        />
      ) : null}

      <Table<QuestionSummary>
        rowKey="id"
        columns={columns}
        dataSource={listQuery.data?.items ?? []}
        loading={listQuery.isFetching}
        scroll={{ x: 1000 }}
        locale={{ emptyText: '没有匹配的问题，换个关键词或直接发布一个新问题。' }}
        pagination={{
          current: page,
          pageSize: DEFAULT_PAGE_SIZE,
          total: listQuery.data?.total ?? 0,
          showSizeChanger: false,
          showTotal: (total) => `共 ${total} 个问题`,
          onChange: setPage,
        }}
      />
    </Space>
  );
}
