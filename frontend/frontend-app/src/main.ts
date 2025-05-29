// frontend/frontend-app/src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import {importProvidersFrom, LOCALE_ID} from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { AuthInterceptor } from './app/auth.interceptor';
import {registerLocaleData} from "@angular/common";
import localePl from '@angular/common/locales/pl';

registerLocaleData(localePl);
bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(HttpClientModule),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    provideRouter(routes),  { provide: LOCALE_ID, useValue: 'pl' }
  ]
}).catch(err => console.error(err));
