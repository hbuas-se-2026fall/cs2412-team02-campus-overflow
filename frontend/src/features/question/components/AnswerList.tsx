import { List, Space, Tag, Typography } from 'antd';
import type { AnswerView } from '@/api/contracts';
import { formatDateTime } from '@/shared/lib/datetime';

export interface AnswerListProps {
  answers: AnswerView[];
}

export function AnswerList({ answers }: AnswerListProps) {
  if (answers.length === 0) {
    return <Typography.Text type="secondary">还没有回答，先来看看别人的思路吧。</Typography.Text>;
  }

  return (
    <List
      bordered
      dataSource={answers}
      renderItem={(answer) => (
        <List.Item key={answer.id}>
          <Space direction="vertical" size={6} style={{ width: '100%' }}>
            <Space size={8} wrap>
              <Typography.Text strong>{answer.author.displayName}</Typography.Text>
              <Tag color="gold">得分 {answer.score}</Tag>
              {answer.accepted ? <Tag color="green">已采纳</Tag> : null}
              {answer.endorsedBy === undefined ? null : <Tag color="blue">助教认可</Tag>}
              <Typography.Text type="secondary">{formatDateTime(answer.createdAt)}</Typography.Text>
            </Space>
            <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
              {answer.body}
            </Typography.Paragraph>
          </Space>
        </List.Item>
      )}
    />
  );
}
