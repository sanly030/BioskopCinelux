module cinelux.bioskopcinelux {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires javafx.graphics;

    opens cinelux.bioskopcinelux to javafx.fxml;
    opens cinelux.bioskopcinelux.controller.menu to javafx.fxml;
    opens cinelux.bioskopcinelux.controller.master to javafx.fxml;
    opens cinelux.bioskopcinelux.controller.login to javafx.fxml;
    opens cinelux.bioskopcinelux.controller.list to javafx.fxml;
    opens cinelux.bioskopcinelux.controller.transaksi to javafx.fxml;


    exports cinelux.bioskopcinelux;
    exports cinelux.bioskopcinelux.controller.menu;
    exports cinelux.bioskopcinelux.controller.list;
    exports cinelux.bioskopcinelux.controller.cart;
    opens cinelux.bioskopcinelux.controller.cart to javafx.fxml;

}