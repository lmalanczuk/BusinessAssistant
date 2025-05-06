// src/app/services/meeting.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Meeting } from '../models/meeting.model';
import { Transcription } from '../models/transcription.model';
import { Invitation } from '../models/invitation.model';
import { Recording } from '../models/recording.model';

@Injectable({
  providedIn: 'root'
})
export class MeetingService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Pobiera wszystkie spotkania użytkownika
   */
  getMeetings(): Observable<Meeting[]> {
    return this.http.get<Meeting[]>(`${this.apiUrl}/api/meetings`);
  }

  /**
   * Pobiera nadchodzące spotkania
   */
  getUpcomingMeetings(): Observable<Meeting[]> {
    return this.http.get<Meeting[]>(`${this.apiUrl}/api/meetings/upcoming`);
  }

  /**
   * Pobiera aktywne spotkania
   */
  getActiveMeetings(): Observable<Meeting[]> {
    return this.http.get<Meeting[]>(`${this.apiUrl}/api/meetings/active`);
  }

  /**
   * Pobiera spotkanie według ID
   * @param meetingId ID spotkania
   */
  getMeetingById(meetingId: string): Observable<Meeting> {
    return this.http.get<Meeting>(`${this.apiUrl}/api/meetings/${meetingId}`);
  }

  /**
   * Pobiera spotkanie według ID pokoju ZEGOCLOUD
   * @param roomId ID pokoju ZEGOCLOUD
   */
  getMeetingByRoomId(roomId: string): Observable<Meeting | null> {
    return this.http.get<Meeting | null>(`${this.apiUrl}/api/meetings/room/${roomId}`);
  }

  /**
   * Pobiera ostatnie transkrypcje
   */
  getRecentTranscriptions(): Observable<Transcription[]> {
    return this.http.get<Transcription[]>(`${this.apiUrl}/api/transcriptions/recent`);
  }

  /**
   * Pobiera zaproszenia użytkownika
   */
  getInvitations(): Observable<Invitation[]> {
    return this.http.get<Invitation[]>(`${this.apiUrl}/api/invitations`);
  }

  /**
   * Pobiera aktywne spotkanie (obecnie trwające)
   */
  getCurrentActiveMeeting(): Observable<Meeting | null> {
    return this.http.get<Meeting | null>(`${this.apiUrl}/api/meetings/current`);
  }

  /**
   * Pobiera ostatnie nagrania
   */
  getRecentRecordings(): Observable<Recording[]> {
    return this.http.get<Recording[]>(`${this.apiUrl}/api/recordings/recent`);
  }
}
