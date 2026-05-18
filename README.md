# 🚀 LiteFlow - Enterprise Resource Planning System

**A comprehensive ERP solution for restaurant and hospitality management with advanced POS, inventory control, workforce management, AI-powered features, and intelligent alerting capabilities.**

---
Video Demo YTB : https://www.youtube.com/watch?v=IKlgj9m4Y84

## 📘 Overview

**LiteFlow** is a full-featured Enterprise Resource Planning (ERP) system specifically designed for the restaurant and hospitality industry. Built with modern Java technologies and enterprise-grade security, LiteFlow streamlines operations across multiple business domains including point-of-sale transactions, kitchen operations, inventory tracking, employee scheduling, procurement management, AI-powered chatbots, and intelligent alert systems.

The system addresses critical pain points in restaurant management by providing:
- **Real-time order processing** with kitchen integration
- **Intelligent inventory tracking** with automated stock alerts and AI-powered forecasting
- **Workforce management** with attendance tracking, scheduling, and payroll calculation
- **Secure multi-role access control** with 2FA authentication and OAuth2 integration
- **Procurement automation** with supplier management, purchase orders, and invoice processing
- **AI-powered chatbot** for intelligent business insights and automation
- **Smart alert system** with multi-channel notifications (Email, Telegram, Slack, In-App)
- **Payment gateway integration** with VNPay for secure online payments
- **Analytics and reporting** with revenue tracking and demand forecasting

**Key Concept:** Unified platform that integrates front-of-house operations (POS, table management) with back-office functions (inventory, procurement, HR, analytics) and AI-powered features to optimize efficiency and reduce operational costs.

---

## ⚙️ Tech Stack

### Backend
- **Language:** Java 16
- **Framework:** Jakarta EE 11 (Servlets, JSP, JSTL 3.0.1)
- **ORM:** Hibernate 6.4.4 + JPA 3.1.0
- **Build Tool:** Maven 3.6+
- **Application Server:** Apache Tomcat 10+

### Database & Caching
- **Primary Database:** Microsoft SQL Server
- **JDBC Driver:** MS SQL Server JDBC 12.6.1
- **Caching Layer:** Redis (Jedis 5.1.0)

### Security & Authentication
- **Password Hashing:** BCrypt (jBCrypt 0.4)
- **JWT Authentication:** JJWT 0.11.5
- **Two-Factor Authentication:** TOTP (java-otp 0.4.0, totp 1.0)
- **OAuth2:** Google Sign-In Integration (Google API Client 2.8.0)

### AI & Machine Learning
- **AI Service:** OpenAI GPT API (OkHttp 4.12.0)
- **Configuration:** AI Agent Configuration with dynamic prompts
- **Features:** Intelligent chatbot, demand forecasting, alert summarization

### Payment Integration
- **Payment Gateway:** VNPay integration
- **Payment Methods:** Cash, Card, Transfer, Wallet, VNPay

### Additional Libraries
- **Excel Processing:** Apache POI 5.2.5
- **Email Service:** Jakarta Mail 2.0.1
- **JSON Processing:** Gson 2.10.1, Jackson Databind 2.17.2
- **HTTP Client:** OkHttp 4.12.0
- **Scheduler:** Quartz Scheduler 2.3.2
- **Environment Variables:** Dotenv Java 3.0.0

### Testing & Quality Assurance
- **Testing Framework:** JUnit 5.10.0
- **Mocking:** Mockito 5.5.0
- **Web Testing:** Selenium WebDriver 4.15.0
- **Code Coverage:** JaCoCo 0.8.10
- **In-Memory Database:** H2 Database 2.2.224 (for testing)

---

## 🧠 Key Features

### 🔐 Authentication & User Management
- Multi-role access control (Admin, Manager, Cashier, Kitchen Staff, Employee)
- Secure login with BCrypt password hashing
- Two-Factor Authentication (2FA) with TOTP
- Google OAuth2 integration
- Password recovery via email OTP
- Session management with JWT tokens
- Audit logging for security tracking

### 💰 Point of Sale (POS) System
- Real-time table/room management with status tracking
- Interactive menu browsing and order creation
- Multiple invoice management per table
- Split bill and payment processing
- Order history and session tracking
- Kitchen notification integration
- Real-time stock validation
- Discount and promotion management

### 🍳 Kitchen Management
- Real-time order queue display
- Order status workflow (Pending → Preparing → Ready → Served)
- Multi-station order distribution
- Priority order handling
- Kitchen notification system
- Order modification support

### 📦 Inventory Management
- Product catalog with variants (size, options)
- Stock level tracking with low-stock alerts
- Room/table-based inventory organization
- Dynamic pricing management
- Excel import/export functionality
- Product category management
- Stock movement tracking
- Inventory alerts and notifications

### 👥 Employee & HR Management
- Employee profile management
- Attendance tracking system
- Schedule management and shift planning
- Payroll calculation (paysheet generation)
- Employee compensation management (Fixed, Hourly, Per-Shift, Hybrid)
- Role-based access assignment
- Employee shift timesheet tracking
- Leave request management
- Forgot clock-in/out requests

### 🛒 Procurement System
- Supplier database management
- Purchase Order (PO) creation and tracking
- Automated PO creation from low-stock items
- Goods receipt recording
- Invoice matching and verification
- Procurement dashboard with analytics
- Supplier SLA tracking
- PO status tracking and alerts

### 🤖 AI-Powered Features
- **Intelligent Chatbot:** GPT-powered assistant for business insights
- **Demand Forecasting:** AI-powered sales prediction and inventory planning
- **Alert Summarization:** GPT-generated alert summaries
- **Intelligent Automation:** Automated PO creation based on stock levels
- **Revenue Analytics:** AI-powered revenue analysis and insights
- **Stock Alert Intelligence:** Smart stock alerts with recommendations

### 🔔 Smart Alert System
- **Multi-Channel Notifications:** Email, Telegram, Slack, In-App
- **Configurable Alerts:** PO pending, low inventory, revenue drop, daily summary
- **AI-Enhanced Alerts:** GPT-powered alert summarization
- **Scheduled Alerts:** Cron-based periodic alerts
- **Alert History:** Comprehensive alert tracking and management
- **User Preferences:** Customizable alert preferences per user
- **Priority Management:** Low, Medium, High, Critical priority levels

### 💳 Payment Processing
- **VNPay Integration:** Secure online payment gateway
- **Multiple Payment Methods:** Cash, Card, Transfer, Wallet, VNPay
- **Payment Transaction Tracking:** Comprehensive payment history
- **Invoice Generation:** Automated invoice creation
- **Payment Status Tracking:** Pending, Processing, Completed, Failed, Refunded

### 📊 Dashboard & Analytics
- Real-time business metrics
- Sales performance tracking
- Inventory status overview
- Employee performance monitoring
- Revenue reports with date range filtering
- Category-based revenue analysis
- Demand forecasting analytics
- Low stock alerts and recommendations

### 📝 Notice Board
- Internal notice management
- Notice creation and editing
- Notice visibility control
- Notice expiration management

### 📅 Timesheet Management
- Employee attendance tracking
- Shift scheduling and assignment
- Personal schedule management
- Leave request processing
- Forgot clock-in/out requests
- Attendance flag recalculation

### 📈 Reporting
- Revenue reports with detailed analytics
- Sales invoice management
- Daily, weekly, monthly reports
- Category-based revenue analysis
- Employee performance reports
- Inventory reports

---

## 📖 User Stories (Completed Features)

This section summarizes the fully implemented features of the system based on the perspective of the main user roles (Admin, Manager, Cashier, Kitchen Staff, Employee).

### 1. 🔐 Authentication & Authorization
* **As a user**, I can securely log into the system using my Email/Password or Google OAuth2.
* **As a user**, I am required to use Two-Factor Authentication (2FA/OTP) via email to protect my account.
* **As a user**, I can recover my password using an email OTP if I forget it.
* **As an Admin**, I can control access based on Roles (Admin, Manager, Cashier, Kitchen, Employee) to ensure staff only see features they are permitted to access.

### 2. 💰 Point of Sale (POS) & Cashier
* **As a Cashier**, I can view a real-time table/room map to know which tables are available or occupied.
* **As a Cashier**, I can create orders for customers, browse an interactive menu, and send requests directly to the kitchen.
* **As a Cashier**, I can split bills, merge tables, or apply discount codes for customers.
* **As a Cashier**, I can process payments using various methods (Cash, Card, Transfer, or VNPay online gateway) and generate invoices.

### 3. 🍳 Kitchen Display System (KDS)
* **As Kitchen Staff**, I can view a real-time order queue on a dedicated screen to see pending dishes in chronological order.
* **As Kitchen Staff**, I can update the status of dishes (Pending → Preparing → Ready) to notify cashiers and servers.
* **As Kitchen Staff**, I receive immediate notifications (visual/audio) when a new or priority order arrives.

### 4. 📦 Inventory & Procurement
* **As a Manager**, I can manage the product/ingredient catalog, variants (sizes, types), and pricing from the web UI.
* **As a Manager**, I can track real-time stock levels and receive low-stock alerts when ingredients are running out.
* **As a Manager**, I can create Purchase Orders (PO) to restock from suppliers saved in the system.
* **As a Manager**, I receive automated PO suggestions from the system when stock is depleted.

### 5. 👥 HR, Timesheet & Payroll
* **As an Employee**, I can clock in and clock out daily to record my working hours.
* **As an Employee**, I can view my work schedule, submit leave requests, or report forgotten clock-ins.
* **As a Manager**, I can schedule shifts for employees and approve or reject leave requests.
* **As a Manager/Accountant**, I can rely on the system to automatically calculate payroll based on the compensation type (fixed, hourly, per-shift) and attendance data.

### 6. 📊 Analytics & Reporting
* **As an Admin/Manager**, I can view a real-time dashboard displaying key business metrics (revenue, profit, costs).
* **As an Admin/Manager**, I can generate detailed daily/weekly/monthly reports and analyze revenue by category or employee performance.
* **As an Admin/Manager**, I can leverage AI forecasting to predict future revenue and inventory demand.

### 7. 🤖 Smart Alerts & AI Chatbot
* **As a user**, I can receive critical system notifications (stock arrivals, revenue drops, pending approvals) via multiple channels like Email, Telegram, Slack, or In-App.
* **As a Manager/Admin**, I can converse with an AI Chatbot (GPT-powered) to quickly extract report data and summarize daily business performance without manually reading metrics.
* **As a Manager**, I can receive automated AI-summarized alerts grouping important system events.

---

## 🧩 Project Structure

```
LiteFlow/
├── src/
│   ├── main/
│   │   ├── java/com/liteflow/
│   │   │   ├── controller/          # Servlets (CashierServlet, ProductServlet, etc.)
│   │   │   │   ├── admin/          # Admin controllers
│   │   │   │   ├── alert/          # Alert system controllers
│   │   │   │   ├── api/            # API endpoints (ChatBot, DemandForecast, AIAgentConfig)
│   │   │   │   ├── auth/           # Authentication controllers
│   │   │   │   ├── cashier/        # Cashier/POS controllers
│   │   │   │   ├── dashboard/      # Dashboard controllers
│   │   │   │   ├── employee/       # Employee management controllers
│   │   │   │   ├── inventory/      # Inventory controllers
│   │   │   │   ├── notice/         # Notice board controllers
│   │   │   │   ├── payment/        # Payment controllers
│   │   │   │   ├── payroll/        # Payroll controllers
│   │   │   │   ├── procurement/    # Procurement controllers
│   │   │   │   ├── report/         # Report controllers
│   │   │   │   ├── sales/          # Sales controllers
│   │   │   │   ├── schedule/       # Schedule controllers
│   │   │   │   └── timesheet/      # Timesheet controllers
│   │   │   ├── dao/                # Data Access Objects
│   │   │   │   ├── ai/             # AI configuration DAOs
│   │   │   │   ├── alert/          # Alert system DAOs
│   │   │   │   ├── analytics/      # Analytics DAOs
│   │   │   │   ├── employee/       # Employee DAOs
│   │   │   │   ├── inventory/      # Inventory DAOs
│   │   │   │   ├── notice/         # Notice DAOs
│   │   │   │   ├── payroll/        # Payroll DAOs
│   │   │   │   ├── procurement/    # Procurement DAOs
│   │   │   │   ├── report/         # Report DAOs
│   │   │   │   ├── sales/          # Sales DAOs
│   │   │   │   └── timesheet/      # Timesheet DAOs
│   │   │   ├── model/              # Entity models
│   │   │   │   ├── ai/             # AI configuration models
│   │   │   │   ├── alert/          # Alert system models
│   │   │   │   ├── auth/           # Authentication models
│   │   │   │   ├── external/       # External data models
│   │   │   │   ├── inventory/      # Inventory models
│   │   │   │   ├── notice/         # Notice models
│   │   │   │   ├── payroll/        # Payroll models
│   │   │   │   ├── procurement/    # Procurement models
│   │   │   │   └── timesheet/      # Timesheet models
│   │   │   ├── service/            # Business logic layer
│   │   │   │   ├── ai/             # AI services (GPTService)
│   │   │   │   ├── alert/          # Alert services
│   │   │   │   ├── analytics/      # Analytics services
│   │   │   │   ├── auth/           # Authentication services
│   │   │   │   ├── employee/       # Employee services
│   │   │   │   ├── inventory/      # Inventory services
│   │   │   │   ├── notice/         # Notice services
│   │   │   │   ├── payment/        # Payment services
│   │   │   │   ├── payroll/        # Payroll services
│   │   │   │   ├── procurement/    # Procurement services
│   │   │   │   ├── report/         # Report services
│   │   │   │   └── timesheet/      # Timesheet services
│   │   │   ├── filter/             # Authentication & authorization filters
│   │   │   ├── security/           # Security utilities (JWT, TOTP)
│   │   │   ├── util/               # Helper utilities
│   │   │   ├── job/                # Scheduled jobs (Quartz)
│   │   │   └── listener/           # Event listeners
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml # JPA configuration
│   │   └── webapp/
│   │       ├── auth/               # Login, signup, OTP pages
│   │       ├── cart/               # Cashier/POS interface
│   │       ├── employee/           # Employee management pages
│   │       ├── inventory/          # Inventory management
│   │       ├── kitchen/            # Kitchen display system
│   │       ├── procurement/        # Procurement module
│   │       ├── reception/          # Reception interface
│   │       ├── sales/              # Sales invoice pages
│   │       ├── payment/            # Payment result pages
│   │       ├── alert/              # Alert dashboard
│   │       ├── css/                # Stylesheets
│   │       ├── js/                 # JavaScript files
│   │       ├── img/                # Images and logos
│   │       ├── uploads/            # Uploaded files
│   │       ├── includes/           # Header, footer components
│   │       └── WEB-INF/
│   │           ├── web.xml         # Servlet configuration
│   │           └── beans.xml       # CDI configuration
│   └── test/
│       ├── java/com/liteflow/      # Unit & integration tests
│       │   ├── unit/               # Unit tests
│       │   └── selenium/           # Selenium E2E tests
│       └── resources/
│           └── META-INF/
│               └── persistence.xml # Test JPA configuration
├── database/
│   ├── liteflow_schema.sql         # Main database schema
│   ├── liteflow_data.sql           # Sample data
│   ├── procurement_schema.sql      # Procurement schema
│   ├── procurement_data.sql        # Procurement data
│   ├── alert_system_schema.sql     # Alert system schema
│   ├── alert_system_data.sql       # Alert system data
│   ├── ai_agent_config_schema.sql  # AI agent configuration schema
│   ├── ai_agent_config_data.sql    # AI agent configuration data
│   ├── po_alert_notification_schema.sql # PO alert notification schema
│   ├── telegram_.sql               # Telegram configuration schema
│   └── telegram_data.sql           # Telegram configuration data
├── target/
│   ├── LiteFlow.war                # Deployable WAR file
│   └── site/jacoco/                # Code coverage reports
├── prompts/                        # AI-assisted development logs
├── pom.xml                         # Maven configuration
└── README.md                       # This file
```

---

## 🚀 Getting Started

### Prerequisites

Before running LiteFlow, ensure you have the following installed:
- **Java Development Kit (JDK) 16 or higher**
- **Apache Maven 3.6+**
- **Microsoft SQL Server 2019 or later** (recommended)
- **Apache Tomcat 10+** (or compatible Jakarta EE server)
- **Redis Server** (optional, for caching)
- **Git** (for cloning the repository)

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/LiteFlow.git
cd LiteFlow
```

### Step 2: Set Up the Database

#### 2.1 Create Database
1. Open **SQL Server Management Studio (SSMS)** or your preferred SQL client
2. Execute the schema creation scripts in order:
```sql
-- Run main schema
USE master;
GO
CREATE DATABASE LiteFlowDBO;
GO
USE LiteFlowDBO;
GO

-- Run schema files
:r database/liteflow_schema.sql
:r database/procurement_schema.sql
:r database/alert_system_schema.sql
:r database/ai_agent_config_schema.sql
:r database/po_alert_notification_schema.sql
:r database/telegram_.sql
```

#### 2.2 Load Sample Data
```sql
USE LiteFlowDBO;
GO

-- Load initial data
:r database/liteflow_data.sql
:r database/procurement_data.sql
:r database/alert_system_data.sql
:r database/ai_agent_config_data.sql
:r database/telegram_data.sql
```

#### 2.3 Update Database Connection
Edit the file: `src/main/resources/META-INF/persistence.xml`

```xml
<property name="jakarta.persistence.jdbc.url" 
          value="jdbc:sqlserver://localhost:1433;databaseName=LiteFlowDBO;encrypt=true;trustServerCertificate=true;"/>
<property name="jakarta.persistence.jdbc.user" value="YOUR_USERNAME"/>
<property name="jakarta.persistence.jdbc.password" value="YOUR_PASSWORD"/>
```

**Note:** Replace `YOUR_USERNAME` and `YOUR_PASSWORD` with your SQL Server credentials.

### Step 3: Configure Environment Variables

Create a `.env` file in the project root:

```env
# OpenAI API Key (for AI chatbot and features)
OPENAI_API_KEY=your_openai_api_key_here

# VNPay Configuration
VNPAY_TMN_CODE=your_tmn_code
VNPAY_HASH_SECRET=your_hash_secret
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html

# Telegram Bot Token (for alerts)
TELEGRAM_BOT_TOKEN=your_telegram_bot_token

# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASSWORD=your_email_password

# Google OAuth2 (already configured in web.xml)
# Google Client ID and Secret are in web.xml
```

### Step 4: Configure Redis (Optional)

If using Redis for caching:
1. Install and start Redis server on default port `6379`
2. No additional configuration needed (Jedis client auto-connects to localhost:6379)

### Step 5: Build the Project

```bash
# Clean and compile the project
mvn clean install

# Skip tests during build (optional)
mvn clean install -DskipTests
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

The WAR file will be generated at: `target/LiteFlow.war`

### Step 6: Deploy to Tomcat

#### Option A: Manual Deployment
1. Copy `target/LiteFlow.war` to Tomcat's `webapps/` directory
2. Start Tomcat:
```bash
# Windows
catalina.bat start

# Linux/Mac
./catalina.sh start
```

#### Option B: Maven Tomcat Plugin (Development)
Add this to your `pom.xml` (if not already present):
```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.2</version>
    <configuration>
        <url>http://localhost:8080/manager/text</url>
        <server>TomcatServer</server>
        <path>/LiteFlow</path>
    </configuration>
</plugin>
```

Then deploy:
```bash
mvn tomcat7:deploy
```

### Step 7: Access the Application

Once Tomcat is running, open your browser and navigate to:

```
http://localhost:8080/LiteFlow
```

#### Default Login Credentials
After loading sample data, use these accounts to test different roles:

| Role | Email | Password |
|------|-------|----------|
| **Admin** | admin@liteflow.com | 1 |
| **Manager** | manager@liteflow.com | Manager123! |
| **Cashier** | cashier@liteflow.com | Cashier123! |
| **Kitchen** | kitchen@liteflow.com | Kitchen123! |

**⚠️ Security Warning:** Change default passwords immediately in production environments!

### Step 8: Run Tests (Optional)

```bash
# Run all unit tests
mvn test

# Run Selenium E2E tests
mvn test -Dtest=*SystemTest

# Generate code coverage report
mvn jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

**Expected Coverage:**
- Line Coverage: ≥70%
- Branch Coverage: ≥80%
- Method Coverage: ≥90%

---

## 🔧 Configuration

### AI Chatbot Configuration

1. Navigate to **Settings > AI Agent Config**
2. Configure OpenAI API key and model settings
3. Set up custom prompts for different use cases
4. Enable/disable AI features

### Alert System Configuration

1. Navigate to **Alert Dashboard**
2. Create alert configurations for different alert types
3. Configure notification channels (Email, Telegram, Slack, In-App)
4. Set up scheduled alerts with Cron expressions
5. Configure AI-enhanced alert summarization

### VNPay Payment Configuration

1. Update VNPay credentials in `.env` file
2. Configure return URL in `VNPayUtil.java`
3. Test payment flow in sandbox mode
4. Update to production credentials for live deployment

### Email Configuration

1. Update SMTP settings in `.env` file
2. Configure email templates in `MailUtil.java`
3. Test email sending functionality

---

## 🐛 Troubleshooting

### Issue: Database Connection Failed
**Solution:** 
- Verify SQL Server is running
- Check credentials in `persistence.xml`
- Ensure database `LiteFlowDBO` exists
- Check SQL Server authentication mode (SQL Server Authentication or Windows Authentication)

### Issue: Port 8080 Already in Use
**Solution:** 
- Change Tomcat port in `conf/server.xml`
- Or kill the process using port 8080:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Issue: 404 Error After Deployment
**Solution:** 
- Ensure WAR file is properly extracted in `webapps/LiteFlow/` directory
- Check Tomcat logs for deployment errors
- Verify `web.xml` configuration

### Issue: JWT Token Errors
**Solution:** 
- Check that JWT secret key is properly configured
- Verify session management configuration
- Check token expiration settings

### Issue: OpenAI API Errors
**Solution:** 
- Verify OpenAI API key in `.env` file
- Check API key permissions and quota
- Verify network connectivity to OpenAI API

### Issue: VNPay Payment Errors
**Solution:** 
- Verify VNPay credentials in `.env` file
- Check IP address whitelist in VNPay dashboard
- Verify return URL configuration
- Check hash calculation in `VNPayUtil.java`

### Issue: Email Not Sending
**Solution:** 
- Verify SMTP credentials in `.env` file
- Check SMTP server settings
- Verify email server firewall rules
- Check spam folder for test emails

---

## 📖 API Documentation

### Authentication API
- `POST /auth/login` - User login
- `POST /auth/signup` - User registration
- `POST /auth/logout` - User logout
- `POST /auth/send-otp` - Send OTP for 2FA
- `POST /auth/verify-otp` - Verify OTP
- `POST /auth/forgot-password` - Request password reset
- `POST /auth/reset-password` - Reset password

### Cashier API
- `GET /api/cashier/tables` - Get all tables
- `GET /api/cashier/menu` - Get menu items
- `POST /api/cashier/order` - Create order
- `PUT /api/cashier/order/{orderId}` - Update order
- `POST /api/cashier/payment` - Process payment

### Inventory API
- `GET /products` - Get all products
- `POST /products` - Create product
- `PUT /products/{productId}` - Update product
- `DELETE /products/{productId}` - Delete product
- `POST /products/import` - Import products from Excel
- `GET /products/export` - Export products to Excel

### Procurement API
- `GET /procurement/po` - Get all purchase orders
- `POST /procurement/po` - Create purchase order
- `PUT /procurement/po/{poId}` - Update purchase order
- `POST /procurement/po/auto-create` - Auto-create PO from low stock
- `GET /procurement/supplier` - Get all suppliers
- `POST /procurement/supplier` - Create supplier

### Payroll API
- `GET /api/payroll/list?month=X&year=Y` - Get payroll list
- `POST /api/payroll/generate` - Generate payroll
- `POST /api/payroll/mark-paid` - Mark payroll as paid
- `POST /api/payroll/recalculate` - Recalculate payroll

### AI Chatbot API
- `POST /api/chatbot` - Send message to chatbot
- `GET /api/ai-agent-config` - Get AI agent configuration
- `PUT /api/ai-agent-config` - Update AI agent configuration

### Alert API
- `GET /alert/dashboard` - Get alert dashboard
- `POST /alert/trigger` - Trigger alert
- `GET /alert/history` - Get alert history
- `PUT /alert/preferences` - Update alert preferences

---

## 🧑‍💻 Development

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods focused and single-purpose

### Database Migrations
- Use SQL migration scripts in `database/` directory
- Always backup database before running migrations
- Test migrations on development environment first

### Testing
- Write unit tests for all service classes
- Write integration tests for API endpoints
- Maintain test coverage above 70%
- Use mocking for external dependencies

### Git Workflow
- Create feature branches for new features
- Write descriptive commit messages
- Create pull requests for code review
- Merge to main only after approval

---

## 📚 Additional Documentation

- 📖 [API Documentation](docs/API.md)
- 🧪 [Testing Guide](prompts/outputs_2/Output_PR1.md)
- 🗄️ [Database Schema](database/liteflow_schema.sql)
- 📝 [Development Logs](prompts/log.md)
- 🎨 [UI/UX Guidelines](docs/UI_GUIDELINES.md)
- 🔔 [Alert System Documentation](database/alert_system_schema.sql)
- 🤖 [AI Chatbot Documentation](database/ai_agent_config_schema.sql)

---

## 🔄 Version History

| Version | Date | Changes |
|---------|------|---------|
| **1.0.0** | October 2025 | Initial release with core ERP modules |
| **0.9.0** | September 2025 | Beta release with procurement module |
| **0.8.0** | August 2025 | Alpha release with POS and inventory |

---

## 🌟 Features Roadmap

### ✅ Completed
- User authentication with 2FA
- POS system with table management
- Kitchen display system
- Basic inventory management
- Employee management
- Procurement module
- AI-powered chatbot
- Smart alert system
- Payment gateway integration (VNPay)
- Notice board
- Timesheet management
- Payroll calculation
- Revenue reporting
- Demand forecasting

### 🚧 In Progress
- Advanced analytics dashboard
- Mobile responsive design
- REST API for mobile app integration
- Multi-language support (English, Vietnamese)

### 📋 Planned
- Cloud deployment (AWS/Azure)
- Mobile application (iOS/Android)
- Advanced reporting with PDF export
- Integration with additional payment gateways
- Customer loyalty program
- Advanced AI features (predictive analytics)
- Real-time analytics dashboard
- WebSocket support for real-time updates

---


## 📞 Contact & Support

- **Project Repository:** [GitHub - LiteFlow](https://github.com/your-username/LiteFlow)
- **Issues & Bug Reports:** [GitHub Issues](https://github.com/your-username/LiteFlow/issues)
- **Documentation:** [Wiki](https://github.com/your-username/LiteFlow/wiki)
- **Email:** liteflow.team@fpt.edu.vn

---

## 🧾 License

**Educational Use Only**

This project is developed by students at **FPT University** as part of the **SWP391 - Software Project** course. All rights are owned by the development team.

### Terms of Use
- ✅ Free to use for **educational and learning purposes**
- ✅ Can be modified and extended for **academic projects**
- ❌ **Not licensed for commercial use** without permission
- ❌ **Not for redistribution** as a standalone product

For commercial licensing inquiries, please contact the development team.

---

## 🙏 Acknowledgments

- **FPT University** - For providing the learning environment and resources
- **Instructor Team** - For guidance and support throughout the project
- **Open Source Community** - For the amazing libraries and tools used in this project
- **Stack Overflow & GitHub** - For countless solutions and inspirations
- **OpenAI** - For providing GPT API for AI features
- **VNPay** - For providing payment gateway integration

---

<div align="center">

**Made with ❤️ by the LiteFlow Team**

⭐ Star this repository if you find it helpful!

[Report Bug](https://github.com/your-username/LiteFlow/issues) · [Request Feature](https://github.com/your-username/LiteFlow/issues) · [Documentation](https://github.com/your-username/LiteFlow/wiki)

---

*© 2025 LiteFlow Development Team - FPT University*

</div>
