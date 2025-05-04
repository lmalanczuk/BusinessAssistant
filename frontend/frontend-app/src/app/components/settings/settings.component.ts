// frontend/frontend-app/src/app/components/settings/settings.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ZoomService } from '../../services/zoom.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {
  zoomConnected = false;
  isLoading = false;
  connectLoading = false;
  errorMessage = '';

  constructor(
    private zoomService: ZoomService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.checkZoomConnection();
  }

  checkZoomConnection() {
    // Sprawdźmy najpierw, czy token istnieje
    const token = localStorage.getItem('token');
    console.log('Token in localStorage:', token ? 'exists' : 'not found');
    if (!token) {
      this.errorMessage = 'Brak tokenu uwierzytelniającego. Wyloguj się i zaloguj ponownie.';
      return;
    }

    // Ręczne dodanie tokenu do żądania, aby zobaczyć czy działa
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    this.isLoading = true;
    this.http.get('http://localhost:8080/api/zoom/test-zoom-config', { headers }).subscribe({
      next: (response) => {
        console.log('Manual auth test successful:', response);
        // Kontynuuj standardowe sprawdzenie
        this.zoomService.checkZoomConfig().subscribe({
          next: (configResponse) => {
            if (configResponse.user && configResponse.user.hasZoomId) {
              this.zoomConnected = true;
            } else {
              this.zoomConnected = false;
            }
            this.isLoading = false;
          },
          error: (configError) => {
            console.error('Error checking Zoom config:', configError);
            this.errorMessage = 'Błąd podczas sprawdzania połączenia z Zoom';
            this.isLoading = false;
          }
        });
      },
      error: (error) => {
        console.error('Manual auth test failed:', error);
        this.errorMessage = 'Błąd uwierzytelniania. Sprawdź konsolę.';
        this.isLoading = false;
      }
    });
  }

  connectZoom() {
    const token = localStorage.getItem('token');
    if (!token) {
      this.errorMessage = 'Brak tokenu uwierzytelniającego. Wyloguj się i zaloguj ponownie.';
      return;
    }

    this.connectLoading = true;

    // Użyjmy bezpośrednio HttpClient z ręcznie dodanym nagłówkiem
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    this.http.get('http://localhost:8080/api/zoom/connect-debug', { headers }).subscribe({
      next: (response: any) => {
        console.log('Zoom connect debug response:', response);
        if (response && response.authUrl) {
          window.location.href = response.authUrl;
        } else {
          this.errorMessage = 'Nieprawidłowa odpowiedź z serwera';
          this.connectLoading = false;
        }
      },
      error: (error) => {
        console.error('Error connecting to Zoom:', error);
        this.errorMessage = 'Błąd podczas łączenia z Zoom: ' +
          (error.statusText || error.message || 'Nieznany błąd');
        this.connectLoading = false;
      }
    });
  }
}
