module com.narukoshin.crawler {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires java.sql;
    requires org.xerial.sqlitejdbc;


    opens com.narukoshin.crawler to javafx.fxml;
    exports com.narukoshin.crawler;
}