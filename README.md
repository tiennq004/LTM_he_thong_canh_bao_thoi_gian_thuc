<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">  
   HỆ THỐNG CẢNH BÁO THỜI GIAN THỰC SỬ DỤNG GIAO THỨC UDP
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

1. Giới thiệu

1.1. Bối cảnh
Trong thời đại **chuyển đổi số** và **Internet of Things (IoT)**, nhu cầu **cảnh báo khẩn cấp theo thời gian thực** ngày càng trở nên cấp thiết. Các hệ thống truyền thống (như loa phóng thanh, chuông báo cháy, thông báo nội bộ) thường có nhiều hạn chế:
- Phạm vi cảnh báo hẹp, khó tiếp cận nhiều đối tượng cùng lúc.  
- Tốc độ truyền tải thông tin chậm, có thể gây ra độ trễ trong tình huống khẩn cấp.  
- Khó tích hợp với các ứng dụng hiện đại như smartphone, hệ thống giám sát tự động hay nền tảng IoT.  

    Do đó, việc nghiên cứu và xây dựng **hệ thống cảnh báo thời gian thực trên nền tảng lập trình mạng** là hết sức cần thiết, không chỉ phục vụ mục tiêu học tập mà còn có thể ứng dụng trong thực tiễn.

1.2. Mục tiêu
Đề tài **Hệ thống cảnh báo thời gian thực (Server gửi cảnh báo tới nhiều Client qua UDP)** được xây dựng với các mục tiêu chính:  
- **Xây dựng mô hình Client–Server** sử dụng Java Socket.  
- **Server đóng vai trò trung tâm**: khi phát hiện sự cố, nó sẽ gửi thông báo cảnh báo đến toàn bộ Client đang hoạt động.  
- **Client đóng vai trò đầu cuối**: lắng nghe và hiển thị thông tin cảnh báo tức thì.  
- Tìm hiểu cách sử dụng **UDP DatagramSocket và DatagramPacket** trong Java để triển khai cơ chế **truyền tin một-nhiều (one-to-many)**.  
- Đảm bảo thông báo cảnh báo được truyền tải nhanh chóng với **độ trễ thấp**, đáp ứng yêu cầu của một hệ thống thời gian thực.  

1.3. Ý nghĩa thực tiễn
Hệ thống có thể được ứng dụng trong nhiều lĩnh vực:  
- **An toàn – PCCC**: Hệ thống báo cháy trong tòa nhà, nhà máy; khi có khói/nhiệt độ bất thường → Server gửi cảnh báo đến toàn bộ máy tính hoặc thiết bị di động trong mạng.  
- **Y tế & môi trường**: Cảnh báo rò rỉ khí độc, chất thải, mức độ ô nhiễm vượt ngưỡng cho phép.  
- **An ninh**: Cảnh báo đột nhập, sự cố trong khu vực cần giám sát.  
- **IoT – Smart City**: Hệ thống cảm biến trong đô thị thông minh có thể gửi thông báo sự cố giao thông, ngập lụt, hoặc thiên tai đến cư dân.  

1.4. Kỹ thuật sử dụng
- **Ngôn ngữ**: Java.  
- **Giao thức truyền thông**: UDP (User Datagram Protocol).  
  - Ưu điểm: nhanh, không yêu cầu kết nối liên tục, hỗ trợ broadcast/multicast để gửi tin đến nhiều Client cùng lúc.  
  - Nhược điểm: không đảm bảo tính tin cậy tuyệt đối (có thể mất gói tin), tuy nhiên chấp nhận được trong bối cảnh **cảnh báo** (ưu tiên tốc độ hơn độ chính xác tuyệt đối).  
- **Mô hình**:  
  - Server (UDP Sender) phát cảnh báo qua broadcast/multicast.  
  - Client (UDP Receiver) chỉ cần đăng ký cổng (port) là có thể nhận dữ liệu.  

1.5. Kết quả mong đợi
- Hệ thống **mô phỏng thành công** quá trình gửi – nhận cảnh báo thời gian thực.  
- Server có thể phát cảnh báo đến **nhiều Client cùng lúc**.  
- Client hiển thị thông tin cảnh báo ngay lập tức khi nhận được.  
- Bộ mã nguồn được tổ chức rõ ràng, có thể mở rộng để tích hợp thêm các chức năng: lưu log, giao diện đồ họa, gửi thông báo đa nền tảng (Mobile/Web).  

 🔧 2. Công nghệ & Công cụ sử dụng
- Ngôn ngữ lập trình: [![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)  
- IDE: Eclipse / IntelliJ IDEA  
- Giao thức: **UDP (Datagram Socket)**  
- Quản lý mã nguồn: Git & GitHub  


## 🚀 3. Hình ảnh các chức năng chính
<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/giao_dien_server.png" alt="Ảnh 1" width="800"/>
</p> 
<p align="center">
  <em>Hình 1: Giao diện của Server  </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/client_1.png" alt="Ảnh 2" width="800"/>
</p> 
<p align="center">
  <em>Hình 2: Khởi chạy Client </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/giao_dien_client_1.png" alt="Ảnh 3" width="800"/>
</p> 
<p align="center">
  <em>Hình 3: Giao diện của Client </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/giao_dien_server_khi_khoi_chay_2_client.png" alt="Ảnh 4" width="800"/>
</p> 
<p align="center">
  <em>Hình 4: Giao diện của Server khi khởi chạy 2 Client </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/giao_dien_server_va_client_khi_gui_canh_bao.png" alt="Ảnh 5" width="800"/>
</p> 
<p align="center">
  <em>Hình 5: Giao diện của Server và Client khi gửi cảnh báo </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/giao_dien_server_va_client_khi_canh_bao_hen_gio.png" alt="Ảnh 6" width="800"/>
</p> 
<p align="center">
  <em>Hình 6: Giao diện của Server và Client khi gửi cảnh báo có hẹn giờ và tự động cảnh báo sau 5s </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/Server_khi_dung_canh_bao.png" alt="Ảnh 7" width="800"/>
</p> 
<p align="center">
  <em>Hình 7: Giao diện Server khi dừng cảnh báo </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/thong_tin_2_client.png" alt="Ảnh 8" width="800"/>
</p> 
<p align="center">
  <em>Hình 8: Thông tin của 2 Client </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/lich_su_gui_canh_bao_va_so_client_nhan_duoc_canh_bao.png" alt="Ảnh 9" width="800"/>
</p> 
<p align="center">
  <em>Hình 9: Lịch sử cảnh báo và số CLient nhận được cảnh báo </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/du_lieu_luu_vao_csv.png" alt="Ảnh 10" width="800"/>
</p> 
<p align="center">
  <em>Hình 10: Dữ liệu được lưu vào file .csv </em>
</p>
## 📂 4. Các bước cài đặt
Bước 1: Tải dự án

Clone từ GitHub hoặc tải file zip về:

git clone <repository_url>

Bước 2: Chuẩn bị môi trường

Cài đặt JDK 8+.

Mở IDE Java và import dự án.

Bước 3: Chạy server

Mở AlertServer.java.

Chạy file (Run/Debug).

Server sẽ khởi tạo giao diện GUI với:

Log

Danh sách client

Lịch sử cảnh báo

Server sẽ tự động lắng nghe các client đăng ký.

Bước 4: Chạy client

Mở AlertClient.java.

Chạy file.

Nhập số tầng khi được hỏi (hoặc mặc định tạo ngẫu nhiên).

Client sẽ tự động:

Gửi đăng ký (REGISTER) tới server

Nhận cảnh báo

Hiển thị popup và bảng thông tin cảnh báo

Phát âm thanh tương ứng

Bước 5: Gửi cảnh báo

Trên server, nhập:

Loại cảnh báo

Mức độ

Nội dung

Hẹn giờ (tùy chọn)

Bấm Gửi ngay hoặc Hẹn giờ gửi.

Client nhận và hiển thị cảnh báo.

Bước 6: Kiểm tra log

Server: server_log.csv

Client: client_log_floorX.csv


## 🧑‍💻 5. Người thực hiện
- Sinh viên: **Nguyễn Quang Tiến** (MSSV: 1671020318)  
- Lớp: CNTT 16-03 – Đại học Đại Nam  
- Học phần: Lập trình mạng  

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.
