import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {map, mergeMap} from 'rxjs/operators';
import {JwtHandlerService} from "../utils/jwt-handler.service";
import {Injectable} from "@angular/core";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private jwtHandler: JwtHandlerService) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (request.url.includes('api/')) {
      return this.jwtHandler.isExpired().pipe(
        mergeMap(isExpired => !isExpired ? this.jwtHandler.token() : of(null)),
        map(token => !!token ? this.cloneWithToken(request, token) : request),
        mergeMap(r => next.handle(r))
      );
    } else {
      return next.handle(request);
    }
  }

  // noinspection JSMethodCanBeStatic
  private cloneWithToken(request: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    return request.clone({setHeaders: {Authorization: `Bearer ${token}`}});
  }

}
