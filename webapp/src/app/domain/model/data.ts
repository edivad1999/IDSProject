export interface ErrorMessageResponse {
  error: string;
}

export type AuthState = 'AUTHENTICATING' | 'AUTHENTICATED' | 'UNAUTHENTICATED';

export type Role = 'CLIENT' | 'KITCHEN' | 'WAITER' | 'MANAGER';


export function getRoleValue(role: Role): number {
  if (role === 'CLIENT') {
    return 0;
  } else if (role === 'MANAGER') {
    return 3;
  } else if (role === 'KITCHEN') {
    return 1;
  } else {
    return 2;
  }
}


export function compareRole(role1: Role, role2: Role): number {
  return getRoleValue(role1) > getRoleValue(role2) ? 1 : getRoleValue(role1) === getRoleValue(role2) ? 0 : -1;
}

export function roleIsBigger(role1: Role, role2: Role): boolean {
  return getRoleValue(role1) > getRoleValue(role2);
}

export function roleIsSmaller(role1: Role, role2: Role): boolean {
  return getRoleValue(role1) < getRoleValue(role2);
}

export function roleEquals(role1: Role, role2: Role): boolean {
  return getRoleValue(role1) === getRoleValue(role2);
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
  id: string;
  secretCode: string;
  coveredNumbers: number;
  openedAt: number;
  closedAt?: number;
  relatedTable: Table;
  users: SimpleUser[];
  courses: Course[];
}

export interface Table {
  id: string;
  number: number;
  isOccupied: boolean;
}

export interface Course {
  id: string;
  number: number;
  isSent: boolean;
  sentAt?: number;
  readyClients: SimpleUser[];
  dishes: Dish[];
}

export interface Dish {
  uuid: string;
  notes: string;
  relatedClient: SimpleUser;
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
