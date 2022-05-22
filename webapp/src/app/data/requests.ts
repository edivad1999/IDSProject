export interface AuthTokenData {
  jwt: string;
  expAt: number;
}

export interface SimpleStringResponse {
  responseString: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  mail: string;
}
