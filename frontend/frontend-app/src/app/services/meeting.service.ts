import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import { environment } from '../../environments/environment';
import {CreateMeetingRequest, JoinMeetingRequest, Meeting, MeetingTokenResponse} from "./dto";

@Injectable({ providedIn: 'root' })
export class MeetingService {
  private base = `${environment.apiUrl}/api/meetings`;

  constructor(private http: HttpClient) {}

  getUpcomingMeetings(): Observable<Meeting[]> {
    return this.http.get<Meeting[]>(`${this.base}/upcoming`).pipe(
      tap({
        next: res => console.log('Upcoming meetings:', res),
        error: err => console.error('Błąd pobierania spotkań:', err)
      })
    );
  }

  startMeeting(req: CreateMeetingRequest): Observable<MeetingTokenResponse> {
    return this.http.post<MeetingTokenResponse>(`${this.base}/start`, req).pipe(
      tap({
        next: res => console.log('Start meeting response:', res),
        error: err => console.error('Błąd startowania spotkania:', err)
      })
    );
  }

  joinMeeting(req: JoinMeetingRequest): Observable<MeetingTokenResponse> {
    return this.http.post<MeetingTokenResponse>(`${this.base}/join`, req);
  }

  scheduleMeeting(req: CreateMeetingRequest): Observable<MeetingTokenResponse> {
    return this.http.post<MeetingTokenResponse>(`${this.base}/schedule`, req);
  }
}
