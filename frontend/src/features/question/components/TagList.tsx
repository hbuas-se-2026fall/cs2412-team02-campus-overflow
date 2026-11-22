import { Space, Tag } from 'antd';

export interface TagListProps {
  tags: string[];
  activeTag?: string;
  onSelect?: (tag: string) => void;
}

export function TagList({ tags, activeTag, onSelect }: TagListProps) {
  return (
    <Space size={4} wrap>
      {tags.map((tag) => (
        <Tag
          key={tag}
          color={tag === activeTag ? 'blue' : undefined}
          style={onSelect === undefined ? undefined : { cursor: 'pointer' }}
          onClick={onSelect === undefined ? undefined : () => onSelect(tag)}
        >
          {tag}
        </Tag>
      ))}
    </Space>
  );
}
