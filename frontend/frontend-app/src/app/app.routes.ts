import { Routes } from '@angular/router';
import {DashboardComponent} from "./dashboard/dashboard.component";
import {CalendarComponent} from "./calendar/calendar.component";
import {TranscriptionsComponent} from "./transcriptions/transcriptions.component";
import {SettingsComponent} from "./settings/settings.component";

export const routes: Routes = [
  {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
  {path: 'dashboard', component: DashboardComponent},
  {path: 'calendar', component: CalendarComponent},
  {path: 'transcriptions', component: TranscriptionsComponent},
  {path: 'settings', component: SettingsComponent}
];
