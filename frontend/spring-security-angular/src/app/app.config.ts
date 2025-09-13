import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptorInterceptor } from './auth/auth-interceptor-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([authInterceptorInterceptor])
    ), // This enables:
              //- HttpClient to work in your Auth service
              //- API calls to your Spring Boot backend
              //- Preparation for adding JWT interceptor
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes)
  ]
};
