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
    document.querySelector('.body')?.classList.add('video-open');
    this.route.queryParamMap.subscribe(params => {
      this.roomUrl = params.get('url');
      this.token   = params.get('token');
      if (this.roomUrl && this.token) {
        this.initDaily();
      } else {
        console.error('Brak roomUrl lub token w queryParams');
      }
    });
    console.log('video-room init fired');
  }

  private async initDaily() {
    const container = document.getElementById('video-container')!;
    if (!container) {
      console.error('Nie ma kontenera video-container!');
      return;
    }

    document
      .querySelectorAll('iframe[data-daily-iframe]')
      .forEach(f => f.remove());

    this.callFrame = DailyIframe.createFrame(
      container,
      {
        showLeaveButton: true,
        iframeStyle: {
          width: '100%',
          height: '100%',
          border: '0',
          display: 'block'
        }
      }
    );

    await this.callFrame.join({
      url: this.roomUrl!,
      token: this.token!
    });
  }


  ngOnDestroy(): void {
    document.querySelector('.body')?.classList.remove('video-open');
    if (this.callFrame) {
      this.callFrame.leave();
      this.callFrame.destroy();
    }
  }
}
