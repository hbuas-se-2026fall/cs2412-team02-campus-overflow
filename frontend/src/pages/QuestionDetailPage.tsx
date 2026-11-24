import { Alert, Button, Card, Divider, Space, Spin, Typography } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { AnswerList } from '@/features/question/components/AnswerList';
import { TagList } from '@/features/question/components/TagList';
import { useQuestionDetail } from '@/features/question/hooks/useQuestionDetail';
import { formatDateTime } from '@/shared/lib/datetime';

export function QuestionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const questionId = Number(id ?? '');
  const isValidId = Number.isInteger(questionId) && questionId > 0;
  const detailQuery = useQuestionDetail(isValidId ? questionId : 0);

  if (!isValidId) {
    return (
      <Alert
        type="warning"
        showIcon
        message="问题编号不合法"
        description={`无法识别的问题编号：${id ?? '空'}`}
        action={<Button onClick={() => navigate('/questions')}>返回列表</Button>}
      />
    );
  }

  if (detailQuery.isPending) {
    return (
      <div style={{ padding: 48, textAlign: 'center' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (detailQuery.isError) {
    return (
      <Alert
        type="error"
        showIcon
        message="加载问题失败"
        description={detailQuery.error?.message ?? '请稍后重试'}
        action={<Button onClick={() => navigate('/questions')}>返回列表</Button>}
      />
    );
  }

  const question = detailQuery.data;
  if (question === undefined) return null;

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Space wrap>
        <Button onClick={() => navigate('/questions')}>返回列表</Button>
        <Typography.Text type="secondary">
          {question.course === undefined
            ? '未关联课程'
            : `${question.course.code} ${question.course.name}`}
        </Typography.Text>
      </Space>

      <Card>
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {question.title}
          </Typography.Title>
          <Space size={12} wrap>
            <TagList tags={question.tags} />
            <Typography.Text type="secondary">{question.author.displayName}</Typography.Text>
            <Typography.Text type="secondary">{formatDateTime(question.createdAt)}</Typography.Text>
            <Typography.Text type="secondary">浏览 {question.viewCount}</Typography.Text>
            <Typography.Text>得分 {question.score}</Typography.Text>
          </Space>
          <Typography.Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
            {question.body}
          </Typography.Paragraph>
        </Space>
      </Card>

      <Divider orientation="left" style={{ margin: 0 }}>
        全部回答（{question.answers.length}）
      </Divider>

      <AnswerList answers={question.answers} />
    </Space>
  );
}
