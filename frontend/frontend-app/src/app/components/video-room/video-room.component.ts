import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import DailyIframe, { DailyCall } from '@daily-co/daily-js';

@Component({
  standalone: true,
  selector: 'app-video-room',
  imports: [CommonModule],
  templateUrl: './video-room.component.html',
  styleUrls: ['./video-room.component.css']
})
export class VideoRoomComponent implements OnInit, OnDestroy {
  callFrame: DailyCall | null = null;

  // Zmień na swój rzeczywisty room URL
  roomUrl: string = 'https://businessassistant.daily.co/test';

  ngOnInit(): void {
    this.callFrame = DailyIframe.createFrame({
      showLeaveButton: true,
      iframeStyle: {
        width: '100%',
        height: '100%',
        border: '0',
      },
    });

    this.callFrame.join({ url: this.roomUrl });

    const iframe = this.callFrame.iframe();
    if (iframe) {
      document.getElementById('video-container')?.appendChild(iframe);
    }
  }

  ngOnDestroy(): void {
    this.callFrame?.leave();
    this.callFrame?.destroy();
  }
}
