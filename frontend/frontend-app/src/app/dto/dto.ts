export enum Platform {
  ZOOM = "ZOOM",
  MICROSOFT_TEAMS = "MICROSOFT_TEAMS"
}

export enum Role {
  ADMIN = "ADMIN",
  USER = "USER"
}

export enum Status {
  PLANNED = "PLANNED",
  ONGOING = "ONGOING",
  COMPLETED = "COMPLETED"
}

export interface UserDTO {
  id: string | null;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  role: Role | null;
}

export interface MeetingDTO {
  id: string | null;
  title?: string | null;
  startTime: string | null;
  endTime: string | null;
  status: Status | null;
  platform: Platform | null;
  transcription?: string | null;
  summary?: string | null;
}

export interface TranscriptionDTO {
  id: string | null;
  meetingId: string | null;
  text: string | null;
  generatedAt: string | null;
}

export interface SummaryDTO {
  id: string | null;
  meetingId: string | null;
  summaryText: string | null;
  generatedAt: string | null;
}

export interface NotificationDTO {
  id: string | null;
  message: string | null;
  createdAt: string | null;
  userId: string | null;
}
