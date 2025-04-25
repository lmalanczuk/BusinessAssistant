import {Component, input, output} from '@angular/core';
import {Router, RouterModule} from "@angular/router";
import {NgClass, NgIf} from "@angular/common";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, NgClass, NgIf],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  isLeftSidebarCollapsed = input.required<boolean>();
  changeIsLeftSidebarCollapsed = output<boolean>();
  constructor(public authService: AuthService, private router: Router) {}

  items = [
    {
      routeLink: 'dashboard',
      icon: 'fal fa-home',
      label: 'Strona główna',
    },
    {
      routeLink: 'calendar',
      icon: 'fal fa-calendar',
      label: 'Kalendarz',
    },
    {
      routeLink: 'transcriptions',
      icon: 'fal fa-microphone',
      label: 'Transkrypcje',
    },
    {
      routeLink: 'summaries',
      icon: 'fal fa-pen',
      label: 'Podsumowania',
    },
    {
      routeLink: 'settings',
      icon: 'fal fa-cog',
      label: 'Ustawienia',
    },
  ];

  toggleCollapse(): void {
    this.changeIsLeftSidebarCollapsed.emit(!this.isLeftSidebarCollapsed());
  }

  closeSidenav(): void {
    this.changeIsLeftSidebarCollapsed.emit(true);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
