<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>LiteFlow - Support</title>
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
                <a href="${pageContext.request.contextPath}/auth/support.jsp" class="nav-item active">
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
                    <i class="fas fa-question-circle header-icon"></i>
                    <h1>Support Center</h1>
                    <p class="last-updated">Get help with LiteFlow ERP System</p>
                </div>

                <div class="content-body">
                    <section class="policy-section">
                        <h2><i class="fas fa-info-circle"></i> Welcome to LiteFlow Support</h2>
                        <p>We're here to help you get the most out of <strong>LiteFlow</strong> - your comprehensive restaurant and hospitality ERP solution. Whether you're experiencing technical issues, need help with features, or want to learn best practices, our support resources are designed to assist you.</p>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-search"></i> Common Issues & Solutions</h2>
                        
                        <div class="faq-item">
                            <h3><i class="fas fa-database"></i> Database Connection Failed</h3>
                            <div class="faq-content">
                                <p><strong>Problem:</strong> Unable to connect to Microsoft SQL Server database.</p>
                                <p><strong>Solutions:</strong></p>
                                <ul>
                                    <li>Verify SQL Server is running on your system</li>
                                    <li>Check database credentials in <code>persistence.xml</code></li>
                                    <li>Ensure database <code>LiteFlowDBO</code> exists</li>
                                    <li>Verify SQL Server authentication mode (SQL Server Authentication enabled)</li>
                                    <li>Check firewall settings and port 1433 access</li>
                                </ul>
                                <div class="code-block">
                                    <pre>&lt;property name="jakarta.persistence.jdbc.url" 
          value="jdbc:sqlserver://localhost:1433;databaseName=LiteFlowDBO;
          encrypt=true;trustServerCertificate=true;"/&gt;</pre>
                                </div>
                            </div>
                        </div>

                        <div class="faq-item">
                            <h3><i class="fas fa-server"></i> Port 8080 Already in Use</h3>
                            <div class="faq-content">
                                <p><strong>Problem:</strong> Tomcat cannot start because port 8080 is occupied.</p>
                                <p><strong>Solutions:</strong></p>
                                <ul>
                                    <li><strong>Windows:</strong></li>
                                </ul>
                                <div class="code-block">
                                    <pre>netstat -ano | findstr :8080
taskkill /PID &lt;PID&gt; /F</pre>
                                </div>
                                <ul>
                                    <li>Or change Tomcat port in <code>conf/server.xml</code></li>
                                </ul>
                            </div>
                        </div>

                        <div class="faq-item">
                            <h3><i class="fas fa-exclamation-triangle"></i> 404 Error After Deployment</h3>
                            <div class="faq-content">
                                <p><strong>Problem:</strong> Application not found after deploying WAR file.</p>
                                <p><strong>Solutions:</strong></p>
                                <ul>
                                    <li>Verify WAR file is properly extracted in <code>webapps/LiteFlow/</code></li>
                                    <li>Check Tomcat logs for deployment errors: <code>logs/catalina.out</code></li>
                                    <li>Ensure context path is correct: <code>/LiteFlow</code></li>
                                    <li>Verify <code>web.xml</code> configuration is valid</li>
                                    <li>Rebuild project: <code>mvn clean install</code></li>
                                </ul>
                            </div>
                        </div>

                        <div class="faq-item">
                            <h3><i class="fas fa-key"></i> JWT Token Errors</h3>
                            <div class="faq-content">
                                <p><strong>Problem:</strong> Authentication fails or "Invalid token" errors.</p>
                                <p><strong>Solutions:</strong></p>
                                <ul>
                                    <li>Clear browser cookies and cache</li>
                                    <li>Verify JWT secret key is configured in environment</li>
                                    <li>Check token expiration settings (default: 24 hours)</li>
                                    <li>Ensure system time is synchronized</li>
                                    <li>Re-login to generate a fresh token</li>
                                </ul>
                            </div>
                        </div>

                        <div class="faq-item">
                            <h3><i class="fas fa-robot"></i> OpenAI API Errors</h3>
                            <div class="faq-content">
                                <p><strong>Problem:</strong> AI chatbot not responding or API errors.</p>
                                <p><strong>Solutions:</strong></p>
                                <ul>
                                    <li>Verify OpenAI API key in <code>.env</code> file</li>
                                    <li>Check API key permissions and quota in OpenAI dashboard</li>
                                    <li>Verify network connectivity to OpenAI API</li>
                                    <li>Check rate limits (default: 3 requests/minute for free tier)</li>
                                    <li>Review AI agent configuration in Settings</li>
                                </ul>
                            </div>
                        </div>

                        <div class="faq-item">
                            <h3><i class="fas fa-credit-card"></i> VNPay Payment Errors</h3>
                            <div class="faq-content">
                                <p><strong>Problem:</strong> Payment processing fails or returns errors.</p>
                                <p><strong>Solutions:</strong></p>
                                <ul>
                                    <li>Verify VNPay credentials in <code>.env</code> file</li>
                                    <li>Check IP address whitelist in VNPay merchant dashboard</li>
                                    <li>Verify return URL configuration matches your domain</li>
                                    <li>Ensure hash calculation in <code>VNPayUtil.java</code> is correct</li>
                                    <li>Use sandbox mode for testing before production</li>
                                </ul>
                            </div>
                        </div>

                        <div class="faq-item">
                            <h3><i class="fas fa-envelope"></i> Email Not Sending</h3>
                            <div class="faq-content">
                                <p><strong>Problem:</strong> OTP emails, password resets, or notifications not arriving.</p>
                                <p><strong>Solutions:</strong></p>
                                <ul>
                                    <li>Verify SMTP credentials in <code>.env</code> file</li>
                                    <li>Check spam/junk folder for test emails</li>
                                    <li>Verify email server firewall rules (port 587 or 465)</li>
                                    <li>For Gmail: Enable "Less secure app access" or use App Password</li>
                                    <li>Check email server logs for delivery errors</li>
                                </ul>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-cogs"></i> System Requirements</h2>
                        <div class="requirements-grid">
                            <div class="req-card">
                                <i class="fab fa-java"></i>
                                <h4>Java Environment</h4>
                                <ul>
                                    <li>JDK 16 or higher</li>
                                    <li>Maven 3.6+</li>
                                    <li>Apache Tomcat 10+</li>
                                </ul>
                            </div>
                            <div class="req-card">
                                <i class="fas fa-database"></i>
                                <h4>Database</h4>
                                <ul>
                                    <li>MS SQL Server 2019+</li>
                                    <li>Min 2GB storage</li>
                                    <li>Redis (optional)</li>
                                </ul>
                            </div>
                            <div class="req-card">
                                <i class="fas fa-server"></i>
                                <h4>Server</h4>
                                <ul>
                                    <li>4GB RAM minimum</li>
                                    <li>8GB RAM recommended</li>
                                    <li>Port 8080 available</li>
                                </ul>
                            </div>
                            <div class="req-card">
                                <i class="fas fa-globe"></i>
                                <h4>Browser Support</h4>
                                <ul>
                                    <li>Chrome 90+</li>
                                    <li>Firefox 88+</li>
                                    <li>Edge 90+</li>
                                </ul>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-tools"></i> Deployment Options</h2>
                        
                        <div class="deployment-option">
                            <h3><i class="fas fa-magic"></i> Quick Deploy (Recommended)</h3>
                            <p>Use PowerShell scripts for automated deployment:</p>
                            <div class="code-block">
                                <pre>.\scripts\quick-deploy.ps1

# Or specify Tomcat path
.\scripts\quick-deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0"</pre>
                            </div>
                        </div>

                        <div class="deployment-option">
                            <h3><i class="fas fa-hammer"></i> Manual Build & Deploy</h3>
                            <p>Step-by-step manual deployment process:</p>
                            <div class="code-block">
                                <pre># Step 1: Build WAR file
mvn clean install

# Step 2: Copy to Tomcat webapps
copy target\LiteFlow.war "%CATALINA_HOME%\webapps\"

# Step 3: Start Tomcat
catalina.bat start</pre>
                            </div>
                        </div>

                        <div class="deployment-option">
                            <h3><i class="fas fa-plug"></i> Maven Tomcat Plugin</h3>
                            <p>Deploy directly from Maven:</p>
                            <div class="code-block">
                                <pre>mvn tomcat7:deploy
# Or for redeployment
mvn tomcat7:redeploy</pre>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-graduation-cap"></i> Learning Resources</h2>
                        
                        <div class="learning-path">
                            <h3>For New Users</h3>
                            <ol>
                                <li><i class="fas fa-check"></i> Read the Quick Start Guide</li>
                                <li><i class="fas fa-check"></i> Watch the system overview video</li>
                                <li><i class="fas fa-check"></i> Explore the demo environment with sample data</li>
                                <li><i class="fas fa-check"></i> Review role-specific tutorials</li>
                            </ol>
                        </div>

                        <div class="learning-path">
                            <h3>For Developers</h3>
                            <ol>
                                <li><i class="fas fa-check"></i> Study the project structure in README.md</li>
                                <li><i class="fas fa-check"></i> Review database schema files</li>
                                <li><i class="fas fa-check"></i> Explore API documentation</li>
                                <li><i class="fas fa-check"></i> Run unit tests and check coverage reports</li>
                                <li><i class="fas fa-check"></i> Contribute via GitHub pull requests</li>
                            </ol>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-bug"></i> Report an Issue</h2>
                        <p>Found a bug or have a feature request? We want to hear from you!</p>
                        <div class="report-options">
                            <div class="report-card">
                                <i class="fab fa-github"></i>
                                <h4>GitHub Issues</h4>
                                <p>Report bugs or request features on our GitHub repository</p>
                                <a href="https://github.com/VuxDucGiang/LiteFlow/issues" class="btn-primary">Open Issue</a>
                            </div>
                            <div class="report-card">
                                <i class="fas fa-envelope"></i>
                                <h4>Email Support</h4>
                                <p>Send detailed reports directly to our team</p>
                                <a href="mailto:liteflow.team@fpt.edu.vn" class="btn-primary">Send Email</a>
                            </div>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-comments"></i> Community & Resources</h2>
                        <div class="community-links">
                            <a href="https://github.com/VuxDucGiang/LiteFlow" class="community-link">
                                <i class="fab fa-github"></i>
                                <div>
                                    <h4>GitHub Repository</h4>
                                    <p>Source code and version history</p>
                                </div>
                            </a>
                            <a href="https://github.com/VuxDucGiang/LiteFlow/wiki" class="community-link">
                                <i class="fas fa-book"></i>
                                <div>
                                    <h4>Documentation Wiki</h4>
                                    <p>Comprehensive guides and tutorials</p>
                                </div>
                            </a>
                            <a href="https://github.com/VuxDucGiang/LiteFlow/discussions" class="community-link">
                                <i class="fas fa-users"></i>
                                <div>
                                    <h4>Community Discussions</h4>
                                    <p>Ask questions and share ideas</p>
                                </div>
                            </a>
                        </div>
                    </section>

                    <section class="policy-section">
                        <h2><i class="fas fa-life-ring"></i> Additional Support</h2>
                        <div class="info-box">
                            <i class="fas fa-info-circle"></i>
                            <p><strong>Need more help?</strong> Visit our <a href="${pageContext.request.contextPath}/auth/customer-care.jsp">Customer Care</a> page for contact information and personalized support options.</p>
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
