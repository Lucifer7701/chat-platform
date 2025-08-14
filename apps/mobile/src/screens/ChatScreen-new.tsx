import React, { useEffect, useRef, useState } from 'react';
import { 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  FlatList, 
  StyleSheet, 
  Image, 
  Alert,
  ActivityIndicator 
} from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { buildWebSocketUrl, markAsRead, get } from '../utils/api';
import Avatar from '../components/Avatar';

type Props = NativeStackScreenProps<RootStackParamList, 'Chat'>;

type Msg = {
  id: string;
  tempId?: string;      // 本地临时ID
  fromUserId: number;
  toUserId: number;
  content: string;
  createdAt?: string;
  status: 'sending' | 'sent' | 'failed' | 'received';
  serverId?: number;    // 服务器返回的真实ID
};

type User = {
  id: number;
  nickname: string;
  avatar?: string;
  gender: number;
};

export default function ChatScreen({ route }: Props) {
  const { toUserId } = route.params;
  const [text, setText] = useState('');
  const [messages, setMessages] = useState<Msg[]>([]);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [contactUser, setContactUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const wsRef = useRef<WebSocket | null>(null);
  const flatListRef = useRef<FlatList>(null);

  useEffect(() => {
    initializeChat();
    return () => wsRef.current?.close();
  }, [toUserId]);

  const initializeChat = async () => {
    try {
      setLoading(true);
      
      // 1. 获取用户信息
      await loadUserInfo();
      
      // 2. 加载聊天历史
      await loadChatHistory();
      
      // 3. 建立WebSocket连接
      await setupWebSocket();
      
      // 4. 标记消息为已读
      const token = await AsyncStorage.getItem('token');
      if (token) {
        try { await markAsRead(toUserId, token); } catch {} 
      }
    } catch (e) {
      console.log('初始化聊天失败:', e);
    } finally {
      setLoading(false);
    }
  };

  const loadUserInfo = async () => {
    try {
      const token = await AsyncStorage.getItem('token');
      if (!token) return;

      // 获取当前用户信息
      const currentUserRes = await get<any>('/api/user/profile', token);
      if (currentUserRes.code === 200) {
        setCurrentUser(currentUserRes.extraData?.user);
      }

      // 获取联系人信息
      const contactRes = await get<any>(`/api/user/profile/${toUserId}`, token);
      if (contactRes.code === 200) {
        setContactUser(contactRes.extraData?.user);
      }
    } catch (e) {
      console.log('加载用户信息失败:', e);
    }
  };

  const loadChatHistory = async () => {
    try {
      const token = await AsyncStorage.getItem('token');
      if (!token) return;
      
      const res = await get<any>(`/api/chat/history/${toUserId}?page=1&size=50`, token);
      if (res.code === 200) {
        const historyMessages = res.data.map((msg: any) => ({
          id: msg.id.toString(),
          fromUserId: msg.fromUserId,
          toUserId: msg.toUserId,
          content: msg.content,
          createdAt: msg.createdAt,
          status: 'received' as const,
        }));
        setMessages(historyMessages.reverse()); // 最新消息在底部
      }
    } catch (e) {
      console.log('加载聊天历史失败:', e);
    }
  };

  const setupWebSocket = async () => {
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
          
          switch (data.type) {
            case 'ack':
              // 处理发送确认
              setMessages(prev => prev.map(msg => 
                msg.tempId === data.tempId
                  ? { ...msg, status: 'sent', serverId: data.id, id: data.id.toString() }
                  : msg
              ));
              break;
              
            case 'message':
              // 处理接收到的消息
              const newMessage: Msg = {
                id: data.id.toString(),
                fromUserId: data.fromUserId,
                toUserId: data.toUserId,
                content: data.content,
                status: 'received',
                createdAt: data.createdAt,
              };
              setMessages(prev => [...prev, newMessage]);
              // 滚动到底部
              setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
              break;
              
            case 'error':
              // 处理发送错误
              if (data.tempId) {
                setMessages(prev => prev.map(msg => 
                  msg.tempId === data.tempId
                    ? { ...msg, status: 'failed' }
                    : msg
                ));
              }
              Alert.alert('发送失败', data.content || '消息发送失败');
              break;
          }
        } catch (e) {
          console.log('处理WebSocket消息失败:', e);
        }
      };

      ws.onerror = (error) => {
        console.log('WebSocket错误:', error);
      };

      ws.onclose = () => {
        console.log('WebSocket连接关闭');
      };
    } catch (e) {
      console.log('建立WebSocket连接失败:', e);
    }
  };

  const sendMessage = () => {
    if (!text.trim() || !currentUser) return;
    
    const tempId = Date.now().toString() + Math.random().toString(36).substr(2, 9);
    
    // 1. 立即显示消息（乐观更新）
    const tempMessage: Msg = {
      id: tempId,
      tempId: tempId,
      fromUserId: currentUser.id,
      toUserId: toUserId,
      content: text.trim(),
      status: 'sending',
      createdAt: new Date().toISOString(),
    };
    
    setMessages(prev => [...prev, tempMessage]);
    setText('');
    
    // 滚动到底部
    setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);

    // 2. 发送到WebSocket
    const payload = {
      tempId: tempId,
      toUserId: toUserId,
      messageType: 1,
      content: text.trim(),
      mediaUrl: null
    };
    
    wsRef.current?.send(JSON.stringify(payload));

    // 3. 设置超时处理
    setTimeout(() => {
      setMessages(prev => prev.map(msg => 
        msg.tempId === tempId && msg.status === 'sending'
          ? { ...msg, status: 'failed' }
          : msg
      ));
    }, 10000); // 10秒超时
  };

  const retryMessage = (message: Msg) => {
    if (!message.tempId) return;
    
    // 更新状态为发送中
    setMessages(prev => prev.map(msg => 
      msg.tempId === message.tempId
        ? { ...msg, status: 'sending' }
        : msg
    ));

    // 重新发送
    const payload = {
      tempId: message.tempId,
      toUserId: message.toUserId,
      messageType: 1,
      content: message.content,
      mediaUrl: null
    };
    
    wsRef.current?.send(JSON.stringify(payload));

    // 重新设置超时
    setTimeout(() => {
      setMessages(prev => prev.map(msg => 
        msg.tempId === message.tempId && msg.status === 'sending'
          ? { ...msg, status: 'failed' }
          : msg
      ));
    }, 10000);
  };

  const MessageStatusIndicator = ({ status }: { status: Msg['status'] }) => {
    switch (status) {
      case 'sending':
        return <ActivityIndicator size="small" color="#999" style={styles.statusIndicator} />;
      case 'sent':
        return <Text style={styles.statusSent}>✓</Text>;
      case 'failed':
        return <Text style={styles.statusFailed}>!</Text>;
      default:
        return null;
    }
  };

  const MessageItem = ({ item }: { item: Msg }) => {
    const isOwnMessage = currentUser && item.fromUserId === currentUser.id;
    const user = isOwnMessage ? currentUser : contactUser;
    const avatarSource = undefined;

    return (
      <View style={[
        styles.messageContainer,
        isOwnMessage ? styles.ownMessageContainer : styles.otherMessageContainer
      ]}>
        {!isOwnMessage && (
          <Avatar avatar={user?.avatar} gender={user?.gender} size={40} />
        )}
        
        <View style={[
          styles.messageBubble,
          isOwnMessage ? styles.ownBubble : styles.otherBubble
        ]}>
          <Text style={[
            styles.messageText,
            isOwnMessage ? styles.ownText : styles.otherText
          ]}>
            {item.content}
          </Text>
        </View>

        {isOwnMessage && (
          <>
            <MessageStatusIndicator status={item.status} />
            <Avatar avatar={user?.avatar} gender={user?.gender} size={40} />
          </>
        )}

        {item.status === 'failed' && (
          <TouchableOpacity 
            style={styles.retryButton} 
            onPress={() => retryMessage(item)}
          >
            <Text style={styles.retryText}>重发</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#1890ff" />
        <Text style={styles.loadingText}>加载中...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        ref={flatListRef}
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={MessageItem}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.messagesList}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: false })}
      />
      
      <View style={styles.inputContainer}>
        <TextInput
          style={styles.textInput}
          value={text}
          onChangeText={setText}
          placeholder="输入消息..."
          multiline
          maxLength={500}
        />
        <TouchableOpacity
          style={[styles.sendButton, !text.trim() && styles.sendButtonDisabled]}
          onPress={sendMessage}
          disabled={!text.trim()}
        >
          <Text style={styles.sendButtonText}>发送</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 10,
    fontSize: 16,
    color: '#666',
  },
  messagesList: {
    padding: 10,
  },
  messageContainer: {
    flexDirection: 'row',
    marginVertical: 4,
    alignItems: 'flex-end',
  },
  ownMessageContainer: {
    justifyContent: 'flex-end',
  },
  otherMessageContainer: {
    justifyContent: 'flex-start',
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    marginHorizontal: 8,
  },
  messageBubble: {
    maxWidth: '70%',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 18,
    marginHorizontal: 4,
  },
  ownBubble: {
    backgroundColor: '#1890ff',
    borderBottomRightRadius: 4,
  },
  otherBubble: {
    backgroundColor: '#fff',
    borderBottomLeftRadius: 4,
    borderWidth: 1,
    borderColor: '#e8e8e8',
  },
  messageText: {
    fontSize: 16,
    lineHeight: 20,
  },
  ownText: {
    color: '#fff',
  },
  otherText: {
    color: '#333',
  },
  statusIndicator: {
    marginRight: 4,
  },
  statusSent: {
    fontSize: 12,
    color: '#999',
    marginRight: 4,
  },
  statusFailed: {
    fontSize: 12,
    color: '#ff4d4f',
    marginRight: 4,
  },
  retryButton: {
    position: 'absolute',
    right: -50,
    backgroundColor: '#ff4d4f',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  retryText: {
    color: '#fff',
    fontSize: 12,
  },
  inputContainer: {
    flexDirection: 'row',
    padding: 10,
    backgroundColor: '#fff',
    alignItems: 'flex-end',
    borderTopWidth: 1,
    borderTopColor: '#e8e8e8',
  },
  textInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#e8e8e8',
    borderRadius: 20,
    paddingHorizontal: 15,
    paddingVertical: 10,
    marginRight: 10,
    maxHeight: 100,
    fontSize: 16,
  },
  sendButton: {
    backgroundColor: '#1890ff',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 20,
  },
  sendButtonDisabled: {
    backgroundColor: '#ccc',
  },
  sendButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '500',
  },
});
