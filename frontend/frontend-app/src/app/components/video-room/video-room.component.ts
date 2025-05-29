import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import DailyIframe, { DailyCall } from '@daily-co/daily-js';

@Component({
  selector: 'app-video-room',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './video-room.component.html',
  styleUrls: ['./video-room.component.css']
})
export class VideoRoomComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  callFrame: DailyCall | null = null;
  roomUrl: string | null = null;
  token: string | null = null;
  loading = true;

  ngOnInit(): void {
    // Odczytujemy parametry query
    this.route.queryParamMap.subscribe(params => {
      this.roomUrl = params.get('url');
      this.token   = params.get('token');
      if (this.roomUrl && this.token) {
        this.initDaily();
      } else {
        console.error('Brak roomUrl lub token w queryParams');
      }
    });
  }

  private async initDaily() {
    try {
      // 1) Tworzymy frame bez url i tokenu
      this.callFrame = DailyIframe.createFrame({
        showLeaveButton: true,
        iframeStyle: { width: '100%', height: '100%', border: '0' }
      });
      // 2) Dołączamy do pokoju
      await this.callFrame.join({
        url: this.roomUrl!,
        token: this.token!
      });
      // 3) Dodajemy iframe do DOM
      const iframeEl = this.callFrame.iframe();
      if (iframeEl) {
        const container = document.getElementById('video-container');
        container?.appendChild(iframeEl);
      }
    } catch (err) {
      console.error('Błąd inicjalizacji Daily:', err);
    } finally {
      this.loading = false;
    }
  }

  ngOnDestroy(): void {
    if (this.callFrame) {
      this.callFrame.leave();
      this.callFrame.destroy();
    }
  }
}
