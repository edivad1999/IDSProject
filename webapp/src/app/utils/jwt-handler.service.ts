import {Observable, zip} from 'rxjs';
import {map} from 'rxjs/operators';
import {Injectable} from '@angular/core';
import {StoreService} from "./store.service";

@Injectable({
  providedIn: 'root'
})
export class JwtHandlerService {

  constructor(
    private storeService: StoreService
  ) {
  }

  store(jwt: string, expAt: number): Observable<boolean> {
    return zip(
      this.storeService.save('authToken', jwt,true),
      this.storeService.save('authTokenExpAt', expAt,true),
    ).pipe(
      map(results => results.every(v => v))
    );
  }

  isExpired(): Observable<boolean> {
    return zip(
      this.storeService.retrieve<string>('authToken'),
      this.storeService.retrieve<number>('authTokenExpAt'),
    ).pipe(
      map(([jwt, expAt]) => {

        return (expAt != null && jwt != null) ? expAt < Math.round(Date.now() / 1000) : true;
      })
    );
  }

  token(): Observable<string | null> {
    return this.storeService.retrieve<string>('authToken');
  }

  remove(): Observable<boolean> {
    return zip(
      this.storeService.delete('authToken'),
      this.storeService.delete('authTokenExpAt')
    ).pipe(
      map(results => results.every(v => v))
    );
  }

}
