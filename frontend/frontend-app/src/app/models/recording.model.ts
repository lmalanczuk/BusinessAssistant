// src/app/models/recording.model.ts
export interface Recording {
  id: string;
  meetingId: string;
  meetingTitle: string;
  url: string;
  fileName: string;
  fileSize: number;
  durationSeconds: number;
  recordedAt: string;
}
