import {Inject, Injectable} from '@angular/core';
import {DOCUMENT} from "@angular/common";

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
    //rework to enable usage in local network
    if (document.location.hostname.startsWith("192")) {
      this.hostname = document.location.hostname
    } else if (document.location.hostname !== "localhost") {
      //this.hostname = Insert backend url
      this.httpProtocol = 'https'
      this.port = 80

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

}
