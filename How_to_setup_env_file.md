# Hướng dẫn cấu hình file .env

File `.env` chứa các thông tin cấu hình quan trọng cho hệ thống LiteFlow. File này cần được đặt ở thư mục gốc của project (cùng cấp với file `pom.xml`).

## Vị trí file .env

```
LiteFlow/
├── pom.xml
├── .env          ← File này
├── src/
└── ...
```

## Các biến môi trường cần cấu hình

### 1. OpenAI API Key (GPT Service)

**Key:** `OPENAI_API_KEY`

**Mô tả:** API key từ OpenAI để sử dụng các tính năng AI Chatbot và AI Agent.

**Cách lấy:**
1. Truy cập [OpenAI Platform](https://platform.openai.com/)
2. Đăng nhập hoặc tạo tài khoản
3. Vào **API Keys** trong menu
4. Click **Create new secret key**
5. Copy key và lưu lại (chỉ hiển thị 1 lần)

**Format trong .env:**
```env
OPENAI_API_KEY=sk-your-api-key-here
```

**Ví dụ:**
```env
OPENAI_API_KEY=sk-proj-abc123xyz789...
```

---

### 2. Telegram Bot Token

**Key:** `TELEGRAM_BOT_TOKEN`

**Mô tả:** Token của Telegram Bot để gửi thông báo qua Telegram khi có sự kiện quan trọng (tạo PO, duyệt PO, từ chối PO, v.v.).

**Cách lấy:**
1. Mở Telegram và tìm [@BotFather](https://t.me/botfather)
2. Gửi lệnh `/newbot`
3. Làm theo hướng dẫn để đặt tên bot
4. BotFather sẽ cung cấp token (dạng: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)
5. Copy token

**Format trong .env:**
```env
TELEGRAM_BOT_TOKEN=your-telegram-bot-token
```

**Ví dụ:**
```env
TELEGRAM_BOT_TOKEN=123456789:ABCdefGHIjklMNOpqrsTUVwxyz
```

**Lưu ý:** 
- Token có thể chứa dấu `:` (không cần escape)
- Đảm bảo bot đã được thêm vào các group/channel cần nhận thông báo

---

### 3. Mã số thuế (Tax Code)

**Key:** `MaSoThue`

**Mô tả:** Mã số thuế của công ty LiteFlow, được sử dụng để hiển thị trên hóa đơn mua hàng.

**Format trong .env:**
```env
MaSoThue=0123456789
```

**Ví dụ:**
```env
MaSoThue=0312345678
```

**Lưu ý quan trọng:**
- Key phải có dấu `=` sau `MaSoThue` (format: `MaSoThue=xxxxx`)
- Chỉ nhập số, không có khoảng trắng hoặc ký tự đặc biệt
- Mã số thuế thường có 10 hoặc 13 chữ số
- Giá trị này sẽ được hiển thị trong phần "Thông tin công ty" (chỉ đọc) và trên hóa đơn

---

### 4. Thuế suất VAT

**Key:** `VAT_RATE`

**Mô tả:** Thuế suất VAT (Giá trị gia tăng) được sử dụng để tính toán thuế trên hóa đơn. Mặc định là 10% nếu không được cấu hình.

**Format trong .env:**
```env
VAT_RATE=10.0
```

**Ví dụ:**
```env
VAT_RATE=10.0
```

**Lưu ý:**
- Giá trị là số thập phân (ví dụ: `10.0` cho 10%)
- Nếu không cấu hình, hệ thống sẽ sử dụng giá trị mặc định là `10.0` (10%)
- Thuế suất VAT phổ biến ở Việt Nam: 0%, 5%, 10%

---

## File .env mẫu hoàn chỉnh

```env
# ============================================
# LiteFlow Environment Configuration
# ============================================

# OpenAI API Key (for AI Chatbot and AI Agent features)
OPENAI_API_KEY=sk-your-openai-api-key-here

# Telegram Bot Token (for notifications)
TELEGRAM_BOT_TOKEN=123456789:ABCdefGHIjklMNOpqrsTUVwxyz

# Company Tax Code (Mã số thuế)
# Format: MaSoThue:xxxxxxxxxx
MaSoThue:0312345678

# VAT Rate (Thuế suất VAT)
# Default: 10.0 (10%)
VAT_RATE=10.0

# ============================================
# Optional: Email Configuration (if needed)
# ============================================
# SMTP_HOST=smtp.gmail.com
# SMTP_PORT=587
# SMTP_USER=your_email@gmail.com
# SMTP_PASSWORD=your_email_password

# ============================================
# Optional: VNPay Configuration (if needed)
# ============================================
# VNPAY_TMN_CODE=your_tmn_code
# VNPAY_HASH_SECRET=your_hash_secret
# VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
```

---

## Các bước thiết lập

### Bước 1: Tạo file .env

1. Tạo file mới tên `.env` trong thư mục gốc của project
2. Copy nội dung mẫu ở trên vào file

### Bước 2: Điền thông tin

1. **OPENAI_API_KEY:** Thay `sk-your-openai-api-key-here` bằng API key thực tế
2. **TELEGRAM_BOT_TOKEN:** Thay bằng token bot Telegram của bạn
3. **MaSoThue:** Thay `0312345678` bằng mã số thuế thực tế của công ty
4. **VAT_RATE:** Điều chỉnh nếu cần (mặc định 10.0)

### Bước 3: Kiểm tra

1. Đảm bảo file `.env` nằm đúng vị trí (cùng cấp với `pom.xml`)
2. Không có khoảng trắng thừa hoặc ký tự đặc biệt không cần thiết
3. Mỗi biến môi trường nằm trên một dòng riêng

### Bước 4: Khởi động lại ứng dụng

Sau khi cấu hình, khởi động lại Tomcat/server để hệ thống đọc file `.env` mới.

---

## Xử lý lỗi thường gặp

### Lỗi: "OPENAI_API_KEY not found"
- **Nguyên nhân:** File `.env` không được tìm thấy hoặc key không đúng
- **Giải pháp:** 
  - Kiểm tra file `.env` có ở thư mục gốc không
  - Kiểm tra key có đúng format: `OPENAI_API_KEY=sk-...`

### Lỗi: "MaSoThue not found"
- **Nguyên nhân:** Key không đúng format
- **Giải pháp:** Đảm bảo format là `MaSoThue:xxxxx` (có dấu `:`)

### Lỗi: "TELEGRAM_BOT_TOKEN not found"
- **Nguyên nhân:** Token không được cấu hình hoặc sai
- **Giải pháp:** Kiểm tra token có đúng format và không có khoảng trắng thừa

### Lỗi: VAT_RATE không hoạt động
- **Nguyên nhân:** Giá trị không phải số
- **Giải pháp:** Đảm bảo format là số thập phân: `10.0` (không phải `10%`)

---

## Bảo mật

⚠️ **QUAN TRỌNG:**

1. **KHÔNG** commit file `.env` lên Git
2. Thêm `.env` vào `.gitignore`:
   ```
   .env
   ```
3. Chia sẻ file `.env` mẫu (không có giá trị thực) thay vì file thực tế
4. Bảo vệ file `.env` trên server production

---

## Kiểm tra cấu hình

Sau khi cấu hình, bạn có thể kiểm tra:

1. **OpenAI API Key:** Sử dụng tính năng AI Chatbot, nếu hoạt động thì đã cấu hình đúng
2. **Telegram Token:** Tạo một PO mới, nếu nhận được thông báo Telegram thì đã cấu hình đúng
3. **MaSoThue:** Vào Settings > Thông tin công ty, nếu hiển thị mã số thuế thì đã cấu hình đúng
4. **VAT_RATE:** In một hóa đơn, kiểm tra phần thuế có đúng thuế suất không

---

## Hỗ trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra log của server (Tomcat logs)
2. Xác nhận file `.env` ở đúng vị trí
3. Kiểm tra format của các key
4. Liên hệ team phát triển nếu cần hỗ trợ

