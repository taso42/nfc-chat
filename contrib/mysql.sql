# MySQL dump 7.1
#
# Host: localhost    Database: nfc
#--------------------------------------------------------
# Server version	3.22.32
#
#--------------------------------------------------------
# This is just an example that happens to match the 
# values in jdbc.conf.  You can certainly create
# a different schema (or use an existing one).  Just be
# sure to modify jdbc.conf
#--------------------------------------------------------

#
# Table structure for table 'nfcusers'
#
CREATE TABLE nfc_users (
  uname char(16) DEFAULT '' NOT NULL,
  password char(32) DEFAULT '' NOT NULL,
  access int(3) DEFAULT '1' NOT NULL,
  PRIMARY KEY (user)
);

#
# Dumping data for table 'users'
#


