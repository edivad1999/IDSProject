import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {SubscriberContextComponent} from './utils/subscriber-context.component';
import {MatDrawer, MatDrawerMode} from '@angular/material/sidenav';
import {RepositoryService} from './data/repository/repository.service';
import {getRoleValue, Role} from './domain/model/data';

interface ListItem {
  name: string;
  url: string;
  needsAuthorization: boolean;
  role?: Role;

}


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent extends SubscriberContextComponent implements OnInit {
  role: Role | null = null;
  title = 'webapp';
  sidenavMode: MatDrawerMode = 'side';
  startOpened: boolean = window.innerWidth >= 916;
  isAuthenticated = false;

  @ViewChild('drawer')
  drawer!: MatDrawer;
  displayedItems: ListItem[] = [
    {name: 'Login', url: '/login', needsAuthorization: false},
    {name: 'Home', url: '/home', needsAuthorization: true},
    {name: 'Imposta il menù', url: '/setMenu', needsAuthorization: true, role: 'MANAGER'},
    {name: 'Imposta i tavoli', url: '/setTables', needsAuthorization: true, role: 'MANAGER'},
    {name: 'Cucina', url: '/kitchen', needsAuthorization: true, role: 'WAITER'},
    {name: 'Visualizza log', url: '/viewLog', needsAuthorization: true, role: 'MANAGER'},
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
    this.subscribeWithContext(
      this.repo.whoAmI(),
      role => {
        this.role = role;
      }
    );
    this.repo.role.subscribe(
      role => this.role = role
    );
    this.setSidenavMode(window.innerWidth);
    this.repo.authenticationStateFlow.subscribe(authState => {
      authState === 'AUTHENTICATED' ? this.isAuthenticated = true : this.isAuthenticated = false;
    });

  }

  authorize(userRole: Role | null, routeRole?: Role): boolean {

    if (routeRole === undefined) {
      return true;
    } else {
      if (userRole === null) {
        return false;

      } else {
        return getRoleValue(userRole) >= getRoleValue(routeRole);
      }
    }
  }

}
