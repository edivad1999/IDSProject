import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {SubscriberContextComponent} from './utils/subscriber-context.component';
import {MatDrawer, MatDrawerMode} from '@angular/material/sidenav';
import {RepositoryService} from './data/repository/repository.service';

interface ListItem {
  name: string;
  url: string;
  needsAuthorization: boolean
}


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent extends SubscriberContextComponent implements OnInit {

  title = 'webapp';
  sidenavMode: MatDrawerMode = 'side';
  startOpened: boolean = window.innerWidth >= 916;
  isAuthenticated = false;

  @ViewChild('drawer')
  drawer!: MatDrawer;
  displayedItems: ListItem[] = [
    {name: 'Login', url: '/login', needsAuthorization: false},
    {name: 'Home', url: '/home', needsAuthorization: true},
    {name: 'Processi', url: '/processes', needsAuthorization: true},
    {name: 'Impostazioni', url: '/settings', needsAuthorization: true},
    {name: 'Qr', url: '/qr', needsAuthorization: true},
    {name: 'Logout', url: '/logout', needsAuthorization: true},

  ];
  isLoadingBarShown = false;

  constructor(
    private repo: RepositoryService
  ) {
    super();
  }

  setSidenavMode(width: number): void {
    if (width >= 916) {
      this.sidenavMode = 'side';
    } else {
      this.sidenavMode = 'over';
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any): void {
    this.setSidenavMode(event.target.innerWidth);
  }

  ngOnInit(): void {
    this.setSidenavMode(window.innerWidth);
    this.repo.authenticationStateFlow.subscribe(authState => {
      authState === 'AUTHENTICATED' ? this.isAuthenticated = true : this.isAuthenticated = false;
    });
  }
}
