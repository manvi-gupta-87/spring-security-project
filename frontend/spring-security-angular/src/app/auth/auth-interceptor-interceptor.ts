import { HttpInterceptorFn } from '@angular/common/http';

// . Intercepts every HTTP request
//  2. Checks if JWT token exists in localStorage
//  3. If token exists, adds Authorization: Bearer <token> header
//  4. Passes request to backend
export const authInterceptorInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip auth header for public endpoints
  const publicEndpoints = ['/api/token', '/api/users/register', '/api/token/refresh'];
  const isPublicEndpoint = publicEndpoints.some(endpoint => req.url.includes(endpoint));
  
  if (isPublicEndpoint) {
    return next(req);
  }
  
  const token = localStorage.getItem('access_token');
  if(token) {
    const cloneRequest = req.clone({ 
      setHeaders: {
          Authorization: `Bearer ${token}`
        }
    })
    return next(cloneRequest);
  }
  // Pass through original request if no token
  return next(req);
};
