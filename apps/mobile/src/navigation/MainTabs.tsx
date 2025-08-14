import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Text } from 'react-native';
import RecommendScreen from '../screens/RecommendScreen';
import ChatListScreen from '../screens/ChatListScreen';
import ProfileScreen from '../screens/ProfileScreen';

export type MainTabParamList = {
  Recommend: undefined;
  ChatList: undefined;
  Profile: undefined;
};

const Tab = createBottomTabNavigator<MainTabParamList>();

export default function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={{
        tabBarActiveTintColor: '#1890ff',
        tabBarInactiveTintColor: '#8c8c8c',
        headerShown: false,
      }}
    >
      <Tab.Screen
        name="Recommend"
        component={RecommendScreen}
        options={{
          tabBarLabel: '推荐',
          tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 20 }}>👥</Text>,
        }}
      />
      <Tab.Screen
        name="ChatList"
        component={ChatListScreen}
        options={{
          tabBarLabel: '聊天',
          tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 20 }}>💬</Text>,
        }}
      />
      <Tab.Screen
        name="Profile"
        component={ProfileScreen}
        options={{
          tabBarLabel: '我的',
          tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 20 }}>👤</Text>,
        }}
      />
    </Tab.Navigator>
  );
}
