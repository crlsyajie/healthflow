-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 05, 2025 at 12:17 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `healthflow`
--

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `id` bigint(20) NOT NULL,
  `contact_number` varchar(255) DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `employee_id` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `appointments`
--

CREATE TABLE `appointments` (
  `id` bigint(20) NOT NULL,
  `patient_id` bigint(20) NOT NULL,
  `doctor_id` bigint(20) NOT NULL,
  `status` enum('PENDING','CONFIRMED','CANCELLED','COMPLETED','RESCHEDULED') NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `appointment_time` datetime(6) NOT NULL,
  `duration_minutes` int(11) NOT NULL,
  `reason` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `appointments`
--

INSERT INTO `appointments` (`id`, `patient_id`, `doctor_id`, `status`, `notes`, `created_at`, `updated_at`, `appointment_time`, `duration_minutes`, `reason`) VALUES
(1, 1, 1, 'PENDING', NULL, '2025-04-25 23:11:40', '2025-04-29 00:47:46', '2025-04-29 09:00:00.000000', 30, 'Regular checkup'),
(2, 1, 1, 'CANCELLED', NULL, '2025-04-23 23:11:40', '2025-04-29 00:47:50', '2025-04-30 14:30:00.000000', 30, 'Follow-up visit'),
(3, 1, 1, 'CANCELLED', NULL, '2025-04-18 23:11:40', '2025-04-29 00:47:54', '2025-05-06 11:00:00.000000', 30, 'Annual physical'),
(4, 1, 1, 'COMPLETED', NULL, '2025-04-14 23:11:40', '2025-04-28 23:11:40', '2025-04-22 10:00:00.000000', 30, 'Blood test'),
(11, 1, 1, 'COMPLETED', 'asdasd', '2025-05-03 04:51:50', '2025-05-05 09:44:41', '2025-05-31 08:30:00.000000', 30, 'asdasd'),
(13, 5, 4, 'PENDING', 'asd', '2025-05-05 02:03:00', '2025-05-05 10:03:00', '2025-05-13 02:30:00.000000', 30, 'yu');

-- --------------------------------------------------------

--
-- Table structure for table `departments`
--

CREATE TABLE `departments` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `departments`
--

INSERT INTO `departments` (`id`, `name`, `description`) VALUES
(1, 'General Medicine', 'Department of General Medicine'),
(2, 'Cardiology', 'Department of Cardiology'),
(3, 'Orthopedics', 'Department of Orthopedics'),
(4, 'Neurology', 'Department of Neurology'),
(5, 'Pediatrics', 'Department of Pediatrics'),
(6, 'Dermatology', 'Department of Dermatology'),
(7, 'Ophthalmology', 'Department of Ophthalmology'),
(8, 'Gynecology', 'Department of Gynecology');

-- --------------------------------------------------------

--
-- Table structure for table `doctors`
--

CREATE TABLE `doctors` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `department_id` bigint(20) DEFAULT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `specialization` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `bio` varchar(255) DEFAULT NULL,
  `license_number` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctors`
--

INSERT INTO `doctors` (`id`, `user_id`, `department_id`, `first_name`, `last_name`, `specialization`, `phone_number`, `bio`, `license_number`) VALUES
(1, 2, 1, 'Test', 'Doctor', 'General Medicine', '123-456-7890', 'This is a test doctor account created for testing the doctor functionalities.', 'DR12345'),
(4, 11, 2, 'cardo', 'di namamatay', 'malakas cumardio', '092222222', 'asdasd', 'CR2521'),
(5, 12, 3, 'orthoi', 'bayolente', 'bayodigrpiy', '3456363423', 'dasfasd', 'DR5312323'),
(6, 13, 4, 'nero', 'logy', 'nuro nuro', '1534122345', 'asdasd', 'af3123r'),
(8, 16, NULL, 'Doctor', 'specialist', 'General Medicine', '000-000-0000', 'Doctor profile automatically created by system fix.', 'DRE02CDD59');

-- --------------------------------------------------------

--
-- Table structure for table `doctor_availability`
--

CREATE TABLE `doctor_availability` (
  `id` bigint(20) NOT NULL,
  `doctor_id` bigint(20) NOT NULL,
  `day_of_week` enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` varchar(1000) NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `type` enum('APPOINTMENT_CANCELLED','APPOINTMENT_CREATED','APPOINTMENT_UPDATED','SYSTEM_NOTIFICATION','USER_MESSAGE') NOT NULL,
  `related_id` bigint(20) DEFAULT NULL,
  `appointment_id` bigint(20) DEFAULT NULL,
  `read` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `is_read`, `created_at`, `type`, `related_id`, `appointment_id`, `read`) VALUES
(6, 4, 'Appointment Canceled', 'Appointment with Dr. Doctor for ge talon on 2025-05-24 15:00 has been canceled', 0, '2025-05-05 00:42:25', 'APPOINTMENT_CANCELLED', NULL, NULL, 0),
(7, 4, 'Appointment Canceled', 'Appointment with Dr. Specialist for ge talon on 2025-03-30 17:30 has been canceled', 0, '2025-05-05 00:42:34', 'APPOINTMENT_CANCELLED', NULL, NULL, 0);

-- --------------------------------------------------------

--
-- Table structure for table `patients`
--

CREATE TABLE `patients` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `date_of_birth` date NOT NULL,
  `gender` enum('MALE','FEMALE','OTHER') DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `patients`
--

INSERT INTO `patients` (`id`, `user_id`, `first_name`, `last_name`, `date_of_birth`, `gender`, `phone_number`, `address`) VALUES
(1, 4, 'ge', 'talon', '1990-01-01', 'MALE', '987-654-3210', '123 Test Street'),
(2, 5, 'Jane', 'Doe', '1985-05-15', 'FEMALE', '555-987-6543', '456 Oak Avenue'),
(4, 10, 'patiente', 'uno', '2003-01-03', 'FEMALE', '09456025423', 'jan sa tabi tabi'),
(5, 15, 'may', 'sakit', '2009-02-13', 'FEMALE', '098239541', 'asjdhfjkads');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `role` enum('PATIENT','DOCTOR','ADMIN') NOT NULL,
  `enabled` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `email`, `role`, `enabled`) VALUES
(1, 'admin', '$2a$10$YTITB7jt11PzSkFtMKraROMbw16Uyu7/nYZX2Zj1YrsxP7rtJwl8q', 'admin@example.com', 'ADMIN', 1),
(2, 'testdoctor', '$2a$10$/4BkVcu04hdClA50z0DuCeWV7x.Coh33Uw.VMH2rLicPOZKcmraJK', 'testdoctor@example.com', 'DOCTOR', 1),
(4, 'testpatient', '$2a$10$YHuv98rytu6koAUS0ttzROmhm02clUtq3CxXubs9txJ1C3qFBBzby', 'testpatient@example.com', 'PATIENT', 1),
(5, 'janedoe', '$2a$10$MTpdLJ6TSErfrR8P5Vo/MuTPgV8VOK5gFER0Ol2Ns2XL5d4afAcB.', 'jane.doe@example.com', 'PATIENT', 1),
(6, 'testadmin', 'password', 'testadmin@gmail.com', 'ADMIN', 6),
(10, 'patiente', '$2a$10$YbirlzC4vmRBD6xwwhIKuOHwweB2AI3ODU2rLYxw5VVwT1KAZhocK', 'patiente@gmail.com', 'PATIENT', 1),
(11, 'cardo', '$2a$10$Jeh08yh1JFZBbinWm21w3uRiFAMrzjHyRPD42jtxrq8D.QPL8id7W', 'cardo@gmail.com', 'DOCTOR', 1),
(12, 'ortho', '$2a$10$GJLcj3k36JTfK9Ze.cDslO/qSPiSEqrqz4FL1gBlm8xRTGhXnqUU6', 'ortho@gmail.com', 'DOCTOR', 1),
(13, 'nero', '$2a$10$SuWPcpjjDJA1EdKjrGzrRe2V4dMr7N2.i8o3B55MsdFKtnlih9jx6', 'nero@gmail.com', 'DOCTOR', 1),
(15, 'sakit', '$2a$10$sD0x0V8SSyPFwXwZKvkOs.yzuGN5Vjchgot1QKhPud18Q8HotzQTq', 'sakitin@gmail.com', 'PATIENT', 1),
(16, 'specialist', '$2a$10$BJt5vvg/is/PX.1izDXEku600t9DiwzrEZ5RbQYnsi0caKz1fjktC', 'specialist@example.com', 'DOCTOR', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKpiovo1hsx7hi5f9ax85epqya9` (`user_id`),
  ADD UNIQUE KEY `UKr331v2yj8mf0btv2ow0g6wuiy` (`employee_id`);

--
-- Indexes for table `appointments`
--
ALTER TABLE `appointments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `patient_id` (`patient_id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Indexes for table `departments`
--
ALTER TABLE `departments`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `doctors`
--
ALTER TABLE `doctors`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK1xu5x0jae737xae254t4rgcd1` (`license_number`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `department_id` (`department_id`);

--
-- Indexes for table `doctor_availability`
--
ALTER TABLE `doctor_availability`
  ADD PRIMARY KEY (`id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `FKssk9idpjfjhwan3hhxle73wfo` (`appointment_id`);

--
-- Indexes for table `patients`
--
ALTER TABLE `patients`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admins`
--
ALTER TABLE `admins`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `appointments`
--
ALTER TABLE `appointments`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `departments`
--
ALTER TABLE `departments`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `doctors`
--
ALTER TABLE `doctors`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `doctor_availability`
--
ALTER TABLE `doctor_availability`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `patients`
--
ALTER TABLE `patients`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `admins`
--
ALTER TABLE `admins`
  ADD CONSTRAINT `FKgc8dtql9mkq268detxiox7fpm` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `appointments`
--
ALTER TABLE `appointments`
  ADD CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  ADD CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`);

--
-- Constraints for table `doctors`
--
ALTER TABLE `doctors`
  ADD CONSTRAINT `doctors_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `doctors_ibfk_2` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`);

--
-- Constraints for table `doctor_availability`
--
ALTER TABLE `doctor_availability`
  ADD CONSTRAINT `doctor_availability_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`);

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `FKssk9idpjfjhwan3hhxle73wfo` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`),
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `patients`
--
ALTER TABLE `patients`
  ADD CONSTRAINT `patients_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
