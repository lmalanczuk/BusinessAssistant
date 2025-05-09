import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule, RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { MeetingService } from '../../services/meeting.service';

// Interfejsy
interface Meeting {
  id: string;
  title: string;
  startTime: Date;
  endTime: Date;
  status: 'PLANNED' | 'ONGOING' | 'COMPLETED';
  participantCount?: number;
}

interface Invitation {
  id: string;
  meetingId: string;
  meetingTitle: string;
  senderName: string;
  sentAt: Date;
}

interface Transcription {
  id: string;
  meetingId: string;
  generatedAt: Date;
  title: string;
  meetingTitle: string; // Dodane na podstawie błędów
}

interface Recording {
  id: string;
  meetingId: string;
  recordedAt: Date;
  title: string;
  duration: number;
  meetingTitle: string; // Dodane na podstawie błędów
  url: string; // Dodane na podstawie błędów
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    RouterLink,
    DatePipe,
    FormsModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  // Dane do wyświetlenia na dashboardzie
  currentDate = new Date();
  currentActiveMeeting: Meeting | null = null;
  upcomingMeetings: Meeting[] = [];
  recentTranscriptions: Transcription[] = [];
  recentRecordings: Recording[] = [];

  // Dodane na podstawie błędów
  isLoading = true;
  invitations: Invitation[] = [];

  constructor(
    public authService: AuthService,
    private meetingService: MeetingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Pobierz dane przy inicjalizacji
    this.loadDashboardData();
  }

  /**
   * Pobiera wszystkie dane potrzebne na dashboardzie
   */
  loadDashboardData(): void {
    this.isLoading = true;

    // Symulujemy ładowanie danych
    setTimeout(() => {
      // W prawdziwej implementacji zastąp to wywołaniami API
      this.loadActiveMeeting();
      this.loadUpcomingMeetings();
      this.loadRecentTranscriptions();
      this.loadRecentRecordings();
      this.loadInvitations();

      this.isLoading = false;
    }, 1000);
  }

  /**
   * Pobiera aktywne spotkanie
   */
  loadActiveMeeting(): void {
    // Przykładowa implementacja - zastąp wywołaniem API
    this.currentActiveMeeting = {
      id: '123',
      title: 'Spotkanie zespołu projektu',
      startTime: new Date(Date.now() - 30 * 60000), // 30 minut temu
      endTime: new Date(Date.now() + 30 * 60000),   // 30 minut w przyszłość
      status: 'ONGOING',
      participantCount: 5
    };
  }

  /**
   * Pobiera nadchodzące spotkania
   */
  loadUpcomingMeetings(): void {
    // Przykładowa implementacja - zastąp wywołaniem API
    this.upcomingMeetings = [
      {
        id: '124',
        title: 'Retrospektywa sprintu',
        startTime: new Date(Date.now() + 2 * 60 * 60000), // Za 2 godziny
        endTime: new Date(Date.now() + 3 * 60 * 60000),   // Za 3 godziny
        status: 'PLANNED'
      },
      {
        id: '125',
        title: 'Spotkanie z klientem',
        startTime: new Date(Date.now() + 5 * 60 * 60000), // Za 5 godzin
        endTime: new Date(Date.now() + 6 * 60 * 60000),   // Za 6 godzin
        status: 'PLANNED'
      }
    ];
  }

  /**
   * Pobiera ostatnie transkrypcje
   */
  loadRecentTranscriptions(): void {
    // Przykładowa implementacja - zastąp wywołaniem API
    this.recentTranscriptions = [
      {
        id: '321',
        meetingId: '111',
        generatedAt: new Date(Date.now() - 2 * 24 * 60 * 60000), // 2 dni temu
        title: 'Transkrypcja: Planowanie sprintu',
        meetingTitle: 'Planowanie sprintu'
      },
      {
        id: '322',
        meetingId: '112',
        generatedAt: new Date(Date.now() - 5 * 24 * 60 * 60000), // 5 dni temu
        title: 'Transkrypcja: Retrospektywa',
        meetingTitle: 'Retrospektywa'
      }
    ];
  }

  /**
   * Pobiera ostatnie nagrania
   */
  loadRecentRecordings(): void {
    // Przykładowa implementacja - zastąp wywołaniem API
    this.recentRecordings = [
      {
        id: '421',
        meetingId: '111',
        recordedAt: new Date(Date.now() - 2 * 24 * 60 * 60000), // 2 dni temu
        title: 'Nagranie: Planowanie sprintu',
        duration: 45, // minut
        meetingTitle: 'Planowanie sprintu',
        url: 'https://example.com/recordings/421.mp3'
      },
      {
        id: '422',
        meetingId: '112',
        recordedAt: new Date(Date.now() - 5 * 24 * 60 * 60000), // 5 dni temu
        title: 'Nagranie: Retrospektywa',
        duration: 30, // minut
        meetingTitle: 'Retrospektywa',
        url: 'https://example.com/recordings/422.mp3'
      }
    ];
  }

  /**
   * Pobiera zaproszenia do spotkań
   */
  loadInvitations(): void {
    // Przykładowa implementacja - zastąp wywołaniem API
    this.invitations = [
      {
        id: '521',
        meetingId: '131',
        meetingTitle: 'Spotkanie projektowe',
        senderName: 'Jan Kowalski',
        sentAt: new Date(Date.now() - 1 * 60 * 60000) // 1 godzinę temu
      },
      {
        id: '522',
        meetingId: '132',
        meetingTitle: 'Spotkanie z klientem',
        senderName: 'Anna Nowak',
        sentAt: new Date(Date.now() - 3 * 60 * 60000) // 3 godziny temu
      }
    ];
  }

  /**
   * Sprawdza czy spotkanie jest aktywne
   */
  isMeetingActive(meeting: Meeting): boolean {
    return meeting.status === 'ONGOING';
  }

  /**
   * Dołącza do spotkania
   */
  joinMeeting(meetingId: string): void {
    console.log(`Dołączanie do spotkania: ${meetingId}`);
    this.meetingService.joinMeeting(meetingId).subscribe({
      next: (result) => {
        console.log('Dołączono do spotkania', result);
        // Tutaj przekierowanie do widoku spotkania
        this.router.navigate(['/meeting', meetingId]);
      },
      error: (error) => {
        console.error('Błąd podczas dołączania do spotkania', error);
      }
    });
  }

  /**
   * Rozpoczyna natychmiastowe spotkanie
   */
  startInstantMeeting(): void {
    console.log('Rozpoczynanie natychmiastowego spotkania');
    this.meetingService.createInstantMeeting().subscribe({
      next: (meeting) => {
        console.log('Utworzono natychmiastowe spotkanie', meeting);
        this.router.navigate(['/meeting', meeting.id]);
      },
      error: (error) => {
        console.error('Błąd podczas tworzenia natychmiastowego spotkania', error);
      }
    });
  }

  /**
   * Planuje nowe spotkanie
   */
  scheduleMeeting(): void {
    console.log('Przekierowanie do zaplanowania spotkania');
    this.router.navigate(['/schedule-meeting']);
  }

  /**
   * Dołącza do spotkania po ID pokoju
   */
  joinByRoomId(roomId: string): void {
    if (!roomId || roomId.trim() === '') {
      console.error('Nieprawidłowe ID pokoju');
      return;
    }

    console.log(`Dołączanie do pokoju: ${roomId}`);
    this.meetingService.joinMeetingByRoomId(roomId).subscribe({
      next: (result) => {
        console.log('Dołączono do pokoju', result);
        this.router.navigate(['/meeting', result.meetingId]);
      },
      error: (error) => {
        console.error('Błąd podczas dołączania do pokoju', error);
      }
    });
  }

  /**
   * Odtwarza nagranie
   */
  playRecording(url: string): void {
    console.log(`Odtwarzanie nagrania: ${url}`);
    // Tutaj implementacja odtwarzania nagrania
    // Na przykład otwarcie w nowym oknie lub przekierowanie do playera
    window.open(url, '_blank');
  }

  /**
   * Pobiera nagranie
   */
  downloadRecording(url: string): void {
    console.log(`Pobieranie nagrania: ${url}`);
    // Implementacja pobierania
    // Można utworzyć ukryty link i kliknąć go
    const a = document.createElement('a');
    a.href = url;
    a.download = url.split('/').pop() || 'recording.mp3';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}
