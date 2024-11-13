-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: mysqldb:3306
-- Gegenereerd op: 05 apr 2023 om 11:03
-- Serverversie: 8.0.32
-- PHP-versie: 8.1.17

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_swiftion_test`
--
CREATE DATABASE IF NOT EXISTS `db_swiftion_test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `db_swiftion_test`;

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`%` PROCEDURE `deleteCostCenter` (IN `cost_center_id` INT(10))   DELETE FROM
    cost_center
                                                                                       WHERE cost_center.id = cost_center_id$$

CREATE DEFINER=`root`@`%` PROCEDURE `deleteCustomTransaction` (IN `id` INT)   DELETE
                                                                              FROM custom_transaction
                                                                              WHERE custom_transaction.id = id$$

CREATE DEFINER=`root`@`%` PROCEDURE `deleteSwiftCopy` (IN `swift_copy_id` INT)   BEGIN
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
START TRANSACTION;
SELECT (SELECT `60F_opening_balance_id` FROM `swift_copy` WHERE id = swift_copy_id) INTO @id60f;
SELECT (SELECT `62F_closing_balance_id` FROM `swift_copy` WHERE id = swift_copy_id) INTO @id62f;
SELECT (SELECT `64_closing_available_balance_id` FROM `swift_copy` WHERE id = swift_copy_id) INTO @id64;

DELETE FROM `61_statement_line` WHERE `61_statement_line`.`swift_copy_id` = swift_copy_id;
DELETE FROM `60f_opening_balance` WHERE `60f_opening_balance`.`id` = @id60f;
DELETE FROM `62f_closing_balance` WHERE `62f_closing_balance`.`id` = @id62f;
DELETE FROM `64_closing_available_balance` WHERE `64_closing_available_balance`.`id` = @id64;
DELETE FROM swift_copy WHERE swift_copy.id = swift_copy_id;
COMMIT;


END$$

CREATE DEFINER=`root`@`%` PROCEDURE `getCardTypes` ()   SELECT transaction_type.id, transaction_type.name
                                                        FROM transaction_type$$

CREATE DEFINER=`root`@`%` PROCEDURE `getCostCenters` ()   SELECT cost_center.id, cost_center.name
                                                          FROM cost_center$$

CREATE DEFINER=`root`@`%` PROCEDURE `getCurrencies` ()  DETERMINISTIC SELECT currency_type.id, currency_type.currency_symbol FROM currency_type$$

CREATE DEFINER=`root`@`%` PROCEDURE `getCurrentPassword` (IN `id` INT(10))   SELECT user.password
                                                                             FROM user
                                                                             WHERE user.id = id$$

CREATE DEFINER=`root`@`%` PROCEDURE `getCustomTransaction` (IN `id` INT(10))   SELECT cost_center.id        as cost_center_id,
                                                                                      cost_center.name      as name,
                                                                                      currency_type.id      as currency_type_id,
                                                                                      custom_transaction.id as custom_transaction_id,
                                                                                      currency_type.currency_symbol,
                                                                                      custom_transaction.payment_date,
                                                                                      custom_transaction.amount,
                                                                                      custom_transaction.description
                                                                               FROM custom_transaction
                                                                                        INNER JOIN cost_center ON custom_transaction.cost_center_id = cost_center.id
                                                                                        INNER JOIN currency_type ON custom_transaction.currency_type_id = currency_type.id
                                                                               WHERE custom_transaction.id = id$$

CREATE DEFINER=`root`@`%` PROCEDURE `getCustomTransactionCostCenter` ()   SELECT
                                                                              cost_center.id as cost_center_id,
                                                                              cost_center.name as cost_center_name,
                                                                              FORMAT(SUM(REPLACE(custom_transaction.amount, ',', '.')), 2) as custom_transaction_amount,
                                                                              COUNT(custom_transaction.id) as custom_transaction_total
                                                                          FROM cost_center
                                                                                   LEFT JOIN custom_transaction
                                                                                             ON custom_transaction.cost_center_id = cost_center.id
                                                                          GROUP BY
                                                                              cost_center.name$$

CREATE DEFINER=`root`@`%` PROCEDURE `getCustomTransactions` ()   SELECT custom_transaction.payment_date as payment_date, custom_transaction.description as custom_transaction_description, custom_transaction.amount as amount, custom_transaction.id as custom_transaction_id, cost_center.name as cost_center_name, currency_type.currency_symbol as currency_symbol FROM custom_transaction INNER JOIN cost_center ON cost_center.id = custom_transaction.cost_center_id INNER JOIN currency_type ON custom_transaction.currency_type_id = currency_type.id$$

CREATE DEFINER=`root`@`%` PROCEDURE `getMt940Overview` ()   SELECT *
                                                            FROM mt940Overview$$

CREATE DEFINER=`root`@`%` PROCEDURE `getMt940Transactions` ()   SELECT `61_statement_line`.`value_date` AS value_date, `61_statement_line`.`amount` AS amount, `61_statement_line`.`payment_reference` AS payment_reference, `61_statement_line`.`swift_copy_id`, `61_statement_line`.`id` as transaction_id, `61_statement_line`.`86_statement_line_narrative` as transaction_narrative, `61_statement_line`.`custom_description` as custom_description, `61_statement_line`.`transaction_type_id` as transaction_type_id, cost_center.name AS cost_center_name FROM `61_statement_line` LEFT JOIN cost_center ON cost_center.id = `61_statement_line`.`cost_center_id`$$

CREATE DEFINER=`root`@`%` PROCEDURE `getPlugins` ()   SELECT
                                                          plugin.id as id,
                                                          plugin.online as online,
                                                          plugin.name as name
                                                      FROM plugin$$

CREATE DEFINER=`root`@`%` PROCEDURE `getRoles` ()   SELECT
                                                        role.id as role_id,
                                                        role.name as name
                                                    FROM role$$

CREATE DEFINER=`root`@`%` PROCEDURE `getStatement` (IN `statementId` INT)   SELECT *
                                                                            FROM statements
                                                                            WHERE id = statementId$$

CREATE DEFINER=`root`@`%` PROCEDURE `getSwiftCopyTransactionCostCenter` ()   SELECT cc.id AS cost_center_id, cc.name AS cost_center_name, tt.id AS transaction_type_id, tt.name AS transaction_type_name, COALESCE(FORMAT(SUM(REPLACE(`sl`.amount, ',', '.')), 2), 0) AS swift_copy_amount,
                                                                                    COUNT(sl.`swift_copy_id`) as swift_copy_total
                                                                             FROM cost_center AS cc
                                                                                      CROSS JOIN transaction_type AS tt
                                                                                      LEFT JOIN `61_statement_line` AS sl ON sl.cost_center_id = cc.id AND sl.transaction_type_id = tt.id
                                                                             GROUP BY cc.id, cc.name, tt.id, tt.name$$

CREATE DEFINER=`root`@`%` PROCEDURE `getTransaction` (IN `transactionId` INT)   SELECT
                                                                                    id,
                                                                                    swift_copy_id,
                                                                                    cost_center_id,
                                                                                    transaction_type_id,
                                                                                    value_date,
                                                                                    entry_date,
                                                                                    amount,
                                                                                    transaction_type,
                                                                                    identification_code,
                                                                                    payment_reference,
                                                                                    transaction_reference,
                                                                                    additional_information,
                                                                                    `86_statement_line_narrative` AS statement_line_narrative,
                                                                                    custom_description
                                                                                FROM `61_statement_line`
                                                                                WHERE id = transactionId$$

CREATE DEFINER=`root`@`%` PROCEDURE `getUser` (IN `userId` INT)   SELECT u.id, u.email, u.firstname, u.lastname, u.role_id, r.name AS rolename
                                                                  FROM user as u
                                                                           INNER JOIN role as r
                                                                                      ON u.role_id = r.id
                                                                  WHERE u.id = userId$$

CREATE DEFINER=`root`@`%` PROCEDURE `getUserLogin()` (IN `mail` VARCHAR(100))   SELECT user.email, user.password, role.name As roleName, role.id AS roleId, user.id As userId
                                                                                FROM user
                                                                                         INNER JOIN role
                                                                                                    ON user.role_id = role.id
                                                                                WHERE user.email = mail$$

CREATE DEFINER=`root`@`%` PROCEDURE `getUsers` ()   SELECT
                                                        user.id as user_id,
                                                        user.email as user_email,
                                                        user.firstname as user_firstname,
                                                        user.lastname as user_lastname,
                                                        role.name as role_name,
                                                        role.id as role_id
                                                    FROM user
                                                             INNER JOIN role
                                                                        ON role.id = user.role_id$$

CREATE DEFINER=`root`@`%` PROCEDURE `insert60F` (IN `id` INT UNSIGNED, IN `transaction_type_id` INT UNSIGNED, IN `currency_type_id` INT UNSIGNED, IN `date` VARCHAR(50), IN `balance` VARCHAR(50))   INSERT INTO `60f_opening_balance`(`id`, `transaction_type_id`, `currency_type_id`, `date`, `balance`)
                                                                                                                                                                                              VALUES (id, transaction_type_id, currency_type_id, date, balance)$$

CREATE DEFINER=`root`@`%` PROCEDURE `insert61` (IN `swift_copy_id` INT, IN `transaction_type_id` INT, IN `value_date` VARCHAR(20), IN `entry_date` VARCHAR(20), IN `amount` VARCHAR(20), IN `transaction_type` VARCHAR(20), IN `identification_code` VARCHAR(10), IN `payment_reference` VARCHAR(20), IN `transaction_reference` VARCHAR(20), IN `additional_information` VARCHAR(80), IN `statement_line_narrative` VARCHAR(500))   INSERT INTO `61_statement_line`(`swift_copy_id`, `cost_center_id`, `transaction_type_id`, `value_date`, `entry_date`, `amount`,
                                                                                                                                                                                                                                                                                                                                                                                                                                                              `transaction_type`, `identification_code`, `payment_reference`, `transaction_reference`,
                                                                                                                                                                                                                                                                                                                                                                                                                                                              `additional_information`, `86_statement_line_narrative`)
                                                                                                                                                                                                                                                                                                                                                                                                                              VALUES (swift_copy_id, 1, transaction_type_id, value_date, entry_date, amount, transaction_type, identification_code,
                                                                                                                                                                                                                                                                                                                                                                                                                                      payment_reference, transaction_reference, additional_information, statement_line_narrative)$$

CREATE DEFINER=`root`@`%` PROCEDURE `insert62f` (IN `id` INT UNSIGNED, IN `transaction_type_id` INT UNSIGNED, IN `currency_type_id` INT UNSIGNED, IN `date` VARCHAR(20), IN `balance` VARCHAR(20))   INSERT INTO `62f_closing_balance`
                                                                                                                                                                                              (`id`, `transaction_type_id`, `currency_type_id`, `date`, `balance`)
                                                                                                                                                                                              VALUES (id, transaction_type_id, currency_type_id, date, balance)$$

CREATE DEFINER=`root`@`%` PROCEDURE `insert64` (IN `id` INT UNSIGNED, IN `transaction_type_id` INT UNSIGNED, IN `currency_type_id` INT UNSIGNED, IN `date` VARCHAR(20), IN `balance` VARCHAR(20))   INSERT INTO `64_closing_available_balance`
                                                                                                                                                                                             (`id`, `transaction_type_id`, `currency_type_id`, `date`, `balance`)
                                                                                                                                                                                             VALUES (id, transaction_type_id, currency_type_id, date, balance)$$

CREATE DEFINER=`root`@`%` PROCEDURE `insertCostCenter` (IN `costCenter` VARCHAR(255))   BEGIN
    IF NOT EXISTS(SELECT 1 FROM cost_center WHERE `name` = costCenter) THEN
        INSERT INTO `cost_center`(`name`) VALUES (costCenter);
ELSE
SELECT 'Cost center already exists in the database.' AS message;
END IF;
END$$

CREATE DEFINER=`root`@`%` PROCEDURE `insertCustomTransaction` (IN `cost_center_id` INT(10) UNSIGNED, IN `currency_type_id` INT(10) UNSIGNED, IN `payment_date` VARCHAR(50), IN `amount` VARCHAR(20), IN `description` TEXT)   INSERT INTO `custom_transaction`( `cost_center_id`, `currency_type_id`, `payment_date`, `amount`, `description`) VALUES (cost_center_id, currency_type_id, payment_date, amount, description)$$

CREATE DEFINER=`root`@`%` PROCEDURE `insertSwiftCopyBase` (IN `id` INT UNSIGNED, IN `user_id` INT UNSIGNED, IN `transaction_reference` VARCHAR(20), IN `account_identification` VARCHAR(40), IN `statement_number` VARCHAR(20), IN `closing_balance_id` INT UNSIGNED, IN `opening_balance_id` INT UNSIGNED, IN `closing_available_balance_id` INT UNSIGNED)   INSERT INTO `swift_copy`
                                                                                                                                                                                                                                                                                                                                                              (`id`, `user_id`, `20_transaction_reference`, `25_account_identification`, `28C_statement_number`,
                                                                                                                                                                                                                                                                                                                                                               `62F_closing_balance_id`, `60F_opening_balance_id`, `64_closing_available_balance_id`)
                                                                                                                                                                                                                                                                                                                                                              VALUES (id, user_id, transaction_reference, account_identification, statement_number, closing_balance_id,
                                                                                                                                                                                                                                                                                                                                                                      opening_balance_id, closing_available_balance_id)$$

CREATE DEFINER=`root`@`%` PROCEDURE `insertUser` (IN `role_id` INT(10), IN `email` VARCHAR(100), IN `password_hashed` VARCHAR(150), IN `firstname` VARCHAR(50), IN `lastname` VARCHAR(50))   INSERT INTO
                                                                                                                                                                                                 `user`
                                                                                                                                                                                             (`role_id`, `email`, `password`, `firstname`, `lastname`)
                                                                                                                                                                                             VALUES
                                                                                                                                                                                                 (role_id, email, password_hashed, firstname, lastname)$$

CREATE DEFINER=`root`@`%` PROCEDURE `updateCustomTransaction` (IN `custom_transaction_id` INT(10), IN `cost_center_id` INT(10), IN `payment_date` DATETIME, IN `amount` VARCHAR(20), IN `description` TEXT)   UPDATE `custom_transaction`
                                                                                                                                                                                                              SET `cost_center_id`=cost_center_id,
                                                                                                                                                                                                                  `payment_date`=payment_date,
                                                                                                                                                                                                                  `amount`=amount,
                                                                                                                                                                                                                  `description`=description
                                                                                                                                                                                                              WHERE custom_transaction.id = custom_transaction_id$$

CREATE DEFINER=`root`@`%` PROCEDURE `updatePassword` (IN `user_id` INT(10), IN `password` VARCHAR(150))   UPDATE user
                                                                                                          SET PASSWORD = password
                                                                                                          WHERE user.id = user_id$$

CREATE DEFINER=`root`@`%` PROCEDURE `updatePlugin` (IN `plugin_id` INT(10), IN `state` INT(1))   UPDATE plugin
                                                                                                 SET plugin.online = state
                                                                                                 WHERE plugin.id = plugin_id$$

CREATE DEFINER=`root`@`%` PROCEDURE `updateTransaction` (IN `transactionId` INT, IN `costCenter` INT, IN `description` TEXT)   UPDATE `61_statement_line`
                                                                                                                               SET
                                                                                                                                   cost_center_id = costCenter,
                                                                                                                                   custom_description = description
                                                                                                                               WHERE id = transactionId$$

CREATE DEFINER=`root`@`%` PROCEDURE `updateUser` (IN `role_id` INT(10), IN `user_id` INT(10), IN `email` VARCHAR(100), IN `firstname` VARCHAR(50), IN `lastname` VARCHAR(50))   UPDATE `user` SET `role_id`=role_id,`email`=email ,`firstname`=firstname,`lastname`=lastname WHERE user.id = user_id$$

CREATE DEFINER=`root`@`%` PROCEDURE `updateUsername` (IN `userId` INT, IN `username` TEXT, IN `firstName` TEXT, IN `lastName` TEXT)   UPDATE user
                                                                                                                                      SET email = username, firstname = firstName, lastname = lastName
                                                                                                                                      WHERE id = userId$$

CREATE DEFINER=`root`@`%` PROCEDURE `updateUserRole` (IN `user_id` INT(10), IN `role_id` INT(10))   UPDATE `user` SET `role_id`=role_id WHERE user.id = user_id$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `60f_opening_balance`
--

CREATE TABLE `60f_opening_balance` (
  `id` int NOT NULL,
  `transaction_type_id` int NOT NULL,
  `currency_type_id` int NOT NULL,
  `date` datetime NOT NULL,
  `balance` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `60f_opening_balance`
--

INSERT INTO `60f_opening_balance` (`id`, `transaction_type_id`, `currency_type_id`, `date`, `balance`) VALUES
(1, 2, 1, '2023-02-08 00:00:00', '2410,27\r');

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `61_statement_line`
--

CREATE TABLE `61_statement_line` (
  `id` int NOT NULL,
  `swift_copy_id` int NOT NULL,
  `cost_center_id` int DEFAULT NULL,
  `transaction_type_id` int NOT NULL,
  `value_date` date NOT NULL,
  `entry_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `amount` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `transaction_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `identification_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `payment_reference` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `transaction_reference` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `additional_information` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `86_statement_line_narrative` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `custom_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `61_statement_line`
--

INSERT INTO `61_statement_line` (`id`, `swift_copy_id`, `cost_center_id`, `transaction_type_id`, `value_date`, `entry_date`, `amount`, `transaction_type`, `identification_code`, `payment_reference`, `transaction_reference`, `additional_information`, `86_statement_line_narrative`, `custom_description`) VALUES
(1, 1, 1, 1, '2023-02-09', '0209', '223,04', 'N', 'DDT', 'EREF', '00000000002592', 'null', '\nEREF\nE6364099P3182268\n\nMARF\nM3-E55\n\nCNTP\nNL96INGB0212812091\nINGBNL2A\nBidfood\n\n\nREMI\nUSTD\n\nDEBIT Maestro 38993 CL\n', NULL),
(2, 1, 1, 1, '2023-02-09', '0209', '75,25', 'N', 'TRF', 'PREF', '00000000004984', 'null', '\nPREF\nE1160183P2430796\n\nCNTP\nNL25RABO0167238268\nRABONL2U\nWit\n\n\nREMI\nUSTD\n\nfactuur 158654\n', NULL),
(3, 1, 1, 2, '2023-02-09', '0209', '82,62', 'N', 'TRF', 'EREF', '0000000000222', 'null', '\nEREF\nE2644188P6281777\n\nCNTP\nNL55ABAN0652321319\nABNANL2A\nD. Wolters\n\n\nREMI\nUSTD\n\nFactuur 687613 KlantNr 1011\n', NULL),
(4, 1, 1, 1, '2023-02-09', '0209', '53,82', 'N', 'TRF', 'PREF', '00000000003243', 'null', '\nPREF\nE850264P9387057\n\nCNTP\nNL57INGB0256233730\nINGBNL2A\nACC3917\n\n\nREMI\nUSTD\n\nfactuur 715363\n', NULL),
(5, 1, 1, 1, '2023-02-09', '0209', '791,74', 'N', 'DDT', 'PREF', '00000000004233', 'null', '\nPREF\nE8881636P4904748\n\nCNTP\nNL98RABO0864523878\nRABONL2U\nEasyPay terzake Bronsport B.V.\n\n\nREMI\nUSTD\n\niDeal payment id-760932\n', NULL),
(6, 1, 1, 2, '2023-02-09', '0209', '149,08', 'N', 'TRF', 'PREF', '00000000009098', 'null', '\nPREF\nE8804447P8803804\n\nCNTP\nNL34INGB0266738919\nINGBNL2A\nJ. Huisman\n\n\nREMI\nUSTD\n\nfactuur 994332\n', NULL),
(7, 1, 1, 2, '2023-02-09', '0209', '251,71', 'N', 'TRF', 'EREF', '00000000004244', 'null', '\nEREF\nE3981435P3110844\n\nCNTP\nNL98INGB0369260475\nINGBNL2A\nJ. Gerritsen\n\n\nREMI\nUSTD\n\nfactuur 40626\n', NULL),
(8, 1, 1, 2, '2023-02-09', '0209', '102,16', 'N', 'TRF', 'PREF', '00000000008643', 'null', '\nPREF\nE3727311P5799499\n\nCNTP\nNL44INGB0557226904\nINGBNL2A\nR. Dongen\n\n\nREMI\nUSTD\n\ncontributie Feb 2023\n', NULL),
(9, 1, 1, 1, '2023-02-09', '0209', '122,94', 'N', 'TRF', 'EREF', '00000000003264', 'null', '\nEREF\nE1079194P235137\n\nMARF\nM6-E34\n\nCNTP\nNL21BUNQ0638538546\nBUNQNL2A\nMoneytransfer\n\n\nREMI\nUSTD\n\nfactuur 588591\n', NULL),
(10, 1, 1, 2, '2023-02-09', '0209', '166,41', 'N', 'TRF', 'EREF', '00000000006147', 'null', '\nEREF\nE9859186P3285083\n\nCSID\nNL1142639Z6173189\n\nCNTP\nNL58INGB0690491114\nINGBNL2A\nZ. Pol\n\n\nREMI\nUSTD\n\nfactuur 784659\n', NULL),
(11, 1, 1, 2, '2023-02-09', '0209', '180,86', 'N', 'TRF', 'PREF', '00000000003081', 'null', '\nPREF\nE8672209P6495550\n\nCNTP\nNL92ABAN0858275602\nABNANL2A\nO. Schipper\n\n\nREMI\nUSTD\n\nFactuur 148819\n', NULL),
(12, 1, 1, 2, '2023-02-09', '0209', '142,72', 'N', 'TRF', 'EREF', '00000000001142', 'null', '\nEREF\nE1595410P5377044\n\nCNTP\nNL45INGB0342428056\nINGBNL2A\nH. Laan\n\n\nREMI\nUSTD\n\ncontributie FEB 23\n', NULL),
(13, 1, 1, 1, '2023-02-09', '0209', '685,02', 'N', 'DDT', 'EREF', '0000000000562', 'null', '\nEREF\nE8755727P8704443\n\nCNTP\nNL86KNAB0183229407\nKNABNL2H\nStripe terzake Idema\n\n\nREMI\nUSTD\n\niDeal payment id-844314\n', NULL),
(14, 1, 1, 2, '2023-02-09', '0209', '204,81', 'N', 'TRF', 'EREF', '00000000003825', 'null', '\nEREF\nE6836052P8685259\n\nCNTP\nNL74RABO0390688039\nRABONL2U\nI. Visser\n\n\nREMI\nUSTD\n\nfactuur 441103 Klantnr. 6978\n', NULL),
(15, 1, 1, 1, '2023-02-09', '0209', '94,29', 'N', 'TRF', 'EREF', '00000000006858', 'null', '\nEREF\nE6283114P8374277\n\nCNTP\nNL38BUNQ0904675047\nBUNQNL2A\nACC95475\n\n\nREMI\nUSTD\n\nFactuur 311477\n', NULL),
(16, 1, 1, 2, '2023-02-09', '0209', '105,33', 'N', 'TRF', 'EREF', '00000000006809', 'null', '\nEREF\nE9910960P9725348\n\nCNTP\nNL99KNAB0112577047\nKNABNL2H\nD. Smeets\n\n\nREMI\nUSTD\n\ncontributie Feb 23\n', NULL),
(17, 1, 1, 1, '2023-02-09', '0209', '359,35', 'N', 'DDT', 'EREF', '00000000007264', 'null', '\nEREF\nE6677075P460973\n\nCNTP\nNL13BUNQ0587794592\nBUNQNL2A\nHocras\n\n\nREMI\nUSTD\n\nDEBIT Maestro 21917 CL\n', NULL),
(18, 1, 1, 2, '2023-02-09', '0209', '270,39', 'N', 'TRF', 'EREF', '00000000009718', 'null', '\nEREF\nE6468922P7974169\n\nMARF\nM0-E86\n\nCSID\nNL2021196Z6540703\n\nCNTP\nNL50ABAN0869693293\nABNANL2A\nY. Jong\n\n\nREMI\nUSTD\n\nFactuur 332678\n', NULL),
(19, 1, 1, 1, '2023-02-09', '0209', '146,78', 'N', 'DDT', 'PREF', '00000000002598', 'null', '\nPREF\nE2364022P8398528\n\nCNTP\nNL51RABO0741303383\nRABONL2U\nEilers sport\n\n\nREMI\nUSTD\n\nCARD 53875 CL\n', NULL),
(20, 1, 1, 2, '2023-02-09', '0209', '48,50', 'N', 'TRF', 'PREF', '00000000008412', 'null', '\nPREF\nE8022174P9205860\n\nCNTP\nNL73KNAB0452729408\nKNABNL2H\nT. Martens\n\n\nREMI\nUSTD\n\ndonatie\n', NULL),
(21, 1, 1, 2, '2023-02-09', '0209', '179,50', 'N', 'TRF', 'PREF', '00000000006194', 'null', '\nPREF\nE402672P2421828\n\nCSID\nNL4836268Z6754888\n\nCNTP\nNL96BUNQ0732350342\nBUNQNL2A\nB. Groen\n\n\nREMI\nUSTD\n\nFactuur 926649\n', NULL),
(22, 1, 1, 2, '2023-02-09', '0209', '145,38', 'N', 'TRF', 'PREF', '00000000007073', 'null', '\nPREF\nE5607273P355702\n\nCNTP\nNL27RABO0384074517\nRABONL2U\nI. Smeets\n\n\nREMI\nUSTD\n\ncontributie feb 2023\n', NULL),
(23, 1, 1, 2, '2023-02-09', '0209', '169,16', 'N', 'TRF', 'PREF', '00000000002207', 'null', '\nPREF\nE4447015P6711064\n\nCNTP\nNL72BUNQ0247764324\nBUNQNL2A\nW. Vliet\n\n\nREMI\nUSTD\n\nfactuur 429729\n', NULL),
(24, 1, 1, 2, '2023-02-09', '0209', '114,69', 'N', 'TRF', 'PREF', '00000000005753', 'null', '\nPREF\nE2659642P2659355\n\nCNTP\nNL55ABAN0449532284\nABNANL2A\nR. Laan\n\n\nREMI\nUSTD\n\ncontributie feb 23\n', NULL),
(25, 1, 1, 2, '2023-02-09', '0209', '156,39', 'N', 'TRF', 'EREF', '000000000044', 'null', '\nEREF\nE4950164P5689372\n\nCSID\nNL2136092Z5914864\n\nCNTP\nNL65KNAB0700971429\nKNABNL2H\nM. Visser\n\n\nREMI\nUSTD\n\nfactuur 861023\n', NULL),
(26, 1, 1, 2, '2023-02-09', '0209', '251,21', 'N', 'TRF', 'EREF', '00000000004535', 'null', '\nEREF\nE6348253P8665830\n\nCNTP\nNL98RABO0565726691\nRABONL2U\nC. Loon\n\n\nREMI\nUSTD\n\nContributie feb 23\n', NULL),
(27, 1, 1, 2, '2023-02-09', '0209', '182,02', 'N', 'TRF', 'PREF', '00000000004848', 'null', '\nPREF\nE3162016P8752257\n\nCNTP\nNL50BUNQ0856353877\nBUNQNL2A\nC. Bosch\n\n\nREMI\nUSTD\n\nfactuur 115666 klantnr 9055\n', NULL),
(28, 1, 1, 2, '2023-02-09', '0209', '166,69', 'N', 'TRF', 'PREF', '00000000003849', 'null', '\nPREF\nE2411900P6838908\n\nCNTP\nNL33ABAN0134144154\nABNANL2A\nQ. Groot\n\n\nREMI\nUSTD\n\nFactuur 744803\n', NULL),
(29, 1, 1, 2, '2023-02-09', '0209', '245,07', 'N', 'TRF', 'EREF', '00000000009776', 'null', '\nEREF\nE3304346P7785936\n\nCNTP\nNL89ABAN0376350515\nABNANL2A\nG. Kuijpers\n\n\nREMI\nUSTD\n\nContributie feb 2023\n', NULL),
(30, 1, 1, 1, '2023-02-09', '0209', '121,96', 'N', 'TRF', 'PREF', '00000000003073', 'null', '\nPREF\nE4080430P893048\n\nCNTP\nNL48ABAN0508427257\nABNANL2A\nHuisman\n\n\nREMI\nUSTD\n\nFactuur 386911\n', NULL),
(31, 1, 1, 2, '2023-02-09', '0209', '143,18', 'N', 'TRF', 'EREF', '00000000008323', 'null', '\nEREF\nE5735764P6229788\n\nMARF\nM7-E4\n\nCNTP\nNL75RABO0802343441\nRABONL2U\nO. Smits\n\n\nREMI\nUSTD\n\ncontributie feb 2023\n', NULL),
(32, 1, 1, 2, '2023-02-09', '0209', '17,74', 'N', 'TRF', 'EREF', '00000000003561', 'null', '\nEREF\nE5188583P9427941\n\nCNTP\nNL79BUNQ0579360054\nBUNQNL2A\nI. Vermeulen\n\n\nREMI\nUSTD\n\nDonatie klantnr. 1468\n', NULL),
(33, 1, 1, 2, '2023-02-09', '0209', '90,89', 'N', 'TRF', 'EREF', '0000000000400', 'null', '\nEREF\nE5979096P1385351\n\nCNTP\nNL42KNAB0863398536\nKNABNL2H\nV. Verbeek\n\n\nREMI\nUSTD\n\nFactuur 128173\n', NULL),
(34, 1, 1, 2, '2023-02-09', '0209', '160,39', 'N', 'TRF', 'EREF', '00000000001541', 'null', '\nEREF\nE3562735P1131856\n\nCNTP\nNL94RABO0670775096\nRABONL2U\nH. Kok\n\n\nREMI\nUSTD\n\ncontributie FEB 2023\n', NULL),
(35, 1, 1, 1, '2023-02-09', '0209', '152,07', 'N', 'DDT', 'PREF', '0000000000811', 'null', '\nPREF\nE2483302P9826406\n\nMARF\nM0-E28\n\nCSID\nNL766850Z7434943\n\nCNTP\nNL65KNAB0366473426\nKNABNL2H\nHocras\n\n\nREMI\nUSTD\n\nPAS Mastercard-56405 CL\n', NULL),
(36, 1, 1, 2, '2023-02-09', '0209', '256,50', 'N', 'TRF', 'EREF', '00000000007752', 'null', '\nEREF\nE1194445P6377791\n\nCNTP\nNL45RABO0553745108\nRABONL2U\nD. Heuvel\n\n\nREMI\nUSTD\n\nFactuur 956536\n', NULL),
(37, 1, 1, 2, '2023-02-09', '0209', '118,02', 'N', 'TRF', 'PREF', '00000000008998', 'null', '\nPREF\nE6551799P6621173\n\nCSID\nNL6105633Z338221\n\nCNTP\nNL94INGB0947027051\nINGBNL2A\nE. Bos\n\n\nREMI\nUSTD\n\nFactuur 79410, Klantnr 452\n', NULL),
(38, 1, 1, 2, '2023-02-09', '0209', '233,01', 'N', 'TRF', 'EREF', '00000000003807', 'null', '\nEREF\nE9228979P7635320\n\nCSID\nNL151863Z1007795\n\nCNTP\nNL31RABO0164952807\nRABONL2U\nM. Boer\n\n\nREMI\nUSTD\n\ncontributie feb 2023\n', NULL),
(39, 1, 1, 2, '2023-02-09', '0209', '224,82', 'N', 'TRF', 'EREF', '00000000005311', 'null', '\nEREF\nE9696218P6612803\n\nCSID\nNL1460408Z7268066\n\nCNTP\nNL97RABO0329177544\nRABONL2U\nH. Prins\n\n\nREMI\nUSTD\n\nFactuur 677353\n', NULL),
(40, 1, 1, 2, '2023-02-09', '0209', '156,49', 'N', 'TRF', 'EREF', '00000000009740', 'null', '\nEREF\nE5971254P1001976\n\nCSID\nNL6752625Z8965588\n\nCNTP\nNL84ABAN0993540430\nABNANL2A\nK. Vink\n\n\nREMI\nUSTD\n\nfactuur 277368 klantnr 1197\n', NULL),
(41, 1, 1, 2, '2023-02-09', '0209', '242,66', 'N', 'TRF', 'PREF', '00000000008581', 'null', '\nPREF\nE3128070P3067892\n\nMARF\nM8-E77\n\nCNTP\nNL59RABO0979852138\nRABONL2U\nN. Vries\n\n\nREMI\nUSTD\n\ncontributie Feb 2023\n', NULL),
(42, 1, 1, 2, '2023-02-09', '0209', '131,07', 'N', 'TRF', 'PREF', '00000000003616', 'null', '\nPREF\nE8399906P311375\n\nMARF\nM0-E80\n\nCNTP\nNL41BUNQ0738865425\nBUNQNL2A\nD. Koning\n\n\nREMI\nUSTD\n\nContributie Feb 23\n', NULL),
(43, 1, 1, 2, '2023-02-09', '0209', '185,39', 'N', 'TRF', 'EREF', '00000000002915', 'null', '\nEREF\nE663701P2405624\n\nCNTP\nNL72RABO0913742334\nRABONL2U\nO. Molenaar\n\n\nREMI\nUSTD\n\ncontributie jan 23\n', NULL),
(44, 1, 1, 1, '2023-02-09', '0209', '535,02', 'N', 'DDT', 'PREF', '00000000007264', 'null', '\nPREF\nE511242P900664\n\nCNTP\nNL72BUNQ0814017326\nBUNQNL2A\nGlobalCollect terzake Bidfood\n\n\nREMI\nUSTD\n\niDeal betaling ID307687\n', NULL),
(45, 1, 1, 1, '2023-02-09', '0209', '483,11', 'N', 'DDT', 'EREF', '00000000009949', 'null', '\nEREF\nE6217180P1527055\n\nCNTP\nNL50ABAN0557472849\nABNANL2A\nGlobalCollect terzake Leba Sport\n\n\nREMI\nUSTD\n\niDeal betaling ID50706\n', NULL),
(46, 1, 1, 1, '2023-02-09', '0209', '413,02', 'N', 'DDT', 'EREF', '00000000003409', 'null', '\nEREF\nE9027171P9284284\n\nCNTP\nNL74ABAN0687922288\nABNANL2A\nGlobalCollect terzake Eilers sport\n\n\nREMI\nUSTD\n\niDeal betaling C513373\n', NULL),
(47, 1, 1, 2, '2023-02-09', '0209', '216,22', 'N', 'TRF', 'PREF', '00000000009312', 'null', '\nPREF\nE8320924P5457464\n\nCNTP\nNL91KNAB0781736930\nKNABNL2H\nJ. Leeuwen\n\n\nREMI\nUSTD\n\nFactuur 526212\n', NULL),
(48, 1, 1, 1, '2023-02-09', '0209', '599,37', 'N', 'DDT', 'PREF', '00000000007812', 'null', '\nPREF\nE2315148P7972617\n\nCSID\nNL616707Z3555305\n\nCNTP\nNL69BUNQ0667073246\nBUNQNL2A\nStripe terzake Idema\n\n\nREMI\nUSTD\n\niDeal payment ID890452\n', NULL),
(49, 1, 1, 2, '2023-02-09', '0209', '224,69', 'N', 'TRF', 'EREF', '00000000003373', 'null', '\nEREF\nE717213P4536436\n\nCNTP\nNL41BUNQ0369764656\nBUNQNL2A\nV. Wijk\n\n\nREMI\nUSTD\n\nContributie Feb 2023\n', NULL),
(50, 1, 1, 2, '2023-02-09', '0209', '180,53', 'N', 'TRF', 'EREF', '00000000003953', 'null', '\nEREF\nE7282592P3151352\n\nCNTP\nNL61BUNQ0811374077\nBUNQNL2A\nJ. Dam\n\n\nREMI\nUSTD\n\nContributie feb 23\n', NULL),
(51, 1, 1, 2, '2023-02-09', '0209', '141,75', 'N', 'TRF', 'EREF', '0000000000404', 'null', '\nEREF\nE7913340P1044357\n\nCNTP\nNL19INGB0407292795\nINGBNL2A\nF. Meulen\n\n\nREMI\nUSTD\n\nContributie jan 23\n', NULL),
(52, 1, 1, 1, '2023-02-09', '0209', '475,30', 'N', 'DDT', 'PREF', '00000000002522', 'null', '\nPREF\nE5049074P1454197\n\nMARF\nM2-E11\n\nCSID\nNL6676083Z2934295\n\nCNTP\nNL54KNAB0617849954\nKNABNL2H\nEasyPay terzake Eilers sport\n\n\nREMI\nUSTD\n\niDeal betaling id-654152\n', NULL),
(53, 1, 1, 2, '2023-02-09', '0209', '19,47', 'N', 'TRF', 'EREF', '00000000008995', 'null', '\nEREF\nE5401652P9077829\n\nMARF\nM6-E79\n\nCNTP\nNL21KNAB0814488382\nKNABNL2H\nB. Veenstra\n\n\nREMI\nUSTD\n\ndonatie\n', NULL),
(54, 1, 1, 1, '2023-02-09', '0209', '332,67', 'N', 'DDT', 'PREF', '00000000006123', 'null', '\nPREF\nE6458730P7957392\n\nCNTP\nNL86BUNQ0494532924\nBUNQNL2A\nBronsport B.V.\n\n\nREMI\nUSTD\n\nPAS Maestro 2294\n', NULL),
(55, 1, 1, 1, '2023-02-09', '0209', '115,40', 'N', 'TRF', 'EREF', '00000000006788', 'null', '\nEREF\nE4510523P7202448\n\nCNTP\nNL80INGB0895612512\nINGBNL2A\nBelastingdienst\n\n\nREMI\nUSTD\n\nRekening 64455\n', NULL),
(56, 1, 1, 2, '2023-02-09', '0209', '159,39', 'N', 'TRF', 'EREF', '00000000004114', 'null', '\nEREF\nE5971484P6719885\n\nCNTP\nNL82BUNQ0814943648\nBUNQNL2A\nJ. Pol\n\n\nREMI\nUSTD\n\nContributie Feb 23\n', NULL),
(57, 1, 1, 1, '2023-02-09', '0209', '760,69', 'N', 'DDT', 'EREF', '00000000001258', 'null', '\nEREF\nE5967019P3736490\n\nCSID\nNL9512307Z8864888\n\nCNTP\nNL86KNAB0355798302\nKNABNL2H\nStripe terzake Hocras\n\n\nREMI\nUSTD\n\niDeal betaling id-589059\n', NULL),
(58, 1, 1, 1, '2023-02-09', '0209', '736,26', 'N', 'DDT', 'EREF', '0000000000166', 'null', '\nEREF\nE9903784P8414031\n\nMARF\nM1-E20\n\nCSID\nNL8301681Z7369642\n\nCNTP\nNL53RABO0944694313\nRABONL2U\nStripe terzake Leba Sport\n\n\nREMI\nUSTD\n\niDeal betaling ID74465\n', NULL),
(59, 1, 1, 1, '2023-02-09', '0209', '482,53', 'N', 'DDT', 'PREF', '00000000005126', 'null', '\nPREF\nE5972731P7086153\n\nCSID\nNL5393168Z9034473\n\nCNTP\nNL22KNAB0985947658\nKNABNL2H\nGlobalCollect terzake Sligro\n\n\nREMI\nUSTD\n\niDeal payment C377155\n', NULL),
(60, 1, 1, 1, '2023-02-09', '0209', '409,56', 'N', 'DDT', 'EREF', '00000000005163', 'null', '\nEREF\nE1541538P1442069\n\nCSID\nNL1634067Z4563769\n\nCNTP\nNL17BUNQ0989235452\nBUNQNL2A\nEilers sport\n\n\nREMI\nUSTD\n\nDEBIT Maestro 54227\n', NULL),
(61, 1, 1, 2, '2023-02-09', '0209', '109,14', 'N', 'TRF', 'PREF', '00000000009168', 'null', '\nPREF\nE294394P6736533\n\nCNTP\nNL42ABAN0668323196\nABNANL2A\nL. Mulder\n\n\nREMI\nUSTD\n\nfactuur 23552\n', NULL),
(62, 1, 1, 2, '2023-02-09', '0209', '191,35', 'N', 'TRF', 'PREF', '0000000000853', 'null', '\nPREF\nE2517012P7025330\n\nCNTP\nNL37ABAN0262431422\nABNANL2A\nG. Bruin\n\n\nREMI\nUSTD\n\nContributie jan 2023\n', NULL),
(63, 1, 1, 2, '2023-02-09', '0209', '276,75', 'N', 'TRF', 'EREF', '00000000005319', 'null', '\nEREF\nE798823P1620421\n\nCSID\nNL5103404Z2104291\n\nCNTP\nNL90KNAB0887770208\nKNABNL2H\nO. Brouwer\n\n\nREMI\nUSTD\n\nFactuur 611440, klantnr 4530\n', NULL),
(64, 1, 1, 1, '2023-02-09', '0209', '286,18', 'N', 'DDT', 'PREF', '00000000001813', 'null', '\nPREF\nE949439P3901182\n\nCNTP\nNL84RABO0297797337\nRABONL2U\nLeba Sport\n\n\nREMI\nUSTD\n\nPIN Mastercard-59932 CL\n', NULL),
(65, 1, 1, 1, '2023-02-09', '0209', '455,19', 'N', 'DDT', 'PREF', '00000000003277', 'null', '\nPREF\nE1629351P9256776\n\nCNTP\nNL28BUNQ0679407913\nBUNQNL2A\nLeba Sport\n\n\nREMI\nUSTD\n\nDEBIT 93319 CL\n', NULL),
(66, 1, 1, 2, '2023-02-09', '0209', '108,07', 'N', 'TRF', 'PREF', '00000000002348', 'null', '\nPREF\nE4429118P456496\n\nCNTP\nNL11ABAN0118268612\nABNANL2A\nN. Boer\n\n\nREMI\nUSTD\n\ncontributie Feb 2023\n', NULL),
(67, 1, 1, 2, '2023-02-09', '0209', '189,04', 'N', 'TRF', 'EREF', '00000000001114', 'null', '\nEREF\nE3270576P6272303\n\nCSID\nNL8573045Z2723133\n\nCNTP\nNL82RABO0393562363\nRABONL2U\nY. Hendriks\n\n\nREMI\nUSTD\n\nFactuur 940375\n', NULL);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `62f_closing_balance`
--

CREATE TABLE `62f_closing_balance` (
  `id` int NOT NULL,
  `transaction_type_id` int NOT NULL,
  `currency_type_id` int NOT NULL,
  `date` datetime DEFAULT NULL,
  `balance` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `62f_closing_balance`
--

INSERT INTO `62f_closing_balance` (`id`, `transaction_type_id`, `currency_type_id`, `date`, `balance`) VALUES
(1, 2, 1, '2023-02-09 00:00:00', '590,97\r');

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `64_closing_available_balance`
--

CREATE TABLE `64_closing_available_balance` (
  `id` int NOT NULL,
  `transaction_type_id` int NOT NULL,
  `currency_type_id` int NOT NULL,
  `date` datetime DEFAULT NULL,
  `balance` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `64_closing_available_balance`
--

INSERT INTO `64_closing_available_balance` (`id`, `transaction_type_id`, `currency_type_id`, `date`, `balance`) VALUES
(1, 2, 1, '2023-02-09 00:00:00', '590,97\r');

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `cost_center`
--

CREATE TABLE `cost_center` (
  `id` int NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `cost_center`
--

INSERT INTO `cost_center` (`id`, `name`) VALUES
(1, 'Algemeen'),
(2, 'Quintor'),
(7, 'NHL Stenden');

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `currency_type`
--

CREATE TABLE `currency_type` (
  `id` int NOT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `currency_symbol` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `currency_type`
--

INSERT INTO `currency_type` (`id`, `name`, `currency_symbol`) VALUES
(1, 'EUR', '€'),
(2, 'USD', '$');

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `custom_transaction`
--

CREATE TABLE `custom_transaction` (
  `id` int NOT NULL,
  `cost_center_id` int DEFAULT NULL,
  `currency_type_id` int NOT NULL,
  `payment_date` date NOT NULL,
  `amount` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Stand-in structuur voor view `mt940Overview`
-- (Zie onder voor de actuele view)
--
CREATE TABLE `mt940Overview` (
`transactionRef` varchar(20)
,`swiftcopyId` int
,`dateAdded` datetime
,`iban` varchar(40)
,`openingBal` varchar(20)
,`closingBal` varchar(20)
,`num_transactions` bigint
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `plugin`
--

CREATE TABLE `plugin` (
  `id` int NOT NULL,
  `online` tinyint(1) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `plugin`
--

INSERT INTO `plugin` (`id`, `online`, `name`) VALUES
(1, 1, 'Boekhouding'),
(2, 1, 'Kasgeld'),
(3, 0, 'Ledenadministratie'),
(4, 0, 'Bar module'),
(5, 0, 'Verhuur module'),
(6, 0, 'Splitsen van transacties');

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `role`
--

CREATE TABLE `role` (
  `id` int NOT NULL,
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `role`
--

INSERT INTO `role` (`id`, `name`) VALUES
(1, 'Gebruiker'),
(2, 'Penningmeester');

-- --------------------------------------------------------

--
-- Stand-in structuur voor view `statements`
-- (Zie onder voor de actuele view)
--
CREATE TABLE `statements` (
`transactionRef` varchar(20)
,`id` int
,`user_id` int
,`date_added` datetime
,`account_identification` varchar(40)
,`statement_number` varchar(20)
,`statement_line_id` int
,`cost_center_id` int
,`transaction_type_id` int
,`value_date` date
,`entry_date` varchar(20)
,`amount` varchar(20)
,`transaction_type` varchar(20)
,`identification_code` varchar(10)
,`payment_reference` varchar(20)
,`transaction_reference` varchar(20)
,`additional_information` varchar(80)
,`statement_line_narrative` text
,`custom_description` text
,`opening_balance_transaction_type_id` int
,`opening_balance_currency_type_id` int
,`opening_balance_date` datetime
,`opening_blanace` varchar(20)
,`closing_balance_transaction_type_id` int
,`closing_balance_currency_type_id` int
,`closing_balance_date` datetime
,`closing_balance` varchar(20)
,`closing_available_balance_transaction_type_id` int
,`closing_available_balance_currency_type_id` int
,`closing_available_balance_date` datetime
,`closing_available_balance` varchar(20)
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `swift_copy`
--

CREATE TABLE `swift_copy` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `date_added` datetime DEFAULT CURRENT_TIMESTAMP,
  `20_transaction_reference` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `25_account_identification` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `28C_statement_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `62F_closing_balance_id` int NOT NULL,
  `60F_opening_balance_id` int NOT NULL,
  `64_closing_available_balance_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `swift_copy`
--

INSERT INTO `swift_copy` (`id`, `user_id`, `date_added`, `20_transaction_reference`, `25_account_identification`, `28C_statement_number`, `62F_closing_balance_id`, `60F_opening_balance_id`, `64_closing_available_balance_id`) VALUES
(1, 1, '2023-04-05 12:41:38', 'P230209000000001\r', 'NL65RABO0224663562EUR\r', '13039\r', 1, 1, 1);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `transaction_type`
--

CREATE TABLE `transaction_type` (
  `id` int NOT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `transaction_type`
--

INSERT INTO `transaction_type` (`id`, `name`) VALUES
(1, 'Debit'),
(2, 'Credit');

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `user`
--

CREATE TABLE `user` (
  `id` int NOT NULL,
  `role_id` int NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `firstname` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `lastname` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Gegevens worden geëxporteerd voor tabel `user`
--

INSERT INTO `user` (`id`, `role_id`, `email`, `password`, `firstname`, `lastname`) VALUES
(1, 2, 'dev@quintor.nl', '$2a$05$JH84aCpJxa.vw7uCVxVywOkS5DJA8m3KIlQw9Jz.JP.NKwm7111cG', 'Quintor', 'Quintor'),
(2, 1, 'test@test.nl', '$2a$05$rYXgGEJ553pYVIo1Wq2V9.BEkQkqr/NT1M2ETpOEf/zQ5BoGR8INm', 'test', 'test');



--
-- Triggers `user`
--
DELIMITER $$
CREATE TRIGGER `user_audit_insert_trigger` AFTER INSERT ON `user` FOR EACH ROW BEGIN

    INSERT INTO user_audit_log (crud_type, email, event_timestamp, user_id)
    VALUES ('INSERT', NEW.email, NOW(), NEW.id);
END
    $$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `user_audit_update_trigger` AFTER UPDATE ON `user` FOR EACH ROW BEGIN

    INSERT INTO user_audit_log (crud_type, email, event_timestamp, user_id)
    VALUES ('UPDATE', NEW.email, NOW(), NEW.id);
END
    $$
DELIMITER ;

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `user_audit_log`
--

CREATE TABLE `user_audit_log` (
                                  `id` int NOT NULL,
                                  `event_timestamp` timestamp NULL DEFAULT NULL,
                                  `user_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                  `crud_type` enum('INSERT','UPDATE') COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structuur voor de view `mt940Overview`
--
DROP TABLE IF EXISTS `mt940Overview`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `mt940Overview`  AS SELECT `swift_copy`.`20_transaction_reference` AS `transactionRef`, `swift_copy`.`id` AS `swiftcopyId`, `swift_copy`.`date_added` AS `dateAdded`, `swift_copy`.`25_account_identification` AS `iban`, `60f_opening_balance`.`balance` AS `openingBal`, `62f_closing_balance`.`balance` AS `closingBal`, count(`61_statement_line`.`id`) AS `num_transactions` FROM (((`swift_copy` join `60f_opening_balance` on((`swift_copy`.`60F_opening_balance_id` = `60f_opening_balance`.`id`))) join `62f_closing_balance` on((`swift_copy`.`62F_closing_balance_id` = `62f_closing_balance`.`id`))) join `61_statement_line` on((`swift_copy`.`id` = `61_statement_line`.`swift_copy_id`))) GROUP BY `swift_copy`.`id` ORDER BY `swift_copy`.`date_added` DESC ;

-- --------------------------------------------------------

--
-- Structuur voor de view `statements`
--
DROP TABLE IF EXISTS `statements`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `statements`  AS SELECT `sc`.`20_transaction_reference` AS `transactionRef`, `sc`.`id` AS `id`, `sc`.`user_id` AS `user_id`, `sc`.`date_added` AS `date_added`, `sc`.`25_account_identification` AS `account_identification`, `sc`.`28C_statement_number` AS `statement_number`, `sl`.`id` AS `statement_line_id`, `sl`.`cost_center_id` AS `cost_center_id`, `sl`.`transaction_type_id` AS `transaction_type_id`, `sl`.`value_date` AS `value_date`, `sl`.`entry_date` AS `entry_date`, `sl`.`amount` AS `amount`, `sl`.`transaction_type` AS `transaction_type`, `sl`.`identification_code` AS `identification_code`, `sl`.`payment_reference` AS `payment_reference`, `sl`.`transaction_reference` AS `transaction_reference`, `sl`.`additional_information` AS `additional_information`, `sl`.`86_statement_line_narrative` AS `statement_line_narrative`, `sl`.`custom_description` AS `custom_description`, `ob`.`transaction_type_id` AS `opening_balance_transaction_type_id`, `ob`.`currency_type_id` AS `opening_balance_currency_type_id`, `ob`.`date` AS `opening_balance_date`, `ob`.`balance` AS `opening_blanace`, `cb`.`transaction_type_id` AS `closing_balance_transaction_type_id`, `cb`.`currency_type_id` AS `closing_balance_currency_type_id`, `cb`.`date` AS `closing_balance_date`, `cb`.`balance` AS `closing_balance`, `cab`.`transaction_type_id` AS `closing_available_balance_transaction_type_id`, `cab`.`currency_type_id` AS `closing_available_balance_currency_type_id`, `cab`.`date` AS `closing_available_balance_date`, `cab`.`balance` AS `closing_available_balance` FROM ((((`swift_copy` `sc` join `61_statement_line` `sl` on((`sc`.`id` = `sl`.`swift_copy_id`))) join `60f_opening_balance` `ob` on((`sc`.`60F_opening_balance_id` = `ob`.`id`))) join `62f_closing_balance` `cb` on((`sc`.`62F_closing_balance_id` = `cb`.`id`))) join `64_closing_available_balance` `cab` on((`sc`.`64_closing_available_balance_id` = `cab`.`id`))) ;

--
-- Indexen voor geëxporteerde tabellen
--

--
-- Indexen voor tabel `60f_opening_balance`
--
ALTER TABLE `60f_opening_balance`
  ADD PRIMARY KEY (`id`),
  ADD KEY `transaction_type_id` (`transaction_type_id`,`currency_type_id`),
  ADD KEY `60f_currency_type` (`currency_type_id`);

--
-- Indexen voor tabel `61_statement_line`
--
ALTER TABLE `61_statement_line`
  ADD PRIMARY KEY (`id`),
  ADD KEY `swift_copy_id` (`swift_copy_id`,`cost_center_id`,`transaction_type_id`),
  ADD KEY `61_transaction_type` (`transaction_type_id`),
  ADD KEY `61_cost_center` (`cost_center_id`);

--
-- Indexen voor tabel `62f_closing_balance`
--
ALTER TABLE `62f_closing_balance`
  ADD PRIMARY KEY (`id`),
  ADD KEY `transaction_type_id` (`transaction_type_id`,`currency_type_id`),
  ADD KEY `62f_currency_type` (`currency_type_id`);

--
-- Indexen voor tabel `64_closing_available_balance`
--
ALTER TABLE `64_closing_available_balance`
  ADD PRIMARY KEY (`id`),
  ADD KEY `transaction_type_id` (`transaction_type_id`,`currency_type_id`),
  ADD KEY `64_currency_type` (`currency_type_id`);

--
-- Indexen voor tabel `cost_center`
--
ALTER TABLE `cost_center`
  ADD PRIMARY KEY (`id`);

--
-- Indexen voor tabel `currency_type`
--
ALTER TABLE `currency_type`
  ADD PRIMARY KEY (`id`);

--
-- Indexen voor tabel `custom_transaction`
--
ALTER TABLE `custom_transaction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cost_center_id` (`cost_center_id`,`currency_type_id`),
  ADD KEY `custom_transaction_currency` (`currency_type_id`);

--
-- Indexen voor tabel `plugin`
--
ALTER TABLE `plugin`
  ADD PRIMARY KEY (`id`);

--
-- Indexen voor tabel `role`
--
ALTER TABLE `role`
  ADD PRIMARY KEY (`id`);

--
-- Indexen voor tabel `swift_copy`
--
ALTER TABLE `swift_copy`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`,`62F_closing_balance_id`,`60F_opening_balance_id`,`64_closing_available_balance_id`),
  ADD KEY `swift_copy_60F` (`60F_opening_balance_id`),
  ADD KEY `swift_copy_62F` (`62F_closing_balance_id`),
  ADD KEY `swift_copy_64` (`64_closing_available_balance_id`);

--
-- Indexen voor tabel `transaction_type`
--
ALTER TABLE `transaction_type`
  ADD PRIMARY KEY (`id`);

--
-- Indexen voor tabel `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_email_unique` (`email`),
  ADD KEY `email` (`email`),
  ADD KEY `role_id` (`role_id`);

--
-- AUTO_INCREMENT voor geëxporteerde tabellen
--

--
-- AUTO_INCREMENT voor een tabel `60f_opening_balance`
--
ALTER TABLE `60f_opening_balance`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT voor een tabel `61_statement_line`
--
ALTER TABLE `61_statement_line`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=68;

--
-- AUTO_INCREMENT voor een tabel `62f_closing_balance`
--
ALTER TABLE `62f_closing_balance`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT voor een tabel `64_closing_available_balance`
--
ALTER TABLE `64_closing_available_balance`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT voor een tabel `cost_center`
--
ALTER TABLE `cost_center`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT voor een tabel `currency_type`
--
ALTER TABLE `currency_type`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT voor een tabel `custom_transaction`
--
ALTER TABLE `custom_transaction`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT voor een tabel `plugin`
--
ALTER TABLE `plugin`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT voor een tabel `role`
--
ALTER TABLE `role`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT voor een tabel `swift_copy`
--
ALTER TABLE `swift_copy`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT voor een tabel `transaction_type`
--
ALTER TABLE `transaction_type`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT voor een tabel `user`
--
ALTER TABLE `user`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Beperkingen voor geëxporteerde tabellen
--

--
-- Beperkingen voor tabel `60f_opening_balance`
--
ALTER TABLE `60f_opening_balance`
  ADD CONSTRAINT `60f_currency_type` FOREIGN KEY (`currency_type_id`) REFERENCES `currency_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `60f_transaction_type` FOREIGN KEY (`transaction_type_id`) REFERENCES `transaction_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Beperkingen voor tabel `61_statement_line`
--
ALTER TABLE `61_statement_line`
  ADD CONSTRAINT `61_cost_center` FOREIGN KEY (`cost_center_id`) REFERENCES `cost_center` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `61_swift_copy` FOREIGN KEY (`swift_copy_id`) REFERENCES `swift_copy` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `61_transaction_type` FOREIGN KEY (`transaction_type_id`) REFERENCES `transaction_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Beperkingen voor tabel `62f_closing_balance`
--
ALTER TABLE `62f_closing_balance`
  ADD CONSTRAINT `62f_currency_type` FOREIGN KEY (`currency_type_id`) REFERENCES `currency_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `62f_transaction_type` FOREIGN KEY (`transaction_type_id`) REFERENCES `transaction_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Beperkingen voor tabel `64_closing_available_balance`
--
ALTER TABLE `64_closing_available_balance`
  ADD CONSTRAINT `64_currency_type` FOREIGN KEY (`currency_type_id`) REFERENCES `currency_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `64_transaction_type` FOREIGN KEY (`transaction_type_id`) REFERENCES `transaction_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Beperkingen voor tabel `custom_transaction`
--
ALTER TABLE `custom_transaction`
  ADD CONSTRAINT `custom_transaction_cost_center` FOREIGN KEY (`cost_center_id`) REFERENCES `cost_center` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `custom_transaction_currency` FOREIGN KEY (`currency_type_id`) REFERENCES `currency_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;