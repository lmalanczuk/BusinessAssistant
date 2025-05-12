import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VideoService } from '../../services/video.service';
import { WebrtcService } from '../../services/webrtc.service';

@Component({
  selector: 'app-video-call',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './video-call.component.html',
  styleUrls: ['./video-call.component.css']
})
export class VideoCallComponent implements OnInit, OnDestroy {
  private videoService = inject(VideoService);
  private webrtcService = inject(WebrtcService);

  @ViewChild('localVideo', { static: true }) localVideoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('remoteVideo', { static: true }) remoteVideoRef!: ElementRef<HTMLVideoElement>;

  ngOnInit(): void {
    this.webrtcService.initLocalMedia(this.localVideoRef.nativeElement, this.remoteVideoRef.nativeElement);
    this.videoService.connect(signal => {
      this.webrtcService.handleSignal(signal);
    });
  }

  ngOnDestroy(): void {
    this.videoService.disconnect();
    this.webrtcService.closeConnection();
  }

  call(): void {
    this.webrtcService.createOffer();
  }

  hangUp(): void {
    this.webrtcService.closeConnection();
  }
}
