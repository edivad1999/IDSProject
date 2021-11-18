import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {JwtHandlerService} from '../../utils/jwt-handler.service';
import {Endpoints} from '../endpoints/endpoints';
import {HttpClient} from '@angular/common/http';
import {AuthTokenData} from '../../data/requests';


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


}
