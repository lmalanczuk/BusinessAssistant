// frontend/frontend-app/src/app/auth.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from './services/auth.service'; // Dostosuj ścieżkę

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Pomiń dodawanie tokenu dla określonych ścieżek
    if (this.authService.shouldSkipAuth(req.url)) {
      return next.handle(req);
    }

    const token = localStorage.getItem('token');

    if (token) {
      const authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });

      return next.handle(authReq).pipe(
        catchError(error => {
          // Obsługa błędu 401 (nieautoryzowany)
          if (error instanceof HttpErrorResponse && error.status === 401) {
            return this.handle401Error(req, next);
          }
          return throwError(() => error);
        })
      );
    }

    return next.handle(req);
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return this.authService.refreshToken().pipe(
      switchMap(() => {
        // Pobierz nowy token z localStorage po odświeżeniu
        const newToken = localStorage.getItem('token');

        // Klonuj oryginalny request z nowym tokenem
        const authReq = request.clone({
          setHeaders: {
            Authorization: `Bearer ${newToken}`
          }
        });

        return next.handle(authReq);
      }),
      catchError(refreshError => {
        // Jeśli odświeżenie się nie powiedzie, wyloguj użytkownika
        this.authService.logout();
        return throwError(() => refreshError);
      })
    );
  }
}
