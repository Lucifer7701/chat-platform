import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, TouchableOpacity } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type Props = NativeStackScreenProps<RootStackParamList, 'Login'>;

export default function LoginScreen({ navigation }: Props) {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');

  const onLogin = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/user/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone, password }),
      });
      const json = await res.json();
      if (json.code !== 200) {
        Alert.alert('登录失败', json.message || '');
        return;
      }
      const token = json.extraData?.token;
      if (token) {
        await AsyncStorage.setItem('token', token);
      }
      navigation.replace('ChatList');
    } catch (e) {
      Alert.alert('错误', '登录失败');
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


