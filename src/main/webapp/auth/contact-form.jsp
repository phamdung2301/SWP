<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>LiteFlow - Contact Form</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/policy-pages.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/contact-form.css">
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
                <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=email" class="nav-item" id="nav-email">
                    <i class="fas fa-envelope"></i>
                    <span>Email Support</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=github" class="nav-item" id="nav-github">
                    <i class="fab fa-github"></i>
                    <span>GitHub Issues</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=telegram" class="nav-item" id="nav-telegram">
                    <i class="fab fa-telegram"></i>
                    <span>Telegram</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth/contact-form.jsp?type=general" class="nav-item" id="nav-general">
                    <i class="fas fa-comments"></i>
                    <span>General Inquiry</span>
                </a>
            </nav>
            
            <a href="${pageContext.request.contextPath}/auth/customer-care.jsp" class="back-btn">
                <i class="fas fa-arrow-left"></i> Back to Customer Care
            </a>
        </div>

        <div class="policy-content">
            <div class="content-wrapper">
                <!-- Email Support Form -->
                <div class="form-container" id="email-form" style="display: none;">
                    <div class="content-header">
                        <i class="fas fa-envelope header-icon"></i>
                        <h1>Email Support</h1>
                        <p class="last-updated">Get help via email with detailed explanations</p>
                    </div>

                    <form class="contact-form" data-type="email">
                        <input type="hidden" name="formType" value="Email Support">
                        
                        <div class="form-group">
                            <label for="email-name"><i class="fas fa-user"></i> Full Name *</label>
                            <input type="text" id="email-name" name="name" required placeholder="Enter your full name">
                        </div>

                        <div class="form-group">
                            <label for="email-email"><i class="fas fa-envelope"></i> Email Address *</label>
                            <input type="email" id="email-email" name="email" required placeholder="your.email@example.com">
                        </div>

                        <div class="form-group">
                            <label for="email-phone"><i class="fas fa-phone"></i> Phone Number</label>
                            <input type="tel" id="email-phone" name="phone" placeholder="+84 xxx xxx xxx">
                        </div>

                        <div class="form-group">
                            <label for="email-subject"><i class="fas fa-tag"></i> Subject *</label>
                            <input type="text" id="email-subject" name="subject" required placeholder="Brief description of your issue">
                        </div>

                        <div class="form-group">
                            <label for="email-priority"><i class="fas fa-exclamation-circle"></i> Priority *</label>
                            <select id="email-priority" name="priority" required>
                                <option value="">Select priority level</option>
                                <option value="Low">Low - General inquiry</option>
                                <option value="Medium">Medium - Need assistance</option>
                                <option value="High">High - System issue</option>
                                <option value="Critical">Critical - Service down</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="email-message"><i class="fas fa-comment-alt"></i> Message *</label>
                            <textarea id="email-message" name="message" required rows="6" placeholder="Describe your issue in detail. Include error messages, steps to reproduce, etc."></textarea>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn-submit">
                                <i class="fas fa-paper-plane"></i> Send Message
                            </button>
                            <button type="reset" class="btn-reset">
                                <i class="fas fa-redo"></i> Reset Form
                            </button>
                        </div>
                    </form>
                </div>

                <!-- GitHub Issues Form -->
                <div class="form-container" id="github-form" style="display: none;">
                    <div class="content-header">
                        <i class="fab fa-github header-icon"></i>
                        <h1>GitHub Issue Report</h1>
                        <p class="last-updated">Report bugs or request features</p>
                    </div>

                    <form class="contact-form" data-type="github">
                        <input type="hidden" name="formType" value="GitHub Issue">
                        
                        <div class="form-group">
                            <label for="github-name"><i class="fas fa-user"></i> Your Name *</label>
                            <input type="text" id="github-name" name="name" required placeholder="Enter your name">
                        </div>

                        <div class="form-group">
                            <label for="github-email"><i class="fas fa-envelope"></i> Email *</label>
                            <input type="email" id="github-email" name="email" required placeholder="your.email@example.com">
                        </div>

                        <div class="form-group">
                            <label for="github-username"><i class="fab fa-github"></i> GitHub Username</label>
                            <input type="text" id="github-username" name="githubUsername" placeholder="@yourusername">
                        </div>

                        <div class="form-group">
                            <label for="github-type"><i class="fas fa-list"></i> Issue Type *</label>
                            <select id="github-type" name="issueType" required>
                                <option value="">Select issue type</option>
                                <option value="Bug Report">🐛 Bug Report</option>
                                <option value="Feature Request">✨ Feature Request</option>
                                <option value="Documentation">📚 Documentation</option>
                                <option value="Question">❓ Question</option>
                                <option value="Enhancement">🚀 Enhancement</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="github-title"><i class="fas fa-heading"></i> Issue Title *</label>
                            <input type="text" id="github-title" name="title" required placeholder="Clear, concise title">
                        </div>

                        <div class="form-group">
                            <label for="github-description"><i class="fas fa-file-alt"></i> Description *</label>
                            <textarea id="github-description" name="description" required rows="8" placeholder="Detailed description of the issue or feature request. Include:&#10;- What happened?&#10;- What did you expect?&#10;- Steps to reproduce (for bugs)&#10;- Environment details (OS, Java version, etc.)"></textarea>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn-submit">
                                <i class="fab fa-github"></i> Submit Issue
                            </button>
                            <button type="reset" class="btn-reset">
                                <i class="fas fa-redo"></i> Reset Form
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Telegram Form -->
                <div class="form-container" id="telegram-form" style="display: none;">
                    <div class="content-header">
                        <i class="fab fa-telegram header-icon"></i>
                        <h1>Telegram Support</h1>
                        <p class="last-updated">Quick questions and real-time support</p>
                    </div>

                    <form class="contact-form" data-type="telegram">
                        <input type="hidden" name="formType" value="Telegram Support">
                        
                        <div class="form-group">
                            <label for="telegram-name"><i class="fas fa-user"></i> Name *</label>
                            <input type="text" id="telegram-name" name="name" required placeholder="Your name">
                        </div>

                        <div class="form-group">
                            <label for="telegram-username"><i class="fab fa-telegram"></i> Telegram Username *</label>
                            <input type="text" id="telegram-username" name="telegramUsername" required placeholder="@yourusername">
                        </div>

                        <div class="form-group">
                            <label for="telegram-email"><i class="fas fa-envelope"></i> Email</label>
                            <input type="email" id="telegram-email" name="email" placeholder="your.email@example.com">
                        </div>

                        <div class="form-group">
                            <label for="telegram-category"><i class="fas fa-folder"></i> Category *</label>
                            <select id="telegram-category" name="category" required>
                                <option value="">Select category</option>
                                <option value="Quick Question">Quick Question</option>
                                <option value="Technical Support">Technical Support</option>
                                <option value="Feature Discussion">Feature Discussion</option>
                                <option value="Community Chat">Community Chat</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="telegram-message"><i class="fas fa-comment"></i> Message *</label>
                            <textarea id="telegram-message" name="message" required rows="5" placeholder="Your message or question..."></textarea>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn-submit">
                                <i class="fab fa-telegram"></i> Send Request
                            </button>
                            <button type="reset" class="btn-reset">
                                <i class="fas fa-redo"></i> Reset Form
                            </button>
                        </div>
                    </form>
                </div>

                <!-- General Inquiry Form -->
                <div class="form-container" id="general-form" style="display: none;">
                    <div class="content-header">
                        <i class="fas fa-comments header-icon"></i>
                        <h1>General Inquiry</h1>
                        <p class="last-updated">Questions, feedback, or other inquiries</p>
                    </div>

                    <form class="contact-form" data-type="general">
                        <input type="hidden" name="formType" value="General Inquiry">
                        
                        <div class="form-group">
                            <label for="general-name"><i class="fas fa-user"></i> Full Name *</label>
                            <input type="text" id="general-name" name="name" required placeholder="Enter your full name">
                        </div>

                        <div class="form-group">
                            <label for="general-email"><i class="fas fa-envelope"></i> Email *</label>
                            <input type="email" id="general-email" name="email" required placeholder="your.email@example.com">
                        </div>

                        <div class="form-group">
                            <label for="general-company"><i class="fas fa-building"></i> Company/Organization</label>
                            <input type="text" id="general-company" name="company" placeholder="Your company name (optional)">
                        </div>

                        <div class="form-group">
                            <label for="general-inquiry"><i class="fas fa-question-circle"></i> Inquiry Type *</label>
                            <select id="general-inquiry" name="inquiryType" required>
                                <option value="">Select inquiry type</option>
                                <option value="General Question">General Question</option>
                                <option value="Partnership">Partnership Opportunity</option>
                                <option value="Feedback">Product Feedback</option>
                                <option value="Licensing">Licensing Inquiry</option>
                                <option value="Academic">Academic Inquiry</option>
                                <option value="Other">Other</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="general-subject"><i class="fas fa-tag"></i> Subject *</label>
                            <input type="text" id="general-subject" name="subject" required placeholder="Brief subject">
                        </div>

                        <div class="form-group">
                            <label for="general-message"><i class="fas fa-comment-alt"></i> Message *</label>
                            <textarea id="general-message" name="message" required rows="6" placeholder="Please provide details about your inquiry..."></textarea>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn-submit">
                                <i class="fas fa-paper-plane"></i> Submit Inquiry
                            </button>
                            <button type="reset" class="btn-reset">
                                <i class="fas fa-redo"></i> Reset Form
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Success Message -->
                <div class="success-message" id="success-message" style="display: none;">
                    <div class="success-icon">
                        <i class="fas fa-check-circle"></i>
                    </div>
                    <h2>Message Sent Successfully!</h2>
                    <p>Thank you for contacting us. We've received your message and will respond within 24 hours.</p>
                    <button class="btn-back" onclick="location.reload()">
                        <i class="fas fa-arrow-left"></i> Send Another Message
                    </button>
                </div>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/contact-form.js"></script>
</body>
</html>
