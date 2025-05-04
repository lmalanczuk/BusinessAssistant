// frontend/frontend-app/src/app/services/zoom.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ZoomService {
  private apiUrl = 'http://localhost:8080/api/zoom';

  constructor(private http: HttpClient) { }

  // Ta metoda zwraca URL do autoryzacji Zoom z parametrem state
  getZoomConnectUrl(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/connect-debug`);
  }

  checkZoomConfig(): Observable<any> {
    return this.http.get(`${this.apiUrl}/test-zoom-config`);
  }
}
