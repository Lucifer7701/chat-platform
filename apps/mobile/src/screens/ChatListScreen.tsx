import React, { useCallback, useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl, Image } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_BASE, ApiResult, get } from '../utils/api';
import { useFocusEffect } from '@react-navigation/native';

type Props = NativeStackScreenProps<RootStackParamList, 'ChatList'>;

type Contact = {
  contactUserId: number;
  nickname: string;
  avatar?: string | null;
  lastMessage?: string | null;
  lastMessageTime?: string | null;
  unreadCount?: number;
  online?: boolean;
};

export default function ChatListScreen({ navigation }: Props) {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [refreshing, setRefreshing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const loadContacts = useCallback(async (isRefresh = false) => {
    try {
      if (isRefresh) setRefreshing(true); else setLoading(true);
      setError(null);
      const token = await AsyncStorage.getItem('token');
      if (!token) {
        setContacts([]);
        setError('未登录');
        return;
      }
      const res: ApiResult<Contact[]> = await get<ApiResult<Contact[]>>('/api/chat/contacts', token);
      if (res.code !== 200) {
        throw new Error(res.message || '加载失败');
      }
      setContacts((res.data || []).sort((a, b) => {
        const ta = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
        const tb = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
        return tb - ta;
      }));
    } catch (e: any) {
      setError(e?.message ?? '网络错误');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    loadContacts();
  }, [loadContacts]);

  useFocusEffect(
    useCallback(() => {
      // 页面重新聚焦时刷新，便于会话返回后更新未读
      loadContacts(true);
    }, [loadContacts])
  );

  const renderItem = ({ item }: { item: Contact }) => (
    <TouchableOpacity
      style={styles.item}
      onPress={() => navigation.navigate('Chat', { toUserId: item.contactUserId })}
    >
      <View style={styles.row}>
        <View style={styles.avatarWrap}>
          {item.avatar ? (
            <Image source={{ uri: item.avatar.startsWith('http') ? item.avatar : `${API_BASE}${item.avatar}` }} style={styles.avatar} />
          ) : (
            <View style={[styles.avatar, styles.avatarPlaceholder]} />
          )}
          {item.online ? <View style={styles.dotOnline} /> : null}
        </View>
        <View style={styles.col}>
          <View style={styles.rowBetween}>
            <Text style={styles.name} numberOfLines={1}>{item.nickname || `用户${item.contactUserId}`}</Text>
            {!!item.unreadCount && item.unreadCount > 0 ? (
              <View style={styles.badge}><Text style={styles.badgeText}>{item.unreadCount}</Text></View>
            ) : null}
          </View>
          <Text style={styles.sub} numberOfLines={1}>{item.lastMessage || ''}</Text>
        </View>
      </View>
    </TouchableOpacity>
  );

  if (loading && contacts.length === 0) {
    return (
      <View style={styles.center}> 
        <ActivityIndicator />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      {(!contacts || contacts.length === 0) && !loading ? (
        <View style={styles.center}><Text style={styles.sub}>暂无会话</Text></View>
      ) : (
        <FlatList
          data={contacts}
          keyExtractor={(item) => String(item.contactUserId)}
          renderItem={renderItem}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => loadContacts(true)} />}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 12 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  error: { color: 'red', padding: 8 },
  item: { paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#eee' },
  row: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  col: { flex: 1 },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  name: { fontSize: 16, fontWeight: '600' },
  sub: { color: '#666', marginTop: 4 },
  avatarWrap: { position: 'relative' },
  avatar: { width: 40, height: 40, borderRadius: 20, backgroundColor: '#ddd' },
  avatarPlaceholder: { backgroundColor: '#eee' },
  dotOnline: { position: 'absolute', right: 0, bottom: 0, width: 10, height: 10, borderRadius: 5, backgroundColor: '#2ecc71', borderWidth: 2, borderColor: '#fff' },
  badge: { minWidth: 18, paddingHorizontal: 6, height: 18, borderRadius: 9, backgroundColor: '#ff4d4f', alignItems: 'center', justifyContent: 'center' },
  badgeText: { color: '#fff', fontSize: 12 },
});


