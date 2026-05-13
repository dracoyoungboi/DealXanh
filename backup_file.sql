-- MySQL dump 10.13  Distrib 9.5.0, for Win64 (x86_64)
--
-- Host: localhost    Database: dealxanh_db
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '694f62dd-b3ac-11f0-8842-025058428eb5:1-716';

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `cart_item_id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int DEFAULT NULL,
  `unit_price` double DEFAULT NULL,
  `cart_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`cart_item_id`),
  KEY `FKpcttvuq4mxppo8sxggjtn5i2c` (`cart_id`),
  KEY `FK1re40cjegsfvw58xrkdp6bac6` (`product_id`),
  CONSTRAINT `FK1re40cjegsfvw58xrkdp6bac6` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`cart_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `cart_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `UK_64t7ox312pqal3p7fg9o503c2` (`user_id`),
  CONSTRAINT `FKb5o626f86h46m4s7ms6ginnop` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `icon_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupons`
--

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons` (
  `coupon_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount_type` varchar(255) DEFAULT NULL,
  `discount_value` double DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `max_discount_amount` double DEFAULT NULL,
  `minimum_order_amount` double DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `usage_limit` int DEFAULT NULL,
  `used_count` int DEFAULT NULL,
  PRIMARY KEY (`coupon_id`),
  UNIQUE KEY `UK_eplt0kkm9yf2of2lnx6c1oy9b` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupons`
--

LOCK TABLES `coupons` WRITE;
/*!40000 ALTER TABLE `coupons` DISABLE KEYS */;
/*!40000 ALTER TABLE `coupons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deal_products`
--

DROP TABLE IF EXISTS `deal_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `deal_products` (
  `deal_product_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `max_quantity` int DEFAULT NULL,
  `original_price` double DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `sale_price` double DEFAULT NULL,
  `sold_quantity` int DEFAULT NULL,
  `deal_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`deal_product_id`),
  KEY `FKhmyy5sqgnsgo7oje6fxqo37iy` (`deal_id`),
  KEY `FKcqb0n5tw7vlr1inkm4vnvr1d2` (`product_id`),
  CONSTRAINT `FKcqb0n5tw7vlr1inkm4vnvr1d2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `FKhmyy5sqgnsgo7oje6fxqo37iy` FOREIGN KEY (`deal_id`) REFERENCES `deals` (`deal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deal_products`
--

LOCK TABLES `deal_products` WRITE;
/*!40000 ALTER TABLE `deal_products` DISABLE KEYS */;
/*!40000 ALTER TABLE `deal_products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deals`
--

DROP TABLE IF EXISTS `deals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `deals` (
  `deal_id` bigint NOT NULL AUTO_INCREMENT,
  `banner_url` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deal_code` varchar(255) DEFAULT NULL,
  `deal_name` varchar(255) DEFAULT NULL,
  `deal_type` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount_type` varchar(255) DEFAULT NULL,
  `discount_value` double DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `max_discount_amount` double DEFAULT NULL,
  `max_usage_count` bigint DEFAULT NULL,
  `min_order_amount` double DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `usage_count` bigint DEFAULT NULL,
  `usage_per_user` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `apply_method` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`deal_id`),
  KEY `FK9tii7qhrwtm0t22p08yp38p5b` (`created_by`),
  KEY `FKf6p6x474eihkvmgjv11um4xwp` (`store_id`),
  CONSTRAINT `FK9tii7qhrwtm0t22p08yp38p5b` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKf6p6x474eihkvmgjv11um4xwp` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deals`
--

LOCK TABLES `deals` WRITE;
/*!40000 ALTER TABLE `deals` DISABLE KEYS */;
/*!40000 ALTER TABLE `deals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disputes`
--

DROP TABLE IF EXISTS `disputes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disputes` (
  `dispute_id` bigint NOT NULL AUTO_INCREMENT,
  `admin_note` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `evidence_url` varchar(255) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`dispute_id`),
  KEY `FKfvupul0vcl8u4ctf5f1ggc39v` (`user_id`),
  KEY `FK7w9qai75udrw8yjppow8vqxa` (`order_id`),
  CONSTRAINT `FK7w9qai75udrw8yjppow8vqxa` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `FKfvupul0vcl8u4ctf5f1ggc39v` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `disputes`
--

LOCK TABLES `disputes` WRITE;
/*!40000 ALTER TABLE `disputes` DISABLE KEYS */;
/*!40000 ALTER TABLE `disputes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_read` bit(1) DEFAULT NULL,
  `link_url` varchar(255) DEFAULT NULL,
  `message` text,
  `title` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`notification_id`),
  KEY `FK9y21adhxn0ayjhfocscqox7bh` (`user_id`),
  CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int DEFAULT NULL,
  `unit_price` double DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `actual_pickup_time` datetime(6) DEFAULT NULL,
  `cancellation_reason` varchar(255) DEFAULT NULL,
  `coupon_code` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `discount_amount` double DEFAULT NULL,
  `final_amount` double DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `payment_status` varchar(255) DEFAULT NULL,
  `pickup_qr_code` varchar(255) DEFAULT NULL,
  `scheduled_pickup_time` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `FKnqkwhwveegs6ne9ra90y1gq0e` (`store_id`),
  KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKnqkwhwveegs6ne9ra90y1gq0e` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deal_end_time` datetime(6) DEFAULT NULL,
  `deal_price` double DEFAULT NULL,
  `deal_start_time` datetime(6) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `description` text,
  `expiry_date` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `original_price` double DEFAULT NULL,
  `pickup_deadline` datetime(6) DEFAULT NULL,
  `product_type` varchar(255) DEFAULT NULL,
  `stock_quantity` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `approval_status` varchar(255) DEFAULT NULL,
  `rejection_reason` text,
  PRIMARY KEY (`product_id`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  KEY `FKgcyffheofvmy2x5l78xam63mc` (`store_id`),
  CONSTRAINT `FKgcyffheofvmy2x5l78xam63mc` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `review_id` bigint NOT NULL AUTO_INCREMENT,
  `comment` text,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `rating` int DEFAULT NULL,
  `verified` bit(1) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`review_id`),
  KEY `FKqwgq1lxgahsxdspnwqfac6sv6` (`order_id`),
  KEY `FKpl51cejpw4gy5swfar8br9ngi` (`product_id`),
  KEY `FKg5v2uypi6rxq6647cqef2d7pt` (`store_id`),
  KEY `FKcgy7qjc1r99dp117y9en6lxye` (`user_id`),
  CONSTRAINT `FKcgy7qjc1r99dp117y9en6lxye` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKg5v2uypi6rxq6647cqef2d7pt` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  CONSTRAINT `FKpl51cejpw4gy5swfar8br9ngi` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `FKqwgq1lxgahsxdspnwqfac6sv6` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,NULL,'STORE_OWNER'),(2,NULL,'USER'),(3,'Quản trị viên hệ thống với toàn quyền','ROLE_ADMIN'),(4,'Kiểm duyệt viên với quyền quản lý nội dung','ROLE_MODERATOR');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stores`
--

DROP TABLE IF EXISTS `stores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stores` (
  `store_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `average_rating` double DEFAULT NULL,
  `close_time` time(6) DEFAULT NULL,
  `cover_image_url` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `open_time` time(6) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `store_name` varchar(255) DEFAULT NULL,
  `total_reviews` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL,
  `approval_mode` varchar(255) DEFAULT NULL,
  `bank_account_number` varchar(255) DEFAULT NULL,
  `bank_account_owner` varchar(255) DEFAULT NULL,
  `bank_name` varchar(255) DEFAULT NULL,
  `business_license_url` varchar(255) DEFAULT NULL,
  `business_type` varchar(255) DEFAULT NULL,
  `categories` varchar(255) DEFAULT NULL,
  `cccd_url` varchar(1000) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `max_slots_per_time` int DEFAULT NULL,
  `operating_days` varchar(255) DEFAULT NULL,
  `pickup_duration_minutes` int DEFAULT NULL,
  `pickup_slots` varchar(255) DEFAULT NULL,
  `vsattp_url` varchar(255) DEFAULT NULL,
  `rejection_reason` varchar(500) DEFAULT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `approved_by` bigint DEFAULT NULL,
  `rejected_by` bigint DEFAULT NULL,
  PRIMARY KEY (`store_id`),
  KEY `FK62smc31fbgclsu56aa4y2hrxg` (`owner_id`),
  KEY `FKpxbl3sqlf6ic0oplca4j58l0a` (`approved_by`),
  KEY `FK9hlm09lvbtq349udv66he2abp` (`rejected_by`),
  CONSTRAINT `FK62smc31fbgclsu56aa4y2hrxg` FOREIGN KEY (`owner_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK9hlm09lvbtq349udv66he2abp` FOREIGN KEY (`rejected_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKpxbl3sqlf6ic0oplca4j58l0a` FOREIGN KEY (`approved_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stores`
--

LOCK TABLES `stores` WRITE;
/*!40000 ALTER TABLE `stores` DISABLE KEYS */;
INSERT INTO `stores` VALUES (1,'Đường Quốc Lộ 21',0,NULL,NULL,'2026-04-25 12:44:46.865532','bcccc',NULL,NULL,NULL,NULL,'0777451107','ACTIVE','abcbcbcbc',0,'2026-05-08 00:42:00.109802',1,'manual','131313123123131','nguyen phan anh','Vietcombank (VCB)',NULL,'Nhà hàng / Quán ăn','Đồ ăn mặn, Thực phẩm đóng gói',NULL,'Hà Nội','Hoàn Kiếm',20,'T2,T3,T4,T5,T6,T7',30,'16:00-18:00, 17:00-19:00, 18:00-20:00, 19:00-21:00',NULL,NULL,'2026-05-08 00:42:00.109802',6,NULL),(2,NULL,0,NULL,NULL,'2026-04-25 19:01:27.792253',NULL,NULL,NULL,NULL,NULL,NULL,'REJECTED',NULL,0,'2026-05-08 10:02:11.149464',2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'khong biet','2026-05-08 10:02:11.149464',NULL,6),(3,'Đường Quốc Lộ 21',0,NULL,NULL,'2026-04-25 21:20:02.585837','abcd',NULL,NULL,NULL,NULL,'0832132728','ACTIVE','bcbc',0,'2026-05-08 10:02:43.315964',3,'manual','13131312312312','NGUYEN PHAN ANH','BIDV','/uploads/1777817012494_image (1).png','Nhà hàng / Quán ăn','Đồ ăn mặn, Đồ ngọt / Bánh','/uploads/1777433383737_ZLHHJzim47xFhx3wi84qoaWBZnDjEXWIZAe1xMKICiarjUhOsTbcgX3_VPTuAHXCHPxQk_k--_pTsQV6ShIkbec4fMLMCnJLoh9I81HMQ9MOjL0DHrwpXXThNa5kDn9Oc-Qgal2Ym1aek07sc33wfbHUeNQgpBJKo2vRucp3bSzgB05zPiPntIMUNf3nmchSHJML3g9KROAeaizi1ak (1).png;/uploads/1777817012481_Demographics Primarily Gen Z and University Students (18-22 years old, accounting for 58.9%). Purchasing Power Strict budget constraints, typically spending 25,000 – 60,000 VND per main meal. High.png','Tỉnh Bắc Ninh','Huyện Gia Bình',20,'T2,T3,T4,T5,T6,T7',30,'16:00-18:00, 17:00-19:00, 18:00-20:00, 19:00-21:00','/uploads/1778026797464_Gemini_Generated_Image_hm9xplhm9xplhm9x.png',NULL,'2026-05-08 10:02:43.315964',6,NULL),(4,'Nghi Xuân',0,NULL,NULL,'2026-05-06 07:34:10.018444','Ngon',NULL,NULL,NULL,NULL,'0777451107','ACTIVE','Tiệm bánh nhà làm',0,'2026-05-08 01:48:47.081729',4,'manual','13131312312313','NGUYEN PHAN ANH','VietinBank','/uploads/1778027650012_ContextDiagram.drawio (2).png','Nhà hàng / Quán ăn','Đồ ngọt / Bánh, Thực phẩm đóng gói','/uploads/1778027649991_Demographics Primarily Gen Z and University Students (18-22 years old, accounting for 58.9%). Purchasing Power Strict budget constraints, typically spending 25,000 – 60,000 VND per main meal. High.png;/uploads/1778027650010_Code_Generated_Image (1).png','Tỉnh Hà Tĩnh','Huyện Nghi Xuân',20,'T2,T3,T4,T5,T6,T7',30,'16:00-18:00, 17:00-19:00, 18:00-20:00, 19:00-21:00','/uploads/1778027650015_Use case diagram.drawio.png',NULL,'2026-05-08 01:48:47.081729',6,NULL);
/*!40000 ALTER TABLE `stores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `transaction_id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `net_amount` double DEFAULT NULL,
  `platform_fee` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `FKfyxndk58yiq2vpn0yd4m09kbt` (`order_id`),
  KEY `FK76d23hljajshvpfgctye587jp` (`store_id`),
  CONSTRAINT `FK76d23hljajshvpfgctye587jp` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  CONSTRAINT `FKfyxndk58yiq2vpn0yd4m09kbt` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  `reset_token` varchar(255) DEFAULT NULL,
  `reset_token_expiry` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `role_id` bigint DEFAULT NULL,
  `work_store_id` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  KEY `FKp56c1712k691lhsyewcssf40f` (`role_id`),
  KEY `FKh1k2y6o88sptjcp330db97n1d` (`work_store_id`),
  CONSTRAINT `FKh1k2y6o88sptjcp330db97n1d` FOREIGN KEY (`work_store_id`) REFERENCES `stores` (`store_id`),
  CONSTRAINT `FKp56c1712k691lhsyewcssf40f` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,_binary '',NULL,NULL,'2026-04-25 12:44:46.774245','','NGUYEN ANH','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','0777451107','local',NULL,NULL,NULL,NULL,'anhphan',1,NULL),(2,_binary '\0','null, null, null',NULL,'2026-04-25 19:01:27.617957','test@example.com','Test User','$2a$10$6BWei0UlsZriBPJkl8c0ruKA6E8eV.Fpg1chJSnH7FZryPDoLsjj6',NULL,'local',NULL,NULL,NULL,'2026-05-08 15:08:52.488466','test',1,NULL),(3,_binary '','Đường Quốc Lộ 21, Huyện Gia Bình, Tỉnh Bắc Ninh',NULL,'2026-04-25 21:20:02.484247','anhnphe186085@fpt.edu.vn','Nguyễn Trung Anh','$2a$10$UMEoQc2MIyuX47gpOUlLOuH1L2raXku4mwhKnINfyImctaHqKjpcm','0832132728','local',NULL,NULL,NULL,NULL,'anhnphe186085',1,NULL),(4,_binary '','Nghi Xuân, Huyện Nghi Xuân, Tỉnh Hà Tĩnh',NULL,'2026-05-06 07:34:09.942043','trumphagamesteam@gmail.com','anh phan','$2a$10$ulCZpjUWaZMDsuZHBPIv6eJqIGTReKylPXOdWy1XVcD76Vhg/runy','0777451107','local',NULL,NULL,NULL,NULL,'trumphagamesteam',1,NULL),(5,_binary '',NULL,NULL,'2026-05-06 07:50:36.741126','nguyenxuanphananh@gmail.com','Nguyễn Phan Anh','$2a$10$bMqC5s/7ZYMfEwYRZrBHQ.N8prItDZ.Ubi0o1kpjI0.QiqtMpqAVm','0832132728','local',NULL,NULL,NULL,NULL,'nguyenxuanphananh',2,NULL),(6,_binary '','Hồ Chí Minh, Việt Nam',NULL,'2026-05-07 15:05:58.000000','admin@dealxanh.com','Quản Trị Viên Hệ Thống','$2a$10$wOtijK2eLTfydik7APTA3uzaCmZqyL0tDJ7w7fleBLtBybjnqcJ6W','0123456789','local',NULL,NULL,NULL,'2026-05-07 15:05:58.000000','admin',3,NULL),(7,_binary '','Hà Nội, Việt Nam',NULL,'2026-05-07 15:05:58.000000','moderator@dealxanh.com','Moderator Hệ Thống','$2a$10$0jkW3rBWcNPlp3THSNI69uR0xfOkU/QJllKTIMi3t/yMU.a6YOLm2','0987654321','local',NULL,NULL,NULL,'2026-05-07 15:05:58.000000','moderator',4,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-13  8:50:05
