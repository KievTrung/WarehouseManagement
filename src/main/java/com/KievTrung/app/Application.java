package com.KievTrung.app;


import com.KievTrung.ui.MainForm;
import com.KievTrung.util.db.DBInitializer;
import com.sun.tools.javac.Main;
import lombok.extern.slf4j.Slf4j;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.sql.SQLException;

@Slf4j
public class Application {
//    private static final Logger log = LoggerFactory.getLogger(Application.class);

  static void main() {
	try {
	  DBInitializer.init();
	  log.info("init schema succeed");
	} catch (SQLException e) {
	  log.error("init schema failed");
	  throw new RuntimeException(e);
	}

	// run ui
	SwingUtilities.invokeLater(() -> {
	  MainForm mainForm = new MainForm();
	});

  }
}
