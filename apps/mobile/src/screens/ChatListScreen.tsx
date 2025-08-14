import React, { useCallback, useEffect, useState, useRef } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl } from 'react-native';

import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_BASE, ApiResult, get, buildWebSocketUrl } from '../utils/api';
import { useFocusEffect } from '@react-navigation/native';
import Avatar from '../components/Avatar';

type Props = {
  navigation: any;
};

type Contact = {
  contactUserId: number;
  nickname: string;
  avatar?: string | null;
  gender?: number;
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
  const wsRef = useRef<WebSocket | null>(null);

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
    setupWebSocketListener();
    return () => wsRef.current?.close();
  }, [loadContacts]);

  useFocusEffect(
    useCallback(() => {
      // 页面重新聚焦时刷新，便于会话返回后更新未读
      loadContacts(true);
    }, [loadContacts])
  );

  const setupWebSocketListener = async () => {
    try {
      const token = await AsyncStorage.getItem('token');
      if (!token) return;
      
      const url = buildWebSocketUrl(`/ws/chat/${token}`);
      const ws = new WebSocket(url);
      wsRef.current = ws;
      
      ws.onmessage = (ev) => {
        try {
          if (ev.data === 'PING') return;
          const data = JSON.parse(ev.data);
          
          // 收到新消息时刷新聊天列表
          if (data.type === 'message') {
            loadContacts(true);
          }
        } catch (e) {
          console.log('处理聊天列表WebSocket消息失败:', e);
        }
      };
      
      ws.onerror = (error) => {
        console.log('聊天列表WebSocket错误:', error);
      };
      
      ws.onclose = () => {
        console.log('聊天列表WebSocket连接关闭');
      };
    } catch (e) {
      console.log('建立聊天列表WebSocket连接失败:', e);
    }
  };

  const renderItem = ({ item }: { item: Contact }) => (
    <TouchableOpacity
      style={styles.item}
      onPress={() => navigation.navigate('Chat', { toUserId: item.contactUserId })}
    >
      <View style={styles.row}>
        <View style={styles.avatarWrap}>
          <Avatar avatar={item.avatar || undefined} gender={item.gender} size={40} />
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


