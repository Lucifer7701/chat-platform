import React, { useEffect, useRef, useState } from 'react';
import { View, Text, TextInput, Button, FlatList, StyleSheet } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { buildWebSocketUrl, markAsRead } from '../utils/api';

type Props = NativeStackScreenProps<RootStackParamList, 'Chat'>;

type Msg = {
  id: string;
  fromUserId: number;
  toUserId: number;
  content: string;
};

export default function ChatScreen({ route }: Props) {
  const { toUserId } = route.params;
  const [text, setText] = useState('');
  const [messages, setMessages] = useState<Msg[]>([]);
  const wsRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    let ws: WebSocket | null = null;
    (async () => {
      const token = await AsyncStorage.getItem('token');
      if (!token) return;
      // 进入会话后标记来自该联系人的未读为已读
      try { await markAsRead(toUserId, token); } catch {}
      const url = buildWebSocketUrl(`/ws/chat/${token}`);
      ws = new WebSocket(url);
      wsRef.current = ws;

      ws.onmessage = (ev) => {
        try {
          if (ev.data === 'PING') return; // 忽略心跳
          const data = JSON.parse(ev.data);
          setMessages((prev) => [
            { id: Date.now().toString(), fromUserId: data.fromUserId, toUserId: data.toUserId, content: data.content },
            ...prev,
          ]);
        } catch {}
      };
    })();

    return () => ws?.close();
  }, [toUserId]);

  const send = () => {
    // 后端期望 Integer 类型的 messageType：1=文本 2=图片 3=语音
    const payload = { toUserId, messageType: 1, content: text, mediaUrl: null };
    wsRef.current?.send(JSON.stringify(payload));
    setText('');
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={messages}
        keyExtractor={(item) => item.id}
        inverted
        renderItem={({ item }) => (
          <View style={styles.msg}><Text>{item.content}</Text></View>
        )}
      />
      <View style={styles.inputRow}>
        <TextInput style={styles.input} value={text} onChangeText={setText} placeholder="输入消息" />
        <Button title="发送" onPress={send} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  msg: { padding: 8, borderBottomWidth: 1, borderBottomColor: '#eee' },
  inputRow: { flexDirection: 'row', padding: 8, gap: 8 },
  input: { flex: 1, borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 8 },
});


