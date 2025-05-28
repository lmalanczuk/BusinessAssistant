import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import {MOCK_MEETINGS} from "../../services/mock-data";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  meetings = MOCK_MEETINGS;
  initials = '';
  currentDate: Date = new Date();

  public authService = inject(AuthService);
  private router = inject(Router);

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.initials = this.auth.getUserInitials();
  }
  get firstThree() {
    return this.meetings.slice(0, 3);
  }
}
