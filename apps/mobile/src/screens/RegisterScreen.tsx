import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type Props = NativeStackScreenProps<RootStackParamList, 'Register'>;

export default function RegisterScreen({ navigation }: Props) {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [smsCode, setSmsCode] = useState('');

  const sendCode = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/user/send-register-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone }),
      });
      const json = await res.json();
      if (json.code !== 200) return Alert.alert('发送失败', json.message || '');
      Alert.alert('验证码', '已发送');
    } catch (e) {
      Alert.alert('错误', '发送失败');
    }
  };

  const register = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/user/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone, password, smsCode })
      });
      const json = await res.json();
      if (json.code !== 200) return Alert.alert('注册失败', json.message || '');
      const token = json.extraData?.token;
      if (token) {
        await AsyncStorage.setItem('token', token);
        navigation.replace('ChatList');
        return;
      }
      // 兼容未返回 token 的情况
      Alert.alert('成功', '注册成功，请登录');
      navigation.replace('Login');
    } catch (e) {
      Alert.alert('错误', '注册失败');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>注册</Text>
      <TextInput style={styles.input} placeholder="手机号" value={phone} onChangeText={setPhone} />
      <TextInput style={styles.input} placeholder="密码" secureTextEntry value={password} onChangeText={setPassword} />
      <View style={styles.row}>
        <TextInput style={[styles.input, { flex: 1 }]} placeholder="验证码" value={smsCode} onChangeText={setSmsCode} />
        <Button title="发送" onPress={sendCode} />
      </View>
      <Button title="注册" onPress={register} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, justifyContent: 'center' },
  title: { fontSize: 24, marginBottom: 16, textAlign: 'center' },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 8, marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 8 },
});


