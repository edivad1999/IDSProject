import {Observable, of} from 'rxjs';
import {IDBPDatabase, openDB} from 'idb';
import {fromPromise} from 'rxjs/internal-compatibility';
import {catchError, map, mergeMap} from 'rxjs/operators';
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class StoreService {

  db(): Observable<IDBPDatabase> {
    return fromPromise(openDB('app', 3, {
      upgrade(database: IDBPDatabase<any>) {
        database.createObjectStore('store');
      }
    }));
  }

  save(key: string | number, value: any, override: boolean = false): Observable<boolean> {
    return this.db().pipe(
      mergeMap(db => override ? db.put('store', value, key) : db.add('store', value, key)),
      map(_ => true),
      catchError(_ => of(false))
    );
  }

  retrieve<T>(key: string | number): Observable<T | null> {
    return this.db().pipe(
      mergeMap(db => db.get('store', key)),
      map(v => v ? v : null),
      catchError(err => {
        console.error(err);
        return of(null);
      })
    );
  }

  delete(key: string | number): Observable<boolean> {
    return this.db().pipe(
      mergeMap(db => db.delete('store', key)),
      map(_ => true),
      catchError(_ => of(false))
    );
  }

}

