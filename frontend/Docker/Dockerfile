FROM node:24

WORKDIR /app

COPY package*.json ./
RUN npm install

CMD ["npm", "run", "start", "--", "--host", "0.0.0.0"]