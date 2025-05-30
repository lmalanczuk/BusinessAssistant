import { Component, HostListener, OnInit, signal } from '@angular/core';
import {Router, NavigationEnd, RouterOutlet} from '@angular/router';
import { filter } from 'rxjs';
import {MainComponent} from "./components/main/main.component";
import {SidebarComponent} from "./components/sidebar/sidebar.component";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    MainComponent,
    RouterOutlet,
    SidebarComponent,
    NgIf
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  isLeftSidebarCollapsed = signal<boolean>(false);
  screenWidth = signal<number>(window.innerWidth);
  showLayout = signal<boolean>(true);

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.onResize();

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const path = event.urlAfterRedirects || event.url;
      this.showLayout.set(!(path.includes('/login') || path.includes('/register')));
    });
  }

  @HostListener('window:resize')
  onResize() {
    this.screenWidth.set(window.innerWidth);
    if (this.screenWidth() < 768) {
      this.isLeftSidebarCollapsed.set(true);
    }
  }

  changeIsLeftSidebarCollapsed(isLeftSidebarCollapsed: boolean): void {
    this.isLeftSidebarCollapsed.set(isLeftSidebarCollapsed);
  }
}
