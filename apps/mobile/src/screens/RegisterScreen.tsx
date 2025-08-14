import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, TouchableOpacity } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { post } from '../utils/api';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type Props = NativeStackScreenProps<RootStackParamList, 'Register'>;

export default function RegisterScreen({ navigation }: Props) {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [smsCode, setSmsCode] = useState('');
  const [gender, setGender] = useState<1 | 2 | null>(null);

  const sendCode = async () => {
    try {
      const json = await post<any>('/api/user/send-register-code', { phone });
      if (json.code !== 200) return Alert.alert('发送失败', json.message || '');
      Alert.alert('验证码', '已发送');
    } catch (e) {
      Alert.alert('错误', '发送失败');
    }
  };

  const register = async () => {
    try {
      if (!gender) {
        Alert.alert('提示', '请选择性别');
        return;
      }
      const json = await post<any>('/api/user/register', { phone, password, smsCode, gender });
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
      <View style={styles.genderRow}>
        <Text style={styles.label}>性别</Text>
        <View style={styles.genderChoices}>
          <TouchableOpacity style={[styles.choice, gender === 1 && styles.choiceActive]} onPress={() => setGender(1)}>
            <Text style={[styles.choiceText, gender === 1 && styles.choiceTextActive]}>男</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.choice, gender === 2 && styles.choiceActive]} onPress={() => setGender(2)}>
            <Text style={[styles.choiceText, gender === 2 && styles.choiceTextActive]}>女</Text>
          </TouchableOpacity>
        </View>
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
  genderRow: { marginBottom: 12 },
  label: { marginBottom: 8, color: '#333' },
  genderChoices: { flexDirection: 'row', gap: 12 },
  choice: { paddingVertical: 8, paddingHorizontal: 16, borderRadius: 20, borderWidth: 1, borderColor: '#ccc' },
  choiceActive: { backgroundColor: '#ffeff3', borderColor: '#ff4d6d' },
  choiceText: { color: '#333' },
  choiceTextActive: { color: '#ff4d6d', fontWeight: '600' },
});


