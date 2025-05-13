import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { RegisterRequest, LoginRequest, AuthResponse } from './dto';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private refreshingToken = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient, private router: Router) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap((response) => {
        this.saveAuthData(response);
        this.router.navigate(['/dashboard']);
      }),
      catchError(this.handleError)
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((response) => {
        this.saveAuthData(response);
        this.router.navigate(['/dashboard']);
      }),
      catchError(this.handleError)
    );
  }

  private saveAuthData(response: AuthResponse) {
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(response));

    // Jeśli backend zwraca refresh token, też go zapisujemy
    if (response.refreshToken) {
      localStorage.setItem('refreshToken', response.refreshToken);
    }
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('refreshToken');
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

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  /**
   * Pobiera dane aktualnego użytkownika
   * @returns Obiekt z danymi użytkownika lub null
   */
  getCurrentUser() {
    const userData = localStorage.getItem('user');
    if (!userData) return null;

    try {
      const parsed = JSON.parse(userData);
      return parsed.user || null;
    } catch {
      return null;
    }
  }

  /**
   * Odświeża token używając refresh tokena
   * @returns Observable z nowym tokenem
   */
  refreshToken(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem('refreshToken');

    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh-token`, { refreshToken }).pipe(
      tap(response => {
        this.saveAuthData(response);
        this.refreshTokenSubject.next(response.token);
      }),
      catchError(error => {
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Obsługuje błędy autoryzacji z HTTP requests
   */
  handleAuthError() {
    if (!this.refreshingToken) {
      this.refreshingToken = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        this.refreshToken().subscribe({
          next: () => this.refreshingToken = false,
          error: () => {
            this.refreshingToken = false;
            this.logout();
          }
        });
      } else {
        this.refreshingToken = false;
        this.logout();
      }
    }
  }

  /**
   * Centralny handler błędów dla serwisu Auth
   */
  private handleError = (error: any) => {
    let errorMessage = 'Wystąpił nieznany błąd';

    if (error.error instanceof ErrorEvent) {
      // Błąd po stronie klienta
      errorMessage = `Błąd: ${error.error.message}`;
    } else {
      // Błąd po stronie serwera
      if (error.status === 401) {
        errorMessage = 'Nieprawidłowy email lub hasło';
      } else if (error.status === 403) {
        errorMessage = 'Brak dostępu';
      } else if (error.status === 400) {
        errorMessage = error.error?.message || 'Nieprawidłowe dane';
      } else {
        errorMessage = `Kod błędu: ${error.status}, wiadomość: ${error.message}`;
      }
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  /**
   * Sprawdza czy określona ścieżka powinna pominąć dodawanie tokenu
   */
  shouldSkipAuth(url: string): boolean {
    const skippedUrls = [
      '/api/auth/login',
      '/api/auth/register',
      '/api/auth/refresh-token'
    ];

    return skippedUrls.some(skipUrl => url.includes(skipUrl));
  }
}
