
# Stage 1: Build the app
FROM node:20-alpine AS build
WORKDIR /app
# Copy package files first (for caching)
COPY package*.json ./
RUN npm ci

 # Copy source code
 COPY . .

 # Build Angular app
 RUN npm run build


# Stage 2: Serve with nginx
FROM nginx:alpine

# Copy built files from stage 1
COPY --from=build /app/dist/spring-security-angular/browser /usr/share/nginx/html

# Configure nginx for Angular routing
RUN echo 'server { \
      listen 80; \
      location / { \
          root /usr/share/nginx/html; \
          try_files $uri $uri/ /index.html; \
      } \
  }' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]



