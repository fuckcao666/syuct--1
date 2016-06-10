---
layout: page
title: System installation
permalink: /:path/
nav: /:path/Administration-guide/System-installation/
sort_idx: 20
---
* [Introduction](#introduction)
* [System requirements](#system-requirements)
* [Supported OS](#supported-os)
* [Third party components](#third-party-components)
  * [Zookeeper](#zookeeper)
  * [SQL database](#sql-database)
  * [NoSQL database](#nosql-database)
* [Installing Kaa](#installing-kaa)
  * [Single node installation](#single-node-installation)
  * [Node cluster setup](#node-cluster-setup)
  * [AWS deployment](#aws-deployment)
  * [Planning your deployment](#planning-your-deployment)
* [Troubleshooting](#troubleshooting)

## Introduction

This guide overview over kaa platform installation.

## System requirements

To use Kaa, your system must meet the following minimum system requirements.

   * 64-bit OS
   * 4 Gb RAM

## Supported OS

Kaa supports the following operating system families and provides installation packages for each of them.

   * Ubuntu and Debian systems
   * Red Hat/CentOS/Oracle 5 or Red Hat 6 systems

Please note that the instructions from this guide were tested on Ubuntu 14.04 and Centos 6.7. Instructions for other OS may have minor differences.

## Third party components

Kaa requires the following third party components to be installed and configured.

* [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Kaa has been tested on JDK 8
* [PostgreSQL 9.4](http://www.postgresql.org/download/) or [MariaDB 5.5](https://mariadb.org/download/) Kaa has been tested on the latest production release of PostgreSQL and MariaDB.
* [Zookeeper 3.4.5](http://zookeeper.apache.org/doc/r3.4.5/). Kaa requires ZooKeeper for coordination of server components.

Kaa also requires [MongoDB 2.6.9](http://www.mongodb.org/downloads) or [Cassandra 2.2.5](http://cassandra.apache.org/download/) as a NoSQL database. The installation steps for third-party components are provided in the following section.

### Zookeeper

Apache ZooKeeper enables highly reliable distributed coordination of Kaa cluster nodes. Each Kaa node push information about connection parameters, enabled services and corresponding services load. Other Kaa nodes use this information in order to get list of their neighbors and communicate with them. Control service uses information about existing Bootstrap services and their connection parameters during SDK generation.

### SQL database

SQL database instance is used to store metadata about tenants, applications, endpoint groups, etc. This information is shared between endpoints, thus it's volume does not scale and it can be efficiently stored in modern SQL databases. To support high availability of Kaa cluster, SQL database should be also deployed in cluster mode.

Kaa supports two SQL databases at the moment: PostgresSQL and MariaDB. If you planning to use kaa in a single node instance we recommend you to use PostgreSQL and MariaDB for multi node cluster because of better clusterization capabilities of MariaDB.

### NoSQL database

NoSQL database instance is used to store information about endpoint profiles, notifications, configurations, etc. The volume of this information scales linearly with amount of endpoints that are managed using particular Kaa cluster instance. NoSQL database nodes can be co-located with Kaa nodes on the same physical or virtual machines. Kaa support Apache Cassandra and MongoDB as a NoSQL database at the moment. The choose between MongoDB and Apache Cassandra depends only on your specific data analysis needs.

## Installing Kaa

Kaa platform provides you several options for kaa-node server installation, for more detail on how to install your kaa server please refer to next sections.

### Single node installation

If you need to install and configure Kaa components on a single Linux node refer to [Single node installation](Single-node-installation) documentation page.

### Node cluster setup

To learn how to create kaa-node cluster refer to [Node cluster setup](Cluster-setup) documentation page.

### AWS deployment.

You can either install Kaa server as described on this page or run it on [Amazon EC2](Planning-your-deployment/#aws-deployment-preparation).

### Planning your deployment

For more information about environment setup please refer to [Planning your deployment](Planning-your-deployment/) documentation page.

## Troubleshooting

Common issues covered in this [guide](../Troubleshooting).