import React from 'react';
import { View, Image, Text, StyleSheet, ImageSourcePropType, ViewStyle, ImageStyle } from 'react-native';
import { API_BASE } from '../utils/api';

// 导入默认头像
const boyAvatarDefault = require('../../assets/boy_avatar_default.png');
const girlAvatarDefault = require('../../assets/girl_avatar_default.png');

type Props = {
  avatar?: string | null;
  gender?: number; // 1 男, 2 女
  size?: number; // 默认40
  style?: ImageStyle;
};

export default function Avatar({ avatar, gender = 1, size = 40, style }: Props) {
  const borderRadius = size / 2;
  const source = resolveSource(avatar, gender);

  return (
    <Image
      source={source}
      style={[{ width: size, height: size, borderRadius }, style]}
      onError={(error) => {
        console.log('Avatar加载失败:', error);
      }}
    />
  );
}

function resolveSource(avatar?: string | null, gender: number = 1): ImageSourcePropType {
  // 如果有自定义头像，使用自定义头像
  if (avatar && !avatar.includes('avatar_default.png')) {
    if (avatar.startsWith('http')) return { uri: avatar };
    if (avatar.startsWith('/')) return { uri: `${API_BASE}${avatar}` };
    return { uri: avatar };
  }
  
  // 没有头像或是默认头像路径时，使用前端assets中的默认头像
  return gender === 2 ? girlAvatarDefault : boyAvatarDefault;
}

// 不再需要样式，因为现在总是使用Image组件


