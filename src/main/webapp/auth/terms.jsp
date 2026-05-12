<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>LiteFlow - Terms & Conditions</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/policy-pages.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
    <div class="policy-container">
        <div class="policy-sidebar">
            <div class="logo-section">
                <h1 class="logo-text">LiteFlow</h1>
                <p class="tagline">Where small shops run smarter</p>
            </div>
            
            <nav class="policy-nav">
                <a href="${pageContext.request.contextPath}/auth/terms.jsp" class="nav-item active">
                    <i class="fas fa-file-contract"></i>
                    <span>Terms & Conditions</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth/support.jsp" class="nav-item">
                    <i class="fas fa-question-circle"></i>
                    <span>Support</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth/customer-care.jsp" class="nav-item">
                    <i class="fas fa-headset"></i>
                    <span>Customer Care</span>
                </a>
            </nav>
            
            <a href="${pageContext.request.contextPath}/login" class="back-btn">
                <i class="fas fa-arrow-left"></i> Back to Login
            </a>
        </div>

        <div class="policy-content">
            <div class="content-wrapper">
                <div class="content-header">
                    <i class="fas fa-file-contract header-icon"></i>
                    <h1>Terms & Conditions</h1>
                    <p class="last-updated">Last Updated: November 17, 2025</p>
                </div>

                <div class="content-body">
                    <section class="policy-section">
                        <h2><i class="fas fa-info-circle"></i> 1. Introduction</h2>
                        <p>Welcome to <strong>LiteFlow</strong> - a comprehensive Enterprise Resource Planning (ERP) solution designed for restaurant and hospitality management. By accessing or using LiteFlow, you agree to be bound by these Terms and Conditions.</p>
                        <p>LiteFlow is developed by students at <strong>FPT University</strong> as part of the SWP391 - Software Project course. These terms govern your use of our system, including all features, services, and modules.</p>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-graduation-cap"></i> 2. Educational License</h2>
                        <div class="highlight-box">
                            <h3>Important Notice</h3>
                            <p>LiteFlow is provided for <strong>educational and learning purposes only</strong>. The following restrictions apply:</p>
                            <ul>
                                <li><i class="fas fa-check"></i> Free to use for educational and academic projects</li>
                                <li><i class="fas fa-check"></i> Can be modified for learning purposes</li>
                                <li><i class="fas fa-times"></i> Not licensed for commercial use without permission</li>
                                <li><i class="fas fa-times"></i> Not for redistribution as a standalone product</li>
                            </ul>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-user-check"></i> 3. User Accounts & Access</h2>
                        <p>To use LiteFlow, you must:</p>
                        <ul>
                            <li>Create an account with valid credentials (email and password)</li>
                            <li>Maintain the confidentiality of your account information</li>
                            <li>Enable Two-Factor Authentication (2FA) when required</li>
                            <li>Accept responsibility for all activities under your account</li>
                            <li>Notify us immediately of any unauthorized access</li>
                        </ul>
                        <div class="info-box">
                            <i class="fas fa-shield-alt"></i>
                            <p><strong>Security Note:</strong> LiteFlow implements BCrypt password hashing, JWT token authentication, and TOTP-based 2FA to protect your account.</p>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-users-cog"></i> 4. User Roles & Permissions</h2>
                        <p>LiteFlow provides role-based access control with the following roles:</p>
                        <div class="roles-grid">
                            <div class="role-card">
                                <i class="fas fa-user-shield"></i>
                                <h4>Admin</h4>
                                <p>Full system access, user management, configuration</p>
                            </div>
                            <div class="role-card">
                                <i class="fas fa-user-tie"></i>
                                <h4>Manager</h4>
                                <p>Operational oversight, reports, procurement</p>
                            </div>
                            <div class="role-card">
                                <i class="fas fa-cash-register"></i>
                                <h4>Cashier</h4>
                                <p>POS operations, order processing, payments</p>
                            </div>
                            <div class="role-card">
                                <i class="fas fa-utensils"></i>
                                <h4>Kitchen</h4>
                                <p>Order management, kitchen display system</p>
                            </div>
                            <div class="role-card">
                                <i class="fas fa-user"></i>
                                <h4>Employee</h4>
                                <p>Personal schedule, attendance, timesheet</p>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-database"></i> 5. Data Usage & Privacy</h2>
                        <p>By using LiteFlow, you acknowledge that:</p>
                        <ul>
                            <li>Your data is stored in Microsoft SQL Server databases</li>
                            <li>Session data may be cached using Redis for performance</li>
                            <li>Payment information is processed securely through VNPay gateway</li>
                            <li>AI features may process your data to provide intelligent insights</li>
                            <li>Alert notifications may be sent via Email, Telegram, or Slack</li>
                        </ul>
                        <div class="warning-box">
                            <i class="fas fa-exclamation-triangle"></i>
                            <p><strong>Data Protection:</strong> We implement industry-standard security measures, but cannot guarantee absolute security. Use strong passwords and enable 2FA.</p>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-cogs"></i> 6. System Features & Services</h2>
                        <p>LiteFlow provides the following modules and features:</p>
                        <div class="features-list">
                            <div class="feature-item">
                                <i class="fas fa-shopping-cart"></i>
                                <div>
                                    <h4>Point of Sale (POS)</h4>
                                    <p>Real-time order processing, table management, payment integration</p>
                                </div>
                            </div>
                            <div class="feature-item">
                                <i class="fas fa-boxes"></i>
                                <div>
                                    <h4>Inventory Management</h4>
                                    <p>Stock tracking, low-stock alerts, product catalog management</p>
                                </div>
                            </div>
                            <div class="feature-item">
                                <i class="fas fa-shopping-basket"></i>
                                <div>
                                    <h4>Procurement System</h4>
                                    <p>Purchase orders, supplier management, automated PO creation</p>
                                </div>
                            </div>
                            <div class="feature-item">
                                <i class="fas fa-users"></i>
                                <div>
                                    <h4>HR & Payroll</h4>
                                    <p>Employee management, attendance, scheduling, payroll calculation</p>
                                </div>
                            </div>
                            <div class="feature-item">
                                <i class="fas fa-robot"></i>
                                <div>
                                    <h4>AI-Powered Features</h4>
                                    <p>Intelligent chatbot, demand forecasting, alert summarization</p>
                                </div>
                            </div>
                            <div class="feature-item">
                                <i class="fas fa-bell"></i>
                                <div>
                                    <h4>Smart Alert System</h4>
                                    <p>Multi-channel notifications, configurable alerts, priority management</p>
                                </div>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-credit-card"></i> 7. Payment Terms</h2>
                        <p>LiteFlow integrates with VNPay payment gateway for secure transactions:</p>
                        <ul>
                            <li>All payments are processed through VNPay's secure infrastructure</li>
                            <li>We do not store complete credit card information</li>
                            <li>Supported payment methods: Cash, Card, Transfer, Wallet, VNPay</li>
                            <li>Payment status tracking: Pending, Processing, Completed, Failed, Refunded</li>
                            <li>Refund policies are subject to VNPay's terms and conditions</li>
                        </ul>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-ban"></i> 8. Prohibited Activities</h2>
                        <p>When using LiteFlow, you must NOT:</p>
                        <ul>
                            <li>Attempt to bypass authentication or security measures</li>
                            <li>Use the system for illegal or fraudulent activities</li>
                            <li>Reverse engineer, decompile, or disassemble the software</li>
                            <li>Share your account credentials with unauthorized persons</li>
                            <li>Use the system to harm, harass, or violate rights of others</li>
                            <li>Attempt SQL injection, XSS, or other security attacks</li>
                            <li>Overload the system with excessive requests (DoS/DDoS)</li>
                        </ul>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-shield-alt"></i> 9. Disclaimer of Warranties</h2>
                        <div class="warning-box">
                            <i class="fas fa-info-circle"></i>
                            <p><strong>IMPORTANT:</strong> LiteFlow is provided "AS IS" without warranties of any kind, either express or implied. This includes, but is not limited to:</p>
                            <ul>
                                <li>Fitness for a particular purpose</li>
                                <li>Merchantability or non-infringement</li>
                                <li>Uninterrupted or error-free operation</li>
                                <li>Accuracy of AI-generated insights or forecasts</li>
                            </ul>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-gavel"></i> 10. Limitation of Liability</h2>
                        <p>The LiteFlow development team and FPT University shall not be liable for:</p>
                        <ul>
                            <li>Any direct, indirect, incidental, or consequential damages</li>
                            <li>Loss of data, profits, or business opportunities</li>
                            <li>Damages arising from unauthorized access to your account</li>
                            <li>Issues related to third-party services (VNPay, OpenAI, Telegram, etc.)</li>
                            <li>System downtime or service interruptions</li>
                        </ul>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-sync-alt"></i> 11. Changes to Terms</h2>
                        <p>We reserve the right to modify these Terms and Conditions at any time. Changes will be effective immediately upon posting. Continued use of LiteFlow after changes constitutes acceptance of the modified terms.</p>
                        <p>Major changes will be communicated through:</p>
                        <ul>
                            <li>In-app notifications</li>
                            <li>Email notifications to registered users</li>
                            <li>Updates on the login page</li>
                        </ul>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-times-circle"></i> 12. Termination</h2>
                        <p>We may terminate or suspend your account immediately, without prior notice, if you:</p>
                        <ul>
                            <li>Breach any terms of this agreement</li>
                            <li>Engage in prohibited activities</li>
                            <li>Attempt to compromise system security</li>
                            <li>Use the system for commercial purposes without authorization</li>
                        </ul>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-balance-scale"></i> 13. Governing Law</h2>
                        <p>These Terms and Conditions are governed by the laws of Vietnam. Any disputes arising from the use of LiteFlow shall be resolved under the jurisdiction of Vietnamese courts.</p>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-envelope"></i> 14. Contact Information</h2>
                        <p>For questions about these Terms and Conditions, please contact:</p>
                        <div class="contact-info">
                            <div class="contact-item">
                                <i class="fas fa-envelope"></i>
                                <span>liteflow.team@fpt.edu.vn</span>
                            </div>
                            <div class="contact-item">
                                <i class="fab fa-github"></i>
                                <span>github.com/VuxDucGiang/LiteFlow</span>
                            </div>
                            <div class="contact-item">
                                <i class="fas fa-university"></i>
                                <span>FPT University - SWP391 Project Team</span>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section agreement">
                        <h2><i class="fas fa-handshake"></i> 15. Agreement</h2>
                        <div class="highlight-box">
                            <p><strong>By using LiteFlow, you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions.</strong></p>
                            <p>If you do not agree to these terms, please discontinue use of the system immediately.</p>
                        </div>
                    </section>
                </div>

                <div class="content-footer">
                    <p>© 2025 LiteFlow Development Team - FPT University</p>
                    <p>Made with <i class="fas fa-heart"></i> by the LiteFlow Team</p>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
