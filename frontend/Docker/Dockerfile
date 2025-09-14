FROM node:24

WORKDIR /app

COPY package*.json ./
RUN npm install
RUN npm install @angular/cli

CMD ["npm", "run", "start"]