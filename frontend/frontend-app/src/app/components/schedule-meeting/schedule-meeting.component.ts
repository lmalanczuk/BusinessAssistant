import { Component } from '@angular/core';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MeetingService, CreateMeetingRequest } from '../../services/meeting.service';
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-schedule-meeting',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './schedule-meeting.component.html'
})
export class ScheduleMeetingComponent {
  title = '';
  startDateTime = '';  // bound to <input type="datetime-local">
  duration = 30;

  private meetingService = inject(MeetingService);
  private router = inject(Router);

  schedule() {
    const req: CreateMeetingRequest = {
      title: this.title,
      startTime: new Date(this.startDateTime).toISOString(),
      durationMinutes: this.duration
    };
    this.meetingService.scheduleMeeting(req)
      .subscribe(resp => {
        this.router.navigate(['/video'], {
          queryParams: { token: resp.token, url: resp.roomUrl }
        });
      });
  }
}
