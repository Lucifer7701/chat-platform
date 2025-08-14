import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, Image, Alert } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_BASE, get } from '../utils/api';

type Props = {
  navigation: any;
};

type User = {
  id: number;
  nickname: string;
  avatar?: string;
  city?: string;
  profession?: string;
  introduction?: string;
  gender: number;
};

export default function RecommendScreen({ navigation }: Props) {
  const [activeTab, setActiveTab] = useState<'same-city' | 'nearby'>('same-city');
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadUsers();
  }, [activeTab]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const token = await AsyncStorage.getItem('token');
      if (!token) return;

      const endpoint = activeTab === 'same-city' ? '/api/user/same-city' : '/api/user/nearby';
      const res = await get<any>(`${endpoint}?limit=20`, token);
      
      if (res.code === 200) {
        setUsers(res.extraData?.users || []);
      } else {
        Alert.alert('提示', res.message || '加载失败');
      }
    } catch (e) {
      Alert.alert('错误', '网络错误');
    } finally {
      setLoading(false);
    }
  };

  const handleUserPress = (user: User) => {
    // 直接进入聊天界面
    navigation.navigate('Chat', { toUserId: user.id });
  };

  const renderUser = ({ item }: { item: User }) => (
    <TouchableOpacity style={styles.userCard} onPress={() => handleUserPress(item)}>
      <Image
        source={{
          uri: item.avatar?.startsWith('http')
            ? item.avatar
            : `${API_BASE}${item.avatar || `/default/avatar_${item.gender === 1 ? 'male' : 'female'}.png`}`
        }}
        style={styles.avatar}
      />
      <View style={styles.userInfo}>
        <Text style={styles.nickname}>{item.nickname}</Text>
        {item.city && <Text style={styles.detail}>📍 {item.city}</Text>}
        {item.profession && <Text style={styles.detail}>💼 {item.profession}</Text>}
        {item.introduction && (
          <Text style={styles.introduction} numberOfLines={2}>
            {item.introduction}
          </Text>
        )}
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {/* 顶部Tab切换 */}
      <View style={styles.tabContainer}>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'same-city' && styles.activeTab]}
          onPress={() => setActiveTab('same-city')}
        >
          <Text style={[styles.tabText, activeTab === 'same-city' && styles.activeTabText]}>
            同城
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'nearby' && styles.activeTab]}
          onPress={() => setActiveTab('nearby')}
        >
          <Text style={[styles.tabText, activeTab === 'nearby' && styles.activeTabText]}>
            附近
          </Text>
        </TouchableOpacity>
      </View>

      {/* 用户列表 */}
      {loading ? (
        <View style={styles.center}>
          <Text>加载中...</Text>
        </View>
      ) : users.length === 0 ? (
        <View style={styles.center}>
          <Text style={styles.emptyText}>
            {activeTab === 'same-city' ? '暂无同城用户' : '暂无附近用户'}
          </Text>
          <TouchableOpacity style={styles.refreshButton} onPress={loadUsers}>
            <Text style={styles.refreshText}>刷新</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={users}
          keyExtractor={(item) => item.id.toString()}
          renderItem={renderUser}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.listContainer}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  tabContainer: {
    flexDirection: 'row',
    backgroundColor: 'white',
    paddingHorizontal: 20,
    paddingTop: 50,
    paddingBottom: 10,
  },
  tab: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
  },
  activeTab: {
    borderBottomColor: '#1890ff',
  },
  tabText: {
    fontSize: 16,
    color: '#666',
  },
  activeTabText: {
    color: '#1890ff',
    fontWeight: 'bold',
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
    marginBottom: 20,
  },
  refreshButton: {
    backgroundColor: '#1890ff',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 20,
  },
  refreshText: {
    color: 'white',
    fontSize: 14,
  },
  listContainer: {
    padding: 15,
  },
  userCard: {
    backgroundColor: 'white',
    flexDirection: 'row',
    padding: 15,
    marginBottom: 10,
    borderRadius: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
  },
  avatar: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#f0f0f0',
  },
  userInfo: {
    flex: 1,
    marginLeft: 12,
    justifyContent: 'center',
  },
  nickname: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  detail: {
    fontSize: 14,
    color: '#666',
    marginBottom: 2,
  },
  introduction: {
    fontSize: 12,
    color: '#999',
    marginTop: 4,
    lineHeight: 16,
  },
});
