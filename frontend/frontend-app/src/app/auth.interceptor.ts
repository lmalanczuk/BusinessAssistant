// frontend/frontend-app/src/app/auth.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    console.log('AuthInterceptor executing for URL:', req.url);
    console.log('Token from localStorage:', token ? 'Token exists' : 'No token found');

    if (token) {
      const authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('Added token to request headers');
      return next.handle(authReq);
    }

    console.log('No token found, proceeding without authorization header');
    return next.handle(req);
  }
}
