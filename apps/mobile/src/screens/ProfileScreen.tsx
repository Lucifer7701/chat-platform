import React, { useEffect, useState } from 'react';
import { View, Text, Image, TouchableOpacity, StyleSheet, Alert, TextInput, Modal } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_BASE, get, post } from '../utils/api';

type User = {
  id: number;
  nickname: string;
  avatar?: string;
  phone: string;
  city?: string;
  profession?: string;
  introduction?: string;
  gender: number;
};

export default function ProfileScreen() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editField, setEditField] = useState<'nickname' | 'introduction'>('nickname');
  const [editValue, setEditValue] = useState('');

  useEffect(() => {
    loadUserProfile();
  }, []);

  const loadUserProfile = async () => {
    try {
      setLoading(true);
      const token = await AsyncStorage.getItem('token');
      if (!token) return;

      const res = await get<any>('/api/user/profile', token);
      if (res.code === 200) {
        setUser(res.extraData?.user);
      }
    } catch (e) {
      Alert.alert('错误', '加载用户信息失败');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (field: 'nickname' | 'introduction') => {
    if (!user) return;
    setEditField(field);
    setEditValue(field === 'nickname' ? user.nickname : (user.introduction || ''));
    setEditModalVisible(true);
  };

  const handleSaveEdit = async () => {
    if (!user || !editValue.trim()) {
      Alert.alert('提示', '请输入内容');
      return;
    }

    try {
      const token = await AsyncStorage.getItem('token');
      if (!token) return;

      const updateData = {
        [editField]: editValue.trim()
      };

      const res = await post<any>('/api/user/update-profile', updateData, token);
      if (res.code === 200) {
        setUser({ ...user, [editField]: editValue.trim() });
        setEditModalVisible(false);
        Alert.alert('成功', '更新成功');
      } else {
        Alert.alert('失败', res.message || '更新失败');
      }
    } catch (e) {
      Alert.alert('错误', '更新失败');
    }
  };

  const getDefaultAvatar = (gender: number) => {
    return `${API_BASE}/default/avatar_${gender === 1 ? 'male' : 'female'}.png`;
  };

  if (loading) {
    return (
      <View style={styles.container}>
        <Text>加载中...</Text>
      </View>
    );
  }

  if (!user) {
    return (
      <View style={styles.container}>
        <Text>用户信息加载失败</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* 头部信息 */}
      <View style={styles.header}>
        <View style={styles.avatarContainer}>
          <Image
            source={{
              uri: user.avatar?.startsWith('http')
                ? user.avatar
                : getDefaultAvatar(user.gender)
            }}
            style={styles.avatar}
          />
          <TouchableOpacity style={styles.avatarEditButton}>
            <Text style={styles.avatarEditText}>编辑</Text>
          </TouchableOpacity>
        </View>
        
        <View style={styles.userInfo}>
          <View style={styles.infoRow}>
            <Text style={styles.nickname}>{user.nickname}</Text>
            <TouchableOpacity 
              style={styles.editButton}
              onPress={() => handleEdit('nickname')}
            >
              <Text style={styles.editButtonText}>编辑</Text>
            </TouchableOpacity>
          </View>
          
          <Text style={styles.phone}>{user.phone}</Text>
          
          {user.city && (
            <Text style={styles.detail}>📍 {user.city}</Text>
          )}
          
          {user.profession && (
            <Text style={styles.detail}>💼 {user.profession}</Text>
          )}
        </View>
      </View>

      {/* 个人介绍 */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>个人介绍</Text>
          <TouchableOpacity 
            style={styles.editButton}
            onPress={() => handleEdit('introduction')}
          >
            <Text style={styles.editButtonText}>编辑</Text>
          </TouchableOpacity>
        </View>
        <Text style={styles.introduction}>
          {user.introduction || '这个人很懒，什么都没有留下...'}
        </Text>
      </View>

      {/* 其他功能区域 */}
      <View style={styles.section}>
        <TouchableOpacity style={styles.menuItem}>
          <Text style={styles.menuText}>设置</Text>
          <Text style={styles.arrow}>→</Text>
        </TouchableOpacity>
        
        <TouchableOpacity style={styles.menuItem}>
          <Text style={styles.menuText}>关于我们</Text>
          <Text style={styles.arrow}>→</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={styles.menuItem}
          onPress={async () => {
            await AsyncStorage.removeItem('token');
            // 这里应该导航回登录页面，但由于导航结构，暂时用Alert提示
            Alert.alert('提示', '已退出登录');
          }}
        >
          <Text style={[styles.menuText, { color: '#ff4d4f' }]}>退出登录</Text>
        </TouchableOpacity>
      </View>

      {/* 编辑模态框 */}
      <Modal
        visible={editModalVisible}
        transparent
        animationType="slide"
        onRequestClose={() => setEditModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>
              编辑{editField === 'nickname' ? '昵称' : '个人介绍'}
            </Text>
            
            <TextInput
              style={[
                styles.modalInput,
                editField === 'introduction' && styles.multilineInput
              ]}
              value={editValue}
              onChangeText={setEditValue}
              placeholder={`请输入${editField === 'nickname' ? '昵称' : '个人介绍'}`}
              multiline={editField === 'introduction'}
              maxLength={editField === 'nickname' ? 20 : 200}
            />
            
            <View style={styles.modalButtons}>
              <TouchableOpacity
                style={[styles.modalButton, styles.cancelButton]}
                onPress={() => setEditModalVisible(false)}
              >
                <Text style={styles.cancelButtonText}>取消</Text>
              </TouchableOpacity>
              
              <TouchableOpacity
                style={[styles.modalButton, styles.saveButton]}
                onPress={handleSaveEdit}
              >
                <Text style={styles.saveButtonText}>保存</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: 'white',
    padding: 20,
    paddingTop: 60,
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatarContainer: {
    position: 'relative',
  },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#f0f0f0',
  },
  avatarEditButton: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    backgroundColor: '#1890ff',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
  },
  avatarEditText: {
    color: 'white',
    fontSize: 10,
  },
  userInfo: {
    flex: 1,
    marginLeft: 15,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  nickname: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
  },
  phone: {
    fontSize: 14,
    color: '#666',
    marginTop: 5,
  },
  detail: {
    fontSize: 14,
    color: '#666',
    marginTop: 3,
  },
  editButton: {
    backgroundColor: '#f0f0f0',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
  },
  editButtonText: {
    color: '#1890ff',
    fontSize: 12,
  },
  section: {
    backgroundColor: 'white',
    marginTop: 10,
    padding: 20,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  introduction: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
  menuItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  menuText: {
    fontSize: 16,
    color: '#333',
  },
  arrow: {
    fontSize: 16,
    color: '#ccc',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: 'white',
    width: '80%',
    borderRadius: 10,
    padding: 20,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
  },
  modalInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 5,
    padding: 10,
    marginBottom: 20,
    fontSize: 16,
  },
  multilineInput: {
    height: 80,
    textAlignVertical: 'top',
  },
  modalButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  modalButton: {
    flex: 1,
    paddingVertical: 12,
    borderRadius: 5,
    alignItems: 'center',
  },
  cancelButton: {
    backgroundColor: '#f0f0f0',
    marginRight: 10,
  },
  saveButton: {
    backgroundColor: '#1890ff',
    marginLeft: 10,
  },
  cancelButtonText: {
    color: '#666',
  },
  saveButtonText: {
    color: 'white',
  },
});
