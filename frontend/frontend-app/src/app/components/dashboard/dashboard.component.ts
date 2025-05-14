import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {Router, RouterLink} from '@angular/router';

import { ZegoService } from '../../services/zego.service';
import { MeetingService } from '../../services/meeting.service';
import { AuthService } from '../../services/auth.service';

import { Meeting } from '../../models/meeting.model';
import { Transcription } from '../../models/transcription.model';
import { Invitation } from '../../models/invitation.model';
import { Recording } from '../../models/recording.model';
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private meetingService = inject(MeetingService);
  private zegoService = inject(ZegoService);
  private router = inject(Router);
  public authService = inject(AuthService);

  upcomingMeetings: Meeting[] = [];
  recentTranscriptions: Transcription[] = [];
  recentRecordings: Recording[] = [];
  invitations: Invitation[] = [];
  currentActiveMeeting: Meeting | null = null;
  isLoading: boolean = false;
  currentDate: Date = new Date();
  randomRoomId: string = this.generateRandomRoomId();

  roomCode: string = '';
  isInitiator: boolean = false;

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;

    this.meetingService.getUpcomingMeetings().subscribe({
      next: (data: Meeting[]) => {
        this.upcomingMeetings = data;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading meetings:', error);
        this.isLoading = false;
      }
    });

    this.meetingService.getCurrentActiveMeeting().subscribe({
      next: (meeting: Meeting | null) => {
        this.currentActiveMeeting = meeting;
      }
    });

    this.meetingService.getRecentTranscriptions().subscribe({
      next: (data: Transcription[]) => {
        this.recentTranscriptions = data;
      },
      error: (error: any) => {
        console.error('Error loading transcriptions:', error);
      }
    });

    this.meetingService.getInvitations().subscribe({
      next: (data: Invitation[]) => {
        this.invitations = data;
      },
      error: (error: any) => {
        console.error('Error loading invitations:', error);
      }
    });

    this.meetingService.getRecentRecordings().subscribe({
      next: (data: Recording[]) => {
        this.recentRecordings = data;
      },
      error: (error: any) => {
        console.error('Error loading recordings:', error);
      }
    });
  }

  goToVideoCall(): void {
    if (!this.roomCode) {
      alert('Wpisz kod spotkania!');
      return;
    }

    this.router.navigate(['/video-call'], {
      queryParams: {
        room: this.roomCode,
        initiator: this.isInitiator
      }
    });
  }

  generateRandomRoomId(): string {
    return Math.random().toString(36).substring(2, 12);
  }

  startInstantMeeting(): void {
    const meetingData = {
      title: 'Spotkanie ad hoc: ' + new Date().toLocaleString(),
      startTime: new Date().toISOString(),
      durationMinutes: 60
    };

    this.zegoService.createMeeting(meetingData).subscribe({
      next: (meeting: Meeting) => {
        this.router.navigate(['/meeting', meeting.id]);
      },
      error: (error: any) => {
        console.error('Error creating instant meeting:', error);
      }
    });
  }

  joinMeeting(meetingId: string): void {
    this.router.navigate(['/meeting', meetingId]);
  }

  scheduleMeeting(): void {
    this.router.navigate(['/meetings/create']);
  }

  joinByRoomId(roomId: string): void {
    if (!roomId) return;

    this.meetingService.getMeetingByRoomId(roomId).subscribe({
      next: (meeting: Meeting | null) => {
        if (meeting) {
          this.router.navigate(['/meeting', meeting.id]);
        } else {
          const meetingData = {
            title: 'Spotkanie z kodem: ' + roomId,
            startTime: new Date().toISOString(),
            durationMinutes: 60,
            zegoRoomId: roomId
          };

          this.zegoService.createMeeting(meetingData).subscribe({
            next: (newMeeting: Meeting) => {
              this.router.navigate(['/meeting', newMeeting.id]);
            },
            error: (error: any) => {
              console.error('Error creating meeting for room ID:', error);
            }
          });
        }
      },
      error: (error: any) => {
        console.error('Error checking room ID:', error);
      }
    });
  }

  isMeetingActive(meeting: Meeting): boolean {
    const now = new Date();
    const startTime = new Date(meeting.startTime);
    const endTime = new Date(meeting.endTime);
    const fifteenMinutesBeforeStart = new Date(startTime);
    fifteenMinutesBeforeStart.setMinutes(startTime.getMinutes() - 15);
    return now >= fifteenMinutesBeforeStart && now <= endTime;
  }

  playRecording(url: string): void {
    window.open(url, '_blank');
  }

  downloadRecording(url: string): void {
    const a = document.createElement('a');
    a.href = url;
    a.download = 'recording.mp4';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  // Dodane metody do obsługi zaproszeń:
  acceptInvitation(invitation: Invitation): void {
    console.log('Zaproszenie zaakceptowane:', invitation);
    // TODO: Wywołaj serwis i odśwież dane
  }

  rejectInvitation(invitation: Invitation): void {
    console.log('Zaproszenie odrzucone:', invitation);
    // TODO: Wywołaj serwis i odśwież dane
  }
}
