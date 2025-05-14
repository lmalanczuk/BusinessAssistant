import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VideoService } from '../../services/video.service';
import { WebrtcService } from '../../services/webrtc.service';
import {FormsModule} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-video-call',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './video-call.component.html',
  styleUrls: ['./video-call.component.css']
})
export class VideoCallComponent implements OnInit, OnDestroy {
  @ViewChild('localVideo', { static: true }) localVideoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('remoteVideo', { static: true }) remoteVideoRef!: ElementRef<HTMLVideoElement>;

  roomCode: string = '';
  isInitiator = false;
  isAudioOn = true;
  isVideoOn = true;


  constructor(
    private webrtcService: WebrtcService,
    private videoService: VideoService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const room = params['room'];
      const initiator = params['initiator'] === 'true';

      if (!room) {
        alert('Brak kodu pokoju!');
        this.router.navigate(['/dashboard']);
        return;
      }

      this.roomCode = room;
      this.isInitiator = initiator;

      this.join(); // automatyczne uruchomienie
    });
  }

  join(): void {
    if (!this.roomCode) {
      alert("Podaj kod pokoju");
      return;
    }

    this.webrtcService.setRoomCode(this.roomCode);

    this.webrtcService.initLocalMedia(
      this.localVideoRef.nativeElement,
      this.remoteVideoRef.nativeElement,
      () => {
        this.videoService.connect(signal => {
          this.webrtcService.handleSignal(signal);
        }, this.roomCode);

        if (this.isInitiator) {
          setTimeout(() => {
            this.webrtcService.createOffer();
          }, 1000);
        }
      }
    );
  }


    hangUp(): void {
    this.videoService.disconnect();
    this.webrtcService.closeConnection();
    this.router.navigate(['/dashboard']);
  }

  toggleMic(): void {
    this.isAudioOn = !this.isAudioOn;
    this.webrtcService.toggleAudio(this.isAudioOn);
  }

  toggleCamera(): void {
    this.isVideoOn = !this.isVideoOn;
    this.webrtcService.toggleVideo(this.isVideoOn);
  }


  ngOnDestroy(): void {
    this.hangUp();
  }
}
