# Demo Script

## Chuan Bi

1. Start MySQL service.
2. Chay `sql/01-schema.sql` va `sql/02-seed.sql` trong MySQL Workbench/phpMyAdmin.
3. Neu MySQL co password, set:

```powershell
$env:HRMS_DB_USER="root"
$env:HRMS_DB_PASSWORD="mat_khau_mysql_cua_ban"
```

4. Chay:

```powershell
.\run.ps1
```

5. Mo `http://localhost:8080/hrms-admin-demo/`.

## Tai Khoan Demo

```text
admin / Admin@123
manager01 / Manager@123
employee01 / Employee@123
hrm01 / Hrm@123
```

## 1. Manage Account - khoang 90 giay

1. Chon **Create account**.
2. Nhap username moi, email, password va role.
3. Nhan **Create account**.
4. Tao lai dung username vua dung de thay loi **Username already exists**.
5. Xoa mem mot account Employee, nhap ly do.
6. Bat **Hien tai khoan da xoa** de thay status `DELETED`.
7. Mo MySQL va kiem tra `deleted_at`, `deleted_by`, `delete_reason`.

## 2. Manage Roles - khoang 45 giay

1. Tao role `Recruiter`.
2. Cap nhat description cua role.
3. Xoa role `Recruiter` khi chua duoc gan: thanh cong.
4. Thu xoa role `Employee` dang duoc su dung: he thong tu choi.

## 3. Reports & Statistics - khoang 30 giay

1. Mo trang Reports & Statistics.
2. Chi ra tong account, Active, Locked va so role.
3. Giai thich so lieu lay bang `COUNT/GROUP BY` tu MySQL.
4. Sau khi soft delete, tai lai trang de thay so lieu thay doi.

## 4. UC9 - View Reports Dashboard

1. Dang nhap `hrm01 / Hrm@123`.
2. Mo **HR Manager -> Reports**.
3. Chon report type, department/date filter.
4. Nhan View de xem chart/table.
5. Xuat Excel/PDF.
6. Mo MySQL kiem tra cache `HR_REPORT` va log `AUDIT_LOG`.

## 5. UC10 - Leave Approval

1. Dang nhap `hrm01 / Hrm@123`.
2. Mo **HR Manager -> Leaves**.
3. Duyet request hop le.
4. Thu request vuot balance/past date de thay warning.
5. Dung override khi can.
6. Revert trong cua so 24 gio.

## 6. UC11 - Request Management

1. Mo **HR Manager -> Requests**.
2. Approve salary/transfer request.
3. Reject mot request kem ly do.
4. Approve promotion de thay status chuyen sang `PENDING_ADMIN`.
