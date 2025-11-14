# 🚀 **Smart Inventory Management System**

### *A Java + MySQL based desktop application for real-time stock, orders & product management.*

---

## 📝 **Overview**

The **Smart Inventory Management System** is a desktop application built using **Java Swing**, **JDBC**, and **MySQL** to simplify product stock management, sales processing, and customer order tracking.

It is designed for **small businesses, shops, and warehouses** to automate manual inventory tasks and reduce human error.

---

## 📌 **Key Features**

### 🔐 **Authentication**

* Secure login system
* Admin & Customer specific dashboards

### 📦 **Inventory Management**

* Add new stock
* Update existing stock
* View total inventory
* Automatic quantity deduction on sales

### 🛒 **Sales & Orders Module**

* Sell items via SellItem panel
* Cart system with MySQL storage
* Generate order details
* Recent orders view
* Customer order history

### 🖥️ **Interactive Dashboards**

* **Admin Dashboard:** Manage stock, view recent orders, track products
* **Customer Dashboard:** Browse products, add to cart, buy items

### 🗄 **Database Connectivity**

* JDBC-based connection using centralized `DBConnection.java`
* DAO pattern implemented via `ProductDAO.java`

### 🎨 **Modern UI**

Built using Java Swing with responsive components, tables, and clean layout.

---

## 🧩 **Tech Stack**

| Layer        | Technology                |
| ------------ | ------------------------- |
| Frontend     | Java Swing                |
| Backend      | Java                      |
| Database     | MySQL                     |
| Architecture | MVC + DAO                 |
| Tools        | JDBC, NetBeans / IntelliJ |

---

## 📁 **Project Structure**

```
Smart-Inventory-Management-System
│── Dashboard.java
│── CustomerDashboard.java
│── LoginForm.java
│── ViewStock.java
│── AddStock.java
│── SellItem.java
│── CartView.java
│── MyOrders.java
│── RecentOrdersView.java
│── OrderDetails.java
│── Product.java
│── ProductDAO.java
│── DBConnection.java
└── README.md
```

---

## ⚙️ **Installation & Setup**

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/vikash852201/Smart-Inventory-Management-System.git
cd Smart-Inventory-Management-System
```

### 2️⃣ Open Project in Your IDE

Use **NetBeans**, **IntelliJ IDEA**, or **Eclipse**.

### 3️⃣ Create MySQL Database

```sql
CREATE DATABASE smart_inventory;
USE smart_inventory;
```

Import tables manually or run SQL scripts (if available).

### 4️⃣ Configure Database in `DBConnection.java`

```java
private static final String url = "jdbc:mysql://localhost:3306/smart_inventory";
private static final String username = "root";
private static final String password = "yourpassword";
```

### 5️⃣ Run the Application

Start with:

```
LoginForm.java
```

---


## 📊 **Time & Space Complexity Overview**

| Operation      | Complexity                                    |
| -------------- | --------------------------------------------- |
| Product Search | **O(n)**                                      |
| Add Product    | **O(1)**                                      |
| Update/Delete  | **O(1)**                                      |
| Memory Usage   | **O(n)** depends on number of products loaded |

---

## 🤝 **Contributing**

Contributions are always welcome!

1. Fork the repository
2. Create a new feature branch
3. Push your changes
4. Open a Pull Request

---

## 👨‍💻 **Author**

**Vikash Anand**
GitHub: [@vikash852201](https://github.com/vikash852201)

---

## 📄 **License**

This project is licensed under the **MIT License**.


Just tell me!
