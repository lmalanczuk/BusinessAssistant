import {Component, input, output} from '@angular/core';
import {RouterModule} from "@angular/router";
import {NgClass} from "@angular/common";

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, NgClass],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  isLeftSidebarCollapsed = input.required<boolean>();
  changeIsLeftSidebarCollapsed = output<boolean>();
  items = [
    {
      routeLink: 'dashboard',
      icon: 'fal fa-home',
      label: 'Dashboard',
    },
    {
      routeLink: 'calendar',
      icon: 'fal fa-calendar',
      label: 'Calendar',
    },
    {
      routeLink: 'transcriptions',
      icon: 'fal fa-microphone',
      label: 'Transcriptions',
    },
    {
      routeLink: 'summaries',
      icon: 'fal fa-pen',
      label: 'Summaries',
    },
    {
      routeLink: 'settings',
      icon: 'fal fa-cog',
      label: 'Settings',
    },
  ];

  toggleCollapse(): void {
    this.changeIsLeftSidebarCollapsed.emit(!this.isLeftSidebarCollapsed());
  }

  closeSidenav(): void {
    this.changeIsLeftSidebarCollapsed.emit(true);
  }
}
