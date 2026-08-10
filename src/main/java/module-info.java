module com.talaatharb.leadmanager {
    requires static lombok;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.scripting;
    requires java.desktop;

    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;

    requires mapdb;
    requires org.jsoup;

    requires org.apache.groovy;
    requires org.apache.groovy.jsr223;

    requires org.jgrapht.core;
    requires org.jgrapht.io;

    requires org.slf4j;

    opens com.talaatharb.leadmanager to javafx.fxml;
    opens com.talaatharb.leadmanager.ui.controller to javafx.fxml;
    opens com.talaatharb.leadmanager.entity to javafx.base;

    exports com.talaatharb.leadmanager;
    exports com.talaatharb.leadmanager.entity;
    exports com.talaatharb.leadmanager.repository;
    exports com.talaatharb.leadmanager.scraper;
    exports com.talaatharb.leadmanager.scripting;
    exports com.talaatharb.leadmanager.graph;
    exports com.talaatharb.leadmanager.service;
    exports com.talaatharb.leadmanager.ui.controller;
    exports com.talaatharb.leadmanager.ui;
    exports com.talaatharb.leadmanager.ui.task;
    exports com.talaatharb.leadmanager.tracking;
    exports com.talaatharb.leadmanager.ui.view;
}
