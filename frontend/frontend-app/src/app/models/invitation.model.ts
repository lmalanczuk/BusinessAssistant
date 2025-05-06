// src/app/models/invitation.model.ts
export interface Invitation {
  id: string;
  meetingId: string;
  meetingTitle: string;
  senderId: string;
  senderName: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  sentAt: string;
}
