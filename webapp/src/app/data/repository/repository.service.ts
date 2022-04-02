import {Injectable} from '@angular/core';
import {DatasourceService} from '../../core/datasource/datasource.service';
import {Observable, of, ReplaySubject} from 'rxjs';
import {AuthState, Bill, Course, Dish, DishState, MenuElement, Role, Table, User} from '../../domain/model/data';
import {catchError, map, mergeMap, tap} from 'rxjs/operators';
import {AuthTokenData} from '../requests';
import {JwtHandlerService} from '../../utils/jwt-handler.service';

@Injectable({
  providedIn: 'root'
})
export class RepositoryService {
  authenticationStateFlow = new ReplaySubject<AuthState>(1);


  constructor(private datasource: DatasourceService,
              private jwtHandlerService: JwtHandlerService
  ) {
    jwtHandlerService.token().subscribe(t => {
      this.authenticationStateFlow.next(t ? 'AUTHENTICATED' : 'UNAUTHENTICATED');
    });


  }

  loginWithEmailAndPassword(username: string, password: string): Observable<boolean> {
    return this.handleLoginFlow(this.datasource.loginWithEmailAndPassword(username, password));
  }

  verifyJWTSession(): Observable<boolean> {
    return this.datasource.verifyToken().pipe(
      mergeMap(r => r ? of(r) : this.logOut().pipe(map(_ => false)))
    );
  }

  logOut(): Observable<boolean> {
    return this.jwtHandlerService.remove().pipe(
      tap(_ => {
        this.authenticationStateFlow.next('UNAUTHENTICATED');
      }),
    );
  }


  private handleLoginFlow(flow: Observable<AuthTokenData>): Observable<boolean> {
    this.authenticationStateFlow.next('AUTHENTICATING');
    return flow.pipe(
      mergeMap(data => this.jwtHandlerService.store(data.jwt, data.expAt)),
      catchError(err => {
        console.error(err);
        return of(false);
      }),
      mergeMap(isTokenStored => isTokenStored ? this.datasource.verifyToken() : of(false)),
      tap((success: boolean) => {
        if (!success) {
          this.logOut();
        } else {
        }
        this.authenticationStateFlow.next(success ? 'AUTHENTICATED' : 'UNAUTHENTICATED');
      }),
      map(u => !!u)
    );
  }

  whoAmI(): Observable<Role> {
    return this.datasource.whoAmI();
  }
  getBill(): Observable<Bill | null> {
    return this.datasource.getBill();
  }

  joinTable(tableNumber: number, secretCode: string): Observable<boolean> {
    return this.datasource.joinTable(tableNumber, secretCode);

  }

  getMenu(): Observable<MenuElement[]> {
    return this.datasource.getMenu();

  }

  editDish(toEditId: string, editedDish: Dish): Observable<Dish> {
    return this.datasource.editDish(toEditId, editedDish);

  }

  addToCourse(dish: Dish, courseId: string): Observable<boolean> {
    return this.datasource.addToCourse(dish, courseId);

  }

  removeDish(dishId: string): Observable<boolean> {
    return this.datasource.removeDish(dishId);

  }

  setReady(courseId: string): Observable<boolean> {
    return this.datasource.setReady(courseId);

  }

  // waiter apis
  billList(): Observable<Bill[]> {
    return this.datasource.billList();

  }

  closeBill(billId: string): Observable<boolean> {
    return this.datasource.closeBill(billId);

  }

  openBill(tableNumber: number, coveredNumber: number): Observable<string | null> {
    return this.datasource.openBill(tableNumber, coveredNumber);

  }

  waiterGetBill(billId: string): Observable<Bill> {
    return this.datasource.waiterGetBill(billId);

  }

  waiterAddToCourse(dish: Dish, courseId: string, billId: string): Observable<boolean> {
    return this.datasource.waiterAddToCourse(dish, courseId, billId);

  }

  waiterEditDish(toEditId: string, editedDish: Dish): Observable<Dish> {
    return this.datasource.waiterEditDish(toEditId, editedDish);

  }

  waiterRemoveDish(dishId: string): Observable<boolean> {
    return this.datasource.waiterRemoveDish(dishId);

  }

  forceSetReady(courseId: string): Observable<boolean> {
    return this.datasource.forceSetReady(courseId);

  }

  // manager apis

  getAllTables(): Observable<Table[]> {
    return this.datasource.getAllTables();

  }

  setTables(maxTables: number): Observable<boolean> {
    return this.datasource.setTables(maxTables);

  }

  setMenu(menuElements: MenuElement[]): Observable<boolean> {
    return this.datasource.setMenu(menuElements);

  }

  // kitchen api
  getCourses(): Observable<Course[]> {
    return this.datasource.getCourses();

  }

  getCoursesByTable(tableId: string): Observable<Course[]> {
    return this.datasource.getCoursesByTable(tableId);


  }

  getOpenCourses(): Observable<Course[]> {
    return this.datasource.getOpenCourses();


  }

  editDishState(dishId: string, newState: DishState): Observable<boolean> {
    return this.datasource.editDishState(dishId, newState);

  }

}
