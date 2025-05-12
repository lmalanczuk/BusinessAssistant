import { Injectable } from '@angular/core';
import { CompatClient, IMessage, Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({ providedIn: 'root' })
export class VideoService {
  private stompClient!: CompatClient;
  private onSignalCallback?: (signal: any) => void;

  connect(onSignal: (signal: any) => void): void {
    this.onSignalCallback = onSignal;
    this.stompClient = Stomp.over(() => new SockJS('http://localhost:8080/ws'));

    this.stompClient.connect({}, () => {
      console.log('WebSocket connected');
      this.stompClient.subscribe('/topic/signal', this.handleMessage);
    });
  }

  disconnect(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect(() => {
        console.log('WebSocket disconnected');
      });
    }
  }

  private handleMessage = (message: IMessage): void => {
    const signal = JSON.parse(message.body);
    console.log('Received signal:', signal);
    this.onSignalCallback?.(signal);
  };

  sendSignal(signal: any): void {
    this.stompClient.send('/app/signal', {}, JSON.stringify(signal));
  }
}
