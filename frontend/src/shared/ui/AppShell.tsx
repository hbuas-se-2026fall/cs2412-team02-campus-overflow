import { useState } from 'react';
import { Button, Layout, Menu, Typography } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { APP_NAME } from '@/shared/lib/app';

const { Header, Sider, Content } = Layout;

const MENU_ITEMS = [
  { key: '/questions', label: '问题列表' },
  { key: '/questions/new', label: '我要提问' },
];

export function AppShell() {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const selectedKey = pathname.startsWith('/questions/new') ? '/questions/new' : '/questions';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', gap: 16, paddingInline: 24 }}>
        <Typography.Title level={4} style={{ margin: 0, color: '#fff', whiteSpace: 'nowrap' }}>
          {APP_NAME}
        </Typography.Title>
        <Typography.Text style={{ color: 'rgba(255, 255, 255, 0.65)' }}>校园技术问答</Typography.Text>
        <div style={{ flex: 1 }} />
        <Button type="primary" onClick={() => navigate('/questions/new')}>
          我要提问
        </Button>
      </Header>
      <Layout>
        <Sider
          theme="light"
          width={208}
          collapsible
          collapsed={collapsed}
          onCollapse={setCollapsed}
        >
          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            items={MENU_ITEMS}
            style={{ height: '100%', borderInlineEnd: 0 }}
            onClick={({ key }) => navigate(key)}
          />
        </Sider>
        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
