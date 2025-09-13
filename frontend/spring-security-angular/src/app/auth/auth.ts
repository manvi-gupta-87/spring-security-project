import { inject, Injectable } from '@angular/core'; // makes this service available for dependency injection
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';
//This service is used to authenticate the user
// it will be used to login, logout, register, refresh token, etc.
@Injectable({
  providedIn: 'root'
})
export class Auth {
  private apiUrl = 'http://localhost:8080/api';
  private tokenSubject = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {
    // check if there is a token in the local storage
    const token = localStorage.getItem('access_token');
    if (token) {
      this.tokenSubject.next(token);
    }
  }

  login(username: string, password: string) {
    return this.http.post<any>(`${this.apiUrl}/token`, { username, password })
    .pipe(
      tap((response) => {
        localStorage.setItem('access_token', response.access_token);
        localStorage.setItem('refresh_token', response.refresh_token);
        this.tokenSubject.next(response.access_token);
      })
    );
  }

  logout() {
    return this.http.post<any>(`${this.apiUrl}/logout`, {})
    .pipe(
      tap(() => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        this.tokenSubject.next(null);
      })
    );
  }

  getToken() {
    return this.tokenSubject.asObservable();
  }
  
  isLoggedIn() {
    return this.tokenSubject.value !== null;
  }

  refreshToken() {
    return this.http.post<any>(`${this.apiUrl}/token/refresh`, { refresh_token: localStorage.getItem('refresh_token') })
    .pipe(
      tap((response) => {
        localStorage.setItem('access_token', response.access_token);
        localStorage.setItem('refresh_token', response.refresh_token);
        this.tokenSubject.next(response.access_token);
      })
    );
  }
  
}
