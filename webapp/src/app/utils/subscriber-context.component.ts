import {Component, OnDestroy} from '@angular/core';
import {Observable, Subscription} from 'rxjs';

@Component({template: ''})
export abstract class SubscriberContextComponent implements OnDestroy {

  private subs: Subscription[] = [];

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  subscribeWithContext<T>(o: Observable<T>, action: ((value: T) => void) = (_ => {})): void {
    this.subs.push(o.subscribe(action));
  }

}
