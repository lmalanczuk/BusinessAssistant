import { Component } from '@angular/core';
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  fullName: string = '';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.fullName = this.authService.getUserFullName();
  }

}
