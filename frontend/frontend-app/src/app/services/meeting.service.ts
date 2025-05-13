import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {InstantMeetingRequest} from "../models/requests/instant-meeting-request";
import {JoinRoomRequest} from "../models/requests/join-room-request";

interface Meeting {
  id: string;
  title: string;
  startTime: Date;
  endTime: Date;
  status: 'PLANNED' | 'ONGOING' | 'COMPLETED';
  participantCount?: number;
}

@Injectable({
  providedIn: 'root'
})
export class MeetingService {
  private apiUrl = 'http://localhost:8080/api/meetings';

  constructor(private http: HttpClient) {}

  /**
   * Pobiera aktywne spotkanie
   */
  getActiveMeeting(): Observable<Meeting | null> {
    return this.http.get<Meeting | null>(`${this.apiUrl}/active`).pipe(
      catchError(error => {
        console.error('Błąd podczas pobierania aktywnego spotkania', error);
        return of(null);
      })
    );
  }

  /**
   * Pobiera nadchodzące spotkania
   */
  getUpcomingMeetings(): Observable<Meeting[]> {
    return this.http.get<Meeting[]>(`${this.apiUrl}/upcoming`).pipe(
      catchError(error => {
        console.error('Błąd podczas pobierania nadchodzących spotkań', error);
        return of([]);
      })
    );
  }

  /**
   * Tworzy nowe spotkanie
   */
  createMeeting(meetingData: any): Observable<Meeting> {
    return this.http.post<Meeting>(this.apiUrl, meetingData).pipe(
      tap(meeting => console.log('Utworzono spotkanie', meeting)),
      catchError(error => {
        console.error('Błąd podczas tworzenia spotkania', error);
        throw error;
      })
    );
  }

  /**
   * Tworzy natychmiastowe spotkanie
   */
  createInstantMeeting(): Observable<Meeting> {
    const request: InstantMeetingRequest = {
      title: 'Natychmiastowe spotkanie',
      isInstant: true,
      durationMinutes: 60
    };
    return this.http.post<Meeting>(`${this.apiUrl}/instant`, request).pipe(
      tap(meeting => console.log('Utworzono natychmiastowe spotkanie', meeting)),
      catchError(error => {
        console.error('Błąd podczas tworzenia natychmiastowego spotkania', error);
        throw error;
      })
    );
  }

  /**
   * Dołącza do spotkania
   */
  joinMeeting(meetingId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${meetingId}/join`, {}).pipe(
      tap(result => console.log('Dołączono do spotkania', result)),
      catchError(error => {
        console.error('Błąd podczas dołączania do spotkania', error);
        throw error;
      })
    );
  }

  /**
   * Dołącza do spotkania po ID pokoju
   */
  joinMeetingByRoomId(roomId: string): Observable<any> {
    const request: JoinRoomRequest = { roomId };
    return this.http.post<any>(`${this.apiUrl}/join-by-room`, request).pipe(
      tap(result => console.log('Dołączono do pokoju', result)),
      catchError(error => {
        console.error('Błąd podczas dołączania do pokoju', error);
        throw error;
      })
    );
  }

  /**
   * Kończy spotkanie
   */
  endMeeting(meetingId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${meetingId}/end`, {}).pipe(
      tap(result => console.log('Zakończono spotkanie', result)),
      catchError(error => {
        console.error('Błąd podczas kończenia spotkania', error);
        throw error;
      })
    );
  }

  /**
   * Pobiera ostatnie transkrypcje
   */
  getRecentTranscriptions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/transcriptions/recent`).pipe(
      catchError(error => {
        console.error('Błąd podczas pobierania transkrypcji', error);
        return of([]);
      })
    );
  }

  /**
   * Pobiera ostatnie nagrania
   */
  getRecentRecordings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/recordings/recent`).pipe(
      catchError(error => {
        console.error('Błąd podczas pobierania nagrań', error);
        return of([]);
      })
    );
  }

  /**
   * Pobiera zaproszenia
   */
  getInvitations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invitations`).pipe(
      catchError(error => {
        console.error('Błąd podczas pobierania zaproszeń', error);
        return of([]);
      })
    );
  }
}
