<div align="center">

# CS320 E-Commerce Project

A full-stack e-commerce web application with a Java/Quarkus backend and a React frontend, packaged as a single deployable unit and built to run serverlessly on AWS.

Built for CS 320 — Web Application Development.

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.14.4-4695EB?style=flat&logo=quarkus&logoColor=white)](https://quarkus.io/)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=flat&logo=react&logoColor=black)](https://react.dev/)
[![React Router](https://img.shields.io/badge/React_Router-7-CA4245?style=flat&logo=reactrouter&logoColor=white)](https://reactrouter.com/)
[![pnpm](https://img.shields.io/badge/pnpm-11-F69220?style=flat&logo=pnpm&logoColor=white)](https://pnpm.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![AWS](https://img.shields.io/badge/AWS-Lambda_·_RDS_·_S3-FF9900?style=flat&logo=amazonaws&logoColor=white)](https://aws.amazon.com/)

</div>

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [API reference](#api-reference)
- [Building for production](#building-for-production)
- [Deployment](#deployment)

## Features

- Product catalog — browse products with images, descriptions, pricing, and live stock quantities
- Accounts — registration, login, and password reset
- Cart & checkout — add items to a cart and place an order
- Guest checkout — place an order without an account, tracked by a unique guest tracking ID and email
- Order lifecycle — pending, completed, canceled, and refunded statuses

## Tech stack

**Backend**
- [Quarkus](https://quarkus.io/) (Java 17) with RESTEasy Reactive as the REST layer
- Hibernate ORM with Panache for data access
- MySQL, via Amazon RDS in production
- [`jbcrypt`](https://github.com/jeremyh/jBCrypt) for password hashing
- [`quarkus-amazon-lambda-http`](https://quarkus.io/guides/amazon-lambda-http) to run the whole backend as an AWS Lambda function behind API Gateway

**Frontend**
- React 18 (Create React App) + React Router v7
- [pnpm](https://pnpm.io/) as the package manager
- Axios for API calls

**Integration**
- [Quinoa](https://quarkiverse.github.io/quarkiverse-docs/quarkus-quinoa/dev/index.html) builds and serves the React app as part of the Quarkus build — one command, one artifact, no separate frontend server to manage

**Cloud infrastructure (AWS)**

| Service | Role |
|---|---|
| Amazon RDS (MySQL) | Application database |
| AWS Lambda + API Gateway | Hosts the backend REST API |
| Amazon S3 | Hosts product images |
| AWS Amplify Hosting | Hosts the frontend (alternate/parallel deployment path) |

## Architecture

```
Browser
   │
   ├─ static assets ──────────────► AWS Amplify Hosting (React build)
   │
   └─ /api/* requests ────────────► API Gateway ──► AWS Lambda (Quarkus backend)
                                                          │
                                                          ├─► Amazon RDS (MySQL)
                                                          └─► Amazon S3 (product images)
```

Locally, Quinoa collapses all of this into a single process: `./mvnw quarkus:dev` runs the Quarkus backend and proxies frontend requests to a live-reloading React dev server, all behind one port.

## Project structure

```
cs320-project-or/
├── src/main/java/org/acme/       Backend: JPA entities + REST resources
├── src/main/resources/           application.properties
├── src/main/frontend/            React app (managed by Quinoa)
│   ├── src/pages/                Home, Products, Account, Cart
│   └── src/components/           Navbar, product tiles, featured slider
└── pom.xml
```

## Getting started

**Prerequisites**
- Java 17
- [pnpm](https://pnpm.io/) installed and available on `PATH`
- A MySQL-compatible database (local MySQL or an AWS RDS instance)

**1. Configure the database**

Create a `.env` file in the project root (already gitignored) with your database credentials:

```env
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
```

`application.properties` reads these as `${DB_USERNAME}` / `${DB_PASSWORD}` — credentials are never hardcoded or committed.

**2. Run in dev mode**

```shell
./mvnw quarkus:dev
```

Starts the backend and the React dev server together via Quinoa at `http://localhost:8080`, with live reload on both sides.

## API reference

All backend endpoints live under `/api`, kept separate from frontend page routes so the two never collide:

| Resource | Base path | Notes |
|---|---|---|
| Products | `/api/products` | Full CRUD for the product catalog |
| Users | `/api/users` | Register, login, reset-password, CRUD |
| Orders | `/api/orders` | Full CRUD; supports guest orders via tracking ID |

## Building for production

```shell
./mvnw clean package
```

Quinoa runs `pnpm install && pnpm run build` and bundles the optimized React build into the final artifact alongside the backend. This also produces the AWS Lambda deployment artifacts (`target/function.zip`, `target/sam.jvm.yaml`).

## Deployment

The backend deploys as an AWS Lambda function behind an API Gateway HTTP API using [AWS SAM](https://aws.amazon.com/serverless/sam/):

```shell
sam deploy --template-file target/sam.jvm.yaml --stack-name cs320-backend --capabilities CAPABILITY_IAM --resolve-s3
```

The frontend can additionally be deployed to AWS Amplify Hosting, connected directly to this repository.

---

<div align="center">

Built with [Quarkus](https://quarkus.io/) and [Quinoa](https://quarkiverse.github.io/quarkiverse-docs/quarkus-quinoa/dev/index.html)

</div>
