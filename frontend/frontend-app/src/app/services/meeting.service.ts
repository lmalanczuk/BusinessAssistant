// src/app/services/meeting.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';  // <-- dokładnie tak

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

@Injectable({ providedIn: 'root' })
export class MeetingService {
  // Używamy environment.apiUrl, dzięk temu wywołujemy backend na 8080:
  private base = `${environment.apiUrl}/api/meetings`;

  constructor(private http: HttpClient) {}

  /** Pobierz nadchodzące spotkania */
  getUpcomingMeetings(): Observable<Meeting[]> {
    return this.http.get<Meeting[]>(`${this.base}/upcoming`);
  }

  /** Natychmiastowy start spotkania */
  startMeeting(req: CreateMeetingRequest): Observable<MeetingTokenResponse> {
    return this.http.post<MeetingTokenResponse>(`${this.base}/start`, req);
  }

  /** Dołącz do istniejącego pokoju */
  joinMeeting(req: JoinMeetingRequest): Observable<MeetingTokenResponse> {
    return this.http.post<MeetingTokenResponse>(`${this.base}/join`, req);
  }

  /** Zaplanuj spotkanie */
  scheduleMeeting(req: CreateMeetingRequest): Observable<MeetingTokenResponse> {
    return this.http.post<MeetingTokenResponse>(`${this.base}/schedule`, req);
  }
}
