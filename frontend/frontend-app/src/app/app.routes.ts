import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {CalendarComponent} from "./components/calendar/calendar.component";
import {TranscriptionsComponent} from "./components/transcriptions/transcriptions.component";
import {SettingsComponent} from "./components/settings/settings.component";
import {LoginComponent} from "./components/login/login.component";
import {RegisterComponent} from "./components/register/register.component";
import {SummariesComponent} from "./components/summaries/summaries.component";
import { Routes } from '@angular/router';
import {AuthGuard} from "./auth.guard";
import {VideoRoomComponent} from "./components/video-room/video-room.component";


export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  // Zabezpieczone ścieżki – tylko po zalogowaniu
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'calendar',
    canActivate: [AuthGuard],
    loadComponent: () => import('./components/calendar/calendar.component').then(m => m.CalendarComponent)
  },
  {
    path: 'transcriptions',
    canActivate: [AuthGuard],
    loadComponent: () => import('./components/transcriptions/transcriptions.component').then(m => m.TranscriptionsComponent)
  },
  {
    path: 'summaries',
    canActivate: [AuthGuard],
    loadComponent: () => import('./components/summaries/summaries.component').then(m => m.SummariesComponent)
  },
  {
    path: 'settings',
    canActivate: [AuthGuard],
    loadComponent: () => import('./components/settings/settings.component').then(m => m.SettingsComponent)
  },

  // Video (token w query param)
  {
    path: 'video',
    loadComponent: () =>
      import('./components/video-room/video-room.component').then(m => m.VideoRoomComponent)
  },

  // Dołączanie
  {
    path: 'join',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./components/join-meeting/join-meeting.component').then(m => m.JoinMeetingComponent)
  },

  // Planowanie
  {
    path: 'schedule',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./components/schedule-meeting/schedule-meeting.component').then(m => m.ScheduleMeetingComponent)
  },

  // Otwarte ścieżki
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'video',
    loadComponent: () =>
      import('./components/video-room/video-room.component').then(
        (m) => m.VideoRoomComponent
      ),
  },
  { path: '', redirectTo: 'video', pathMatch: 'full' },

  // Domyślna ścieżka fallback
  { path: '**', redirectTo: 'dashboard' }
];
