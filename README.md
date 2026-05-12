# 🍔 Cuesta Cougars Food Delivery

A full-stack web application for a campus food delivery service, built with **Spring Boot** and **Thymeleaf**. Originally developed as a Java console application for a CS course at Cuesta College, then converted to a browser-accessible web app.

## Live Demo

> 🔗 _Deploy link here (see [Deployment](#deployment))_

## Features

### Customer
- Browse the menu and place orders (multi-item selection)
- View full order history with live status badges
- Rate drivers after delivery

### Driver
- View assigned orders with item breakdown and delivery address
- Mark orders as **Picked Up** and then **Delivered**
- Live availability status and average rating display

### Admin
- Add, update prices, and remove menu items in real time
- View all orders across the system with status tracking

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 · Spring Boot 3.5 |
| Templating | Thymeleaf |
| Styling | Tailwind CSS (CDN) |
| Persistence | File-based (`.txt` flat files) |
| Build | Maven Wrapper (`mvnw`) |

## Running Locally

**Prerequisites:** Java 21+

```bash
git clone https://github.com/YOUR_USERNAME/cuesta-cougars-web.git
cd cuesta-cougars-web
./mvnw spring-boot:run
```

Then open **http://localhost:8080** in your browser.

### Demo Accounts

| Role | Username | Password |
|---|---|---|
| Customer | `bob` | `bob` |
| Driver | `mary` | `mary` |
| Admin | `rhoo` | `rhoo` |

> You can also register a new account from the login page.

## Project Structure

```
src/main/java/com/cuestacougars/
├── config/          # Web configuration (RelativeRedirectFilter)
├── controller/      # AuthController, CustomerController, DriverController, AdminController
├── model/           # User, Customer, Driver, Admin, Order, MenuItem, GlobalData, FileManager…
└── service/         # DeliveryService (business logic layer)

src/main/resources/
├── templates/       # Thymeleaf HTML templates
│   ├── login.html
│   ├── register.html
│   ├── customer/dashboard.html
│   ├── driver/dashboard.html
│   └── admin/dashboard.html
└── application.properties

data/                # Seed data loaded at startup
├── menu.txt
├── customers.txt
├── drivers.txt
├── admins.txt
└── orders.txt
```

## Deployment

This app is ready to deploy on [Railway](https://railway.app):

1. Push this repo to GitHub
2. Go to [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub repo**
3. Select this repository — Railway auto-detects Maven and builds it
4. Once deployed, copy the public URL and update the Live Demo link above

> **Note:** The app uses flat-file persistence. Data resets on each new deployment (files are re-read from the committed `data/` folder). For production use, swap `FileManager` for a database like PostgreSQL.

## Original Project

This web app is a conversion of a Java console application built for CS courses at Cuesta College. The core business logic (data structures, order processing, driver prioritization) is preserved from the original.
