import { useState } from 'react';
import { Alert, App as AntdApp, Button, Card, Form, Input, Select, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import type { CreateQuestionRequest } from '@/api/contracts';
import { useCourseOptions } from '@/features/question/hooks/useCourseOptions';
import { useCreateQuestion } from '@/features/question/hooks/useCreateQuestion';

interface QuestionFormValues {
  title: string;
  body: string;
  tags: string[];
  courseId?: number;
}

export function NewQuestionPage() {
  const navigate = useNavigate();
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<QuestionFormValues>();
  const createQuestion = useCreateQuestion();
  const courseOptions = useCourseOptions();
  const [errorText, setErrorText] = useState('');

  const handleFinish = async (values: QuestionFormValues) => {
    setErrorText('');
    const payload: CreateQuestionRequest = {
      title: values.title.trim(),
      body: values.body.trim(),
      tags: values.tags.map((tag) => tag.trim()).filter((tag) => tag.length > 0),
      courseId: values.courseId,
    };
    try {
      const created = await createQuestion.mutateAsync(payload);
      message.success('发布成功，正在跳转到详情页');
      navigate(`/questions/${created.id}`, { replace: true });
    } catch (error) {
      const reason = error instanceof Error ? error.message : '发布失败，请稍后重试';
      setErrorText(reason);
      message.error(reason);
    }
  };

  return (
    <Space direction="vertical" size={16} style={{ width: '100%', maxWidth: 760 }}>
      <Typography.Title level={3} style={{ margin: 0 }}>
        发布问题
      </Typography.Title>
      <Typography.Text type="secondary">
        标题 10~150 个字符，正文 10~30000 个字符，标签 1~5 个。
      </Typography.Text>

      {errorText === '' ? null : <Alert type="error" showIcon message={errorText} />}

      <Card>
        <Form<QuestionFormValues>
          form={form}
          layout="vertical"
          initialValues={{ tags: [] }}
          onFinish={handleFinish}
          onFinishFailed={() => message.warning('请先修正表单中标红的字段')}
        >
          <Form.Item
            label="标题"
            name="title"
            rules={[
              { required: true, message: '请输入标题' },
              { min: 10, max: 150, message: '标题长度需为 10~150 个字符' },
            ]}
          >
            <Input showCount maxLength={150} placeholder="用一句话描述你的问题" />
          </Form.Item>

          <Form.Item
            label="正文"
            name="body"
            rules={[
              { required: true, message: '请输入正文' },
              { min: 10, max: 30000, message: '正文长度需为 10~30000 个字符' },
            ]}
          >
            <Input.TextArea
              rows={10}
              showCount
              maxLength={30000}
              placeholder="补充上下文、复现步骤以及已经尝试过的方案"
            />
          </Form.Item>

          <Form.Item
            label="标签"
            name="tags"
            rules={[
              { required: true, message: '至少添加 1 个标签' },
              {
                validator: (_rule, value: string[] | undefined) => {
                  const count = value?.length ?? 0;
                  if (count < 1 || count > 5) return Promise.reject(new Error('标签数量需为 1~5 个'));
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Select
              mode="tags"
              tokenSeparators={[',', ' ']}
              notFoundContent={null}
              placeholder="输入标签后回车，最多 5 个"
            />
          </Form.Item>

          <Form.Item label="所属课程" name="courseId">
            <Select allowClear placeholder="可留空" options={courseOptions} />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <Space>
              <Button type="primary" htmlType="submit" loading={createQuestion.isPending}>
                发布问题
              </Button>
              <Button onClick={() => navigate('/questions')}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </Space>
  );
}
