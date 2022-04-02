import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {JwtHandlerService} from '../../utils/jwt-handler.service';
import {Endpoints} from '../endpoints/endpoints';
import {HttpClient} from '@angular/common/http';
import {AuthTokenData, SimpleStringResponse} from '../../data/requests';
import {Bill, Course, Dish, DishState, MenuElement, Role, Table, User} from '../../domain/model/data';


@Injectable({
  providedIn: 'root'
})
export class DatasourceService {

  constructor(
    private endpoints: Endpoints,
    private jwtHandler: JwtHandlerService,
    private httpClient: HttpClient) {
  }

  loginWithEmailAndPassword(username: string, password: string): Observable<AuthTokenData> {

    return this.httpClient.post<AuthTokenData>(
      this.endpoints.loginWithEmailAndPasswordUrl(),
      {username: btoa(username), password: btoa(password)}/*,{headers}*/
    );
  }


  verifyToken(): Observable<boolean> {
    return this.httpClient.get<string>(this.endpoints.verifyTokenUrl(), {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }
  whoAmI(): Observable<Role> {
    return this.httpClient.get(
      this.endpoints.whoAmIUrl(),
      {observe: 'response'}
    ).pipe(
      map(
        response => {
          // @ts-ignore
          return ((response.body as SimpleStringResponse).responseString  as Role);
        })
    );
  }
  getBill(): Observable<Bill | null> {
    return this.httpClient.get(
      this.endpoints.getBillUrl(),
      {observe: 'response'}
    ).pipe(
      map(response => response.body as Bill),
      catchError(err => {
        console.error(err);
        return of(null);
      })
    );
  }

  joinTable(tableNumber: number, secretCode: string): Observable<boolean> {
    return this.httpClient.post(this.endpoints.joinTableUrl(),
      {tableNumber, secretCode},
      {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  getMenu(): Observable<MenuElement[]> {
    return this.httpClient.get(
      this.endpoints.getMenuUrl(), {observe: 'response'}
    ).pipe(
      map(it => it.body as MenuElement[])
    );
  }

  editDish(toEditId: string, editedDish: Dish): Observable<Dish> {
    return this.httpClient.post(
      this.endpoints.editDishUrl(),
      {
        toEditId, editedDish
      }, {observe: 'response'}
    ).pipe(
      map(it => it.body as Dish)
    );
  }

  addToCourse(dish: Dish, courseId: string): Observable<boolean> {
    return this.httpClient.post(
      this.endpoints.addToCourseUrl(),
      {dish, courseId}, {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  removeDish(dishId: string): Observable<boolean> {
    return this.httpClient.get(this.endpoints.removeDishUrl(dishId), {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  setReady(courseId: string): Observable<boolean> {
    return this.httpClient.get(this.endpoints.setReadyUrl(courseId), {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  // waiter apis
  billList(): Observable<Bill[]> {
    return this.httpClient.get<Bill[]>(this.endpoints.billListUrl());
  }

  closeBill(billId: string): Observable<boolean> {
    return this.httpClient.get(this.endpoints.closeBillUrl(billId), {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  openBill(tableNumber: number, coveredNumber: number): Observable<string | null> {
    return this.httpClient.get(this.endpoints.openBillUrl(tableNumber, coveredNumber), {observe: 'response'}).pipe(
      map(response => (response.body as SimpleStringResponse).responseString),
      catchError(err => {
        console.error(err);
        return of(null);
      })
    );
  }

  waiterGetBill(billId: string): Observable<Bill> {
    return this.httpClient.get<Bill>(this.endpoints.waiterGetBillUrl(billId));
  }

  waiterAddToCourse(dish: Dish, courseId: string, billId: string): Observable<boolean> {
    return this.httpClient.post(this.endpoints.waiterAddToCourseUrl(),
      {dish, courseId, billId},
      {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  waiterEditDish(toEditId: string, editedDish: Dish): Observable<Dish> {
    return this.httpClient.post(
      this.endpoints.waiterEditDishUrl(),
      {
        toEditId, editedDish
      }, {observe: 'response'}
    ).pipe(
      map(it => it.body as Dish)
    );
  }

  waiterRemoveDish(dishId: string): Observable<boolean> {
    return this.httpClient.get(this.endpoints.waiterRemoveDishUrl(dishId), {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  forceSetReady(courseId: string): Observable<boolean> {
    return this.httpClient.get(this.endpoints.forceSetReadyUrl(courseId), {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  // manager apis

  getAllTables(): Observable<Table[]> {
    return this.httpClient.get<Table[]>(this.endpoints.getAllTablesUrl());
  }

  setTables(maxTables: number): Observable<boolean> {
    return this.httpClient.post(this.endpoints.setTablesUrl(), {maxTables}, {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  setMenu(menuElements: MenuElement[]): Observable<boolean> {
    return this.httpClient.post(this.endpoints.setMenuUrl(), {menuElements}, {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }

  // kitchen api
  getCourses(): Observable<Course[]> {
    return this.httpClient.get<Course[]>(this.endpoints.getCoursesUrl());
  }

  getCoursesByTable(tableId: string): Observable<Course[]> {
    return this.httpClient.get<Course[]>(this.endpoints.getCoursesByTableUrl(tableId));

  }

  getOpenCourses(): Observable<Course[]> {
    return this.httpClient.get<Course[]>(this.endpoints.getOpenCoursesUrl());

  }

  editDishState(dishId: string, newState: DishState): Observable<boolean> {
    return this.httpClient.post(this.endpoints.editDishStateUrl(), {dishId, newState}, {observe: 'response'}).pipe(
      map((response) => response.status === 200),
      catchError(err => {
        console.error(err);
        return of(false);
      })
    );
  }


}
