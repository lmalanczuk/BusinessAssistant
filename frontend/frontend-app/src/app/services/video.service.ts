import { Injectable } from '@angular/core';
import { CompatClient, IMessage, Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({ providedIn: 'root' })
export class VideoService {
  private stompClient!: CompatClient;
  private onSignalCallback?: (signal: any) => void;
  private roomCode: string = '';

  connect(onSignal: (signal: any) => void, roomCode: string): void {
    this.onSignalCallback = onSignal;
    this.roomCode = roomCode;
    this.stompClient = Stomp.over(() => new SockJS('http://localhost:8080/ws'));

    this.stompClient.connect({}, () => {
      console.log('WebSocket connected');
      this.stompClient.subscribe(`/topic/signal/${roomCode}`, this.handleMessage);
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

  sendSignal(signal: any, roomCode: string): void {
    console.log("Wysyłam sygnał:", signal);
    this.stompClient.send(`/app/signal/${roomCode}`, {}, JSON.stringify(signal));
  }

}
