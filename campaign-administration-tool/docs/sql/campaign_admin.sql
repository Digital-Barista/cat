SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `campaign_admin` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `campaign_admin`;

-- -----------------------------------------------------
-- Table `campaign_admin`.`jms_messages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`jms_messages` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`jms_messages` (
  `MESSAGEID` INT(11) NOT NULL ,
  `DESTINATION` VARCHAR(150) NOT NULL ,
  `TXID` INT(11) NULL DEFAULT NULL ,
  `TXOP` CHAR(1) NULL DEFAULT NULL ,
  `MESSAGEBLOB` LONGBLOB NULL DEFAULT NULL ,
  PRIMARY KEY (`MESSAGEID`, `DESTINATION`) ,
  INDEX `JMS_MESSAGES_TXOP_TXID` (`TXOP` ASC, `TXID` ASC) ,
  INDEX `JMS_MESSAGES_DESTINATION` (`DESTINATION` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_admin`.`jms_roles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`jms_roles` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`jms_roles` (
  `ROLEID` VARCHAR(32) NOT NULL ,
  `USERID` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`USERID`, `ROLEID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_admin`.`jms_subscriptions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`jms_subscriptions` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`jms_subscriptions` (
  `CLIENTID` VARCHAR(128) NOT NULL ,
  `SUBNAME` VARCHAR(128) NOT NULL ,
  `TOPIC` VARCHAR(255) NOT NULL ,
  `SELECTOR` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`CLIENTID`, `SUBNAME`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_admin`.`jms_transactions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`jms_transactions` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`jms_transactions` (
  `TXID` INT(11) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`TXID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_admin`.`jms_users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`jms_users` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`jms_users` (
  `USERID` VARCHAR(32) NOT NULL ,
  `PASSWD` VARCHAR(32) NOT NULL ,
  `CLIENTID` VARCHAR(128) NULL DEFAULT NULL ,
  PRIMARY KEY (`USERID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_admin`.`roles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`roles` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`roles` (
  `role_pk` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key for this table' ,
  `username` VARCHAR(50) NOT NULL COMMENT 'Username applied to this table.' ,
  `role_name` VARCHAR(50) NOT NULL COMMENT 'Actual permission name' ,
  `role_type` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Actual role type' ,
  `ref_id` BIGINT NULL ,
  PRIMARY KEY (`role_pk`) ,
  INDEX `Index_2` (`username` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COMMENT = 'roles applied to all users';


-- -----------------------------------------------------
-- Table `campaign_admin`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`users` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`users` (
  `username` VARCHAR(50) NOT NULL COMMENT 'The case-sensitive login' ,
  `user_pk` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key for the table' ,
  `password` VARCHAR(100) NOT NULL COMMENT 'The encrypted password for this account' ,
  `active` TINYINT(1) NOT NULL DEFAULT '1' COMMENT 'Flag set to disable an account.' ,
  `email` VARCHAR(100) NULL ,
  PRIMARY KEY (`user_pk`) )
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1
COMMENT = 'Table containing user roles';


-- -----------------------------------------------------
-- Table `campaign_admin`.`campaigns`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`campaigns` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`campaigns` (
  `campaign_id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(200) NOT NULL ,
  `current_version` INT NOT NULL ,
  `uid` VARCHAR(36) NOT NULL ,
  `client_id` BIGINT NOT NULL ,
  `status` VARCHAR(24) NOT NULL DEFAULT 'Active' ,
  `add_in_message` VARCHAR(160) NULL ,
  `mode` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`campaign_id`) ,
  UNIQUE INDEX `cpn_uuid` (`uid` ASC) ,
  INDEX `cpn_status` (`status` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`nodes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`nodes` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`nodes` (
  `node_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT NOT NULL ,
  `type` INT NOT NULL ,
  `name` VARCHAR(200) NULL ,
  `uid` CHAR(36) NOT NULL DEFAULT 'uuid()' ,
  PRIMARY KEY (`node_id`) ,
  INDEX `node_fk_campaign` (`campaign_id` ASC) ,
  UNIQUE INDEX `node_uid` (`uid` ASC) ,
  CONSTRAINT `node_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaign_admin`.`campaigns` (`campaign_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`campaign_node_link`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`campaign_node_link` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`campaign_node_link` (
  `cnl_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT NOT NULL ,
  `node_id` BIGINT NOT NULL ,
  `version` INT NOT NULL ,
  PRIMARY KEY (`cnl_id`) ,
  UNIQUE INDEX `cnl_unique_link` (`campaign_id` ASC, `node_id` ASC, `version` ASC) ,
  INDEX `cnl_fk_campaign` (`campaign_id` ASC) ,
  INDEX `cnl_fk_node` (`node_id` ASC) ,
  INDEX `cnl_idx_campaign_version` (`campaign_id` ASC, `version` DESC) ,
  CONSTRAINT `cnl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaign_admin`.`campaigns` (`campaign_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `cnl_fk_node`
    FOREIGN KEY (`node_id` )
    REFERENCES `campaign_admin`.`nodes` (`node_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`campaign_versions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`campaign_versions` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`campaign_versions` (
  `campaign_version_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT NOT NULL ,
  `version` INT NOT NULL ,
  `status` VARCHAR(45) NOT NULL ,
  `published_date` DATETIME NULL ,
  PRIMARY KEY (`campaign_version_id`) ,
  INDEX `cv_fk_campaign` (`campaign_id` ASC) ,
  CONSTRAINT `cv_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaign_admin`.`campaigns` (`campaign_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`node_info`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`node_info` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`node_info` (
  `node_info_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `value` VARCHAR(256) NOT NULL ,
  `node_id` BIGINT NOT NULL ,
  `version` INT NOT NULL ,
  PRIMARY KEY (`node_info_id`) ,
  INDEX `ni_idx_node_name` (`node_id` ASC, `name` ASC, `version` ASC) ,
  INDEX `ni_fk_node_id` (`node_id` ASC) ,
  CONSTRAINT `ni_fk_node_id`
    FOREIGN KEY (`node_id` )
    REFERENCES `campaign_admin`.`nodes` (`node_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`connectors`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`connectors` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`connectors` (
  `connector_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(200) NULL ,
  `type` INT NOT NULL ,
  `campaign_id` INT NOT NULL ,
  `uid` CHAR(36) NOT NULL ,
  PRIMARY KEY (`connector_id`) ,
  INDEX `conn_fk_campaign` (`campaign_id` ASC) ,
  UNIQUE INDEX `conn_uid` (`uid` ASC) ,
  CONSTRAINT `conn_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaign_admin`.`campaigns` (`campaign_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`connector_info`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`connector_info` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`connector_info` (
  `connector_info_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `value` VARCHAR(256) NOT NULL ,
  `connector_id` BIGINT NOT NULL ,
  `version` INT NOT NULL ,
  PRIMARY KEY (`connector_info_id`) ,
  INDEX `ci_fk_connector` (`connector_id` ASC) ,
  INDEX `ci_idx_conn_name` (`connector_id` ASC, `name` ASC, `version` ASC) ,
  CONSTRAINT `ci_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `campaign_admin`.`connectors` (`connector_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`campaign_connector_link`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`campaign_connector_link` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`campaign_connector_link` (
  `ccl_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT NOT NULL ,
  `connector_id` BIGINT NOT NULL ,
  `version` INT NOT NULL ,
  PRIMARY KEY (`ccl_id`) ,
  INDEX `ccl_fk_campaign` (`campaign_id` ASC) ,
  INDEX `ccl_fk_connector` (`connector_id` ASC) ,
  INDEX `ccl_idx_campaign_version` (`campaign_id` ASC, `version` DESC) ,
  UNIQUE INDEX `ccl_unique_all` (`campaign_id` ASC, `connector_id` ASC, `version` ASC) ,
  CONSTRAINT `ccl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaign_admin`.`campaigns` (`campaign_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `ccl_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `campaign_admin`.`connectors` (`connector_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`node_connector_link`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`node_connector_link` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`node_connector_link` (
  `node_connector_link_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `node_id` BIGINT NOT NULL ,
  `connector_id` BIGINT NOT NULL ,
  `version` INT NOT NULL ,
  `type` INT NOT NULL ,
  PRIMARY KEY (`node_connector_link_id`) ,
  INDEX `ncl_fk_node` (`node_id` ASC) ,
  INDEX `ncl_fk_connector` (`connector_id` ASC) ,
  UNIQUE INDEX `ncl_unique_all` (`node_id` ASC, `connector_id` ASC, `version` DESC, `type` ASC) ,
  INDEX `ncl_idx_node_version` (`node_id` ASC, `version` DESC, `type` ASC) ,
  INDEX `ncl_idx_connector_version` (`connector_id` ASC, `version` DESC, `type` ASC) ,
  CONSTRAINT `ncl_fk_node`
    FOREIGN KEY (`node_id` )
    REFERENCES `campaign_admin`.`nodes` (`node_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `ncl_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `campaign_admin`.`connectors` (`connector_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`layout_info`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`layout_info` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`layout_info` (
  `layout_info_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT NOT NULL ,
  `x_loc` INT NOT NULL ,
  `y_loc` INT NOT NULL ,
  `version` INT NOT NULL ,
  `uid` CHAR(36) NOT NULL ,
  PRIMARY KEY (`layout_info_id`) ,
  INDEX `li_fk_campaign` (`campaign_id` ASC) ,
  UNIQUE INDEX `li_unique_all` (`campaign_id` ASC, `version` ASC, `uid` ASC) ,
  INDEX `li_campaign_version` (`campaign_id` ASC, `version` DESC) ,
  CONSTRAINT `li_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaign_admin`.`campaigns` (`campaign_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`client`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`client` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`client` (
  `client_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `admin_add_in_message` VARCHAR(160) NULL ,
  `user_add_in_message` VARCHAR(160) NULL ,
  `contact_name` VARCHAR(128) NULL ,
  `contact_email` VARCHAR(128) NULL ,
  `contact_phone` VARCHAR(32) NULL ,
  `active` TINYINT(1) NOT NULL DEFAULT 1 ,
  PRIMARY KEY (`client_id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`subscribers`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`subscribers` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`subscribers` (
  `subscriber_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `email` VARCHAR(128) NULL ,
  `phone_number` VARCHAR(16) NULL ,
  `twitter_name` VARCHAR(64) NULL ,
  PRIMARY KEY (`subscriber_id`) ,
  UNIQUE INDEX `sub_unq` (`email` ASC, `phone_number` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`campaign_subscriber_link`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`campaign_subscriber_link` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`campaign_subscriber_link` (
  `campaign_subscriber_link_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT NOT NULL ,
  `subscriber_id` BIGINT NOT NULL ,
  `last_completed_node_id` BIGINT NOT NULL ,
  `last_used_entry_point` VARCHAR(64) NOT NULL ,
  `last_used_entry_point_type` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`campaign_subscriber_link_id`) ,
  UNIQUE INDEX `csl_unq_all` (`campaign_id` ASC, `subscriber_id` ASC) ,
  INDEX `csl_fk_campaign` (`campaign_id` ASC) ,
  INDEX `csl_fk_subscriber` (`subscriber_id` ASC) ,
  INDEX `csl_fk_node` (`last_completed_node_id` ASC) ,
  CONSTRAINT `csl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaign_admin`.`campaigns` (`campaign_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `csl_fk_subscriber`
    FOREIGN KEY (`subscriber_id` )
    REFERENCES `campaign_admin`.`subscribers` (`subscriber_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `csl_fk_node`
    FOREIGN KEY (`last_completed_node_id` )
    REFERENCES `campaign_admin`.`nodes` (`node_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`campaign_entry_points`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`campaign_entry_points` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`campaign_entry_points` (
  `campaign_entry_point_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT NOT NULL ,
  `entry_point_type` VARCHAR(64) NOT NULL ,
  `entry_point` VARCHAR(128) NOT NULL ,
  `keyword` VARCHAR(64) NULL ,
  `entry_point_qty` INT NOT NULL DEFAULT 0 ,
  `published` TINYINT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`campaign_entry_point_id`) ,
  UNIQUE INDEX `cep_unq` (`entry_point_type` ASC, `entry_point` ASC, `keyword` ASC) ,
  INDEX `cep_fk_campaign` (`campaign_id` ASC) ,
  CONSTRAINT `cep_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaign_admin`.`campaigns` (`campaign_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`scheduled_campaign_tasks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`scheduled_campaign_tasks` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`scheduled_campaign_tasks` (
  `scheduled_task_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `scheduled_date` DATETIME NOT NULL ,
  `source_uid` VARCHAR(36) NOT NULL ,
  `target` VARCHAR(36) NULL ,
  `event_type` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`scheduled_task_id`) ,
  INDEX `sct_idx_date` (`scheduled_date` ASC) ,
  INDEX `sct_idx_source` (`source_uid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`entry_points`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`entry_points` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`entry_points` (
  `entry_point_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `description` VARCHAR(128) NULL ,
  `entry_point` VARCHAR(64) NOT NULL ,
  `entry_type` VARCHAR(32) NOT NULL ,
  `restriction_type` VARCHAR(32) NOT NULL ,
  `restriction_id` BIGINT NULL ,
  `credentials` VARCHAR(64) NULL ,
  PRIMARY KEY (`entry_point_id`) ,
  UNIQUE INDEX `ep_unq` (`entry_point` ASC, `entry_type` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`client_entry_point_link`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`client_entry_point_link` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`client_entry_point_link` (
  `client_entry_point_link_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `client_id` BIGINT NOT NULL ,
  `entry_point_id` BIGINT NOT NULL ,
  PRIMARY KEY (`client_entry_point_link_id`) ,
  UNIQUE INDEX `cepl_unq` (`client_id` ASC, `entry_point_id` ASC) ,
  INDEX `cepl_fk_client` (`client_id` ASC) ,
  INDEX `cepl_fk_entry_points` (`entry_point_id` ASC) ,
  CONSTRAINT `cepl_fk_client`
    FOREIGN KEY (`client_id` )
    REFERENCES `campaign_admin`.`client` (`client_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `cepl_fk_entry_points`
    FOREIGN KEY (`entry_point_id` )
    REFERENCES `campaign_admin`.`entry_points` (`entry_point_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`keywords`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`keywords` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`keywords` (
  `keyword_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `entry_point_id` BIGINT NOT NULL ,
  `keyword` VARCHAR(64) NOT NULL ,
  `client_id` BIGINT NOT NULL ,
  PRIMARY KEY (`keyword_id`) ,
  UNIQUE INDEX `kwd_unq` (`keyword` ASC, `entry_point_id` ASC, `client_id` ASC) ,
  INDEX `kwd_fk_entry_point` (`entry_point_id` ASC) ,
  INDEX `kwd_fk_client` (`client_id` ASC) ,
  CONSTRAINT `kwd_fk_entry_point`
    FOREIGN KEY (`entry_point_id` )
    REFERENCES `campaign_admin`.`entry_points` (`entry_point_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `kwd_fk_client`
    FOREIGN KEY (`client_id` )
    REFERENCES `campaign_admin`.`client` (`client_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`audit_incoming_message`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`audit_incoming_message` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`audit_incoming_message` (
  `incoming_audit_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `incoming_address` VARCHAR(64) NOT NULL ,
  `incoming_type` VARCHAR(16) NOT NULL ,
  `date_received` DATETIME NOT NULL ,
  `msg_or_subject` VARCHAR(256) NULL ,
  `payload` BLOB NULL ,
  `return_address` VARCHAR(64) NOT NULL ,
  `matched_uid` CHAR(36) NULL ,
  `matched_version` INT NULL ,
  `matched_type` VARCHAR(16) NULL ,
  PRIMARY KEY (`incoming_audit_id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`audit_outgoing_message`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`audit_outgoing_message` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`audit_outgoing_message` (
  `audit_outgoing_message_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `node_uid` VARCHAR(36) NOT NULL ,
  `node_version` INT NOT NULL ,
  `destination` VARCHAR(64) NOT NULL ,
  `date_sent` DATETIME NOT NULL ,
  `msg_type` VARCHAR(16) NOT NULL ,
  `subject_or_message` VARCHAR(256) NOT NULL ,
  `payload` BLOB NULL ,
  PRIMARY KEY (`audit_outgoing_message_id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`subscriber_blacklist`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`subscriber_blacklist` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`subscriber_blacklist` (
  `subscriber_blacklist_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `subscriber_id` BIGINT NOT NULL ,
  `incoming_address` VARCHAR(64) NOT NULL ,
  `incoming_type` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`subscriber_blacklist_id`) ,
  INDEX `sbs_fk_sub` (`subscriber_id` ASC) ,
  CONSTRAINT `sbs_fk_sub`
    FOREIGN KEY (`subscriber_id` )
    REFERENCES `campaign_admin`.`subscribers` (`subscriber_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`audit_generic`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`audit_generic` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`audit_generic` (
  `audit_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `audit_type` VARCHAR(32) NOT NULL ,
  `audit_time` DATETIME NOT NULL ,
  `descriminator_1` VARCHAR(36) NULL ,
  `descriminator_2` VARCHAR(36) NULL ,
  `audit_data` TEXT NOT NULL ,
  `audit_user` VARCHAR(50) NOT NULL DEFAULT 'system' ,
  PRIMARY KEY (`audit_id`) ,
  INDEX `at_type_idx` (`audit_type` ASC) ,
  INDEX `at_timestamp_idx` (`audit_time` ASC) ,
  INDEX `at_type_timestamp_idx` (`audit_type` ASC, `audit_time` ASC) ,
  INDEX `at_descriminator1_idx` (`descriminator_1` ASC) ,
  INDEX `at_descriminator2_idx` (`descriminator_2` ASC) ,
  INDEX `at_all_descriminator_idx` (`descriminator_1` ASC, `descriminator_2` ASC) ,
  INDEX `at_everything_idx` (`audit_type` ASC, `descriminator_1` ASC, `descriminator_2` ASC, `audit_time` ASC, `audit_user` ASC) ,
  INDEX `at_user_idx` (`audit_user` ASC) ,
  INDEX `at_user_activity_idx` (`audit_user` ASC, `audit_time` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`coupon_counters`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`coupon_counters` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`coupon_counters` (
  `coupon_code_length` INT NOT NULL ,
  `coupon_bit_scramble` BLOB NOT NULL ,
  `coupon_next_number` BIGINT NOT NULL DEFAULT 1 ,
  PRIMARY KEY (`coupon_code_length`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`coupon_offers`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`coupon_offers` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`coupon_offers` (
  `coupon_offer_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `max_coupons_issued` BIGINT NOT NULL ,
  `coupon_issue_count` BIGINT NOT NULL DEFAULT 0 ,
  `rejected_response_count` BIGINT NOT NULL DEFAULT 0 ,
  `expiration_date` DATETIME NULL ,
  `unavailable_date` DATETIME NULL ,
  `node_uid` VARCHAR(36) NOT NULL ,
  `max_redemptions` BIGINT NOT NULL ,
  `coupon_name` VARCHAR(50) NULL ,
  `campaign_id` INT NULL ,
  PRIMARY KEY (`coupon_offer_id`) ,
  INDEX `co_node_uid` (`node_uid` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`coupon_responses`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`coupon_responses` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`coupon_responses` (
  `coupon_response_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `response_date` DATETIME NOT NULL ,
  `response_detail` VARCHAR(16) NULL ,
  `coupon_offer_id` BIGINT NOT NULL ,
  `subscriber_id` BIGINT NOT NULL ,
  `response_type` VARCHAR(16) NOT NULL ,
  `coupon_message` VARCHAR(256) NOT NULL ,
  `redemption_count` INT NULL ,
  PRIMARY KEY (`coupon_response_id`) ,
  INDEX `cr_coupon_offer_fk` (`coupon_offer_id` ASC) ,
  INDEX `cr_subscriber_fk` (`subscriber_id` ASC) ,
  INDEX `cr_response_date` (`response_date` ASC) ,
  INDEX `cr_response_type` (`response_type` ASC) ,
  INDEX `cr_response_detail` (`response_detail` ASC) ,
  CONSTRAINT `cr_coupon_offer_fk`
    FOREIGN KEY (`coupon_offer_id` )
    REFERENCES `campaign_admin`.`coupon_offers` (`coupon_offer_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `cr_subscriber_fk`
    FOREIGN KEY (`subscriber_id` )
    REFERENCES `campaign_admin`.`subscribers` (`subscriber_id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`coupon_redemptions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`coupon_redemptions` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`coupon_redemptions` (
  `coupon_redemption_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `redemption_date` TIMESTAMP NOT NULL ,
  `redeemed_by_username` VARCHAR(50) NOT NULL ,
  `coupon_response_id` BIGINT NOT NULL ,
  PRIMARY KEY (`coupon_redemption_id`) ,
  INDEX `cred_date` (`redemption_date` ASC) ,
  INDEX `cred_response_fk` (`coupon_response_id` ASC) ,
  CONSTRAINT `cred_response_fk`
    FOREIGN KEY (`coupon_response_id` )
    REFERENCES `campaign_admin`.`coupon_responses` (`coupon_response_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`contact`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`contact` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`contact` (
  `contact_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `address` VARCHAR(256) NOT NULL ,
  `create_date` DATETIME NOT NULL ,
  `client_id` BIGINT NOT NULL ,
  `type` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`contact_id`) ,
  INDEX `fk_contact_client1` (`client_id` ASC) ,
  CONSTRAINT `fk_contact_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `campaign_admin`.`client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`contact_tag`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`contact_tag` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`contact_tag` (
  `contact_tag_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `tag` VARCHAR(45) NOT NULL ,
  `type` INT NOT NULL ,
  `client_id` BIGINT NOT NULL ,
  PRIMARY KEY (`contact_tag_id`) ,
  INDEX `fk_contact_tag_client1` (`client_id` ASC) ,
  CONSTRAINT `fk_contact_tag_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `campaign_admin`.`client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`contact_tag_link`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`contact_tag_link` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`contact_tag_link` (
  `contact_tag_link_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `contact_tag_id` BIGINT NOT NULL ,
  `contact_id` BIGINT NOT NULL ,
  PRIMARY KEY (`contact_tag_link_id`) ,
  INDEX `fk_contact_tag_link_contact_tag1` (`contact_tag_id` ASC) ,
  INDEX `fk_contact_tag_link_contact1` (`contact_id` ASC) ,
  CONSTRAINT `fk_contact_tag_link_contact_tag1`
    FOREIGN KEY (`contact_tag_id` )
    REFERENCES `campaign_admin`.`contact_tag` (`contact_tag_id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contact_tag_link_contact1`
    FOREIGN KEY (`contact_id` )
    REFERENCES `campaign_admin`.`contact` (`contact_id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`reserved_keyword`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`reserved_keyword` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`reserved_keyword` (
  `reserved_keyword_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `keyword` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`reserved_keyword_id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `campaign_admin`.`keyword_limit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `campaign_admin`.`keyword_limit` ;

CREATE  TABLE IF NOT EXISTS `campaign_admin`.`keyword_limit` (
  `keyword_limit_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `client_id` BIGINT NOT NULL ,
  `entry_type` VARCHAR(245) NOT NULL ,
  `max_keywords` INT NOT NULL DEFAULT 5 ,
  PRIMARY KEY (`keyword_limit_id`) ,
  INDEX `fk_keyword_limit_client1` (`client_id` ASC) ,
  UNIQUE INDEX `unique_type` (`client_id` ASC, `entry_type` ASC) ,
  CONSTRAINT `fk_keyword_limit_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `campaign_admin`.`client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
