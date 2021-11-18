import {Injectable} from '@angular/core';
import {DatasourceService} from '../../core/datasource/datasource.service';
import {Observable, of, ReplaySubject} from 'rxjs';
import {AuthState} from '../../domain/model/data';
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

}
