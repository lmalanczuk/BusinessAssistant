import { Component } from '@angular/core';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MeetingService } from '../../services/meeting.service';
import {FormsModule} from "@angular/forms";
import {CreateMeetingRequest} from "../../services/dto";

@Component({
  selector: 'app-schedule-meeting',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './schedule-meeting.component.html',
  styleUrls: ['./schedule-meeting.component.css'],
})
export class ScheduleMeetingComponent {
  title = '';
  startDateTime = '';
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

  goBack() {
    this.router.navigate(['/dashboard']);
  }
}
