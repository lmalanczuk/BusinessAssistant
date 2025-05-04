// frontend/frontend-app/src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import { importProvidersFrom } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { AuthInterceptor } from './app/auth.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(HttpClientModule),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    provideRouter(routes)
  ]
}).catch(err => console.error(err));
