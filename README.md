<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   NETWORK PROGRAMMING  
   HỆ THỐNG CẢNH BÁO THỜI GIAN THỰC (UDP)
</h2>

<div align="center">
    <p align="center">
        <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/fitdnu_logo.png" alt="FIT DNU Logo" width="180"/>
        <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)
</div>

---

## 📖 1. Giới thiệu

### 1.1. Bối cảnh
Trong thời đại **chuyển đổi số** và **Internet of Things (IoT)**, nhu cầu **cảnh báo khẩn cấp theo thời gian thực** ngày càng trở nên cấp thiết. Các hệ thống truyền thống (như loa phóng thanh, chuông báo cháy, thông báo nội bộ) thường có nhiều hạn chế:
- Phạm vi cảnh báo hẹp, khó tiếp cận nhiều đối tượng cùng lúc.  
- Tốc độ truyền tải thông tin chậm, có thể gây ra độ trễ trong tình huống khẩn cấp.  
- Khó tích hợp với các ứng dụng hiện đại như smartphone, hệ thống giám sát tự động hay nền tảng IoT.  

Do đó, việc nghiên cứu và xây dựng **hệ thống cảnh báo thời gian thực trên nền tảng lập trình mạng** là hết sức cần thiết, không chỉ phục vụ mục tiêu học tập mà còn có thể ứng dụng trong thực tiễn.

---

### 1.2. Mục tiêu
Đề tài **Hệ thống cảnh báo thời gian thực (Server gửi cảnh báo tới nhiều Client qua UDP)** được xây dựng với các mục tiêu chính:  
- **Xây dựng mô hình Client–Server** sử dụng Java Socket.  
- **Server đóng vai trò trung tâm**: khi phát hiện sự cố, nó sẽ gửi thông báo cảnh báo đến toàn bộ Client đang hoạt động.  
- **Client đóng vai trò đầu cuối**: lắng nghe và hiển thị thông tin cảnh báo tức thì.  
- Tìm hiểu cách sử dụng **UDP DatagramSocket và DatagramPacket** trong Java để triển khai cơ chế **truyền tin một-nhiều (one-to-many)**.  
- Đảm bảo thông báo cảnh báo được truyền tải nhanh chóng với **độ trễ thấp**, đáp ứng yêu cầu của một hệ thống thời gian thực.  

---

### 1.3. Ý nghĩa thực tiễn
Hệ thống có thể được ứng dụng trong nhiều lĩnh vực:  
- **An toàn – PCCC**: Hệ thống báo cháy trong tòa nhà, nhà máy; khi có khói/nhiệt độ bất thường → Server gửi cảnh báo đến toàn bộ máy tính hoặc thiết bị di động trong mạng.  
- **Y tế & môi trường**: Cảnh báo rò rỉ khí độc, chất thải, mức độ ô nhiễm vượt ngưỡng cho phép.  
- **An ninh**: Cảnh báo đột nhập, sự cố trong khu vực cần giám sát.  
- **IoT – Smart City**: Hệ thống cảm biến trong đô thị thông minh có thể gửi thông báo sự cố giao thông, ngập lụt, hoặc thiên tai đến cư dân.  

👉 **Ví dụ minh họa**:  
Trong ký túc xá sinh viên, khi cảm biến phát hiện khói → Server lập tức gửi cảnh báo đến nhiều Client (máy tính hoặc điện thoại của sinh viên và quản lý ký túc). Tất cả mọi người đều nhận được cảnh báo gần như ngay lập tức để kịp thời xử lý tình huống.  

---

### 1.4. Kỹ thuật sử dụng
- **Ngôn ngữ**: Java.  
- **Giao thức truyền thông**: UDP (User Datagram Protocol).  
  - Ưu điểm: nhanh, không yêu cầu kết nối liên tục, hỗ trợ broadcast/multicast để gửi tin đến nhiều Client cùng lúc.  
  - Nhược điểm: không đảm bảo tính tin cậy tuyệt đối (có thể mất gói tin), tuy nhiên chấp nhận được trong bối cảnh **cảnh báo** (ưu tiên tốc độ hơn độ chính xác tuyệt đối).  
- **Mô hình**:  
  - Server (UDP Sender) phát cảnh báo qua broadcast/multicast.  
  - Client (UDP Receiver) chỉ cần đăng ký cổng (port) là có thể nhận dữ liệu.  

---

### 1.5. Kết quả mong đợi
- Hệ thống **mô phỏng thành công** quá trình gửi – nhận cảnh báo thời gian thực.  
- Server có thể phát cảnh báo đến **nhiều Client cùng lúc**.  
- Client hiển thị thông tin cảnh báo ngay lập tức khi nhận được.  
- Bộ mã nguồn được tổ chức rõ ràng, có thể mở rộng để tích hợp thêm các chức năng: lưu log, giao diện đồ họa, gửi thông báo đa nền tảng (Mobile/Web).  

---

## 🔧 2. Công nghệ & Công cụ sử dụng
- Ngôn ngữ lập trình: [![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)  
- IDE: Eclipse / IntelliJ IDEA  
- Giao thức: **UDP (Datagram Socket)**  
- Quản lý mã nguồn: Git & GitHub  

---

## ⚙️ 3. Kiến trúc hệ thống
- **Server (UDP Sender)**:  
  - Đóng vai trò trung tâm.  
  - Khi có sự cố, Server sẽ gửi thông điệp cảnh báo qua UDP broadcast/multicast.  

- **Client (UDP Receiver)**:  
  - Nhiều Client có thể chạy song song.  
  - Nhận và hiển thị thông báo cảnh báo theo **thời gian thực**.  

📌 **Mô hình hoạt động:**
    ┌─────────────┐       UDP Broadcast        ┌─────────────┐
    │             │  ----------------------->  │             │
    │   SERVER    │ ----------------------->  │   CLIENT 1  │
    │ (Datagram)  │ ----------------------->  │             │
    └─────────────┘                           └─────────────┘
           │
           │
           │                           ┌─────────────┐
           └-------------------------> │   CLIENT 2  │
                                       └─────────────┘

---

## 🚀 4. Chức năng chính
- [x] Server gửi thông điệp cảnh báo theo giao thức UDP.  
- [x] Nhiều Client có thể cùng nhận được thông điệp.  
- [x] Hiển thị cảnh báo tức thì trên Client.  
- [ ] Bổ sung log lưu trữ lịch sử cảnh báo *(dự kiến mở rộng)*.  
- [ ] Gửi cảnh báo đa nền tảng (Mobile/Desktop/Web) *(dự kiến phát triển)*.  

---

## 📂 5. Cấu trúc thư mục

---

## 🧑‍💻 6. Nhóm thực hiện
- Sinh viên: **Bùi Thị Hoa** (MSSV: 39)  
- Lớp: Công nghệ thông tin – Đại học Đại Nam  
- Học phần: Lập trình mạng  

---

## 📝 7. License
© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.
