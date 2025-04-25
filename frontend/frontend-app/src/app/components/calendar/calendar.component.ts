import { Component } from '@angular/core';
import {MOCK_MEETINGS} from "../../services/mock-data";
import {DatePipe, NgClass, NgForOf} from "@angular/common";

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [
    NgClass,
    NgForOf,
    DatePipe
  ],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.css'
})
export class CalendarComponent {
 meetings = MOCK_MEETINGS;

  formatDate(dateString: string | null): string {
    if (!dateString) return 'No date available';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).replace(',', '');
  }

}
