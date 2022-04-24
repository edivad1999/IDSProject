import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatButtonModule} from '@angular/material/button';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatCardModule} from '@angular/material/card';
import {MatToolbarModule} from '@angular/material/toolbar';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatListModule} from '@angular/material/list';
import {LoginComponent} from './login/login.component';
import {AppRoutingModule} from './app-routing.module';
import {MatRippleModule} from '@angular/material/core';
import {MatIconModule} from '@angular/material/icon';
import {ReactiveFormsModule} from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatInputModule} from '@angular/material/input';
import {LogoutComponent} from './logout/logout.component';
import {AuthInterceptor} from './core/auth.interceptor';
import {MatTableModule} from '@angular/material/table';
import {MatSortModule} from '@angular/material/sort';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {CentralColumnModule} from './utils/central-column/central-column.module';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {HomeComponent} from './components/Common/home/home.component';

import {MatStepperModule} from '@angular/material/stepper';
import {MatTabsModule} from '@angular/material/tabs';

import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatMenuModule} from '@angular/material/menu';
import {MatChipsModule} from '@angular/material/chips';
import {ManagerHomeComponent} from './components/Manager/manager-home/manager-home.component';
import {KitchenHomeComponent} from './components/Kitchen/kitchen-home/kitchen-home.component';
import {WaiterHomeComponent} from './components/Waiter/waiter-home/waiter-home.component';
import {ClientHomeComponent} from './components/Client/client-home/client-home.component';
import {SetMenuComponent} from './components/Manager/set-menu/set-menu.component';
import {SetTablesComponent} from './components/Manager/set-tables/set-tables.component';
import { ManageBillComponent } from './components/Client/manage-bill/manage-bill.component';
import { MenuReadonlyComponent } from './components/Common/menu-readonly/menu-readonly.component';
import { BillAssociationDialogComponent } from './components/Client/client-home/bill-association-dialog/bill-association-dialog.component';
import { MenuComponent } from './components/Common/menu/menu.component';
import { AddMenuElementBottomSheetComponent } from './components/Common/menu/add-menu-element-bottom-sheet/add-menu-element-bottom-sheet.component';
import {MatBottomSheet, MatBottomSheetModule} from '@angular/material/bottom-sheet';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    LogoutComponent,
    HomeComponent,
    ManagerHomeComponent,
    KitchenHomeComponent,
    WaiterHomeComponent,
    ClientHomeComponent,
    SetMenuComponent,
    SetTablesComponent,
    ManageBillComponent,
    MenuReadonlyComponent,
    BillAssociationDialogComponent,
    MenuComponent,
    AddMenuElementBottomSheetComponent,

  ],
  imports: [
    BrowserModule,
    MatFormFieldModule,
    MatButtonModule,
    MatSidenavModule,
    HttpClientModule,
    MatCardModule,
    MatToolbarModule,
    BrowserAnimationsModule,
    MatListModule,
    AppRoutingModule,
    MatRippleModule,
    MatIconModule,
    ReactiveFormsModule,
    MatSnackBarModule,
    MatInputModule,
    MatDialogModule,
    MatTableModule,
    MatSortModule,
    MatButtonToggleModule,
    MatSlideToggleModule,
    MatSelectModule,
    MatProgressBarModule,
    CentralColumnModule,
    MatExpansionModule,
    MatCheckboxModule,
    MatStepperModule,
    MatTabsModule,
    MatAutocompleteModule,
    MatMenuModule,
    MatChipsModule,
    MatListModule,
    MatBottomSheetModule,
  ],
  providers: [{provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
