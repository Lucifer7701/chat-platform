import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, TouchableOpacity } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { post } from '../utils/api';
import { getAndUpdateLocation } from '../utils/locationUtils';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type Props = NativeStackScreenProps<RootStackParamList, 'Login'>;

export default function LoginScreen({ navigation }: Props) {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');

  const onLogin = async () => {
    try {
      // 1. 先进行登录
      const json = await post<any>('/api/user/login', { phone, password });
      if (json.code !== 200) {
        Alert.alert('登录失败', json.message || '');
        return;
      }
      
      const token = json.extraData?.token;
      if (token) {
        await AsyncStorage.setItem('token', token);
        
        // 2. 获取并更新位置
        await updateLocation(token);
        
        // 3. 跳转到主页面
        navigation.replace('MainTabs');
      }
    } catch (e) {
      Alert.alert('错误', '登录失败');
    }
  };

  const updateLocation = async (token: string) => {
    try {
      const result = await getAndUpdateLocation(token);
      if (result.success) {
        console.log('位置更新成功');
      } else {
        console.log('位置更新失败:', result.message);
        // 位置更新失败不阻止登录流程，用户可以在推荐页面重新授权
      }
    } catch (e) {
      console.log('位置更新失败:', e);
      // 位置更新失败不阻止登录流程
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>登录</Text>
      <TextInput style={styles.input} placeholder="手机号" value={phone} onChangeText={setPhone} />
      <TextInput style={styles.input} placeholder="密码" secureTextEntry value={password} onChangeText={setPassword} />
      <Button title="登录" onPress={onLogin} />
      <TouchableOpacity onPress={() => navigation.navigate('Register')} style={{ marginTop: 12 }}>
        <Text style={{ textAlign: 'center', color: '#1677ff' }}>没有账号？去注册</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, justifyContent: 'center' },
  title: { fontSize: 24, marginBottom: 16, textAlign: 'center' },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 8, marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 8 },
});


