// src/app/components/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { ZegoService } from '../../services/zego.service';
import { MeetingService } from '../../services/meeting.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { Meeting } from '../../models/meeting.model';
import { Transcription } from '../../models/transcription.model';
import { Invitation } from '../../models/invitation.model';
import { Recording } from '../../models/recording.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  upcomingMeetings: Meeting[] = [];
  recentTranscriptions: Transcription[] = [];
  recentRecordings: Recording[] = [];
  invitations: Invitation[] = [];
  currentActiveMeeting: Meeting | null = null;
  isLoading: boolean = false;
  currentDate: Date = new Date();
  randomRoomId: string = this.generateRandomRoomId();

  constructor(
    private meetingService: MeetingService,
    private zegoService: ZegoService,
    public authService: AuthService, // public, aby móc używać w szablonie
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;

    // Pobierz nadchodzące spotkania
    this.meetingService.getUpcomingMeetings().subscribe(
      (data: Meeting[]) => {
        this.upcomingMeetings = data;
        this.isLoading = false;
      },
      (error: any) => {
        console.error('Error loading meetings:', error);
        this.isLoading = false;
      }
    );

    // Pobierz aktywne spotkanie
    this.meetingService.getCurrentActiveMeeting().subscribe(
      (meeting: Meeting | null) => {
        this.currentActiveMeeting = meeting;
      }
    );

    // Pobierz ostatnie transkrypcje
    this.meetingService.getRecentTranscriptions().subscribe(
      (data: Transcription[]) => {
        this.recentTranscriptions = data;
      },
      (error: any) => {
        console.error('Error loading transcriptions:', error);
      }
    );

    // Pobierz zaproszenia
    this.meetingService.getInvitations().subscribe(
      (data: Invitation[]) => {
        this.invitations = data;
      },
      (error: any) => {
        console.error('Error loading invitations:', error);
      }
    );

    // Pobierz ostatnie nagrania
    this.meetingService.getRecentRecordings().subscribe(
      (data: Recording[]) => {
        this.recentRecordings = data;
      },
      (error: any) => {
        console.error('Error loading recordings:', error);
      }
    );
  }

  // Generuje losowe ID pokoju dla szybkich spotkań
  generateRandomRoomId(): string {
    return Math.random().toString(36).substring(2, 12);
  }

  // Rozpoczyna nowe szybkie spotkanie bez planowania
  startInstantMeeting(): void {
    // Tworzymy tymczasowe spotkanie w bazie danych
    const meetingData = {
      title: 'Spotkanie ad hoc: ' + new Date().toLocaleString(),
      startTime: new Date().toISOString(),
      durationMinutes: 60
    };

    this.zegoService.createMeeting(meetingData).subscribe(
      (meeting: Meeting) => {
        this.router.navigate(['/meeting', meeting.id]);
      },
      (error: any) => {
        console.error('Error creating instant meeting:', error);
      }
    );
  }

  // Dołącza do istniejącego spotkania
  joinMeeting(meetingId: string): void {
    this.router.navigate(['/meeting', meetingId]);
  }

  // Planuje nowe spotkanie
  scheduleMeeting(): void {
    this.router.navigate(['/meetings/create']);
  }

  // Dołącza do spotkania przez bezpośredni kod pokoju
  joinByRoomId(roomId: string): void {
    if (!roomId) {
      return;
    }

    // Najpierw sprawdzamy, czy spotkanie istnieje
    this.meetingService.getMeetingByRoomId(roomId).subscribe(
      (meeting: Meeting | null) => {
        if (meeting) {
          this.router.navigate(['/meeting', meeting.id]);
        } else {
          // Jeśli nie istnieje, tworzymy nowe tymczasowe spotkanie
          const meetingData = {
            title: 'Spotkanie z kodem: ' + roomId,
            startTime: new Date().toISOString(),
            durationMinutes: 60,
            zegoRoomId: roomId
          };

          this.zegoService.createMeeting(meetingData).subscribe(
            (newMeeting: Meeting) => {
              this.router.navigate(['/meeting', newMeeting.id]);
            },
            (error: any) => {
              console.error('Error creating meeting for room ID:', error);
            }
          );
        }
      },
      (error: any) => {
        console.error('Error checking room ID:', error);
      }
    );
  }

  // Sprawdza czy spotkanie jest już aktywne lub zaraz się zacznie
  isMeetingActive(meeting: Meeting): boolean {
    const now = new Date();
    const startTime = new Date(meeting.startTime);
    const endTime = new Date(meeting.endTime);

    // Spotkanie jest aktywne jeśli już się zaczęło, ale jeszcze się nie skończyło
    // lub zaczyna się w ciągu 15 minut
    const fifteenMinutesBeforeStart = new Date(startTime);
    fifteenMinutesBeforeStart.setMinutes(fifteenMinutesBeforeStart.getMinutes() - 15);

    return (now >= fifteenMinutesBeforeStart && now <= endTime);
  }

  // Odtwarza nagranie
  playRecording(url: string): void {
    // Otwiera okno modalne z odtwarzaczem lub przekierowuje do strony z nagraniem
    window.open(url, '_blank');
  }

  // Pobiera nagranie
  downloadRecording(url: string): void {
    // Tworzy link do pobrania pliku
    const a = document.createElement('a');
    a.href = url;
    a.download = 'recording.mp4'; // Nazwa pliku
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}
