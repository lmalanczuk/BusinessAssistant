// src/app/models/meeting.model.ts
export interface Meeting {
  id: string;
  title: string;
  startTime: string;
  endTime: string;
  status: 'PLANNED' | 'ONGOING' | 'COMPLETED';
  platform: 'ZEGOCLOUD';
  zegoRoomId: string;
  zegoStreamId?: string;
  participantCount?: number;
}

