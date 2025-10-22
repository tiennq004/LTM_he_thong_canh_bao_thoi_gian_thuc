<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">  
   HỆ THỐNG CẢNH BÁO THỜI GIAN THỰC (SERVER GỬI CẢNH BÁO TỚI NHIỀU CLIENT QUA UDP)
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

📖 1. Giới thiệu hệ thống

Hệ thống cảnh báo thời gian thực tại chung cư The Vesta sử dụng mô hình Client–Server với giao thức UDP, cho phép nhiều tầng/cư dân nhận cảnh báo cùng lúc.

Server: đóng vai trò trung tâm, gửi cảnh báo đến tất cả Client đang hoạt động, lưu lịch sử cảnh báo và quản lý kết nối.
Client: nhận cảnh báo, hiển thị thông tin và phát âm thanh tương ứng với mức độ khẩn cấp.

Các chức năng chính:

🖥️ Chức năng của Server:

- Kết nối & quản lý Client: Lắng nghe các Client đăng ký, lưu danh sách Client đang hoạt động, quản lý IP/port/tầng.

- Gửi cảnh báo: Server phát cảnh báo đến tất cả Client (one-to-many), có thể chia gói tin dài thành nhiều phần để truyền qua UDP.

- Quản lý lịch sử: Lưu tất cả cảnh báo đã gửi vào server_log.csv với timestamp, loại, mức độ, nội dung, số lượng Client nhận.

- Hẹn giờ & lặp cảnh báo: Cho phép gửi cảnh báo theo lịch định sẵn hoặc lặp lại theo khoảng thời gian.

- Xử lý ACK & lỗi: Nhận phản hồi ACK từ Client, đánh dấu Client đã nhận cảnh báo; khi Client ngắt kết nối hoặc lỗi, vẫn tiếp tục phục vụ các Client khác.

💻 Chức năng của Client:

- Đăng ký Server: Gửi tin nhắn REGISTER kèm số tầng đến Server khi khởi động.

- Nhận cảnh báo: Lắng nghe các gói UDP từ Server, ghép các gói PART[x/y] thành thông điệp đầy đủ.

- Hiển thị thông báo: Popup cảnh báo, bảng thông tin trong GUI, phát âm thanh theo mức độ.

- Lưu log: Ghi cảnh báo nhận được vào file client_log_floorX.csv.

- Quản lý trạng thái: Hiển thị thông báo khi mất kết nối, xử lý lỗi nhận/gửi gói tin.

🌐 Chức năng hệ thống:

- Giao thức UDP: Dùng DatagramSocket/DatagramPacket, truyền tin nhanh, hỗ trợ broadcast/multicast, ưu tiên tốc độ hơn độ tin cậy tuyệt đối.

- Trung gian quản lý cảnh báo: Server giữ vai trò trung tâm, tất cả thông tin cảnh báo đều đi qua Server.

- Lưu trữ dữ liệu: File I/O (append mode) ghi kèm timestamp, loại cảnh báo, mức độ, nội dung, số tầng nhận.

- Xử lý lỗi & bảo trì: Server và Client xử lý ngoại lệ, giữ hệ thống hoạt động liên tục, ghi log debug để kiểm tra.

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
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/khoi_chay_client.png" alt="Ảnh 2" width="800"/>
</p> 
<p align="center">
  <em>Hình 2: Khởi chạy Client </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/giao_dien_client_sau_khi_khoi_chay.png" alt="Ảnh 3" width="800"/>
</p> 
<p align="center">
  <em>Hình 3: Giao diện của Client </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/giao_dien_cua_server_khi_khoi_chay_client.png" alt="Ảnh 4" width="800"/>
</p> 
<p align="center">
  <em>Hình 4: Sau khi khởi chạy Client thông tin Client sẽ được lưu vào Server</em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/server_sau_khi_gui_canh_bao_den_toan_bo_toa_nha.png" alt="Ảnh 5" width="800"/>
</p> 
<p align="center">
  <em>Hình 5: Server sau khi gửi cảnh báo đến toàn bộ tòa nhà </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/client_can_ho_810_tang_8_nhan_canh_bao_tu_server.png" alt="Ảnh 6" width="800"/>
</p> 
<p align="center">
  <em>Hình 6: Client căn hộ 810 tầng 8 nhận cảnh báo từ Server </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/client_can_ho_1009_tang_10_nhan_canh_bao_tu_server.png" alt="Ảnh 7" width="800"/>
</p> 
<p align="center">
  <em>Hình 7: Client căn hộ 1009 tầng 10 nhận cảnh báo từ Server </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/server_khi_gui_canh_bao_den_cac_tang_cu_the.png" alt="Ảnh 8" width="800"/>
</p> 
<p align="center">
  <em>Hình 8: Server gửi cảnh báo đến các tầng cụ thể </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/client_tang_10_ko_nhan_canh_bao_tu_server_khi_khong_duoc_gui_canh_bao.png" alt="Ảnh 9" width="800"/>
</p> 
<p align="center">
  <em>Hình 9: Client tầng 10 không nhận cảnh báo từ Server khi không được gửi cảnh báo </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/client_tang_8_nhan_canh_bao_tu_server.png" alt="Ảnh 10" width="800"/>
</p> 
<p align="center">
  <em>Hình 10: Client tầng 8 nhận cảnh báo từ Server </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/server_gui_canh_bao_tu_dong.png" alt="Ảnh 11" width="800"/>
</p> 
<p align="center">
  <em>Hình 11: Server gửi cảnh báo tự động đến toàn bộ tòa nhà sau </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/server_gui_canh_bao_tu_dong_sau_5s.png" alt="Ảnh 12" width="800"/>
</p> 
<p align="center">
  <em>Hình 12: Server gửi cảnh báo tự động đến toàn bộ tòa nhà sau 5 giây </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/nhat_ky_gui_canh_bao.png" alt="Ảnh 13" width="800"/>
</p> 
<p align="center">
  <em>Hình 13: Nhật ký gửi cảnh báo </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/lich_su_gui_canh_bao.png" alt="Ảnh 14" width="800"/>
</p> 
<p align="center">
  <em>Hình 14: Lịch sử cảnh báo </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/khi_tat_1_client_se_co_trang_thai_offline.png" alt="Ảnh 15" width="800"/>
</p> 
<p align="center">
  <em>Hình 15: Trạng thái của 1 Client khi tắt đi sẽ chuyển thành Offline </em>
</p>

<p align="center">
  <img src="https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc/blob/main/docs/du_lieu_duoc_luu_vao_file.xlsx.png" alt="Ảnh 16" width="800"/>
</p> 
<p align="center">
  <em>Hình 16: Dữ liệu được lưu vào file.xlsx </em>
</p>

## 📂 4. Các bước cài đặt 

Bước 1. Chuẩn bị môi trường

- JDK 17+

- Eclipse IDE (phiên bản mới)

- Hệ điều hành: Windows 10/11

- Thư viện: java.net, javax.swing, java.io (có sẵn trong JDK)

Bước 2. Tải và mở dự án

- Clone hoặc tải mã nguồn:

    git clone https://github.com/tiennq004/LTM_he_thong_canh_bao_thoi_gian_thuc

- Mở Eclipse → File → Import → Existing Projects into Workspace

- → Chọn thư mục dự án.

Bước 3. Chạy ứng dụng

🖥️ 1: Chạy Server

- Mở file Server.java trong thư mục server.

- Nhấn Run để khởi động server.

- Server sẽ bắt đầu lắng nghe các gói tin từ client và hiển thị giao diện điều khiển:

- Danh sách client đang hoạt động

- Nhật ký cảnh báo

- Các nút gửi thông báo thử, xem log,...

💻 2: Chạy Client

- Mở file Client.java trong thư mục client.

- Nhấn Run để khởi động client.

- Giao diện hiển thị thông tin từng tầng/phòng, nhận cảnh báo từ server.

- Có thể chạy nhiều client trên cùng một máy hoặc máy khác trong mạng LAN (chỉ cần trùng địa chỉ IP subnet).

- Đảm bảo Firewall không chặn cổng UDP (thường là cổng 8888 hoặc 9999 tùy cấu hình trong code).

Bước 5. Kết quả

- Server hiển thị danh sách client đang hoạt động.

- Khi 1 client gửi cảnh báo, các client khác sẽ nhận được thông báo ngay.

- Log lưu tại server_log.csv.

## 🧑‍💻 5. Người thực hiện
- Sinh viên: **Nguyễn Quang Tiến** (MSSV: 1671020318)  
- Lớp: CNTT 16-03 – Đại học Đại Nam  
- Học phần: Lập trình mạng  

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.
