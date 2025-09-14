import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from './auth';

// protects protects routes from unauthorized acces

// How it works:
// 1. Route Navigation Attempt: When a user tries to navigate to a protected route (e.g., /home, /profile)
// 2. Guard Intercepts: Before Angular loads the component, the guard function executes
// 3. Authentication Check: The guard checks if the user is authenticated by:
//    - Checking if a valid JWT token exists in localStorage
//    - Verifying the user's login status through the Auth service
//  4. Decision Making:
//    - If authenticated → Returns true → User proceeds to the requested route
//    - If NOT authenticated → Returns false or a UrlTree → User is redirected to login page
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(Auth); // get the auth service
  const router = inject(Router); // get the router service 

  if(auth.isLoggedIn()) {
    return true;
  }

  // not logged in: redirect to login
  return router.createUrlTree(['/login'], {
    queryParams: { redirectUrl: state.url }  // Save where they wanted to go
  });
};
