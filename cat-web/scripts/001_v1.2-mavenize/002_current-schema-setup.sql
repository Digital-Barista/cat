SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

use campaign_admin;

-- -----------------------------------------------------
-- Table `JBM_COUNTER`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_COUNTER` (
  `NAME` VARCHAR(255) NOT NULL DEFAULT '' ,
  `NEXT_ID` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`NAME`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_DUAL`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_DUAL` (
  `DUMMY` INT(11) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`DUMMY`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_ID_CACHE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_ID_CACHE` (
  `NODE_ID` INT(11) NOT NULL DEFAULT '0' ,
  `CNTR` INT(11) NOT NULL DEFAULT '0' ,
  `JBM_ID` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`NODE_ID`, `CNTR`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_MSG`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_MSG` (
  `MESSAGE_ID` BIGINT(20) NOT NULL DEFAULT '0' ,
  `RELIABLE` CHAR(1) NULL DEFAULT NULL ,
  `EXPIRATION` BIGINT(20) NULL DEFAULT NULL ,
  `TIMESTAMP` BIGINT(20) NULL DEFAULT NULL ,
  `PRIORITY` TINYINT(4) NULL DEFAULT NULL ,
  `TYPE` TINYINT(4) NULL DEFAULT NULL ,
  `HEADERS` VARBINARY(2048) NULL DEFAULT NULL ,
  `PAYLOAD` VARBINARY(25600) NULL DEFAULT NULL ,
  PRIMARY KEY (`MESSAGE_ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_MSG_REF`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_MSG_REF` (
  `MESSAGE_ID` BIGINT(20) NOT NULL DEFAULT '0' ,
  `CHANNEL_ID` BIGINT(20) NOT NULL DEFAULT '0' ,
  `TRANSACTION_ID` BIGINT(20) NULL DEFAULT NULL ,
  `STATE` CHAR(1) NULL DEFAULT NULL ,
  `ORD` BIGINT(20) NULL DEFAULT NULL ,
  `PAGE_ORD` BIGINT(20) NULL DEFAULT NULL ,
  `DELIVERY_COUNT` INT(11) NULL DEFAULT NULL ,
  `SCHED_DELIVERY` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`MESSAGE_ID`, `CHANNEL_ID`) ,
  INDEX `JBM_MSG_REF_TX` (`TRANSACTION_ID` ASC, `STATE` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_POSTOFFICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_POSTOFFICE` (
  `POSTOFFICE_NAME` VARCHAR(255) NOT NULL DEFAULT '' ,
  `NODE_ID` INT(11) NOT NULL DEFAULT '0' ,
  `QUEUE_NAME` VARCHAR(255) NOT NULL DEFAULT '' ,
  `COND` VARCHAR(1023) NULL DEFAULT NULL ,
  `SELECTOR` VARCHAR(1023) NULL DEFAULT NULL ,
  `CHANNEL_ID` BIGINT(20) NULL DEFAULT NULL ,
  `CLUSTERED` CHAR(1) NULL DEFAULT NULL ,
  `ALL_NODES` CHAR(1) NULL DEFAULT NULL ,
  PRIMARY KEY (`POSTOFFICE_NAME`, `NODE_ID`, `QUEUE_NAME`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_ROLE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_ROLE` (
  `ROLE_ID` VARCHAR(32) NOT NULL ,
  `USER_ID` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`USER_ID`, `ROLE_ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_TX`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_TX` (
  `NODE_ID` INT(11) NULL DEFAULT NULL ,
  `TRANSACTION_ID` BIGINT(20) NOT NULL DEFAULT '0' ,
  `BRANCH_QUAL` VARBINARY(254) NULL DEFAULT NULL ,
  `FORMAT_ID` INT(11) NULL DEFAULT NULL ,
  `GLOBAL_TXID` VARBINARY(254) NULL DEFAULT NULL ,
  PRIMARY KEY (`TRANSACTION_ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_USER`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_USER` (
  `USER_ID` VARCHAR(32) NOT NULL ,
  `PASSWD` VARCHAR(32) NOT NULL ,
  `CLIENTID` VARCHAR(128) NULL DEFAULT NULL ,
  PRIMARY KEY (`USER_ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaigns`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaigns` (
  `campaign_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(200) NOT NULL ,
  `current_version` INT(11) NOT NULL ,
  `uid` VARCHAR(36) NOT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  `status` VARCHAR(24) NOT NULL DEFAULT 'Active' ,
  `mode` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`campaign_id`) ,
  UNIQUE INDEX `cpn_uuid` (`uid` ASC) ,
  INDEX `cpn_status` (`status` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 73
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `client`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `client` (
  `client_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `contact_name` VARCHAR(128) NULL DEFAULT NULL ,
  `contact_email` VARCHAR(128) NULL DEFAULT NULL ,
  `contact_phone` VARCHAR(32) NULL DEFAULT NULL ,
  `active` TINYINT(1) NOT NULL DEFAULT '1' ,
  PRIMARY KEY (`client_id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `add_in_message`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `add_in_message` (
  `add_in_message_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NULL DEFAULT NULL ,
  `client_id` BIGINT(20) NULL DEFAULT NULL ,
  `entry_type` VARCHAR(64) NOT NULL ,
  `message` TEXT NOT NULL ,
  `type` VARCHAR(64) NOT NULL ,
  PRIMARY KEY (`add_in_message_id`) ,
  UNIQUE INDEX `aim_unique_type` (`client_id` ASC, `entry_type` ASC, `type` ASC, `campaign_id` ASC) ,
  INDEX `fk_add_in_message_client1_idx` (`client_id` ASC) ,
  INDEX `fk_add_in_message_campaigns1_idx` (`campaign_id` ASC) ,
  CONSTRAINT `fk_add_in_message_campaigns1`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_add_in_message_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `audit_generic`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `audit_generic` (
  `audit_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `audit_type` VARCHAR(32) NOT NULL ,
  `audit_time` DATETIME NOT NULL ,
  `descriminator_1` VARCHAR(36) NULL DEFAULT NULL ,
  `descriminator_2` VARCHAR(36) NULL DEFAULT NULL ,
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
ENGINE = InnoDB
AUTO_INCREMENT = 2709
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `audit_incoming_message`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `audit_incoming_message` (
  `incoming_audit_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `incoming_address` VARCHAR(64) NOT NULL ,
  `incoming_type` VARCHAR(16) NOT NULL ,
  `date_received` DATETIME NOT NULL ,
  `msg_or_subject` TEXT NULL DEFAULT NULL ,
  `payload` BLOB NULL DEFAULT NULL ,
  `return_address` VARCHAR(64) NOT NULL ,
  `matched_uid` CHAR(36) NULL DEFAULT NULL ,
  `matched_version` INT(11) NULL DEFAULT NULL ,
  `matched_type` VARCHAR(16) NULL DEFAULT NULL ,
  PRIMARY KEY (`incoming_audit_id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 24
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `audit_outgoing_message`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `audit_outgoing_message` (
  `audit_outgoing_message_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `node_uid` VARCHAR(36) NOT NULL ,
  `node_version` INT(11) NOT NULL ,
  `destination` VARCHAR(64) NOT NULL ,
  `date_sent` DATETIME NOT NULL ,
  `msg_type` VARCHAR(16) NOT NULL ,
  `subject_or_message` TEXT NOT NULL ,
  `payload` BLOB NULL DEFAULT NULL ,
  PRIMARY KEY (`audit_outgoing_message_id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 496
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `blacklist`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `blacklist` (
  `blacklist_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `entry_type` VARCHAR(64) NOT NULL ,
  `address` VARCHAR(256) NOT NULL ,
  `incoming_address` VARCHAR(64) NULL DEFAULT NULL ,
  `client_id` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`blacklist_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `connectors`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `connectors` (
  `connector_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(200) NULL DEFAULT NULL ,
  `type` INT(11) NOT NULL ,
  `campaign_id` INT(11) NOT NULL ,
  `uid` CHAR(36) NOT NULL ,
  PRIMARY KEY (`connector_id`) ,
  UNIQUE INDEX `conn_uid` (`uid` ASC) ,
  INDEX `conn_fk_campaign_idx` (`campaign_id` ASC) ,
  CONSTRAINT `conn_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 400
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_connector_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_connector_link` (
  `ccl_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `connector_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  PRIMARY KEY (`ccl_id`) ,
  UNIQUE INDEX `ccl_unique_all` (`campaign_id` ASC, `connector_id` ASC, `version` ASC) ,
  INDEX `ccl_fk_campaign_idx` (`campaign_id` ASC) ,
  INDEX `ccl_fk_connector_idx` (`connector_id` ASC) ,
  INDEX `ccl_idx_campaign_version` (`campaign_id` ASC, `version` ASC) ,
  CONSTRAINT `ccl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ),
  CONSTRAINT `ccl_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `connectors` (`connector_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 1161
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_entry_points`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_entry_points` (
  `campaign_entry_point_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `entry_point_type` VARCHAR(64) NOT NULL ,
  `entry_point` VARCHAR(128) NOT NULL ,
  `keyword` VARCHAR(64) NULL DEFAULT NULL ,
  `entry_point_qty` INT(11) NOT NULL DEFAULT '0' ,
  `published` TINYINT(4) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`campaign_entry_point_id`) ,
  UNIQUE INDEX `cep_unq` (`entry_point_type` ASC, `entry_point` ASC, `keyword` ASC) ,
  INDEX `cep_fk_campaign_idx` (`campaign_id` ASC) ,
  CONSTRAINT `cep_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 85
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_info` (
  `campaign_info_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `value` TEXT NULL DEFAULT NULL ,
  `entry_type` VARCHAR(64) NULL DEFAULT NULL ,
  `entry_address` VARCHAR(64) NULL DEFAULT NULL ,
  PRIMARY KEY (`campaign_info_id`) ,
  UNIQUE INDEX `unique_name_value` (`campaign_id` ASC, `name` ASC, `entry_type` ASC) ,
  INDEX `fk_campaign_info_campaigns1_idx` (`campaign_id` ASC) ,
  CONSTRAINT `fk_campaign_info_campaigns1`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `nodes`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `nodes` (
  `node_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `type` INT(11) NOT NULL ,
  `name` VARCHAR(200) NULL DEFAULT NULL ,
  `uid` CHAR(36) NOT NULL DEFAULT 'uuid()' ,
  PRIMARY KEY (`node_id`) ,
  UNIQUE INDEX `node_uid` (`uid` ASC) ,
  INDEX `node_fk_campaign_idx` (`campaign_id` ASC) ,
  CONSTRAINT `node_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 329
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_node_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_node_link` (
  `cnl_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `node_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  PRIMARY KEY (`cnl_id`) ,
  UNIQUE INDEX `cnl_unique_link` (`campaign_id` ASC, `node_id` ASC, `version` ASC) ,
  INDEX `cnl_fk_campaign_idx` (`campaign_id` ASC) ,
  INDEX `cnl_fk_node_idx` (`node_id` ASC) ,
  INDEX `cnl_idx_campaign_version` (`campaign_id` ASC, `version` ASC) ,
  CONSTRAINT `cnl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ),
  CONSTRAINT `cnl_fk_node`
    FOREIGN KEY (`node_id` )
    REFERENCES `nodes` (`node_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 983
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `subscribers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `subscribers` (
  `subscriber_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `address` VARCHAR(64) NULL DEFAULT NULL ,
  `subscriber_type` VARCHAR(16) NULL DEFAULT NULL ,
  PRIMARY KEY (`subscriber_id`) ,
  UNIQUE INDEX `sub_unq_index` (`address` ASC, `subscriber_type` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 29
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `campaign_subscriber_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_subscriber_link` (
  `campaign_subscriber_link_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `subscriber_id` BIGINT(20) NOT NULL ,
  `last_completed_node_id` BIGINT(20) NOT NULL ,
  `last_used_entry_point` VARCHAR(64) NOT NULL ,
  `last_used_entry_point_type` VARCHAR(32) NOT NULL ,
  `active` TINYINT(1) NULL DEFAULT '1' ,
  PRIMARY KEY (`campaign_subscriber_link_id`) ,
  UNIQUE INDEX `csl_unq_all` (`campaign_id` ASC, `subscriber_id` ASC) ,
  INDEX `csl_fk_campaign_idx` (`campaign_id` ASC) ,
  INDEX `csl_fk_subscriber_idx` (`subscriber_id` ASC) ,
  INDEX `csl_fk_node_idx` (`last_completed_node_id` ASC) ,
  CONSTRAINT `csl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ),
  CONSTRAINT `csl_fk_node`
    FOREIGN KEY (`last_completed_node_id` )
    REFERENCES `nodes` (`node_id` ),
  CONSTRAINT `csl_fk_subscriber`
    FOREIGN KEY (`subscriber_id` )
    REFERENCES `subscribers` (`subscriber_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 24
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `campaign_versions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_versions` (
  `campaign_version_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `version` INT(11) NOT NULL DEFAULT '0' ,
  `status` VARCHAR(45) NOT NULL ,
  `published_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`campaign_version_id`) ,
  INDEX `cv_fk_campaign_idx` (`campaign_id` ASC) ,
  CONSTRAINT `cv_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 228
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `entry_points`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `entry_points` (
  `entry_point_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `description` VARCHAR(128) NULL DEFAULT NULL ,
  `entry_point` VARCHAR(64) NOT NULL ,
  `entry_type` VARCHAR(32) NOT NULL ,
  `restriction_type` VARCHAR(32) NOT NULL ,
  `restriction_id` BIGINT(20) NULL DEFAULT NULL ,
  `credentials` VARCHAR(256) NULL DEFAULT NULL ,
  PRIMARY KEY (`entry_point_id`) ,
  UNIQUE INDEX `ep_unq` (`entry_point` ASC, `entry_type` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 10
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `client_entry_point_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `client_entry_point_link` (
  `client_entry_point_link_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `client_id` BIGINT(20) NOT NULL ,
  `entry_point_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`client_entry_point_link_id`) ,
  UNIQUE INDEX `cepl_unq` (`client_id` ASC, `entry_point_id` ASC) ,
  INDEX `cepl_fk_client_idx` (`client_id` ASC) ,
  INDEX `cepl_fk_entry_points_idx` (`entry_point_id` ASC) ,
  CONSTRAINT `cepl_fk_client`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` ),
  CONSTRAINT `cepl_fk_entry_points`
    FOREIGN KEY (`entry_point_id` )
    REFERENCES `entry_points` (`entry_point_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 14
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `client_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `client_info` (
  `client_info_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `client_id` BIGINT(20) NOT NULL ,
  `name` VARCHAR(256) NOT NULL ,
  `value` TEXT NULL DEFAULT NULL ,
  `entry_type` VARCHAR(64) NULL DEFAULT NULL ,
  PRIMARY KEY (`client_info_id`) ,
  INDEX `client_info_client_idx` (`client_id` ASC) ,
  CONSTRAINT `client_info_client`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `connector_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `connector_info` (
  `connector_info_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `value` VARCHAR(256) NOT NULL ,
  `connector_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  PRIMARY KEY (`connector_info_id`) ,
  INDEX `ci_fk_connector_idx` (`connector_id` ASC) ,
  INDEX `ci_idx_conn_name` (`connector_id` ASC, `name` ASC, `version` ASC) ,
  CONSTRAINT `ci_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `connectors` (`connector_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 824
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `contact`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `contact` (
  `contact_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `address` VARCHAR(256) NULL DEFAULT NULL ,
  `create_date` DATETIME NOT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  `type` VARCHAR(45) NOT NULL ,
  `alternate_id` VARCHAR(45) NULL DEFAULT NULL ,
  `contact_uid` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`contact_id`) ,
  INDEX `fk_contact_client1_idx` (`client_id` ASC) ,
  UNIQUE INDEX `unique_contact` (`address` ASC, `client_id` ASC, `type` ASC) ,
  CONSTRAINT `fk_contact_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 140
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `contact_tag`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `contact_tag` (
  `contact_tag_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `tag` VARCHAR(45) NOT NULL ,
  `type` INT(11) NOT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`contact_tag_id`) ,
  INDEX `fk_contact_tag_client1_idx` (`client_id` ASC) ,
  UNIQUE INDEX `unique_tag` (`tag` ASC, `type` ASC, `client_id` ASC) ,
  CONSTRAINT `fk_contact_tag_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 24
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `contact_tag_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `contact_tag_link` (
  `contact_tag_link_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `contact_tag_id` BIGINT(20) NOT NULL ,
  `contact_id` BIGINT(20) NOT NULL ,
  `initial_tag_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`contact_tag_link_id`) ,
  INDEX `fk_contact_tag_link_contact_tag1_idx` (`contact_tag_id` ASC) ,
  INDEX `fk_contact_tag_link_contact1_idx` (`contact_id` ASC) ,
  UNIQUE INDEX `tag_contact_unique` (`contact_tag_id` ASC, `contact_id` ASC) ,
  CONSTRAINT `fk_contact_tag_link_contact1`
    FOREIGN KEY (`contact_id` )
    REFERENCES `contact` (`contact_id` )
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contact_tag_link_contact_tag1`
    FOREIGN KEY (`contact_tag_id` )
    REFERENCES `contact_tag` (`contact_tag_id` )
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 57
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `coupon_counters`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `coupon_counters` (
  `coupon_code_length` INT(11) NOT NULL ,
  `coupon_bit_scramble` BLOB NOT NULL ,
  `coupon_next_number` BIGINT(20) NOT NULL DEFAULT '1' ,
  PRIMARY KEY (`coupon_code_length`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `coupon_offers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `coupon_offers` (
  `coupon_offer_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `max_coupons_issued` BIGINT(20) NOT NULL ,
  `coupon_issue_count` BIGINT(20) NOT NULL DEFAULT '0' ,
  `rejected_response_count` BIGINT(20) NOT NULL DEFAULT '0' ,
  `expiration_date` DATETIME NULL DEFAULT NULL ,
  `unavailable_date` DATETIME NULL DEFAULT NULL ,
  `node_uid` VARCHAR(36) NOT NULL ,
  `max_redemptions` BIGINT(20) NOT NULL ,
  `coupon_name` VARCHAR(256) NULL DEFAULT NULL ,
  `campaign_id` INT(11) NULL DEFAULT NULL ,
  `expiration_days` INT(11) NULL DEFAULT NULL ,
  `offer_code` VARCHAR(64) NULL DEFAULT NULL ,
  PRIMARY KEY (`coupon_offer_id`) ,
  INDEX `co_node_uid` (`node_uid` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 66
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `coupon_responses`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `coupon_responses` (
  `coupon_response_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `response_date` DATETIME NOT NULL ,
  `response_detail` VARCHAR(16) NULL DEFAULT NULL ,
  `coupon_offer_id` BIGINT(20) NOT NULL ,
  `subscriber_id` BIGINT(20) NOT NULL ,
  `response_type` VARCHAR(16) NOT NULL ,
  `coupon_message` TEXT NOT NULL ,
  `redemption_count` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`coupon_response_id`) ,
  INDEX `cr_coupon_offer_fk_idx` (`coupon_offer_id` ASC) ,
  INDEX `cr_subscriber_fk_idx` (`subscriber_id` ASC) ,
  INDEX `cr_response_date` (`response_date` ASC) ,
  INDEX `cr_response_type` (`response_type` ASC) ,
  INDEX `cr_response_detail` (`response_detail` ASC) ,
  CONSTRAINT `cr_coupon_offer_fk`
    FOREIGN KEY (`coupon_offer_id` )
    REFERENCES `coupon_offers` (`coupon_offer_id` ),
  CONSTRAINT `cr_subscriber_fk`
    FOREIGN KEY (`subscriber_id` )
    REFERENCES `subscribers` (`subscriber_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 132
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `coupon_redemptions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `coupon_redemptions` (
  `coupon_redemption_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `redemption_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `redeemed_by_username` VARCHAR(50) NOT NULL ,
  `coupon_response_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`coupon_redemption_id`) ,
  INDEX `cred_date` (`redemption_date` ASC) ,
  INDEX `cred_response_fk_idx` (`coupon_response_id` ASC) ,
  CONSTRAINT `cred_response_fk`
    FOREIGN KEY (`coupon_response_id` )
    REFERENCES `coupon_responses` (`coupon_response_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `facebook_app`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `facebook_app` (
  `facebook_app_id` VARCHAR(256) NOT NULL ,
  `api_key` VARCHAR(32) NOT NULL ,
  `secret` VARCHAR(32) NOT NULL ,
  `id` VARCHAR(45) NULL DEFAULT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`facebook_app_id`, `client_id`) ,
  INDEX `fk_facebook_app_client1_idx` (`client_id` ASC) ,
  CONSTRAINT `fk_facebook_app_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `facebook_message`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `facebook_message` (
  `facebook_message_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `facebook_app_id` VARCHAR(256) NOT NULL ,
  `facebook_uid` VARCHAR(256) NOT NULL ,
  `title` VARCHAR(1024) NULL DEFAULT NULL ,
  `body` TEXT NULL DEFAULT NULL ,
  `create_date` DATETIME NOT NULL ,
  `metadata` VARCHAR(1024) NULL DEFAULT NULL ,
  `response` VARCHAR(1024) NULL DEFAULT NULL ,
  PRIMARY KEY (`facebook_message_id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 369
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `jms_messages`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_messages` (
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
-- Table `jms_roles`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_roles` (
  `ROLEID` VARCHAR(32) NOT NULL ,
  `USERID` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`USERID`, `ROLEID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `jms_subscriptions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_subscriptions` (
  `CLIENTID` VARCHAR(128) NOT NULL ,
  `SUBNAME` VARCHAR(128) NOT NULL ,
  `TOPIC` VARCHAR(255) NOT NULL ,
  `SELECTOR` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`CLIENTID`, `SUBNAME`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `jms_transactions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_transactions` (
  `TXID` INT(11) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`TXID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `jms_users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_users` (
  `USERID` VARCHAR(32) NOT NULL ,
  `PASSWD` VARCHAR(32) NOT NULL ,
  `CLIENTID` VARCHAR(128) NULL DEFAULT NULL ,
  PRIMARY KEY (`USERID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `keyword_limit`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `keyword_limit` (
  `keyword_limit_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `client_id` BIGINT(20) NOT NULL ,
  `entry_type` VARCHAR(245) NOT NULL ,
  `max_keywords` INT(11) NOT NULL DEFAULT '5' ,
  PRIMARY KEY (`keyword_limit_id`) ,
  UNIQUE INDEX `unique_type` (`client_id` ASC, `entry_type` ASC) ,
  INDEX `fk_keyword_limit_client1_idx` (`client_id` ASC) ,
  CONSTRAINT `fk_keyword_limit_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 13
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `keywords`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `keywords` (
  `keyword_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `entry_point_id` BIGINT(20) NOT NULL ,
  `keyword` VARCHAR(64) NOT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`keyword_id`) ,
  UNIQUE INDEX `kwd_unq` (`keyword` ASC, `entry_point_id` ASC, `client_id` ASC) ,
  INDEX `kwd_fk_entry_point_idx` (`entry_point_id` ASC) ,
  INDEX `kwd_fk_client_idx` (`client_id` ASC) ,
  CONSTRAINT `kwd_fk_client`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` ),
  CONSTRAINT `kwd_fk_entry_point`
    FOREIGN KEY (`entry_point_id` )
    REFERENCES `entry_points` (`entry_point_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 67
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `layout_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `layout_info` (
  `layout_info_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `x_loc` INT(11) NOT NULL ,
  `y_loc` INT(11) NOT NULL ,
  `version` INT(11) NOT NULL ,
  `uid` CHAR(36) NOT NULL ,
  PRIMARY KEY (`layout_info_id`) ,
  UNIQUE INDEX `li_unique_all` (`campaign_id` ASC, `version` ASC, `uid` ASC) ,
  INDEX `li_fk_campaign_idx` (`campaign_id` ASC) ,
  INDEX `li_campaign_version` (`campaign_id` ASC, `version` ASC) ,
  CONSTRAINT `li_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 2973
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `node_connector_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `node_connector_link` (
  `node_connector_link_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `node_id` BIGINT(20) NOT NULL ,
  `connector_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  `type` INT(11) NOT NULL ,
  PRIMARY KEY (`node_connector_link_id`) ,
  UNIQUE INDEX `ncl_unique_all` (`node_id` ASC, `connector_id` ASC, `version` ASC, `type` ASC) ,
  INDEX `ncl_fk_node_idx` (`node_id` ASC) ,
  INDEX `ncl_fk_connector_idx` (`connector_id` ASC) ,
  INDEX `ncl_idx_node_version` (`node_id` ASC, `version` ASC, `type` ASC) ,
  INDEX `ncl_idx_connector_version` (`connector_id` ASC, `version` ASC, `type` ASC) ,
  CONSTRAINT `ncl_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `connectors` (`connector_id` ),
  CONSTRAINT `ncl_fk_node`
    FOREIGN KEY (`node_id` )
    REFERENCES `nodes` (`node_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 1629
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `node_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `node_info` (
  `node_info_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `value` TEXT NOT NULL ,
  `node_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  PRIMARY KEY (`node_info_id`) ,
  INDEX `ni_idx_node_name` (`node_id` ASC, `name` ASC, `version` ASC) ,
  INDEX `ni_fk_node_id_idx` (`node_id` ASC) ,
  CONSTRAINT `ni_fk_node_id`
    FOREIGN KEY (`node_id` )
    REFERENCES `nodes` (`node_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 2218
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `reserved_keyword`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `reserved_keyword` (
  `reserved_keyword_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `keyword` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`reserved_keyword_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `roles`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `roles` (
  `role_pk` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '\\\'Primary Key for this tabl' ,
  `username` VARCHAR(50) NOT NULL COMMENT '\\\'Username applied to this table' ,
  `role_name` VARCHAR(50) NOT NULL COMMENT '\\\'Actual permission nam' ,
  `role_type` VARCHAR(50) NOT NULL COMMENT '\\\'\\\'\\\'Actual role t' ,
  `ref_id` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`role_pk`) ,
  INDEX `Index_2` (`username` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 17
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `scheduled_campaign_tasks`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `scheduled_campaign_tasks` (
  `scheduled_task_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `scheduled_date` DATETIME NOT NULL ,
  `source_uid` VARCHAR(36) NOT NULL ,
  `target` VARCHAR(36) NULL DEFAULT NULL ,
  `event_type` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`scheduled_task_id`) ,
  INDEX `sct_idx_date` (`scheduled_date` ASC) ,
  INDEX `sct_idx_source` (`source_uid` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `users` (
  `username` VARCHAR(50) NOT NULL COMMENT '\\\'The case-sensitive logi' ,
  `user_pk` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '\\\'Primary Key for the tabl' ,
  `password` VARCHAR(100) NOT NULL COMMENT '\\\'The encrypted password for this accoun' ,
  `active` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '\\\'Flag set to disable an account' ,
  `email` VARCHAR(100) NULL DEFAULT NULL ,
  `name` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`user_pk`) )
ENGINE = InnoDB
AUTO_INCREMENT = 12
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `contact_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `contact_info` (
  `contact_info_id` INT NOT NULL AUTO_INCREMENT ,
  `contact_id` BIGINT(20) NOT NULL ,
  `name` VARCHAR(245) NOT NULL ,
  `value` TEXT NULL ,
  PRIMARY KEY (`contact_info_id`) ,
  INDEX `fk_contact_info_contact1_idx` (`contact_id` ASC) ,
  CONSTRAINT `fk_contact_info_contact1`
    FOREIGN KEY (`contact_id` )
    REFERENCES `contact` (`contact_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `JBM_COUNTER`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_COUNTER` (
  `NAME` VARCHAR(255) NOT NULL DEFAULT '' ,
  `NEXT_ID` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`NAME`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_DUAL`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_DUAL` (
  `DUMMY` INT(11) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`DUMMY`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_ID_CACHE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_ID_CACHE` (
  `NODE_ID` INT(11) NOT NULL DEFAULT '0' ,
  `CNTR` INT(11) NOT NULL DEFAULT '0' ,
  `JBM_ID` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`NODE_ID`, `CNTR`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_MSG`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_MSG` (
  `MESSAGE_ID` BIGINT(20) NOT NULL DEFAULT '0' ,
  `RELIABLE` CHAR(1) NULL DEFAULT NULL ,
  `EXPIRATION` BIGINT(20) NULL DEFAULT NULL ,
  `TIMESTAMP` BIGINT(20) NULL DEFAULT NULL ,
  `PRIORITY` TINYINT(4) NULL DEFAULT NULL ,
  `TYPE` TINYINT(4) NULL DEFAULT NULL ,
  `HEADERS` VARBINARY(2048) NULL DEFAULT NULL ,
  `PAYLOAD` VARBINARY(25600) NULL DEFAULT NULL ,
  PRIMARY KEY (`MESSAGE_ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_MSG_REF`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_MSG_REF` (
  `MESSAGE_ID` BIGINT(20) NOT NULL DEFAULT '0' ,
  `CHANNEL_ID` BIGINT(20) NOT NULL DEFAULT '0' ,
  `TRANSACTION_ID` BIGINT(20) NULL DEFAULT NULL ,
  `STATE` CHAR(1) NULL DEFAULT NULL ,
  `ORD` BIGINT(20) NULL DEFAULT NULL ,
  `PAGE_ORD` BIGINT(20) NULL DEFAULT NULL ,
  `DELIVERY_COUNT` INT(11) NULL DEFAULT NULL ,
  `SCHED_DELIVERY` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`MESSAGE_ID`, `CHANNEL_ID`) ,
  INDEX `JBM_MSG_REF_TX` (`TRANSACTION_ID` ASC, `STATE` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_POSTOFFICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_POSTOFFICE` (
  `POSTOFFICE_NAME` VARCHAR(255) NOT NULL DEFAULT '' ,
  `NODE_ID` INT(11) NOT NULL DEFAULT '0' ,
  `QUEUE_NAME` VARCHAR(255) NOT NULL DEFAULT '' ,
  `COND` VARCHAR(1023) NULL DEFAULT NULL ,
  `SELECTOR` VARCHAR(1023) NULL DEFAULT NULL ,
  `CHANNEL_ID` BIGINT(20) NULL DEFAULT NULL ,
  `CLUSTERED` CHAR(1) NULL DEFAULT NULL ,
  `ALL_NODES` CHAR(1) NULL DEFAULT NULL ,
  PRIMARY KEY (`POSTOFFICE_NAME`, `NODE_ID`, `QUEUE_NAME`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_ROLE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_ROLE` (
  `ROLE_ID` VARCHAR(32) NOT NULL ,
  `USER_ID` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`USER_ID`, `ROLE_ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_TX`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_TX` (
  `NODE_ID` INT(11) NULL DEFAULT NULL ,
  `TRANSACTION_ID` BIGINT(20) NOT NULL DEFAULT '0' ,
  `BRANCH_QUAL` VARBINARY(254) NULL DEFAULT NULL ,
  `FORMAT_ID` INT(11) NULL DEFAULT NULL ,
  `GLOBAL_TXID` VARBINARY(254) NULL DEFAULT NULL ,
  PRIMARY KEY (`TRANSACTION_ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `JBM_USER`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `JBM_USER` (
  `USER_ID` VARCHAR(32) NOT NULL ,
  `PASSWD` VARCHAR(32) NOT NULL ,
  `CLIENTID` VARCHAR(128) NULL DEFAULT NULL ,
  PRIMARY KEY (`USER_ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaigns`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaigns` (
  `campaign_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(200) NOT NULL ,
  `current_version` INT(11) NOT NULL ,
  `uid` VARCHAR(36) NOT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  `status` VARCHAR(24) NOT NULL DEFAULT 'Active' ,
  `mode` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`campaign_id`) ,
  UNIQUE INDEX `cpn_uuid` (`uid` ASC) ,
  INDEX `cpn_status` (`status` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 49
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `client`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `client` (
  `client_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `contact_name` VARCHAR(128) NULL DEFAULT NULL ,
  `contact_email` VARCHAR(128) NULL DEFAULT NULL ,
  `contact_phone` VARCHAR(32) NULL DEFAULT NULL ,
  `active` TINYINT(1) NOT NULL DEFAULT '1' ,
  PRIMARY KEY (`client_id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 8
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `add_in_message`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `add_in_message` (
  `add_in_message_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NULL DEFAULT NULL ,
  `client_id` BIGINT(20) NULL DEFAULT NULL ,
  `entry_type` VARCHAR(64) NOT NULL ,
  `message` TEXT NOT NULL ,
  `type` VARCHAR(64) NOT NULL ,
  PRIMARY KEY (`add_in_message_id`) ,
  UNIQUE INDEX `aim_unique_type` (`client_id` ASC, `entry_type` ASC, `type` ASC, `campaign_id` ASC) ,
  INDEX `fk_add_in_message_client1_idx` (`client_id` ASC) ,
  INDEX `fk_add_in_message_campaigns1_idx` (`campaign_id` ASC) ,
  CONSTRAINT `fk_add_in_message_campaigns1`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_add_in_message_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `audit_generic`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `audit_generic` (
  `audit_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `audit_type` VARCHAR(32) NOT NULL ,
  `audit_time` DATETIME NOT NULL ,
  `descriminator_1` VARCHAR(36) NULL DEFAULT NULL ,
  `descriminator_2` VARCHAR(36) NULL DEFAULT NULL ,
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
ENGINE = InnoDB
AUTO_INCREMENT = 4230
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `audit_incoming_message`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `audit_incoming_message` (
  `incoming_audit_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `incoming_address` VARCHAR(64) NOT NULL ,
  `incoming_type` VARCHAR(16) NOT NULL ,
  `date_received` DATETIME NOT NULL ,
  `msg_or_subject` TEXT NULL DEFAULT NULL ,
  `payload` BLOB NULL DEFAULT NULL ,
  `return_address` VARCHAR(64) NOT NULL ,
  `matched_uid` CHAR(36) NULL DEFAULT NULL ,
  `matched_version` INT(11) NULL DEFAULT NULL ,
  `matched_type` VARCHAR(16) NULL DEFAULT NULL ,
  PRIMARY KEY (`incoming_audit_id`) ,
  INDEX `idx_audit_incoming_type` (`incoming_type` ASC) ,
  INDEX `idx_audit_incoming_matched_uid` (`matched_uid` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 1038
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `audit_outgoing_message`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `audit_outgoing_message` (
  `audit_outgoing_message_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `node_uid` VARCHAR(36) NOT NULL ,
  `node_version` INT(11) NOT NULL ,
  `destination` VARCHAR(64) NOT NULL ,
  `date_sent` DATETIME NOT NULL ,
  `msg_type` VARCHAR(16) NOT NULL ,
  `subject_or_message` TEXT NOT NULL ,
  `payload` BLOB NULL DEFAULT NULL ,
  PRIMARY KEY (`audit_outgoing_message_id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 20852
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `blacklist`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `blacklist` (
  `blacklist_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `entry_type` VARCHAR(64) NOT NULL ,
  `address` VARCHAR(256) NOT NULL ,
  PRIMARY KEY (`blacklist_id`) ,
  UNIQUE INDEX `unique_address_type` (`entry_type` ASC, `address` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `connectors`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `connectors` (
  `connector_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(200) NULL DEFAULT NULL ,
  `type` INT(11) NOT NULL ,
  `campaign_id` INT(11) NOT NULL ,
  `uid` CHAR(36) NOT NULL ,
  PRIMARY KEY (`connector_id`) ,
  UNIQUE INDEX `conn_uid` (`uid` ASC) ,
  INDEX `conn_fk_campaign_idx` (`campaign_id` ASC) ,
  CONSTRAINT `conn_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 343
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_connector_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_connector_link` (
  `ccl_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `connector_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  PRIMARY KEY (`ccl_id`) ,
  UNIQUE INDEX `ccl_unique_all` (`campaign_id` ASC, `connector_id` ASC, `version` ASC) ,
  INDEX `ccl_fk_campaign_idx` (`campaign_id` ASC) ,
  INDEX `ccl_fk_connector_idx` (`connector_id` ASC) ,
  INDEX `ccl_idx_campaign_version` (`campaign_id` ASC, `version` ASC) ,
  CONSTRAINT `ccl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ),
  CONSTRAINT `ccl_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `connectors` (`connector_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 3277
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_entry_points`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_entry_points` (
  `campaign_entry_point_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `entry_point_type` VARCHAR(64) NOT NULL ,
  `entry_point` VARCHAR(128) NOT NULL ,
  `keyword` VARCHAR(64) NULL DEFAULT NULL ,
  `entry_point_qty` INT(11) NOT NULL DEFAULT '0' ,
  `published` TINYINT(4) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`campaign_entry_point_id`) ,
  UNIQUE INDEX `cep_unq` (`entry_point_type` ASC, `entry_point` ASC, `keyword` ASC) ,
  INDEX `cep_fk_campaign_idx` (`campaign_id` ASC) ,
  CONSTRAINT `cep_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 78
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_info` (
  `campaign_info_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `value` TEXT NULL DEFAULT NULL ,
  `entry_type` VARCHAR(64) NULL DEFAULT NULL ,
  `entry_address` VARCHAR(64) NULL DEFAULT NULL ,
  PRIMARY KEY (`campaign_info_id`) ,
  UNIQUE INDEX `unique_name_value` (`campaign_id` ASC, `name` ASC, `entry_type` ASC) ,
  INDEX `fk_campaign_info_campaigns1_idx` (`campaign_id` ASC) ,
  CONSTRAINT `fk_campaign_info_campaigns1`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `nodes`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `nodes` (
  `node_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `type` INT(11) NOT NULL ,
  `name` VARCHAR(200) NULL DEFAULT NULL ,
  `uid` CHAR(36) NOT NULL DEFAULT 'uuid()' ,
  PRIMARY KEY (`node_id`) ,
  UNIQUE INDEX `node_uid` (`uid` ASC) ,
  INDEX `node_fk_campaign_idx` (`campaign_id` ASC) ,
  CONSTRAINT `node_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 345
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_node_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_node_link` (
  `cnl_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `node_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  PRIMARY KEY (`cnl_id`) ,
  UNIQUE INDEX `cnl_unique_link` (`campaign_id` ASC, `node_id` ASC, `version` ASC) ,
  INDEX `cnl_fk_campaign_idx` (`campaign_id` ASC) ,
  INDEX `cnl_fk_node_idx` (`node_id` ASC) ,
  INDEX `cnl_idx_campaign_version` (`campaign_id` ASC, `version` ASC) ,
  CONSTRAINT `cnl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ),
  CONSTRAINT `cnl_fk_node`
    FOREIGN KEY (`node_id` )
    REFERENCES `nodes` (`node_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 2956
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `subscribers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `subscribers` (
  `subscriber_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `email` VARCHAR(128) NULL DEFAULT NULL ,
  `phone_number` VARCHAR(16) NULL DEFAULT NULL ,
  `twitter_name` VARCHAR(64) NULL DEFAULT NULL ,
  `twitter_id` VARCHAR(64) NULL DEFAULT NULL ,
  `facebook_id` VARCHAR(64) NULL DEFAULT NULL ,
  `facebook_name` VARCHAR(64) NULL DEFAULT NULL ,
  PRIMARY KEY (`subscriber_id`) ,
  UNIQUE INDEX `sub_unq` (`email` ASC, `phone_number` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 2752
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_subscriber_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_subscriber_link` (
  `campaign_subscriber_link_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `subscriber_id` BIGINT(20) NOT NULL ,
  `last_completed_node_id` BIGINT(20) NOT NULL ,
  `last_used_entry_point` VARCHAR(64) NOT NULL ,
  `last_used_entry_point_type` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`campaign_subscriber_link_id`) ,
  UNIQUE INDEX `csl_unq_all` (`campaign_id` ASC, `subscriber_id` ASC) ,
  INDEX `csl_fk_campaign_idx` (`campaign_id` ASC) ,
  INDEX `csl_fk_subscriber_idx` (`subscriber_id` ASC) ,
  INDEX `csl_fk_node_idx` (`last_completed_node_id` ASC) ,
  CONSTRAINT `csl_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ),
  CONSTRAINT `csl_fk_node`
    FOREIGN KEY (`last_completed_node_id` )
    REFERENCES `nodes` (`node_id` ),
  CONSTRAINT `csl_fk_subscriber`
    FOREIGN KEY (`subscriber_id` )
    REFERENCES `subscribers` (`subscriber_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 18435
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `campaign_versions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `campaign_versions` (
  `campaign_version_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `version` INT(11) NOT NULL DEFAULT '0' ,
  `status` VARCHAR(45) NOT NULL ,
  `published_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`campaign_version_id`) ,
  INDEX `cv_fk_campaign_idx` (`campaign_id` ASC) ,
  CONSTRAINT `cv_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 265
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `entry_points`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `entry_points` (
  `entry_point_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `description` VARCHAR(128) NULL DEFAULT NULL ,
  `entry_point` VARCHAR(64) NOT NULL ,
  `entry_type` VARCHAR(32) NOT NULL ,
  `restriction_type` VARCHAR(32) NOT NULL ,
  `restriction_id` BIGINT(20) NULL DEFAULT NULL ,
  `credentials` VARCHAR(256) NULL DEFAULT NULL ,
  PRIMARY KEY (`entry_point_id`) ,
  UNIQUE INDEX `ep_unq` (`entry_point` ASC, `entry_type` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `client_entry_point_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `client_entry_point_link` (
  `client_entry_point_link_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `client_id` BIGINT(20) NOT NULL ,
  `entry_point_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`client_entry_point_link_id`) ,
  UNIQUE INDEX `cepl_unq` (`client_id` ASC, `entry_point_id` ASC) ,
  INDEX `cepl_fk_client_idx` (`client_id` ASC) ,
  INDEX `cepl_fk_entry_points_idx` (`entry_point_id` ASC) ,
  CONSTRAINT `cepl_fk_client`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` ),
  CONSTRAINT `cepl_fk_entry_points`
    FOREIGN KEY (`entry_point_id` )
    REFERENCES `entry_points` (`entry_point_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `client_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `client_info` (
  `client_info_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `client_id` BIGINT(20) NOT NULL ,
  `name` VARCHAR(256) NOT NULL ,
  `value` TEXT NULL DEFAULT NULL ,
  `entry_type` VARCHAR(64) NULL DEFAULT NULL ,
  PRIMARY KEY (`client_info_id`) ,
  INDEX `client_info_client_idx` (`client_id` ASC) ,
  CONSTRAINT `client_info_client`
    FOREIGN KEY (`client_id` )
    REFERENCES `campaign_admin`.`client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `connector_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `connector_info` (
  `connector_info_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `value` VARCHAR(256) NOT NULL ,
  `connector_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  PRIMARY KEY (`connector_info_id`) ,
  INDEX `ci_fk_connector_idx` (`connector_id` ASC) ,
  INDEX `ci_idx_conn_name` (`connector_id` ASC, `name` ASC, `version` ASC) ,
  CONSTRAINT `ci_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `connectors` (`connector_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 4844
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `contact`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `contact` (
  `contact_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `address` VARCHAR(256) NULL DEFAULT NULL ,
  `create_date` DATETIME NOT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  `type` VARCHAR(45) NOT NULL ,
  `alternate_id` VARCHAR(45) NULL DEFAULT NULL ,
  `contact_uid` VARCHAR(36) NULL ,
  PRIMARY KEY (`contact_id`) ,
  UNIQUE INDEX `contact_unique_idx` (`address` ASC, `client_id` ASC, `type` ASC) ,
  INDEX `fk_contact_client1_idx` (`client_id` ASC) ,
  CONSTRAINT `fk_contact_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 2799
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `contact_tag`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `contact_tag` (
  `contact_tag_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `tag` VARCHAR(45) NOT NULL ,
  `type` INT(11) NOT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`contact_tag_id`) ,
  INDEX `fk_contact_tag_client1_idx` (`client_id` ASC) ,
  UNIQUE INDEX `unique_tag` (`tag` ASC, `type` ASC, `client_id` ASC) ,
  CONSTRAINT `fk_contact_tag_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 99
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `contact_tag_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `contact_tag_link` (
  `contact_tag_link_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `contact_tag_id` BIGINT(20) NOT NULL ,
  `contact_id` BIGINT(20) NOT NULL ,
  `initial_tag_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`contact_tag_link_id`) ,
  INDEX `fk_contact_tag_link_contact_tag1_idx` (`contact_tag_id` ASC) ,
  INDEX `fk_contact_tag_link_contact1_idx` (`contact_id` ASC) ,
  UNIQUE INDEX `tag_contact_unique` (`contact_tag_id` ASC, `contact_id` ASC) ,
  CONSTRAINT `fk_contact_tag_link_contact1`
    FOREIGN KEY (`contact_id` )
    REFERENCES `contact` (`contact_id` )
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contact_tag_link_contact_tag1`
    FOREIGN KEY (`contact_tag_id` )
    REFERENCES `contact_tag` (`contact_tag_id` )
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 4447
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `coupon_counters`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `coupon_counters` (
  `coupon_code_length` INT(11) NOT NULL ,
  `coupon_bit_scramble` BLOB NOT NULL ,
  `coupon_next_number` BIGINT(20) NOT NULL DEFAULT '1' ,
  PRIMARY KEY (`coupon_code_length`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `coupon_offers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `coupon_offers` (
  `coupon_offer_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `max_coupons_issued` BIGINT(20) NOT NULL ,
  `coupon_issue_count` BIGINT(20) NOT NULL DEFAULT '0' ,
  `rejected_response_count` BIGINT(20) NOT NULL DEFAULT '0' ,
  `expiration_date` DATETIME NULL DEFAULT NULL ,
  `unavailable_date` DATETIME NULL DEFAULT NULL ,
  `node_uid` VARCHAR(36) NOT NULL ,
  `max_redemptions` BIGINT(20) NOT NULL ,
  `coupon_name` VARCHAR(256) NULL DEFAULT NULL ,
  `campaign_id` INT(11) NULL DEFAULT NULL ,
  `expiration_days` INT(11) NULL DEFAULT NULL ,
  `offer_code` VARCHAR(64) NULL DEFAULT NULL ,
  PRIMARY KEY (`coupon_offer_id`) ,
  INDEX `co_node_uid` (`node_uid` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 94
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `coupon_responses`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `coupon_responses` (
  `coupon_response_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `response_date` DATETIME NOT NULL ,
  `response_detail` VARCHAR(16) NULL DEFAULT NULL ,
  `coupon_offer_id` BIGINT(20) NOT NULL ,
  `subscriber_id` BIGINT(20) NOT NULL ,
  `response_type` VARCHAR(16) NOT NULL ,
  `coupon_message` TEXT NOT NULL ,
  `redemption_count` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`coupon_response_id`) ,
  INDEX `cr_coupon_offer_fk_idx` (`coupon_offer_id` ASC) ,
  INDEX `cr_subscriber_fk_idx` (`subscriber_id` ASC) ,
  INDEX `cr_response_date` (`response_date` ASC) ,
  INDEX `cr_response_type` (`response_type` ASC) ,
  INDEX `cr_response_detail` (`response_detail` ASC) ,
  CONSTRAINT `cr_coupon_offer_fk`
    FOREIGN KEY (`coupon_offer_id` )
    REFERENCES `coupon_offers` (`coupon_offer_id` ),
  CONSTRAINT `cr_subscriber_fk`
    FOREIGN KEY (`subscriber_id` )
    REFERENCES `subscribers` (`subscriber_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 17994
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `coupon_redemptions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `coupon_redemptions` (
  `coupon_redemption_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `redemption_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP ,
  `redeemed_by_username` VARCHAR(50) NOT NULL ,
  `coupon_response_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`coupon_redemption_id`) ,
  INDEX `cred_date` (`redemption_date` ASC) ,
  INDEX `cred_response_fk_idx` (`coupon_response_id` ASC) ,
  CONSTRAINT `cred_response_fk`
    FOREIGN KEY (`coupon_response_id` )
    REFERENCES `coupon_responses` (`coupon_response_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 25
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `facebook_app`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `facebook_app` (
  `facebook_app_id` VARCHAR(256) NOT NULL ,
  `api_key` VARCHAR(32) NOT NULL ,
  `secret` VARCHAR(32) NOT NULL ,
  `id` VARCHAR(45) NULL DEFAULT NULL ,
  `client_id` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`facebook_app_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `facebook_message`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `facebook_message` (
  `facebook_message_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `facebook_app_id` VARCHAR(256) NOT NULL ,
  `facebook_uid` VARCHAR(256) NOT NULL ,
  `title` VARCHAR(1024) NULL DEFAULT NULL ,
  `body` TEXT NULL DEFAULT NULL ,
  `create_date` DATETIME NOT NULL ,
  `metadata` VARCHAR(1024) NULL DEFAULT NULL ,
  `response` VARCHAR(1024) NULL DEFAULT NULL ,
  PRIMARY KEY (`facebook_message_id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 20659
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `jms_messages`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_messages` (
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
-- Table `jms_roles`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_roles` (
  `ROLEID` VARCHAR(32) NOT NULL ,
  `USERID` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`USERID`, `ROLEID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `jms_subscriptions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_subscriptions` (
  `CLIENTID` VARCHAR(128) NOT NULL ,
  `SUBNAME` VARCHAR(128) NOT NULL ,
  `TOPIC` VARCHAR(255) NOT NULL ,
  `SELECTOR` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`CLIENTID`, `SUBNAME`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `jms_transactions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_transactions` (
  `TXID` INT(11) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`TXID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `jms_users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `jms_users` (
  `USERID` VARCHAR(32) NOT NULL ,
  `PASSWD` VARCHAR(32) NOT NULL ,
  `CLIENTID` VARCHAR(128) NULL DEFAULT NULL ,
  PRIMARY KEY (`USERID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `keyword_limit`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `keyword_limit` (
  `keyword_limit_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `client_id` BIGINT(20) NOT NULL ,
  `entry_type` VARCHAR(245) NOT NULL ,
  `max_keywords` INT(11) NOT NULL DEFAULT '5' ,
  PRIMARY KEY (`keyword_limit_id`) ,
  UNIQUE INDEX `unique_type` (`client_id` ASC, `entry_type` ASC) ,
  INDEX `fk_keyword_limit_client1_idx` (`client_id` ASC) ,
  CONSTRAINT `fk_keyword_limit_client1`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 11
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `keywords`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `keywords` (
  `keyword_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `entry_point_id` BIGINT(20) NOT NULL ,
  `keyword` VARCHAR(64) NOT NULL ,
  `client_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`keyword_id`) ,
  UNIQUE INDEX `kwd_unq` (`keyword` ASC, `entry_point_id` ASC, `client_id` ASC) ,
  INDEX `kwd_fk_entry_point_idx` (`entry_point_id` ASC) ,
  INDEX `kwd_fk_client_idx` (`client_id` ASC) ,
  CONSTRAINT `kwd_fk_client`
    FOREIGN KEY (`client_id` )
    REFERENCES `client` (`client_id` ),
  CONSTRAINT `kwd_fk_entry_point`
    FOREIGN KEY (`entry_point_id` )
    REFERENCES `entry_points` (`entry_point_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 89
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `layout_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `layout_info` (
  `layout_info_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `campaign_id` INT(11) NOT NULL ,
  `x_loc` INT(11) NOT NULL ,
  `y_loc` INT(11) NOT NULL ,
  `version` INT(11) NOT NULL ,
  `uid` CHAR(36) NOT NULL ,
  PRIMARY KEY (`layout_info_id`) ,
  UNIQUE INDEX `li_unique_all` (`campaign_id` ASC, `version` ASC, `uid` ASC) ,
  INDEX `li_fk_campaign_idx` (`campaign_id` ASC) ,
  INDEX `li_campaign_version` (`campaign_id` ASC, `version` ASC) ,
  CONSTRAINT `li_fk_campaign`
    FOREIGN KEY (`campaign_id` )
    REFERENCES `campaigns` (`campaign_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 6993
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `node_connector_link`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `node_connector_link` (
  `node_connector_link_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `node_id` BIGINT(20) NOT NULL ,
  `connector_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  `type` INT(11) NOT NULL ,
  PRIMARY KEY (`node_connector_link_id`) ,
  UNIQUE INDEX `ncl_unique_all` (`node_id` ASC, `connector_id` ASC, `version` ASC, `type` ASC) ,
  INDEX `ncl_fk_node_idx` (`node_id` ASC) ,
  INDEX `ncl_fk_connector_idx` (`connector_id` ASC) ,
  INDEX `ncl_idx_node_version` (`node_id` ASC, `version` ASC, `type` ASC) ,
  INDEX `ncl_idx_connector_version` (`connector_id` ASC, `version` ASC, `type` ASC) ,
  CONSTRAINT `ncl_fk_connector`
    FOREIGN KEY (`connector_id` )
    REFERENCES `connectors` (`connector_id` ),
  CONSTRAINT `ncl_fk_node`
    FOREIGN KEY (`node_id` )
    REFERENCES `nodes` (`node_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 6595
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `node_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `node_info` (
  `node_info_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `value` TEXT NOT NULL ,
  `node_id` BIGINT(20) NOT NULL ,
  `version` INT(11) NOT NULL ,
  PRIMARY KEY (`node_info_id`) ,
  INDEX `ni_idx_node_name` (`node_id` ASC, `name` ASC, `version` ASC) ,
  INDEX `ni_fk_node_id_idx` (`node_id` ASC) ,
  CONSTRAINT `ni_fk_node_id`
    FOREIGN KEY (`node_id` )
    REFERENCES `nodes` (`node_id` ))
ENGINE = InnoDB
AUTO_INCREMENT = 6084
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `reserved_keyword`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `reserved_keyword` (
  `reserved_keyword_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `keyword` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`reserved_keyword_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `roles`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `roles` (
  `role_pk` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key for this table' ,
  `username` VARCHAR(50) NOT NULL COMMENT 'Username applied to this table.' ,
  `role_name` VARCHAR(50) NOT NULL COMMENT 'Actual permission name' ,
  `role_type` VARCHAR(50) NOT NULL COMMENT 'Actual role typ' ,
  `ref_id` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`role_pk`) ,
  INDEX `Index_2` (`username` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 49
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `scheduled_campaign_tasks`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `scheduled_campaign_tasks` (
  `scheduled_task_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `scheduled_date` DATETIME NOT NULL ,
  `source_uid` VARCHAR(36) NOT NULL ,
  `target` VARCHAR(36) NULL DEFAULT NULL ,
  `event_type` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`scheduled_task_id`) ,
  INDEX `sct_idx_date` (`scheduled_date` ASC) ,
  INDEX `sct_idx_source` (`source_uid` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 3916
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `subscriber_blacklist`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `subscriber_blacklist` (
  `subscriber_blacklist_id` BIGINT(20) NOT NULL AUTO_INCREMENT ,
  `subscriber_id` BIGINT(20) NOT NULL ,
  `incoming_address` VARCHAR(64) NOT NULL ,
  `incoming_type` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`subscriber_blacklist_id`) ,
  INDEX `sbs_fk_sub_idx` (`subscriber_id` ASC) ,
  CONSTRAINT `sbs_fk_sub`
    FOREIGN KEY (`subscriber_id` )
    REFERENCES `subscribers` (`subscriber_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `users` (
  `username` VARCHAR(50) NOT NULL COMMENT 'The case-sensitive login' ,
  `user_pk` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key for the table' ,
  `password` VARCHAR(100) NOT NULL COMMENT 'The encrypted password for this account' ,
  `active` TINYINT(1) NOT NULL DEFAULT '1' COMMENT 'Flag set to disable an account.' ,
  `email` VARCHAR(100) NULL DEFAULT NULL ,
  `name` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`user_pk`) )
ENGINE = InnoDB
AUTO_INCREMENT = 10
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `contact_info`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `contact_info` (
  `contact_info_id` INT NOT NULL AUTO_INCREMENT ,
  `contact_id` BIGINT(20) NOT NULL ,
  `name` VARCHAR(245) NOT NULL ,
  `value` TEXT NULL ,
  PRIMARY KEY (`contact_info_id`) ,
  INDEX `fk_contact_info_contact1_idx` (`contact_id` ASC) ,
  CONSTRAINT `fk_contact_info_contact1`
    FOREIGN KEY (`contact_id` )
    REFERENCES `campaign_admin`.`contact` (`contact_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
