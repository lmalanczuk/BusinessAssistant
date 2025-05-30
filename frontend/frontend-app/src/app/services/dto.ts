export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
}

export interface MeetingTokenResponse {
  token: string;
  roomUrl: string;
  roomName: string;
}

export interface Meeting {
  id: string;
  title: string;
  startTime: string;       // ISO 8601
  endTime: string;         // ISO 8601
  status: 'PLANNED' | 'ONGOING' | 'COMPLETED';
  platform: string;
  dailyRoomName: string;
  dailyRoomUrl: string;
}

export interface CreateMeetingRequest {
  title: string;
  startTime: string;
  durationMinutes: number;
}

export interface JoinMeetingRequest {
  roomName: string;
  userName: string;
}
