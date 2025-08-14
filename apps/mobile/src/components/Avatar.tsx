import React from 'react';
import { View, Image, Text, StyleSheet, ImageSourcePropType, ViewStyle, ImageStyle } from 'react-native';
import { API_BASE } from '../utils/api';

type Props = {
  avatar?: string | null;
  gender?: number; // 1 男, 2 女
  size?: number; // 默认40
  style?: ViewStyle | ImageStyle;
};

export default function Avatar({ avatar, gender = 1, size = 40, style }: Props) {
  const borderRadius = size / 2;
  const source = resolveSource(avatar);

  if (source) {
    return (
      <Image
        source={source}
        style={{ width: size, height: size, borderRadius }}
      />
    );
  }

  const backgroundColor = gender === 2 ? '#E91E63' : '#4A90E2';
  const text = gender === 2 ? '女' : '男';

  return (
    <View
      style={[
        styles.circle,
        { width: size, height: size, borderRadius, backgroundColor },
        style,
      ]}
    >
      <Text style={styles.text}>{text}</Text>
    </View>
  );
}

function resolveSource(avatar?: string | null): ImageSourcePropType | undefined {
  if (!avatar) return undefined;
  if (avatar.startsWith('http')) return { uri: avatar };
  if (avatar.startsWith('/')) return { uri: `${API_BASE}${avatar}` };
  return { uri: avatar };
}

const styles = StyleSheet.create({
  circle: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  text: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '700',
  },
});


