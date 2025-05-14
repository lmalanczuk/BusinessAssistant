import { Injectable } from '@angular/core';
import { VideoService } from './video.service';

@Injectable({ providedIn: 'root' })
export class WebrtcService {
  private localVideo!: HTMLVideoElement;
  private remoteVideo!: HTMLVideoElement;
  private peerConnection!: RTCPeerConnection;
  private localStream!: MediaStream;

  constructor(private videoService: VideoService) {}

  private roomCode: string = '';

  setRoomCode(code: string): void {
    this.roomCode = code;
  }

  initLocalMedia(local: HTMLVideoElement, remote: HTMLVideoElement, onReady?: () => void): void {
    this.localVideo = local;
    this.remoteVideo = remote;

    navigator.mediaDevices.getUserMedia({ video: true, audio: true }).then(stream => {
      this.localStream = stream;
      this.localVideo.srcObject = stream;
      onReady?.();
    });
  }



  async createOffer(): Promise<void> {
    this.peerConnection = this.createPeerConnection();
    this.localStream.getTracks().forEach(track => this.peerConnection.addTrack(track, this.localStream));

    const offer = await this.peerConnection.createOffer();
    await this.peerConnection.setLocalDescription(offer);

// loguj i upewnij się, że offer ma type: 'offer'
    console.log("Stworzona oferta:", offer);
    this.videoService.sendSignal({ type: 'offer', data: offer }, this.roomCode);

  }

  handleSignal(signal: any): void {
    switch (signal.type) {
      case 'offer':
        this.handleOffer(signal.data);
        break;
      case 'answer':
        this.handleAnswer(signal.data);
        break;
      case 'candidate':
        this.handleIceCandidate(signal.data);
        break;
    }
  }

  private async handleOffer(offer: RTCSessionDescriptionInit): Promise<void> {
    this.peerConnection = this.createPeerConnection();
    await this.peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
    this.localStream.getTracks().forEach(track => this.peerConnection.addTrack(track, this.localStream));

    const answer = await this.peerConnection.createAnswer();
    await this.peerConnection.setLocalDescription(answer);
    this.videoService.sendSignal({ type: 'answer', data: answer }, this.roomCode);

  }

  private async handleAnswer(answer: RTCSessionDescriptionInit): Promise<void> {
    await this.peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
  }

  private async handleIceCandidate(candidate: any): Promise<void> {
    if (candidate && candidate.candidate) {
      await this.peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
    } else {
      console.log("Pominięto pustego kandydata ICE (end-of-candidates).");
    }

  }


  hasLocalStream(): boolean {
    return !!this.localStream;
  }

  closeConnection(): void {
    this.peerConnection?.close();
    this.localStream?.getTracks().forEach(track => track.stop());
  }

  private createPeerConnection(): RTCPeerConnection {
    const pc = new RTCPeerConnection({ iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] });

    pc.onicecandidate = event => {
      if (event.candidate) {
        this.videoService.sendSignal({ type: 'candidate', data: event.candidate }, this.roomCode);
      }
    };


    pc.ontrack = event => {
      this.remoteVideo.srcObject = event.streams[0];
    };

    return pc;
  }

  toggleAudio(enabled: boolean): void {
    this.localStream?.getAudioTracks().forEach(track => track.enabled = enabled);
  }

  toggleVideo(enabled: boolean): void {
    this.localStream?.getVideoTracks().forEach(track => track.enabled = enabled);
  }
}
