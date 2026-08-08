module com.talaatharb.leadmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;

    requires org.mapdb;
    requires org.jsoup;

    requires groovy;
    requires groovy.jsr223;

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
    exports com.talaatharb.leadmanager.ui.controller;
    exports com.talaatharb.leadmanager.ui.view;
}
