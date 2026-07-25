FROM node:22-alpine AS build
WORKDIR /app

# dependências primeiro, para aproveitar a cache enquanto só muda o código (mesma ideia do Dockerfile do backend)
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80