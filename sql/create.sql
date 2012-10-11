DROP DATABASE IF EXISTS `manysound`;;

CREATE DATABASE `manysound`;;

GRANT ALL ON `manysound`.* TO manysound;;

GRANT ALL ON `manysound`.* TO manysound@localhost;;

USE `manysound`;;

CREATE TABLE `user` (
  `login` VARCHAR(20) NOT NULL,
  INDEX (`login`),
  PRIMARY KEY (`login`),
  CHECK (`login` REGEXP "^[[:alnum:]_]{3,20}$")
) ENGINE = InnoDB;;

CREATE PROCEDURE `select_all_users` ()
  SELECT (`login`) FROM `user`;;

CREATE PROCEDURE `insert_new_user` (IN `login1` VARCHAR(20))
  IF
    (`login1` REGEXP "^[[:alnum:]_]{3,20}$")
  THEN
    INSERT INTO `user` (`login`) VALUES (`login1`);
  ELSE
    SIGNAL SQLSTATE "45000"
      SET MESSAGE_TEXT = "Wrong login word";
  END IF;;

CREATE FUNCTION `try_login` (`login1` VARCHAR(20))
  RETURNS BOOL
  RETURN EXISTS (SELECT * FROM `user` WHERE `login`=`login1`);;

CREATE TABLE `meeting` (
  `id_meeting` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_meeting`),
  PRIMARY KEY (`id_meeting`),
  `name` VARCHAR(20) NOT NULL,
  INDEX (`name`),
  `description` TEXT,
  `visible` BOOL NOT NULL,
  `owner` VARCHAR(20) NOT NULL,
  FOREIGN KEY (`owner`) REFERENCES `user`(`login`)
) ENGINE = InnoDB;;

CREATE FUNCTION `in_meeting` (`login1` VARCHAR(20), `id_meeting1` INT UNSIGNED)
  RETURNS BOOL
  RETURN EXISTS (SELECT * FROM `meeting` WHERE `login`=`login1` AND `id_meeting`=`id_meeting1`);;

CREATE PROCEDURE `veiw_meetings` (IN `login1` VARCHAR(20))
  SELECT (`name`, `description`) FROM `meetings`
    WHERE (`visible` OR `in_meeting`(`login1`, `id_meeting`));;

CREATE TABLE `participant` (
  `login` VARCHAR(20) NOT NULL,
  INDEX (`login`),
  FOREIGN KEY (`login`) REFERENCES `user`(`login`),
  `id_meeting` INT UNSIGNED NOT NULL,
  INDEX (`id_meeting`),
  FOREIGN KEY (`id_meeting`) REFERENCES `meeting`(`id_meeting`),
  PRIMARY KEY (`login`, `id_meeting`)
) ENGINE = InnoDB;;

CREATE TABLE `group_func` (
  `id_group_func` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_group_func`),
  PRIMARY KEY (`id_group_func`)
) ENGINE = InnoDB;;

CREATE TABLE `group` (
  `id_group` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_group`),
  `id_meeting` INT UNSIGNED NOT NULL,
  INDEX (`id_meeting`),
  INDEX (`id_group`,`id_meeting`),
  PRIMARY KEY (`id_group`,`id_meeting`),
  FOREIGN KEY (`id_meeting`) REFERENCES `meeting`(`id_meeting`),  
  `id_group_func` INT UNSIGNED NULL,
  FOREIGN KEY (`id_group_func`) REFERENCES `group_func`(`id_group_func`)
) ENGINE = InnoDB;;

CREATE TABLE `role` (
  `id_role` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_role`),
  PRIMARY KEY (`id_role`),
  `id_group` INT UNSIGNED NOT NULL,
  INDEX (`id_group`),
  FOREIGN KEY (`id_group`,`id_meeting`) REFERENCES `group`(`id_group`,`id_meeting`),
  `login` VARCHAR(20) NOT NULL,
  INDEX (`login`),
  `id_meeting` INT UNSIGNED NOT NULL,
  INDEX (`id_meeting`),
  FOREIGN KEY (`login`, `id_meeting`) REFERENCES `participant`(`login`, `id_meeting`)
) ENGINE = InnoDB;;

CREATE TABLE `func_instr` (
  `id_func_instr` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_func_instr`),
  PRIMARY KEY (`id_func_instr`),
  `id_group_func` INT UNSIGNED NOT NULL,
  INDEX (`id_group_func`),
  FOREIGN KEY (`id_group_func`) REFERENCES `group_func`(`id_group_func`)
  
) ENGINE = InnoDB;;

CREATE TABLE `instrument_type` (
  `id_instrument_type` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_instrument_type`),
  PRIMARY KEY (`id_instrument_type`),
  `arguments_num` INT UNSIGNED NOT NULL
) ENGINE = InnoDB;;

CREATE TABLE `instrument` (
  `id_instrument` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_instrument`),
  PRIMARY KEY (`id_instrument`),
  `id_instrument_type` INT UNSIGNED NOT NULL,
  FOREIGN KEY (`id_instrument_type`) REFERENCES `instrument_type`(`id_instrument_type`)
) ENGINE = InnoDB;;

CREATE TABLE `instrument_argument` (
  `id_group_func` INT UNSIGNED NOT NULL,
  FOREIGN KEY (`id_group_func`) REFERENCES `group_func`(`id_group_func`),
  `id_instrument` INT UNSIGNED NOT NULL,
  INDEX (`id_instrument`),
  FOREIGN KEY (`id_instrument`) REFERENCES `instrument`(`id_instrument`)
) ENGINE = InnoDB;;