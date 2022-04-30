import {Inject, Injectable} from '@angular/core';
import {DOCUMENT} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class Endpoints {
  httpProtocol = 'http';
  wsProtocol = 'ws';
  hostname = 'localhost';
  port = 42069;
  basePath = 'api';

  constructor(@Inject(DOCUMENT) private document: Document) {
    // rework to enable usage in local network
    if (document.location.hostname.startsWith('192')) {
      this.hostname = document.location.hostname;
    } else if (document.location.hostname !== 'localhost') {
      // this.hostname = Insert backend url
      this.httpProtocol = 'https';
      this.port = 80;

    }

  }

  verifyTokenUrl(): string {
    return this.buildUrl('verifyToken');
  }

  loginWithEmailAndPasswordUrl(): string {
    return this.buildUrl('login');
  }


  protected buildUrl(finalPath: string, type: 'ws' | 'http' = 'http'): string {
    let url = `${type === 'http' ? this.httpProtocol : this.wsProtocol}://${this.hostname}`;
    url += this.port === 80 ? `` : `:${this.port}`;
    if (!this.basePath.startsWith('/')) {
      url += '/';
    }
    url += this.basePath;
    if (!finalPath.startsWith('/')) {
      url += '/';
    }
    url += finalPath;
    return url;
  }

  whoAmIUrl(): string {
    return this.buildUrl('clients/whoAmI');
  }

  // client apis endpoint

  getBillUrl(): string {
    return this.buildUrl(`clients/getBill`);
  }

  joinTableUrl(): string {
    return this.buildUrl(`clients/joinTable`);
  }

  getMenuUrl(): string {
    return this.buildUrl(`clients/getMenu`);
  }

  editDishUrl(): string {
    return this.buildUrl(`clients/editDish`);
  }

  addToCourseUrl(): string {
    return this.buildUrl(`clients/addToCourse`);
  }

  removeDishUrl(dishId: string): string {
    return this.buildUrl(`clients/removeDish?dishId=${dishId}`);
  }

  setReadyUrl(courseId: string): string {
    return this.buildUrl(`clients/setReady?courseId=${courseId}`);
  }

  // waiter apis
  billListUrl(): string {
    return this.buildUrl(`waiter/billList`);
  }

  closeBillUrl(billId: string): string {
    return this.buildUrl(`waiter/closeBill?billId=${billId}`);
  }

  openBillUrl(tableNumber: number, coveredNumber: number): string {
    return this.buildUrl(`waiter/openBill?tableNumber=${tableNumber}&coveredNumber=${coveredNumber}`);
  }

  waiterGetBillUrl(billId: string): string {
    return this.buildUrl(`waiter/getBill?billId=${billId}`);
  }

  waiterAddToCourseUrl(): string {
    return this.buildUrl(`waiter/addToCourse`);
  }

  waiterEditDishUrl(): string {
    return this.buildUrl(`waiter/editDish`);
  }

  waiterRemoveDishUrl(dishId: string): string {
    return this.buildUrl(`waiter/removeDish?dishId=${dishId}`);
  }

  forceSetReadyUrl(courseId: string): string {
    return this.buildUrl(`waiter/forceSetReady?courseId=${courseId}`);
  }

  // manager apis

  getAllTablesUrl(): string {
    return this.buildUrl(`manager/getTables`);
  }

  setTablesUrl(): string {
    return this.buildUrl(`manager/setTables`);
  }

  setMenuUrl(): string {
    return this.buildUrl(`manager/setMenu`);
  }

  // kitchen api
  getCoursesUrl(): string {
    return this.buildUrl(`kitchen/courses`);
  }

  getCoursesByTableUrl(tableId: string): string {
    return this.buildUrl(`kitchen/coursesByTable?tableId=${tableId}`);
  }

  getOpenCoursesUrl(): string {
    return this.buildUrl(`kitchen/openCourses`);
  }

  editDishStateUrl(): string {
    return this.buildUrl(`kitchen/editDishState`);
  }


  getUserUrl(): string {
    return this.buildUrl(`clients/user`);
  }

  billFlowUrl(billId: string, token: string): string {
    return this.buildUrl(`clients/ws/${billId}`, 'ws');
  }
}
