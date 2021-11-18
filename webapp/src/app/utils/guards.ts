import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {Injectable} from '@angular/core';
import {RepositoryService} from "../data/repository/repository.service";
import {MatSnackBar} from "@angular/material/snack-bar";


@Injectable({
  providedIn: 'any'
})
export class AuthGuard implements CanActivate {

  constructor(
    private repo: RepositoryService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
  }

  canActivate(
    route: ActivatedRouteSnapshot, state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.repo.verifyJWTSession().pipe(
      map(s => s ? true : this.router.createUrlTree(
        ['login']))
    )
      ;
  }

}

@Injectable({
  providedIn: 'any'
})
export class UnauthGuard implements CanActivate {

  constructor(
    private repo: RepositoryService,
    private router: Router,
  ) {
  }

  canActivate(
    route: ActivatedRouteSnapshot, state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.repo.verifyJWTSession().pipe(
      map(s => !s ? true : this.router.createUrlTree(['home']))
    );
  }

}
