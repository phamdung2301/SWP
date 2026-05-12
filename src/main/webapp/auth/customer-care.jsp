<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>LiteFlow - Customer Care</title>
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
                <a href="${pageContext.request.contextPath}/auth/terms.jsp" class="nav-item">
                    <i class="fas fa-file-contract"></i>
                    <span>Terms & Conditions</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth/support.jsp" class="nav-item">
                    <i class="fas fa-question-circle"></i>
                    <span>Support</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth/customer-care.jsp" class="nav-item active">
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
                    <i class="fas fa-headset header-icon"></i>
                    <h1>Customer Care</h1>
                    <p class="last-updated">We're here to help you succeed</p>
                </div>

                <div class="content-body">
                    <section class="policy-section">
                        <h2><i class="fas fa-heart"></i> Our Commitment to You</h2>
                        <p>At <strong>LiteFlow</strong>, we're dedicated to providing exceptional customer care and ensuring your restaurant management experience is smooth, efficient, and successful. Our team is committed to helping you maximize the value of our ERP system.</p>
                        <div class="highlight-box">
                            <p><strong>Our Promise:</strong> We're not just providing software—we're partnering with you to transform how your restaurant operates. Your success is our success.</p>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-phone-alt"></i> Contact Us</h2>
                        <div class="contact-grid">
                            <div class="contact-card">
                                <div class="contact-icon">
                                    <i class="fas fa-envelope"></i>
                                </div>
                                <h3>Email Support</h3>
                                <p>Get help via email with detailed explanations</p>
                                <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=email" class="contact-link">Submit Request</a>
                                <span class="response-time">Response time: Within 24 hours</span>
                            </div>

                            <div class="contact-card">
                                <div class="contact-icon">
                                    <i class="fab fa-github"></i>
                                </div>
                                <h3>GitHub Issues</h3>
                                <p>Report bugs, request features, track development</p>
                                <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=github" class="contact-link">Report Issue</a>
                                <span class="response-time">Response time: 1-3 business days</span>
                            </div>

                            <div class="contact-card">
                                <div class="contact-icon">
                                    <i class="fab fa-telegram"></i>
                                </div>
                                <h3>Telegram Community</h3>
                                <p>Join our community for quick questions and tips</p>
                                <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=telegram" class="contact-link">Connect on Telegram</a>
                                <span class="response-time">Response time: Real-time</span>
                            </div>

                            <div class="contact-card">
                                <div class="contact-icon">
                                    <i class="fas fa-comments"></i>
                                </div>
                                <h3>General Inquiry</h3>
                                <p>Questions, feedback, or other inquiries</p>
                                <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=general" class="contact-link">Contact Us</a>
                                <span class="response-time">Response time: 1-2 business days</span>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-life-ring"></i> Support Services</h2>
                        
                        <div class="service-item">
                            <div class="service-icon">
                                <i class="fas fa-graduation-cap"></i>
                            </div>
                            <div class="service-content">
                                <h3>Onboarding & Training</h3>
                                <p>New to LiteFlow? We provide comprehensive onboarding to help you get started:</p>
                                <ul>
                                    <li>System setup and configuration assistance</li>
                                    <li>Role-based training for your team (Admin, Manager, Cashier, Kitchen Staff)</li>
                                    <li>Best practices for restaurant ERP management</li>
                                    <li>Video tutorials and documentation guides</li>
                                    <li>Sample data and demo environment access</li>
                                </ul>
                            </div>
                        </div>

                        <div class="service-item">
                            <div class="service-icon">
                                <i class="fas fa-tools"></i>
                            </div>
                            <div class="service-content">
                                <h3>Technical Support</h3>
                                <p>Experiencing technical issues? Our development team is here to help:</p>
                                <ul>
                                    <li>Database configuration and troubleshooting</li>
                                    <li>Deployment and server setup assistance</li>
                                    <li>Integration support (VNPay, OpenAI, Telegram)</li>
                                    <li>Performance optimization and tuning</li>
                                    <li>Security configuration and best practices</li>
                                </ul>
                            </div>
                        </div>

                        <div class="service-item">
                            <div class="service-icon">
                                <i class="fas fa-lightbulb"></i>
                            </div>
                            <div class="service-content">
                                <h3>Feature Consultation</h3>
                                <p>Need help understanding or using specific features?</p>
                                <ul>
                                    <li>POS system optimization for your workflow</li>
                                    <li>Inventory management strategies</li>
                                    <li>Procurement automation setup</li>
                                    <li>AI chatbot customization and training</li>
                                    <li>Alert system configuration for your business needs</li>
                                    <li>Payroll calculation and employee management</li>
                                </ul>
                            </div>
                        </div>

                        <div class="service-item">
                            <div class="service-icon">
                                <i class="fas fa-code"></i>
                            </div>
                            <div class="service-content">
                                <h3>Developer Support</h3>
                                <p>Building on top of LiteFlow or contributing to the project?</p>
                                <ul>
                                    <li>API documentation and integration guides</li>
                                    <li>Custom module development assistance</li>
                                    <li>Code review and best practices guidance</li>
                                    <li>Testing strategies and quality assurance</li>
                                    <li>Contribution guidelines and pull request support</li>
                                </ul>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-clock"></i> Support Hours & Response Times</h2>
                        <div class="hours-grid">
                            <div class="hours-card">
                                <i class="fas fa-business-time"></i>
                                <h3>Business Hours</h3>
                                <p><strong>Monday - Friday</strong></p>
                                <p>8:00 AM - 5:00 PM (GMT+7)</p>
                                <p class="note">Vietnam Time Zone</p>
                            </div>
                            
                            <div class="hours-card">
                                <i class="fas fa-envelope-open-text"></i>
                                <h3>Email Support</h3>
                                <p><strong>Response Time</strong></p>
                                <p>Within 24 hours</p>
                                <p class="note">During business days</p>
                            </div>
                            
                            <div class="hours-card">
                                <i class="fab fa-github"></i>
                                <h3>GitHub Issues</h3>
                                <p><strong>Response Time</strong></p>
                                <p>1-3 business days</p>
                                <p class="note">Depending on complexity</p>
                            </div>
                            
                            <div class="hours-card">
                                <i class="fas fa-exclamation-triangle"></i>
                                <h3>Critical Issues</h3>
                                <p><strong>Priority Response</strong></p>
                                <p>Within 4-8 hours</p>
                                <p class="note">Security & data loss issues</p>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-user-friends"></i> Meet the Team</h2>
                        <p>LiteFlow is developed and maintained by a dedicated team of students at FPT University:</p>
                        
                        <div class="team-grid">
                            <div class="team-member">
                                <div class="member-avatar">
                                    <i class="fas fa-user-tie"></i>
                                </div>
                                <h4>Vu Duc Giang</h4>
                                <p class="role">Project Lead & Backend Developer</p>
                                <p class="id">DE190556</p>
                                <p class="responsibility">Project leadership, backend architecture, core system development</p>
                            </div>
                            
                            <div class="team-member">
                                <div class="member-avatar">
                                    <i class="fas fa-robot"></i>
                                </div>
                                <h4>Huynh Quang Huy</h4>
                                <p class="role">AI & Feature Developer</p>
                                <p class="id">SE123457</p>
                                <p class="responsibility">AI integration, intelligent features, automation development</p>
                            </div>
                            
                            <div class="team-member">
                                <div class="member-avatar">
                                    <i class="fas fa-palette"></i>
                                </div>
                                <h4>Dang Dong Hoa</h4>
                                <p class="role">UI Developer</p>
                                <p class="id">SE123458</p>
                                <p class="responsibility">User interface design, frontend development, user experience</p>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-hands-helping"></i> Community Guidelines</h2>
                        <p>When interacting with our support channels, please:</p>
                        <ul>
                            <li>Be respectful and professional in all communications</li>
                            <li>Provide clear, detailed descriptions of issues</li>
                            <li>Include relevant error messages, logs, or screenshots</li>
                            <li>Specify your LiteFlow version and environment details</li>
                            <li>Search existing issues before creating new ones</li>
                            <li>Follow up on your requests with additional information if needed</li>
                        </ul>
                        <div class="info-box">
                            <i class="fas fa-info-circle"></i>
                            <p><strong>Tip:</strong> The more information you provide upfront, the faster we can help resolve your issue!</p>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-shield-alt"></i> Privacy & Security</h2>
                        <p>When contacting customer care:</p>
                        <ul>
                            <li>We will never ask for your password</li>
                            <li>Sensitive data should be shared through secure channels only</li>
                            <li>We may request system logs for debugging (sanitize personal data first)</li>
                            <li>All support communications are treated confidentially</li>
                            <li>We comply with data protection best practices</li>
                        </ul>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-globe"></i> Language Support</h2>
                        <div class="language-info">
                            <p><strong>Supported Languages:</strong></p>
                            <ul>
                                <li><i class="fas fa-check"></i> English (Primary)</li>
                                <li><i class="fas fa-check"></i> Tiếng Việt (Vietnamese)</li>
                            </ul>
                            <p class="note">Feel free to communicate in either language—we're here to help!</p>
                        </div>
                    </section>

                    <section class="policy-section cta-section">
                        <h2><i class="fas fa-paper-plane"></i> Ready to Get Help?</h2>
                        <p>Don't hesitate to reach out. Our team is dedicated to ensuring your success with LiteFlow.</p>
                        <div class="cta-buttons">
                            <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=email" class="btn-cta primary">
                                <i class="fas fa-envelope"></i> Email Us
                            </a>
                            <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=github" class="btn-cta secondary">
                                <i class="fab fa-github"></i> Report Issue
                            </a>
                            <a href="${pageContext.request.contextPath}/auth/support.jsp" class="btn-cta tertiary">
                                <i class="fas fa-question-circle"></i> Browse Support Center
                            </a>
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
