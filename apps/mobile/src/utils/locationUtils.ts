import { Platform, Alert } from 'react-native';
import { post } from './api';

// 条件导入 expo-location，避免Web端问题
const Location = Platform.OS !== 'web' ? require('expo-location') : null;

export interface LocationResult {
  success: boolean;
  message?: string;
  coords?: {
    latitude: number;
    longitude: number;
  };
}

/**
 * 请求位置权限并获取当前位置
 */
export const requestLocationPermission = async (): Promise<LocationResult> => {
  // Web端跳过位置获取
  if (Platform.OS === 'web' || !Location) {
    return {
      success: false,
      message: 'Web端不支持位置获取'
    };
  }

  try {
    // 请求位置权限
    let { status } = await Location.requestForegroundPermissionsAsync();
    if (status !== 'granted') {
      return {
        success: false,
        message: '位置权限被拒绝'
      };
    }

    // 获取当前位置
    let location = await Location.getCurrentPositionAsync({
      accuracy: Location.Accuracy.Balanced,
      timeout: 10000, // 10秒超时
    });

    return {
      success: true,
      coords: {
        latitude: location.coords.latitude,
        longitude: location.coords.longitude
      }
    };
  } catch (error: any) {
    return {
      success: false,
      message: error.message || '位置获取失败'
    };
  }
};

/**
 * 更新用户位置到服务器
 */
export const updateLocationToServer = async (
  latitude: number, 
  longitude: number, 
  token: string
): Promise<{ success: boolean; message?: string }> => {
  try {
    const response = await post<any>('/api/user/update-location', {
      latitude,
      longitude
    }, token);

    if (response.code === 200) {
      return { success: true };
    } else {
      return { 
        success: false, 
        message: response.message || '位置更新失败' 
      };
    }
  } catch (error: any) {
    return { 
      success: false, 
      message: error.message || '网络错误' 
    };
  }
};

/**
 * 完整的位置获取和更新流程
 */
export const getAndUpdateLocation = async (token: string): Promise<LocationResult> => {
  // 1. 获取位置权限和坐标
  const locationResult = await requestLocationPermission();
  if (!locationResult.success || !locationResult.coords) {
    return locationResult;
  }

  // 2. 更新到服务器
  const updateResult = await updateLocationToServer(
    locationResult.coords.latitude,
    locationResult.coords.longitude,
    token
  );

  if (!updateResult.success) {
    return {
      success: false,
      message: updateResult.message
    };
  }

  return {
    success: true,
    coords: locationResult.coords
  };
};
