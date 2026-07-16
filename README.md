# HRMS Admin Demo - JSP + MySQL

Demo HRMS chay bang Java 17, Servlet/JSP, Tomcat 10.1 va MySQL 8.x.

## Yeu Cau

- JDK 17
- Apache Tomcat 10.1+
- MySQL 8.x hoac MySQL trong XAMPP/WAMP
- MySQL Workbench, phpMyAdmin, hoac MySQL CLI de chay script

## Cai Dat Database

1. Start MySQL service.
2. Mo MySQL Workbench/phpMyAdmin.
3. Chay `sql/01-schema.sql`.
4. Chay `sql/02-seed.sql`.

Hai migration `sql/04-admin-upgrade.sql` va `sql/05-hrm-manager.sql` se duoc app tu apply khi Tomcat start.

## Cau Hinh Ket Noi

Mac dinh app dung:

```text
HRMS_DB_URL=jdbc:mysql://localhost:3306/HRMS_Demo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh&useSSL=false&allowPublicKeyRetrieval=true
HRMS_DB_USER=root
HRMS_DB_PASSWORD=
```

Neu MySQL root cua ban co password, set bien moi truong truoc khi chay:

```powershell
$env:HRMS_DB_USER="root"
$env:HRMS_DB_PASSWORD="mat_khau_mysql_cua_ban"
```

## Build Va Chay

```powershell
.\run.ps1
```

Dung Tomcat local:

```powershell
.\run.ps1 stop
```

Mo app:

```text
http://localhost:8080/hrms-admin-demo/
```

Tai khoan demo:

```text
admin / Admin@123
manager01 / Manager@123
employee01 / Employee@123
hrm01 / Hrm@123
```

## Chuc Nang Da Co

- Admin: manage accounts, roles, reports/statistics.
- UC9: HR Manager reports dashboard, co cache bang `HR_REPORT` va audit bang `AUDIT_LOG`.
- UC10: HR Manager leave approval/reject/revert, co leave balance va override.
- UC11: HR Manager employee request management, promotion forward sang Admin.
