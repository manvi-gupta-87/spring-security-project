import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { Auth } from './auth';

// . Intercepts every HTTP request
//  2. Checks if JWT token exists in localStorage
//  3. If token exists, adds Authorization: Bearer <token> header
//  4. Handles 401 errors by attempting token refresh
//  5. Passes request to backend
export const authInterceptorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const router = inject(Router);
  
  // Skip auth header for public endpoints
  const publicEndpoints = ['/api/token', '/api/users/register', '/api/token/refresh'];
  const isPublicEndpoint = publicEndpoints.some(endpoint => req.url.includes(endpoint));
  
  if (isPublicEndpoint) {
    return next(req);
  }
  
  const token = localStorage.getItem('access_token');
  let authReq = req;
  
  if(token) {
    authReq = req.clone({ 
      setHeaders: {
          Authorization: `Bearer ${token}`
        }
    });
  }
  
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // If we get a 401 and we have a refresh token, try to refresh
      if (error.status === 401 && localStorage.getItem('refresh_token')) {
        // Don't try to refresh on refresh endpoint itself
        if (req.url.includes('/api/token/refresh')) {
          localStorage.clear();
          router.navigate(['/login']);
          return throwError(() => error);
        }
        
        return auth.refreshToken().pipe(
          switchMap(() => {
            // Retry the original request with new token
            const newToken = localStorage.getItem('access_token');
            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newToken}`
              }
            });
            return next(retryReq);
          }),
          catchError((refreshError) => {
            // Refresh failed, clear storage and redirect to login
            localStorage.clear();
            router.navigate(['/login']);
            return throwError(() => refreshError);
          })
        );
      }
      
      return throwError(() => error);
    })
  );
};
