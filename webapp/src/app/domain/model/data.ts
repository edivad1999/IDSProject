export interface ErrorMessageResponse {
  error: string;
}

export type AuthState = 'AUTHENTICATING' | 'AUTHENTICATED' | 'UNAUTHENTICATED';

export enum Role {
  CLIENT, KITCHEN, WAITER, MANAGER

}

export type DishState = 'WAITING' | 'PREPARING' | 'DELIVERED' | 'PROBLEM';


export interface User {
  username: string;
  role: Role;
  email: string;
  billHistory: Bill[];

}

export interface SimpleUser {
  username: string;
  role: Role;
  email: string;
}

export interface Bill {
  secretCode: string;
  coveredNumbers: number;
  openedAt: number;
  closedAt?: number;
  relatedTable: Table;
  users: SimpleUser[];
  courses: Course[];
}

export interface Table {
  number: number;
  isOccupied: boolean;
}

export interface Course {
  number: number;
  isSent: boolean;
  sentAt?: number;
  readyClients: SimpleUser[];
  dishes: Dish[];
}

export interface Dish {
  uuid: string;
  notes: string;
  relatedClient: SimpleUser | null;
  menuElement: MenuElement;
  state: DishState;
}

export interface MenuElement {
  uuid: string;
  name: string;
  ingredients: string;
  description: string;
  price: number;

}
