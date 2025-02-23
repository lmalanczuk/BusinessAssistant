import { Routes } from '@angular/router';
import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {CalendarComponent} from "./components/calendar/calendar.component";
import {TranscriptionsComponent} from "./components/transcriptions/transcriptions.component";
import {SettingsComponent} from "./components/settings/settings.component";
import {LoginComponent} from "./components/login/login.component";
import {RegisterComponent} from "./components/register/register.component";
import {SummariesComponent} from "./components/summaries/summaries.component";

export const routes: Routes = [
  {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
  //{ path: '', redirectTo: 'login', pathMatch: 'full' } kiedy będzie możliwość logowania się
  {path: 'dashboard', component: DashboardComponent},
  {path: 'calendar', component: CalendarComponent},
  {path: 'transcriptions', component: TranscriptionsComponent},
  {path: 'summaries', component: SummariesComponent},
  {path: 'settings', component: SettingsComponent},
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent }
];
