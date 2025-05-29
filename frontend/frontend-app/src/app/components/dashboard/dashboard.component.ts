import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MeetingService, Meeting, CreateMeetingRequest } from '../../services/meeting.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  meetings: Meeting[] = [];
  initials: string = '';
  currentDate: Date = new Date();
  meetingTitle: string = 'Spotkanie';
  duration: number = 30;

  private meetingService = inject(MeetingService);
  public authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    // Pobierz nadchodzące spotkania z backendu
    this.meetingService.getUpcomingMeetings()
      .subscribe({
        next: (data: Meeting[]) => this.meetings = data,
        error: err => console.error('Błąd pobierania spotkań', err)
      });

    // Inicjały użytkownika
    this.initials = this.authService.getUserInitials();
  }

  get firstThree(): Meeting[] {
    return this.meetings.slice(0, 3);
  }

  startMeeting(): void {
    const req: CreateMeetingRequest = {
      title: this.meetingTitle,
      startTime: new Date().toISOString(),
      durationMinutes: this.duration
    };
    this.meetingService.startMeeting(req).subscribe({
      next: resp => {
        this.router.navigate(['/video'], {
          queryParams: { token: resp.token, url: resp.roomUrl }
        });
      },
      error: err => console.error('Błąd startowania spotkania', err)
    });
  }

  goToJoin(): void {
    this.router.navigate(['/join']);
  }

  goToSchedule(): void {
    this.router.navigate(['/schedule']);
  }
}
