import { Component } from '@angular/core';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MeetingService } from '../../services/meeting.service';
import {FormsModule} from "@angular/forms";
import {JoinMeetingRequest} from "../../services/dto";

@Component({
  selector: 'app-join-meeting',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './join-meeting.component.html'
})
export class JoinMeetingComponent {
  roomName = '';
  userName = '';

  private meetingService = inject(MeetingService);
  private router = inject(Router);

  join() {
    const req: JoinMeetingRequest = { roomName: this.roomName, userName: this.userName };
    this.meetingService.joinMeeting(req)
      .subscribe(resp => {
        this.router.navigate(['/video'], {
          queryParams: { token: resp.token, url: resp.roomUrl }
        });
      });
  }
}
