import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {AuthGuard, AuthorizeExactlyRole, AuthorizeRole, UnauthGuard} from './utils/guards';
import {HomeComponent} from './components/Common/home/home.component';
import {LogoutComponent} from './logout/logout.component';
import {SetMenuComponent} from './components/Manager/set-menu/set-menu.component';
import {SetTablesComponent} from './components/Manager/set-tables/set-tables.component';
import {ManageBillComponent} from './components/Client/manage-bill/manage-bill.component';
import {MenuComponent} from './components/Common/menu/menu.component';
import {KitchenHomeComponent} from './components/Kitchen/kitchen-home/kitchen-home.component';
import {RegisterScreenComponent} from './register-screen/register-screen.component';
import {ViewLogComponent} from './components/Manager/view-log/view-log.component';


const routes: Routes = [
  {path: '', redirectTo: 'login', pathMatch: 'prefix'},
  {path: 'login', component: LoginComponent, canActivate: [UnauthGuard]},
  {path: 'register', component: RegisterScreenComponent, canActivate: [UnauthGuard]},
  {path: 'logout', component: LogoutComponent, canActivate: [AuthGuard]},
  {path: 'home', component: HomeComponent, canActivate: [AuthGuard]},
  {path: 'setMenu', component: SetMenuComponent, canActivate: [AuthGuard, AuthorizeRole], data: {role: 'MANAGER'}},
  {path: 'viewLog', component: ViewLogComponent, canActivate: [AuthGuard, AuthorizeRole], data: {role: 'MANAGER'}},
  {path: 'setTables', component: SetTablesComponent, canActivate: [AuthGuard, AuthorizeRole], data: {role: 'MANAGER'}},
  {path: 'bill', component: ManageBillComponent, canActivate: [AuthGuard, AuthorizeExactlyRole], data: {role: 'CLIENT'}},
  {path: 'bill/:idBill', component: ManageBillComponent, canActivate: [AuthGuard, AuthorizeRole], data: {role: 'CLIENT'}},
  {path: 'menu', component: MenuComponent, canActivate: [AuthGuard, AuthorizeExactlyRole], data: {role: 'CLIENT'}},
  {path: 'kitchen', component: KitchenHomeComponent, canActivate: [AuthGuard, AuthorizeRole], data: {role: 'WAITER'}},
  {path: ':bill/waiterMenu', component: MenuComponent, canActivate: [AuthGuard, AuthorizeRole], data: {role: 'KITCHEN'}},

];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
