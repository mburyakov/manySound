DROP DATABASE IF EXISTS `manysound`;;

CREATE DATABASE `manysound`;;

GRANT ALL ON `manysound`.* TO manysound;;

GRANT ALL ON `manysound`.* TO manysound@localhost;;

USE `manysound`;;

CREATE TABLE `user` (
  `login` VARCHAR(20) NOT NULL,
  INDEX (`login`),
  PRIMARY KEY (`login`),
  `password` VARCHAR(60) NOT NULL,
  `can_create` BOOL NOT NULL DEFAULT TRUE,
  CHECK (`login` REGEXP "^[[:alnum:]_]{3,4}$")
) ENGINE = InnoDB;;

CREATE TABLE `meeting` (
  `id_meeting` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_meeting`),
  PRIMARY KEY (`id_meeting`),
  `name` VARCHAR(20) NOT NULL,
  INDEX (`name`),
  `description` TEXT NULL,
  `visible` BOOL NOT NULL DEFAULT TRUE
  #`owner` VARCHAR(20) NOT NULL,
  #FOREIGN KEY (`owner`) REFERENCES `user`(`login`)
) ENGINE = InnoDB;;

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
  `description` TEXT NULL,
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

CREATE TABLE `instrument_type` (
  `id_instrument_type` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_instrument_type`),
  PRIMARY KEY (`id_instrument_type`),
  `description` TEXT NULL,
  `arguments_num` INT UNSIGNED NOT NULL,
  `script` TEXT NOT NULL,
  `script_arg` VARCHAR(10) NOT NULL #author, meeting, arg1, arg2, ...
) ENGINE = InnoDB;;

CREATE TABLE `instrument` (
  `id_instrument` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_instrument`),
  PRIMARY KEY (`id_instrument`),
  `description` TEXT NOT NULL,
  `id_instrument_type` INT UNSIGNED NOT NULL,
  FOREIGN KEY (`id_instrument_type`) REFERENCES `instrument_type`(`id_instrument_type`)
) ENGINE = InnoDB;;

CREATE TABLE `func_instr` (
  `id_func_instr` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  INDEX (`id_func_instr`),
  PRIMARY KEY (`id_func_instr`),
  `id_instrument` INT UNSIGNED NOT NULL,
  INDEX (`id_instrument`),
  `id_group_func` INT UNSIGNED NOT NULL,
  INDEX (`id_group_func`),
  FOREIGN KEY (`id_group_func`) REFERENCES `group_func`(`id_group_func`)
) ENGINE = InnoDB;;

CREATE TABLE `instrument_argument` (
  `id_group_func` INT UNSIGNED NOT NULL,
  FOREIGN KEY (`id_group_func`) REFERENCES `group_func`(`id_group_func`),
  `id_instrument` INT UNSIGNED NOT NULL,
  INDEX (`id_instrument`),
  FOREIGN KEY (`id_instrument`) REFERENCES `instrument`(`id_instrument`)
) ENGINE = InnoDB;;


INSERT INTO `group_func` (`id_group_func`, `description`) VALUES
  (1, "owners"),
  (2, "participants"),
  (3, "non-participants"),
  (4, "all")
;;

INSERT INTO `instrument_type` (`id_instrument_type`, `description`, `arguments_num`,`script`,`script_arg`) VALUES
  (1, "invite", 1,"INSERT INTO `participant` (`login`, `id_meeting`) VALUES (?,?)","0,2,1"),
  (2, "delete", 1,"DELETE FROM `participant` WHERE ((`login`=?) AND (`id_meeting`=?))","0,2,1"),
  #(2, "delete", 1,"DELETE p, pa FROM `participant` p JOIN `role` pa ON (`pa`.`login` = `p`.`login` AND `pa`.`id_meeting`=`p`.`id_meeting`) WHERE ((`pa`.`login`=?) AND (`p`.`id_meeting`=?))","0,2,1"),
  (3, "send_message", 1,"","0,0"),
  #(4, "set_owner", 1,"INSERT INTO `role` (`id_group`, `id_meeting`, `login`) (SELECT * FROM (SELECT `id_group`,`id_meeting` FROM `group` WHERE ((`id_group_func`=1) AND (`id_meeting`=?))) AS a JOIN (SELECT ? AS b) c)","0,1,2")
  (4, "set_owner", 1,"UPDATE (`role` JOIN `group_func`) SET `login`=? WHERE ((`id_group_func`=1) AND (`id_meeting`=?))","0,2,1")
;;

INSERT INTO `instrument` (`id_instrument`, `description`, `id_instrument_type`) VALUES
  (1, "invite user", 1),
  (2, "delete user", 2),
  (3, "send message", 3),
  (4, "ask owner", 3),
  (5, "set owner", 4)
;;

INSERT INTO `instrument_argument` (`id_instrument`, `id_group_func`) VALUES
  (1, 3),
  (2, 2),
  (3, 4),
  (4, 1),
  (5, 2)
;;

INSERT INTO `func_instr` (`id_group_func`, `id_instrument`) VALUES
  (1, 1),
  (1, 2),
  (2, 3),
  (4, 4),
  (1, 5)
;;



CREATE PROCEDURE `select_all_users` ()
  SELECT `login` FROM `user`;;

CREATE PROCEDURE `insert_new_user` (IN `login1` VARCHAR(20), IN `password1` VARCHAR(60))
  IF
    (`login1` REGEXP "^[[:alnum:]_]{5,20}$")
  THEN
    IF
      (`password1` REGEXP "^.{5,60}$")
    THEN
      INSERT INTO `user` (`login`,`password`) VALUES (`login1`,`password1`);
    ELSE
      SIGNAL SQLSTATE "45000"
        SET MESSAGE_TEXT = "Wrong password";
    END IF;
  ELSE
    SIGNAL SQLSTATE "45000"
      SET MESSAGE_TEXT = "Wrong login word";
  END IF;;

CREATE FUNCTION `try_login` (`login1` VARCHAR(20), `password1` VARCHAR(60))
  RETURNS BOOL
  RETURN EXISTS (SELECT * FROM `user` WHERE `login`=`login1` AND `password`=`password1`);;

CREATE FUNCTION `in_meeting` (`login1` VARCHAR(20), `id_meeting1` INT UNSIGNED)
  RETURNS BOOL
  RETURN EXISTS (SELECT * FROM `meeting` WHERE `login`=`login1` AND `id_meeting`=`id_meeting1`);;

CREATE PROCEDURE `view_meetings` (IN `login1` VARCHAR(20))
  SELECT `id_meeting`,`name`, `description`, `in_group_func`(`login1`,1,`id_meeting`) as `is_owner` FROM `meeting`
    WHERE (`visible` OR `in_meeting`(`login1`, `id_meeting`));;

CREATE PROCEDURE `view_meeting` (IN `meeting1` INT UNSIGNED, IN `login1` VARCHAR(20))
  SELECT `name`, `description`, `in_group_func`(`login1`,1,`id_meeting`) as `is_owner` FROM `meeting`
    WHERE (`id_meeting`=`meeting1`);;

CREATE PROCEDURE `view_script` (IN `id_type` INT UNSIGNED)
  SELECT `script`,`script_arg` FROM `instrument_type`
    WHERE (`id_instrument_type`=`id_type`);;

CREATE FUNCTION `instr_type`(`id_instr` INT UNSIGNED)
  RETURNS INT UNSIGNED
  RETURN (SELECT `id_instrument_type` as `id_instrument_type` FROM `instrument`
    WHERE `id_instrument`=`id_instr`);;

CREATE FUNCTION `can_create_meeting`(`login1` VARCHAR(20))
  RETURNS BOOL
  RETURN (SELECT `can_create` FROM `user` WHERE `login`=`login1`);;

CREATE PROCEDURE `invite_in_meeting` (IN `login1` VARCHAR(20), `meeting1` INT UNSIGNED)
  INSERT INTO `participant` (`login`, `id_meeting`) VALUES (
    `login1`,
    `meeting1`
  );;

CREATE PROCEDURE `insert_new_meeting` (IN `login1` VARCHAR(20), `name1` VARCHAR(20))
  IF
    `can_create_meeting`(`login1`)
  THEN
    IF
      (`name1` REGEXP "^[[:alnum:]_]{3,20}$")
    THEN
      INSERT INTO `meeting` (`name`) VALUES (`name1`);
      INSERT INTO `group` (`id_meeting`,`id_group_func`) VALUES (
        (SELECT MAX(`id_meeting`) FROM `meeting`),
        1
      );
      CALL `invite_in_meeting` (login1, (SELECT MAX(`id_meeting`) FROM `meeting`));
      INSERT INTO `role` (`id_group`, `login`, `id_meeting`) VALUES (
        (SELECT MAX(`id_group`) FROM `group`),
        `login1`,
        (SELECT MAX(`id_meeting`) FROM `meeting`)
      );
    ELSE
      SIGNAL SQLSTATE "45000"
        SET MESSAGE_TEXT = "Wrong name word";
    END IF;
  ELSE
    SIGNAL SQLSTATE "45000"
      SET MESSAGE_TEXT = "You have not permissions to create new meetings";
  END IF;;

CREATE PROCEDURE `view_instruments` (IN `meeting1` INT UNSIGNED, IN `login1` VARCHAR(20))
  SELECT DISTINCT `instrument`.`id_instrument` as `id_instrument`, CONCAT(`instrument_type`.`description`," (",`instrument`.`description`,")") as `description` FROM `role`,`group`,`func_instr`,`instrument`,`instrument_type`
    WHERE(#(`role`.`login`=`login1`)
      #AND (`role`.`id_meeting`=`meeting1`)
      #AND (`role`.`id_group`=`group`.`id_group`)
      #AND (`group`.`id_group_func`=`func_instr`.`id_group_func`)
      `in_group_func`(`login1`,`func_instr`.`id_group_func`,`meeting1`)
      AND (`func_instr`.`id_instrument`=`instrument`.`id_instrument`)
      AND (`instrument`.`id_instrument_type`=`instrument_type`.`id_instrument_type`)
    );;

CREATE PROCEDURE `view_instrument` (IN `id` INT UNSIGNED)
  SELECT `description`,`id_instrument_type` FROM `instrument`
    WHERE(
      `id_instrument` = `id`
    );;

CREATE FUNCTION `in_group_func` (`login1`  VARCHAR(20), `id_group_func1` INT UNSIGNED, `id_meeting1` INT UNSIGNED)
  RETURNS BOOL
  RETURN (CASE `id_group_func1`
   WHEN 4 THEN TRUE
   WHEN 2 THEN
    EXISTS (SELECT * FROM `participant` WHERE
        `login`=`login1`
    AND `id_meeting`=`id_meeting1`)
   WHEN 3 THEN
    NOT EXISTS (SELECT * FROM `participant` WHERE
        `login`=`login1`
    AND `id_meeting`=`id_meeting1`)
   ELSE
    EXISTS (SELECT * FROM `role`,`group` WHERE
        `login`=`login1`
    AND `role`.`id_meeting`=`id_meeting1`
    AND `group`.`id_meeting`=`id_meeting1`
    AND (`role`.`id_group`=`group`.`id_group`)
    AND `id_group_func`=`id_group_func1`)
   END);;

CREATE PROCEDURE `view_recipients` (IN `meeting1` INT UNSIGNED, IN `login1` VARCHAR(20), IN `instr` INT UNSIGNED)
  SELECT DISTINCT `user`.`login` FROM (SELECT DISTINCT `instrument`.`id_instrument`, `instrument_type`.`description` FROM `role`,`group`,`func_instr`,`instrument`,`instrument_type`
                                            WHERE(
                                                  `in_group_func`(`login1`,`func_instr`.`id_group_func`,`meeting1`)
                                                  #(`role`.`login`=`login1`)
                                              #AND (`role`.`id_meeting`=`meeting1`)
                                              #AND (`role`.`id_group`=`group`.`id_group`)
                                              #AND (`group`.`id_group_func`=`func_instr`.`id_group_func`)
                                              AND (`func_instr`.`id_instrument`=`instrument`.`id_instrument`)
                                              AND (`instrument`.`id_instrument_type`=`instrument_type`.`id_instrument_type`)
                                            )) AS `instrs` ,`instrument_argument`,`group`,`role`,`user`
    WHERE((`instrs`.`id_instrument`=`instrument_argument`.`id_instrument`)
      AND `in_group_func`(`user`.`login`,`instrument_argument`.`id_group_func`,`meeting1`)
      AND `instrs`.`id_instrument`=`instr`
      #AND (`instrument_argument`.`id_group_func`=`group`.`id_group_func`)
      #AND (`group`.`id_group`=`role`.`id_group`)
    );;