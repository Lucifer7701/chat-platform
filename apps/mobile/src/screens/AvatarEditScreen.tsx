import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ActivityIndicator,
  Platform,
} from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import AsyncStorage from '@react-native-async-storage/async-storage';
import Avatar from '../components/Avatar';
import { API_BASE } from '../utils/api';

type Props = NativeStackScreenProps<RootStackParamList, 'AvatarEdit'>;

export default function AvatarEditScreen({ navigation, route }: Props) {
  const { currentAvatar, gender } = route.params;
  const [uploading, setUploading] = useState(false);
  const [newAvatarUrl, setNewAvatarUrl] = useState<string | null>(null);

  const selectImage = () => {
    if (Platform.OS === 'web') {
      // Web端使用input file
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'image/*';
      input.onchange = (e: any) => {
        const file = e.target.files[0];
        if (file) {
          uploadImage(file);
        }
      };
      input.click();
    } else {
      // 移动端使用expo-image-picker（需要安装）
      Alert.alert('提示', '移动端图片选择功能需要安装expo-image-picker');
    }
  };

  const uploadImage = async (file: File) => {
    try {
      setUploading(true);
      
      const token = await AsyncStorage.getItem('token');
      if (!token) {
        Alert.alert('错误', '请先登录');
        return;
      }

      const formData = new FormData();
      formData.append('file', file);
      formData.append('type', 'avatar');

      const response = await fetch(`${API_BASE}/api/file/upload`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: formData,
      });

      const result = await response.json();
      
      if (result.code === 200) {
        setNewAvatarUrl(result.extraData.photoUrl);
        Alert.alert('成功', '头像上传成功！', [
          {
            text: '确定',
            onPress: () => navigation.goBack(),
          },
        ]);
      } else {
        Alert.alert('失败', result.message || '上传失败');
      }
    } catch (error) {
      console.error('上传头像失败:', error);
      Alert.alert('错误', '网络错误，请重试');
    } finally {
      setUploading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>编辑头像</Text>
      
      <View style={styles.avatarContainer}>
        <Avatar 
          avatar={newAvatarUrl || currentAvatar} 
          gender={gender} 
          size={120} 
        />
      </View>

      <TouchableOpacity 
        style={[styles.button, uploading && styles.buttonDisabled]}
        onPress={selectImage}
        disabled={uploading}
      >
        {uploading ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.buttonText}>选择新头像</Text>
        )}
      </TouchableOpacity>

      <TouchableOpacity 
        style={styles.cancelButton}
        onPress={() => navigation.goBack()}
      >
        <Text style={styles.cancelButtonText}>取消</Text>
      </TouchableOpacity>

      <Text style={styles.tip}>
        支持 JPG、PNG 格式，文件大小不超过 5MB
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    padding: 20,
    alignItems: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginTop: 40,
    marginBottom: 40,
  },
  avatarContainer: {
    marginBottom: 40,
    padding: 10,
    backgroundColor: '#fff',
    borderRadius: 70,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  button: {
    backgroundColor: '#1890ff',
    paddingHorizontal: 40,
    paddingVertical: 15,
    borderRadius: 25,
    marginBottom: 20,
    minWidth: 200,
    alignItems: 'center',
  },
  buttonDisabled: {
    backgroundColor: '#ccc',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  cancelButton: {
    paddingHorizontal: 40,
    paddingVertical: 15,
    marginBottom: 30,
  },
  cancelButtonText: {
    color: '#666',
    fontSize: 16,
  },
  tip: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
    lineHeight: 20,
  },
});
