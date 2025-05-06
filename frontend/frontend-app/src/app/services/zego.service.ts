// src/app/services/zego.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Meeting } from '../models/meeting.model';

@Injectable({
  providedIn: 'root'
})
export class ZegoService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Generuje token dla spotkania
   * @param roomId ID pokoju ZEGOCLOUD
   * @param expireTime Czas wygaśnięcia tokenu w sekundach
   */
  generateToken(roomId: string, expireTime: number = 3600): Observable<{token: string, message: string}> {
    return this.http.get<{token: string, message: string}>(`${this.apiUrl}/api/zego/token`, {
      params: {
        roomId,
        expireTime: expireTime.toString()
      }
    });
  }

  /**
   * Tworzy nowe spotkanie
   * @param meetingData Dane spotkania
   */
  createMeeting(meetingData: {
    title: string;
    startTime: string;
    durationMinutes: number;
    zegoRoomId?: string;
  }): Observable<Meeting> {
    return this.http.post<Meeting>(`${this.apiUrl}/api/zego/meetings`, meetingData);
  }

  /**
   * Aktualizuje status spotkania (start/end)
   * @param meetingId ID spotkania
   * @param status Nowy status (start/end)
   */
  updateMeetingStatus(meetingId: string, status: string): Observable<Meeting> {
    return this.http.put<Meeting>(`${this.apiUrl}/api/zego/meetings/${meetingId}/status`, { status });
  }
}
