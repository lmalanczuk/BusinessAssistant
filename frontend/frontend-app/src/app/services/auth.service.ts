import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { RegisterRequest, LoginRequest, AuthResponse } from './dto';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient, private router: Router) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap((response) => {
        this.saveAuthData(response);
        this.router.navigate(['/dashboard']);
      })
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((response) => {
        this.saveAuthData(response);
        this.router.navigate(['/dashboard']);
      })
    );
  }

  private saveAuthData(response: AuthResponse) {
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(response));
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  getUserFullName(): string {
    const userData = localStorage.getItem('user');
    if (!userData) return '';

    try {
      const parsed = JSON.parse(userData);
      const user = parsed.user;
      return user ? `${user.firstName} ${user.lastName}` : '';
    } catch {
      return '';
    }
  }

  getUserInitials(): string {
    const userData = localStorage.getItem('user');
    if (!userData) return '';
    try {
      const { user } = JSON.parse(userData);
      if (!user) return '';
      const first = user.firstName?.charAt(0) ?? '';
      const last  = user.lastName?.charAt(0) ?? '';
      return (first + last).toUpperCase();
    } catch {
      return '';
    }
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

}
