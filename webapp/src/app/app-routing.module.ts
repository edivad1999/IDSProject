import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {AuthGuard, AuthorizeExactlyRole, AuthorizeRole, UnauthGuard} from './utils/guards';
import {HomeComponent} from './components/Common/home/home.component';
import {LogoutComponent} from './logout/logout.component';

import {Role} from './domain/model/data';
import {SetMenuComponent} from './components/Manager/set-menu/set-menu.component';
import {SetTablesComponent} from './components/Manager/set-tables/set-tables.component';
import {ManageBillComponent} from './components/Client/manage-bill/manage-bill.component';


const routes: Routes = [
  {path: '', redirectTo: 'login', pathMatch: 'prefix'},
  {path: 'login', component: LoginComponent, canActivate: [UnauthGuard]},
  {path: 'logout', component: LogoutComponent, canActivate: [AuthGuard]},
  {path: 'home', component: HomeComponent, canActivate: [AuthGuard]},
  {path: 'setMenu', component: SetMenuComponent, canActivate: [AuthGuard, AuthorizeRole], data: {role: Role.MANAGER}},
  {path: 'setTables', component: SetTablesComponent, canActivate: [AuthGuard, AuthorizeRole], data: {role: Role.MANAGER}},
  {path: 'bill', component: ManageBillComponent, canActivate: [AuthGuard, AuthorizeExactlyRole], data: {role: Role.CLIENT}},
  {path: 'menu', component: ManageBillComponent, canActivate: [AuthGuard, AuthorizeExactlyRole], data: {role: Role.CLIENT}},

];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
