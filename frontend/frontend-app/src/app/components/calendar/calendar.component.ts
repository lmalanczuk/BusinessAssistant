import {Component, inject} from '@angular/core';
import {MOCK_MEETINGS} from "../../services/mock-data";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {Meeting} from "../../services/dto";
import {MeetingService} from "../../services/meeting.service";
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [
    NgClass,
    NgForOf,
    DatePipe,
    NgIf
  ],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.css'
})
export class CalendarComponent {
  meetings: Meeting[] = [];
  private meetingService = inject(MeetingService);

  ngOnInit(): void {
    // Pobierz nadchodzące spotkania z backendu
    this.meetingService.getUpcomingMeetings()
      .subscribe({
        next: (data: Meeting[]) => this.meetings = data,
        error: err => console.error('Błąd pobierania spotkań', err)
      });
  }

  copyToClipboard(text: string) {
    navigator.clipboard.writeText(text).then(() => {
      alert('Skopiowano: ' + text);
    });
  }
}
